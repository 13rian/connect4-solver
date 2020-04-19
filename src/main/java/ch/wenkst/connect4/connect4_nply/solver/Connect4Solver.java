package ch.wenkst.connect4.connect4_nply.solver;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import ch.wenkst.connect4.connect4_nply.configuration.AppConfig;
import ch.wenkst.connect4.connect4_nply.game.MoveSorter;
import ch.wenkst.connect4.connect4_nply.game.Position;
import ch.wenkst.connect4.connect4_nply.game.SolvedPosition;
import ch.wenkst.connect4.connect4_nply.game.TranspositionTable;
import ch.wenkst.sw_utils.logging.Log;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;

public class Connect4Solver {	
	private static Log log = Log.getLogger(Connect4Solver.class);
	private boolean isStrongSolver; 				// true if the exact score of the position should be found
	private int nplyTranspositions = -1; 			// the nply to use for the transposition table, e.g. 8ply will use all 8ply positions
	
	private Long2ByteOpenHashMap nplyPositionMap;	// the nply transposition table with the solved positions
	private TranspositionTable tpTable; 			// transposition table to save the upper bound of the position
	
	
	// defines in which columns the moves are explored
	private int[] columnOrder = new int[AppConfig.boardWidth];	
	
	
	public Connect4Solver() {
		this(true, -1);
	}
	
	
	public Connect4Solver(boolean isStrongSolver) {
		this(isStrongSolver, -1);
	}


	/**
	 * solver for connect4, based on the tutorial: http://blog.gamesolver.org/
	 * this solver contains all strategies that are implemented in the other classes, namely:
	 * - negamax (version of minimax where you assume that the opponent score is the negative value of your score)
	 * - alpha-bet pruning
	 * - ordering of the move exploration order based on a score function. the score is equal to the number of created
	 *   winning chances as they are most likely moves that lead to a winning path more quickly. if the score is equal
	 *   central columns are explored first because they are often good moves.
	 * - transposition table to save the upper bound (saving lower bound seems to slow the search)
	 * - iterative deepening
	 * - avoid exploring losing moves to prune the tree faster	
	 */
	public Connect4Solver(boolean isStrongSolver, int nplyTranspositions) {
		this.isStrongSolver = isStrongSolver;
		this.nplyTranspositions = nplyTranspositions;
		tpTable = new TranspositionTable(AppConfig.tpTableSize);
		
		init();
	}
	
	
	/**
	 * initializes the connect4 solver, creates the column order and the transposition table for
	 * all 12ply positions
	 */
	private void init() {
		// define the column exploration order, central columns are explored first
		for (int col = 0; col < AppConfig.boardWidth; col++) {
			int sign = (col%2 == 0) ? 1 : -1; 			
			columnOrder[col] = AppConfig.boardWidth/2 + (int) Math.ceil((double) col/2) * sign;
		}

		// initialize the nply table map where either side cannot win with their next move
		if (nplyTranspositions > 0) {
			log.fine("start to read in the " + nplyTranspositions + "ply position table");
			try {
				nplyPositionMap = new Long2ByteOpenHashMap();
				CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader();
				FileReader fileReader = new FileReader(AppConfig.dirTranspositionTable + "connect4_" + nplyTranspositions + "ply.csv");
				CSVParser csvFileParser = new CSVParser(fileReader, csvFileFormat);


				// add all positions to the position map
				for (CSVRecord csvRecord : csvFileParser) {	
					long position = Long.parseLong(csvRecord.get("position"));
					long diskMask = Long.parseLong(csvRecord.get("disk_mask"));
					byte score = Byte.parseByte(csvRecord.get("score"));

					Position p = new Position(position, diskMask);
					nplyPositionMap.put(p.toKey(), score);
				}

				// close the reader resources
				if (csvFileParser != null) csvFileParser.close();
				if (fileReader != null) fileReader.close();
				log.fine("finsihed creating the " + nplyTranspositions + "ply position table, size: " + nplyPositionMap.size());

			} catch (Exception e) {
				log.severe("error creating the " + nplyTranspositions + "ply position table: ", e);
				log.info("the solver will not use any nply transposition table");
				nplyPositionMap = null;
				nplyTranspositions = -1;
			}
		
		} else {
			log.info("solver will not use any nply transposition table");
		}
	}
	
	
	/**
	 * finds the best score of the passed position if both players play optimal
	 * @param position	the connect4 position
	 * @return 			the score
	 */
	public byte findBestScore(Position position) {		
		return solve(position);
	}
	
	
	
	/**
	 * solves the passed position using the negamax algorithm
	 * @param position 	the position to solve 	
	 * @return  		best score of the passed position
	 */
	private byte solve(Position position) {
//		tpTable.clear(); 
		
		// check if the player can win with his next move, this case is not checked by negamax
		byte moveCount = position.getMoveCount();
		if (position.canWinNext()) { 
			return (byte) ((AppConfig.boardSize+1 - moveCount) / 2);
		}

		// apply the idea of iterative deepening and do a different search strategy
		byte min, max;
		if (isStrongSolver) {
			min = (byte) -((AppConfig.boardSize - moveCount) / 2); 
			max = (byte) ((AppConfig.boardSize+1 - moveCount) / 2);
		} else {
			min = -1;
			max = 1;
		}

		while (min < max) {                    // iteratively narrow the min-max exploration window
			byte med = (byte) (min + (max - min)/2);
			if (med <= 0 && min/2 < med) {
				med = (byte) (min/2);
			} else if (med >= 0 && max/2 > med) {
				med = (byte) (max/2);
			}

			// use a null depth window to know if the actual score is greater or smaller than med
			byte r = negamax(position, med, (byte) (med + 1));   
			if (r <= med) {
				max = r;
			} else {
				min = r;
			}
		}
		
		return min;
	}
	


	/**
	 * method that is called recursively to solve the passed position. 
	 * a better move exploration order was introduces. a score function based on possible winning chances
	 * decides which move will be explored first. this moves usually lead to a faster victory.
	 * if the score is equal the old order of central columns first is kept.
	 * @param position 		connect4 position
	 * @param alpha 		lower window bound 
	 * @param beta 			upper window bound
	 * @return				score according to the alpha, beta algorithm (see comment in the constructor)
	 */
	private byte negamax(Position position, byte alpha, byte beta) {
		// check if the position can be found in the 8ply position table
		byte moveCount = position.getMoveCount();
		if (moveCount == nplyTranspositions) {
			byte score = nplyPositionMap.getOrDefault(position.toKey(), Byte.MIN_VALUE);
			if (score != Byte.MIN_VALUE) {
				return score;
			}
		}
		
		// do not explore the position if there are no non losing moves
		long nonLosingMoves = position.nonLosingMoves();
		if (nonLosingMoves == 0) {
			// the player can not play any non losing moves
			return (byte) -((AppConfig.boardSize - moveCount) / 2);
		}

		// check if the games is drawn
		if (moveCount >= AppConfig.boardSize - 2) {
			return 0;
		} 

		// define the minimal possible score in this position
		byte min = (byte) -((AppConfig.boardSize-2 - moveCount) / 2);
		if (alpha < min) {
			// there is no need to keep beta above our max possible score
			alpha = min;

			// prune the exploration if the [alpha;beta] window is empty
			if (alpha >= beta) {
				return alpha;
			}
		}


		// define the maximal possible score in this position
		byte max = (byte) ((AppConfig.boardSize-1 - moveCount) / 2);
		byte cachedScore = tpTable.get(position.toKey());
		if (cachedScore != 0) {
			max = (byte) (cachedScore + AppConfig.minScore - 1);
		}

		if (beta > max) {
			// there is no need to keep beta above our max possible score.
			beta = max;                     

			// prune the exploration if the [alpha;beta] window is empty
			if (alpha >= beta) {
				return beta;  
			}
		}


		// fill the move sorter with the exploration moves (only non losing moves)
		MoveSorter moveSorter = new MoveSorter();
		for (int i = AppConfig.boardWidth - 1; i > -1; i--) {
			long move = nonLosingMoves & Position.columnMask(columnOrder[i]);
			if (move > 0) {
				moveSorter.add(move, position.moveScore(move));
			}
		}

		long[] explorationMoves = moveSorter.getMoves();	
		for (int i=moveSorter.size(); i>0; i--) {
			// move mask will always be larger than 0, 0 is used to mark no move, break the loop if there are no more moves
			long move = explorationMoves[i-1];			
			if (move <= 0) {
				break;
			}
			
			Position positionClone = new Position(position);
			positionClone.play(move);
			byte score = (byte) -negamax(positionClone, (byte) -beta, (byte) -alpha);

			
			// prune the exploration if we find a possible move better than what we were looking for
			if (score >= beta) {
				return score;
			}

			// reduce the [alpha;beta] window for next exploration, as we only need to search for positions
			// better than the current best
			if (score > alpha) {
				alpha = score; 
			}
		}
		

		tpTable.put(position.toKey(), (byte) (alpha - AppConfig.minScore + 1)); // save the upper bound of the position
		return alpha;
	}	
	
	
	
	/**
	 * finds all optimal moves of the passed connect4 position
	 * @param position	the connect4 position
	 * @return 			a list with all optimal moves
	 */
	public SolvedPosition findOptimalMoves(Position position) {		
		// save all the moves and the score in the position to solve
		List<Integer> moveList = new ArrayList<>();				// holds all possible moves in the position to solve
		List<Integer> scoreList = new ArrayList<>();			// caches the scores of all possible moves
		
		// play each legal move and strongly solve for the score, the move(s) with the best score is the optimal move
		for (int col=0; col<AppConfig.boardWidth; col++) {
			if (position.legalMove(col)) {
				Position positionClone = new Position(position);
				byte moveCount = position.getMoveCount();
				if (position.isWinningMove(col)) { 
					int score =  -((AppConfig.boardSize+1 - moveCount) / 2); 	// opponent score
					scoreList.add(score);
					moveList.add(col);
					continue;
				}
				
				positionClone.play(col);
				moveList.add(col);
				
				// solve the position
				int score = solve(positionClone);
				scoreList.add(score);
			}
		}
		
		SolvedPosition solvedPosition = new SolvedPosition(position);
		solvedPosition.addResult(moveList, scoreList);
		
		return solvedPosition;
	}
}
