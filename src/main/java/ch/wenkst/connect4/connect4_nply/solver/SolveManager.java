package ch.wenkst.connect4.connect4_nply.solver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import ch.wenkst.connect4.connect4_nply.configuration.AppConfig;
import ch.wenkst.connect4.connect4_nply.game.Position;
import ch.wenkst.sw_utils.conversion.Conversion;
import ch.wenkst.sw_utils.logging.Log;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SolveManager {
	private static Log log = Log.getLogger(SolveManager.class);
	private AppConfig appConfig;

	private ObjectArrayList<Position> positions; 			// holds the positions to solve				
	private int nplys; 										// the number of plys
	private String positionFilePath; 						// the path to the file containing all the positions
	private Connect4Solver solver;


	/**
	 * solves a bunch of nply positions
	 * @param nplys					the number of moves played in a positions to solve
	 * @param nplyTranspositions	nply transposition table that should be used by the solver
	 */
	public SolveManager(int nplys, int nplyTranspositions) {
		this.nplys = nplys;
		
		appConfig = AppConfig.getInstance();
		positions = new ObjectArrayList<>();

		// ensure that the csv-file directory exists
		new File(AppConfig.dirSolvedPos).mkdirs();
		positionFilePath = AppConfig.dirPositions + "positions_" + nplys + "ply.csv";
		
		solver = new Connect4Solver(true, nplyTranspositions);
	}


	/**
	 * creates all legal nply positions that are not won by the next 2 moves
	 * mirrored positions are taken out
	 */
	public void createPositions() {
		if (new File(positionFilePath).exists()) {
			log.fine("test position file already exists at " + positionFilePath);
			return;
		}
		
		log.fine("start creating all " + nplys + " ply positions");
		Long2ObjectMap<Position> positions = allPositions();
		log.fine("finished creating the connect4 positions, non-winning: " + positions.size());


		
		removeSymmetric(positions); 	// take out the symmetric positions
		positionsToCsv(positions); 		// write the positions to a csv-file
	}
	
	
	/**
	 * creates all legal non-duplicate positions
	 * @return	list of positions
	 */
	private Long2ObjectMap<Position> allPositions() {
		Long2ObjectMap<Position> positions = new Long2ObjectLinkedOpenHashMap<>();
				
		long maxCombinations = (long) Math.pow(AppConfig.boardWidth, nplys);
		for (long i=0; i<maxCombinations; i++) {
			String columnSequence = Long.toString(i, AppConfig.boardWidth);
			columnSequence = Conversion.padLeft(columnSequence, '0', nplys);	
						
			Position position = new Position();
			boolean legalPosition = position.fromColumnSequence(columnSequence);
			if (legalPosition && !position.isWon()) {
				// only add non-duplicate positions
				if (!positions.containsKey(position.toKey())) {
					positions.put(position.toKey(), position);
				}
			}
		}
		
		return positions;
	}
	
	
	/**
	 * removes all symmetric positions
	 * @param positions		map with connect positions
	 */
	private void removeSymmetric(Long2ObjectMap<Position> positions) {
		// take out the symmetric positions
		Map<Long, Position> positionsToDelete = new HashMap<>();
		for (Position p : positions.values()) {
			Position mirroredPosition = p.mirror();
			long mirroredKey = mirroredPosition.toKey();
			long positionKey = p.toKey();

			// skip symmetric positions
			if (mirroredKey == positionKey) {
				continue;
			}

			// if the delete map does not contain both keys, remove the mirrored key from the positions
			if (!positionsToDelete.containsKey(mirroredKey) && !positionsToDelete.containsKey(positionKey)) {
				positionsToDelete.put(mirroredPosition.toKey(), mirroredPosition);
				if (positions.get(mirroredPosition.toKey()) == null) {
					log.severe("mirrored position no part of the set");
				}
			}
		}
		
		System.out.println("pos-size:" + positions.size());
		System.out.println("pos to delete size: " + positionsToDelete.size());
		
		for (long symPosKey : positionsToDelete.keySet()) {
			positions.remove(symPosKey);
		}
		log.fine("finished taking out all symmetric positions position count: " + positions.size());
	}
	
	
	/**
	 * saves the passed positions to a csv-file
	 * @param positions
	 */
	private void positionsToCsv(Long2ObjectMap<Position> positions) {
		new File(AppConfig.dirPositions).mkdirs();
		try {			
			// add the header line
			BufferedWriter writer = new BufferedWriter(new FileWriter(positionFilePath));
			CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("position", "disk_mask"));
			csvPrinter.flush();

			// go through all solved positions and add them to the csv-file
			for (Position p : positions.values()) {
				csvPrinter.printRecord(
						p.getPosition(),
						p.getDiskMask());

				// write the line to the file
				csvPrinter.flush();
			}

			csvPrinter.close();
			log.info("positions to solve successfully saved to: " + positionFilePath);

		} catch (Exception e) {
			log.severe("error saving solutions to: " + positionFilePath, e);
		}
	}


	/**
	 * solves the configured amount of connect4 positions
	 */
	public void solvePositions() {
		int startIndex = appConfig.getConfigValue("startIndex", -1);
		int endIndex = appConfig.getConfigValue("endIndex", -1);
		int checkpoint = appConfig.getConfigValue("checkpoint", -1);
		
		
		// parse the positions to solve form the csv-file
		log.info("start to parse the positions to solve form the csv-file");
		try {
			CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader();
			FileReader fileReader = new FileReader(positionFilePath);
			CSVParser csvParser = new CSVParser(fileReader, csvFileFormat);
			
			List<CSVRecord> csvRecords = csvParser.getRecords();
			if (endIndex < 0) {
				endIndex = csvRecords.size();
			}
			positions = new ObjectArrayList<>(endIndex-startIndex);
			
			for (int i=startIndex; i<endIndex; i++) {
				CSVRecord csvPosition = csvRecords.get(i);
				long position = Long.parseLong(csvPosition.get("position"));
				long diskMask = Long.parseLong(csvPosition.get("disk_mask"));
				Position p = new Position(position, diskMask);
				positions.add(p);
			}
			
			
			csvParser.close();
			fileReader.close();
			
		} catch (Exception e) {
			log.severe("error parsing csv-file: ", e);
		}


		// solve all configured positions
		log.fine("start to solve the positions, start-index: " + startIndex + ", end-index: " + endIndex + ", checkpoint: " + checkpoint);
		List<Solution> solutions = new ObjectArrayList<>(endIndex-startIndex);
		for (int i=0; i<endIndex-startIndex; i++) {
			Position position = positions.get(i);
			int score = solver.findBestScore(positions.get(i));
			Solution solution = new Solution(position.getPosition(), position.getDiskMask(), score);
			solutions.add(solution);

			// save the solutions to a csv-file at the configured checkpoint interval or when the end
			// index is reached
			if ((i+1) % checkpoint == 0 || i==endIndex-startIndex-1) {
				log.fine("positions solved so far: " + (i+1));

				String fileStartIndex = Conversion.padLeft((i+startIndex-checkpoint+1) + "", '0', 7);
				String fileEndIndex = Conversion.padLeft((i+startIndex+1) + "", '0', 7);
				if (i==endIndex-startIndex-1) {
					int start = (i+startIndex+1) - ((i+startIndex-checkpoint+1) % checkpoint);
					fileStartIndex = Conversion.padLeft(start + "", '0', 7);
				}
				
				String filePath = AppConfig.dirSolvedPos + nplys + "ply" + File.separator + fileStartIndex + "_" + fileEndIndex + ".csv";
				solutuionsToCsv(filePath, solutions);
				solutions.clear();
			}
		}
	}


	/**
	 * saves the passed connect4 solutions to a csv-file
	 * @param filePath		path of the csv-file to which the solutions are saved
	 * @param solutions		the connect4 solution
	 */
	private void solutuionsToCsv(String filePath, List<Solution> solutions) {
		try {			
			// add the header line
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
			CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("position", "disk_mask", "score"));
			csvPrinter.flush();

			// go through all solved positions and add them to the csv-file
			for (Solution solution : solutions) {
				csvPrinter.printRecord(
						solution.position,
						solution.diskMask,
						solution.score);

				// write the line to the file
				csvPrinter.flush();
			}

			csvPrinter.close();
			log.info("solutions successfully saved to: " + filePath);

		} catch (Exception e) {
			log.severe("error saving solutions to: " + filePath, e);
		}
	}


	private class Solution {
		private long position;
		private long diskMask;
		private int score;

		/**
		 * holds the values of a solved position
		 * @param position	the position of the current player
		 * @param diskMask	the disk mask with all disks
		 * @param score		the score of the position
		 */
		public Solution(long position, long diskMask, int score) {
			super();
			this.position = position;
			this.diskMask = diskMask;
			this.score = score;
		}
	}
}
