package ch.wenkst.connect4.connect4_nply;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.wenkst.connect4.connect4_nply.game.Position;
import ch.wenkst.connect4.connect4_nply.game.SolvedPosition;
import ch.wenkst.connect4.connect4_nply.position.TestPosition;
import ch.wenkst.connect4.connect4_nply.position.TestPositionParser;
import ch.wenkst.connect4.connect4_nply.solver.Connect4Solver;
import ch.wenkst.sw_utils.Utils;

public class SolverTest {
	private static String testFilePath = Utils.getWorkDir() + File.separator + "test" + File.separator + "test_positions.txt";
	
	private static List<TestPosition> testPositionList;
	
	private static Connect4Solver npTpSolver; 
	private static Connect4Solver ply8Solver;
	private static Connect4Solver ply9Solver;
	private static Connect4Solver ply10Solver;
	private static Connect4Solver ply11Solver;
	private static Connect4Solver ply12Solver;
	
	
	@BeforeAll
	public static void prseTestFile() {
		TestPositionParser parser = new TestPositionParser();
		testPositionList = parser.positionsFromFile(testFilePath);
		
		// initialize all the solvers, this will need some time as the positions need to be parsed into the solver
		npTpSolver = new Connect4Solver(); 
		ply8Solver = new Connect4Solver(true, 8);
		ply9Solver = new Connect4Solver(true, 9);
		ply10Solver = new Connect4Solver(true, 10);
		ply11Solver = new Connect4Solver(true, 11);
		ply12Solver = new Connect4Solver(true, 12);
	}
	
	
	/**
	 * solver with no transposition table
	 */
	@Test
	public void noTranspositionTableTest() {		
		solvePositions(npTpSolver);
	}
	
	
	/**
	 * solver with an 8ply transposition table
	 */
	@Test
	public void ply8SolverTestTest() {		
		solvePositions(ply8Solver);
	}
	
	
	/**
	 * solver with an 9ply transposition table
	 */
	@Test
	public void ply9SolverTestTest() {		
		solvePositions(ply9Solver);
	}
	
	
	/**
	 * solver with an 10ply transposition table
	 */
	@Test
	public void ply10SolverTestTest() {		
		solvePositions(ply10Solver);
	}
	
	
	/**
	 * solver with an 11ply transposition table
	 */
	@Test
	public void ply11SolverTestTest() {		
		solvePositions(ply11Solver);
	}
	
	
	/**
	 * solver with an 12ply transposition table
	 */
	@Test
	public void ply12SolverTestTest() {		
		solvePositions(ply12Solver);
	}
	
	
	/**
	 * tests if the solver can correctly find the optimal move to play
	 */
	@Test
	public void optimalMoveTest() {
		// create a position
		Position position = new Position();
		position.play(3);
		position.play(1);
		position.play(3);
		position.play(2);
		position.play(0);
		position.play(3);
		position.play(6);
		
		// another solver on the internet was used to solve this position
		// https://connect4.gamesolver.org/de/?pos=4273441
		SolvedPosition solution = ply12Solver.findOptimalMoves(position);
		
		assertEquals(4, solution.getStrongScore(), "strong score solved correctly");
		assertEquals(1, solution.getWeakScore(), "weak score solved correctly");
		
		assertEquals("3", solution.getStrongMovesStr(), "strong moves correct");
		assertEquals("1-2-3-4", solution.getWeakMovesStr(), "weak moves correct");
		
		
		// solve the starting position, red can win with the last move and needs to start in the middle row
		position = new Position();
		solution = ply12Solver.findOptimalMoves(position);
		assertEquals(1, solution.getStrongScore(), "strong score of starting positions solved correctly");
		assertEquals("3", solution.getStrongMovesStr(), "strong moves of starting position correct");
	}
	
	
	/**
	 * uses the passed solver to solve all the test positions
	 * @param solver
	 */
	private void solvePositions(Connect4Solver solver) {
		for (TestPosition testPosition : testPositionList) {
			Position position = testPosition.toPosition();
			int score = solver.findBestScore(position);
			int expectedScore = testPosition.getScore();
			
			assertEquals(expectedScore, score, "score solved correctly");
		}
	}
}
