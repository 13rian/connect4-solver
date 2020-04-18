package ch.wenkst.connect4.connect4_nply;
import java.io.IOException;

import ch.wenkst.connect4.connect4_nply.configuration.AppConfig;
import ch.wenkst.connect4.connect4_nply.test_set.TestSetCreator;
import ch.wenkst.sw_utils.logging.Log;

public class MainCreateTestSet {
	private static Log log = Log.getLogger(MainCreateTestSet.class);
	private String csvFile = AppConfig.dirTestSet + "positions.csv";
	
	// define how many positions to solve, 40 40 100
	private int nPositions2 = 40; 				// number of positions with only 2 moves played
	private int nPositions3 = 200; 				// number of positions with only 3 moves played
	private int nPositions4_8 = 1000; 			// number of positions with moves 4-8
	private int nPositions9_40 = 1000; 			// number of positions with moves 9-40
	
	
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		// initialize the logger
		Log.initLogger(AppConfig.dirLoggerConfig);		
		MainCreateTestSet app = new MainCreateTestSet();
		TestSetCreator testSetCreator = new TestSetCreator(app.nPositions2, app.nPositions3, app.nPositions4_8, app.nPositions9_40);
				
		
		// create the random positions if they are not created yet
		testSetCreator.createRandomPositions();

		// solve the positions
		testSetCreator.solvePositions();
		
		
		// write the test-set to a csv-file
		testSetCreator.toCsv(app.csvFile);
		
		
		log.info("all positions solved");
	}
}
