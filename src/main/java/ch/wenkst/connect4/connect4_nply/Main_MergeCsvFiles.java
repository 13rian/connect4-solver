package ch.wenkst.connect4.connect4_nply;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import ch.wenkst.connect4.connect4_nply.configuration.AppConfig;
import ch.wenkst.connect4.connect4_nply.game.Position;
import ch.wenkst.sw_utils.file.FileUtils;
import ch.wenkst.sw_utils.logging.Log;

public class Main_MergeCsvFiles {
	static {
		System.setProperty("config.file", "config/app.conf"); 					// application config file
	}
		
	private static Log log = Log.getLogger(Main_MergeCsvFiles.class);
	
	
	private int nply = 11; 												// all positions with this number of moves played will be solved
		

	public static void main(String[] args) throws InterruptedException {
		Main_MergeCsvFiles app = new Main_MergeCsvFiles();
		
		// initialize the logger
		Log.initLogger(AppConfig.dirLoggerConfig);
		log.fine("starting connect4 n ply position merging");
		
		app.startApp();
	}

	
	private void startApp() {
		// get the configuration
		AppConfig.getInstance();
		
		mergePositions();
	}
	
	
	private void mergePositions() {
		try {			
			// create the csv-printer for the merged-file
			String transpositionTableFile = AppConfig.dirTranspositionTable + "connect4_" + nply + "ply.csv";
			BufferedWriter writer = new BufferedWriter(new FileWriter(transpositionTableFile));
			CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("position", "disk_mask", "score"));
			csvPrinter.flush();
			
			// read out all positions
			String folderName = nply + "ply";
			List<String> positionFiles = FileUtils.findFilesByPattern(AppConfig.dirSolvedPos + folderName, "", "csv");
			for (String filePath : positionFiles) {
				CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader();
				FileReader fileReader = new FileReader(filePath);
				CSVParser csvFileParser = new CSVParser(fileReader, csvFileFormat);
				
				
				// create a position to get the symmetry
				for (CSVRecord csvRecord : csvFileParser) {	
					long position = Long.parseLong(csvRecord.get("position"));
					long diskMask = Long.parseLong(csvRecord.get("disk_mask"));
					int score = Integer.parseInt(csvRecord.get("score"));
					
					// add the original position
					csvPrinter.printRecord(position, diskMask, score);
					
					// add the mirror if it is different form the original position
					Position p = new Position(position, diskMask);
					Position pMirrored = p.mirror();
					
					if (pMirrored.toKey() != p.toKey()) {
						csvPrinter.printRecord(pMirrored.getPosition(), pMirrored.getDiskMask(), score);
					}
				}
				
				// close the reader resources
				if (csvFileParser != null) csvFileParser.close();
				if (fileReader != null) fileReader.close();
			}
			
			// close the writer resources
			if (writer != null) writer.close();
			if (csvPrinter != null) csvPrinter.close();
			
			log.info("finished to merge the positions");

		} catch (Exception e) {
			log.severe("error merging the solved position-files: ", e);
		}
	}
}
