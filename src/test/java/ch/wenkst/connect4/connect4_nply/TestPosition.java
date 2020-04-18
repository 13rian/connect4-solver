package ch.wenkst.connect4.connect4_nply;

import ch.wenkst.connect4.connect4_nply.game.Position;
import ch.wenkst.sw_utils.logging.Log;

public class TestPosition {
	private static Log log = Log.getLogger(TestPosition.class);
	
	private String moveSequence = ""; 	// defines a position by the played moves
	private int score = 0; 				// defines the score of the position
	
	/**
	 * holds one test position from the file
	 * @param moveSequence 		sequence of moves played
	 * @param score 			the score of the position
	 */
	public TestPosition(String moveSequence, int score) {
		this.moveSequence = moveSequence;
		this.score = score;
	}
	
	
	/**
	 * converts this test position to a connect4 position
	 * @return
	 */
	public Position toPosition() {
		Position position = new Position();
		boolean legalPosition = position.fromMoveSequence(moveSequence);
		if (!legalPosition) {
			log.severe("illegal position, move sequence: " + moveSequence);
		}
		return position;
	}

	public String getMoveSequence() {
		return moveSequence;
	}

	public int getScore() {
		return score;
	}
}
