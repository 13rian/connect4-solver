package ch.wenkst.connect4.connect4_nply.game;

import ch.wenkst.connect4.connect4_nply.configuration.AppConfig;

/**
 * class that orders the move according to the score function
 */
public class MoveSorter {
	private long[] moves = new long[AppConfig.boardWidth];
	private byte[] scores = new byte[AppConfig.boardWidth];
	private int size = 0;


	/**
	 * adds the move and the score the the move sorter, the elements are inserted with the insert sort algorithm
	 * which is fast for small lists
	 * @param moveMask		move mask
	 * @param score			the score of the move
	 */
	public void add(long move, byte score) {
		int pos = size++;
		for (; pos > 0 && scores[pos-1] > score; --pos) {
			scores[pos] = scores[pos-1];
			moves[pos] = moves[pos-1];
		}
		moves[pos] = move;
		scores[pos] = score;
	}

	public long[] getMoves() {
		return moves;
	}

	public int size() {
		return size;
	}
}
