package ch.wenkst.connect4.connect4_nply.test_set;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import ch.wenkst.connect4.connect4_nply.configuration.AppConfig;
import ch.wenkst.connect4.connect4_nply.game.Position;
import ch.wenkst.connect4.connect4_nply.game.SolvedPosition;
import ch.wenkst.connect4.connect4_nply.solver.Connect4Solver;
import ch.wenkst.sw_utils.file.FileUtils;
import ch.wenkst.sw_utils.logging.Log;

public class TestSetCreator {
	private static Log log = Log.getLogger(TestSetCreator.class);
	
	private Random random;
	private int nPositions2; 			// number of positions with only 2 moves played
	private int nPositions3; 			// number of positions with only 3 moves played
	private int nPositions4_8; 			// number of positions with 4-8 moves
	private int nPositions9_40; 		// number of positions with 9-40 moves
	private Connect4Solver solver; 		// the solver that solves the positions
	
	
	private List<Position> positions;				// holds the positions of the test set
	private List<SolvedPosition> solvedPositions;	// holds all solved positions


	
	/**
	 * creates a test set with connect4 positions and their optimal moves, the set does not contain any
	 * duplicates
	 * @param nPositions2 		number of positions with 2 moves
	 * @param nPositions3 		number of positions with 3 moves
	 * @param nPositions4_10 	number of positions with 4-8 moves
	 * @param nPositions11_40	number of positions with 9-40 moves
	 */
	public TestSetCreator(int nPositions2, int nPositions3, int nPositions4_8, int nPositions9_40) {
		this.nPositions2 = nPositions2;
		this.nPositions3 = nPositions3;
		this.nPositions4_8 = nPositions4_8;
		this.nPositions9_40 = nPositions9_40;
	
		
		positions = new ArrayList<>();			
		solvedPositions = new ArrayList<>();	
		
		// create the number generator with the current time as seed
		random = new Random(Instant.now().toEpochMilli());	
		
		// initialize the solver
		solver = new Connect4Solver(true, 12);		// use a transposition table of 12 ply
	}
	
	
	
	/**
	 * creates the random test positions if they were not created already
	 */
	public void createRandomPositions() {
		log.info("create some random test positions");
		positions = createUniquePositions();
	}

	
	/**
	 * solves the position that are not solved yet
	 */
	public void solvePositions() {
		log.fine("start to solve positions");
		
		for (Position position : positions) {
			// solve the position
			SolvedPosition solution = solver.findOptimalMoves(position);
			solvedPositions.add(solution);
			
			double completion = solvedPositions.size() / (double) positions.size();
			log.fine("solved position " + position.getMoveCount() + " ply, completion: " + String.format("%.2f", completion*100) + "%");
		}
	}
	
	
	/**
	 * writes the test set to a csv-file
	 * @param csvFilePath	the csv file path
	 */
	public void toCsv(String csvFilePath) {		
		try {
			// ensure that the directory exists
			new File(AppConfig.dirTestSet).mkdirs();
			
			// delete the old file
			File csvFile = new File(csvFilePath);
			if (csvFile.exists()) {
				FileUtils.deleteFile(csvFilePath);
				log.info("old csv-file successfully deleted");
			}
			
			// add the header line
			BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath));
			CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("position", "disk_mask", "strong_score", "weak_score", "strong_moves", "weak_moves"));
			csvPrinter.flush();
			
			// go through all solved positions and add them to the csv-file
			for (SolvedPosition solution : solvedPositions) {
				csvPrinter.printRecord(
						solution.getPosition().getPosition(),
						solution.getPosition().getDiskMask(),
						solution.getStrongScore(),
						solution.getWeakScore(),
						solution.getStrongMovesStr(),
						solution.getWeakMovesStr());
				
				// write the line to the file
				csvPrinter.flush();
				
			}

			csvPrinter.close();
			log.info("test-set successfully written to csv-file: " + csvFilePath);
			
		} catch (Exception e) {
			log.severe("error writing the test set to a csv-file: ", e);
		}
	}
	
	
	/**
	 * creates a list with unique random connect4 positions
	 * the positions at the beginning have 36 moves then 35 etc, this way the easy to solve
	 * positions are at the beginning
	 * @return
	 */
	private List<Position> createUniquePositions() {
		List<Position> result = new ArrayList<>();
		
		// positions with 9-40 moves played
		for (int i=40; i>10; i--) {
			List<Position> positionSubset = randomPositions(nPositions9_40, i);
			result.addAll(positionSubset);
			log.fine("random positions with " + i + " moves created");
		}
		
		// positions with 4-8 moves played
		for (int i=10; i>3; i--) {
			List<Position> positionSubset = randomPositions(nPositions4_8, i);
			result.addAll(positionSubset);
			log.fine("random positions with " + i + " moves created");
		}
		
		// positions with three moves played
		List<Position> positionSubset = randomPositions(nPositions3, 3);
		result.addAll(positionSubset);
		log.fine("random positions with 3 moves created");
		
		// positions with two moves played
		positionSubset = randomPositions(nPositions2, 2);
		result.addAll(positionSubset);
		log.fine("random positions with 2 moves created");
		
		return result;
	}
	
	
	
	/**
	 * creates random connect4 positions, there are not positions duplicates in the list
	 * @param positionCount		number of positions to create
	 * @param nMoves			the number of moves played
	 * @return					list of random positions
	 */
	private List<Position> randomPositions(int positionCount, int nMoves) {
		// use a sorted set in order not to add duplicate positions to the set
		SortedSet<Position> positionSet = new TreeSet<>(new PositionComparator());
		
		while (positionSet.size() < positionCount) {
			Position position = new Position();
			List<Integer> legalMoves = new ArrayList<>();
			
			for (int i=0; i<nMoves; i++) {
				for (int col=0; col<AppConfig.boardWidth; col++) {
					if (position.legalMove(col)) {
						legalMoves.add(col);
					}
				}
		
		
				// play a random move
				int idx = random.nextInt(legalMoves.size());
				int move = legalMoves.get(idx);
				
				// check if this is a winning move
				if (position.isWinningMove(move)) {
					break;
				}
				
				position.play(move);
				legalMoves.clear();
				
				if (i == nMoves-1) {
					positionSet.add(position);
				}
			}
		}
		
		List<Position> positions = new ArrayList<Position>(positionSet);
		return positions;
	}
	
	
	private class PositionComparator implements Comparator<Position> {
	    @Override
	    public int compare(Position p1, Position p2) {
	    	long p1Key = p1.toKey();
	    	long p2Key = p2.toKey();
	    	
	    	if (p1Key < p2Key) {
	    		return -1;
	    	} else if (p1Key > p2Key) {
	    		return 1;
	    	} else {
	    		return 0;
	    	}
	    }
	}
}
