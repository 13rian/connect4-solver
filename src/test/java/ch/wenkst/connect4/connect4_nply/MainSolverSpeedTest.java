package ch.wenkst.connect4.connect4_nply;

import java.io.File;
import java.util.List;

import ch.wenkst.connect4.connect4_nply.configuration.AppConfig;
import ch.wenkst.connect4.connect4_nply.game.Position;
import ch.wenkst.connect4.connect4_nply.position.TestPosition;
import ch.wenkst.connect4.connect4_nply.position.TestPositionParser;
import ch.wenkst.connect4.connect4_nply.solver.Connect4Solver;
import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.logging.Log;

public class MainSolverSpeedTest {
	private static Log log = Log.getLogger(MainSolverSpeedTest.class);
	
	public String testFilePath = Utils.getWorkDir() + File.separator + "test" + File.separator + "5_begin_medium.txt";
		

	public static void main(String[] args) {
		// initialize the logger
		Log.initLogger(AppConfig.dirLoggerConfig);
		log.fine("Starting the solver test");
		
		// test the mirrored position
		String moveSequence = "1112234444566777"; 	// "171717262635"
		Position position = new Position();
		position.fromMoveSequence(moveSequence);
		System.out.println(position);
		
		Position mirroredPosition = position.mirror();
		System.out.println(mirroredPosition.mirror());
		
		MainSolverSpeedTest app = new MainSolverSpeedTest();
		app.executeTest();
	}
	
	
	/**
	 * solves all test positions from the test file with the known results
	 */
	private void executeTest() {
		boolean allPositionsCorrect = true;
		
		// parse in the test files
		log.info("start to parse the test positions");
		TestPositionParser parser = new TestPositionParser();
		List<TestPosition> testPositionList = parser.positionsFromFile(testFilePath);
		
		
		// put them to the connect4 solver
		log.info("start to solve the test positions");
		Connect4Solver solver = new Connect4Solver(); 
		long totTime = 0;
		for (TestPosition testPosition : testPositionList) {
			Position position = testPosition.toPosition();
			
			long startTime = System.nanoTime();
			int score = solver.findBestScore(position);
			long endTime = System.nanoTime();
			long timeDiff = endTime - startTime;
			totTime += timeDiff;
			
			// log wrong scores for strong solvers
			if (score != testPosition.getScore()) {
				allPositionsCorrect = false;
				log.severe("error in solver!! calculated score: " + score + ", true score: " + testPosition.getScore());
			}
		}
		
		String statusMessage = (allPositionsCorrect) ? "test successful" : "test failed";
		
		// calculate the mean time in ms and the number of explored positions
		double meanTime = (double) totTime / testPositionList.size() / 1000000D;
		log.info(statusMessage + " mean time: " + meanTime + "ms");
	}
}
