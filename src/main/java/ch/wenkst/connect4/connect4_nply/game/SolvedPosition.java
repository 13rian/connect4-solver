package ch.wenkst.connect4.connect4_nply.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.wenkst.connect4.connect4_nply.configuration.AppConfig;

public class SolvedPosition implements Serializable {
	private Position position; 				// connect4 position
	private int strongScore; 				// strong score of the position
	private List<Integer> strongMoves; 		// optimal moves for the fastest win
	private int weakScore; 					// weak score of the position
	private List<Integer> weakMoves; 		// winning moves or drawing moves if the position is drawn

	
	
	/**
	 * strong and weak solutions of a connect4 position
	 * @param position			connect4 position
	 */
	public SolvedPosition(Position position) {
		this.position = position;
	}
	
	
	/**
	 * adds the result to the solved position
	 * @param moves 		all legal moves (column number to play)
	 * @param scores 		all scores corresponding to the moves list
	 */
	public void addResult(List<Integer> moves, List<Integer> scores) {
		addStrongResults(moves, scores);
		addWeakResults(moves, scores);
	}

	
	/**
	 * adds the result to the solved position (strong solver)
	 * @param moves		all legal moves (column number to play)
	 * @param scores	all scores corresponding to the moves list
	 */
	private void addStrongResults(List<Integer> moves, List<Integer> scores) {
		// the scores are for the opponent player therefore the move with the smallest score
		// is the optimal move
		
		
		// find the minimum
		strongScore = AppConfig.maxScore;
		for (int i=0; i<scores.size(); i++) {
			if (scores.get(i) < strongScore) {
				strongScore = scores.get(i);
			}
		}
		
		// get all moves with the minimal score
		strongMoves = new ArrayList<Integer>();
		for (int i=0; i<scores.size(); i++) {
			if (scores.get(i) == strongScore) {
				strongMoves.add(moves.get(i));
			}
		}
		
		// revert the sign since this is the score of the opponent
		strongScore = -strongScore;
	}
	
	
	/**
	 * adds the result to the solved position (weak solver)
	 * @param moves		all legal moves (column number to play)
	 * @param scores	all scores corresponding to the moves list
	 */
	private void addWeakResults(List<Integer> moves, List<Integer> scores) {
		// the scores are for the opponent player, if the smallest score is:
		// <0 	: game is won
		//  0	: game is drawn
		// >0 	: game is lost
		
		// find the minimal score
		weakScore = AppConfig.maxScore;
		for (int i=0; i<scores.size(); i++) {
			if (scores.get(i) < weakScore) {
				weakScore = scores.get(i);
			}
		}
		
		// set the score to -1/0/1
		if (weakScore != 0) {
			weakScore = weakScore / Math.abs(weakScore);
		}		
		
		
		// check if there are only losing moves
		weakMoves = new ArrayList<Integer>();
		if (weakScore > 0) {
			weakScore = -weakScore; 		// revert the sign since this is the score of the opponent
			return;
		}
		
		
		// get all moves with the minimal score
		for (int i=0; i<scores.size(); i++) {
			int weakPosScore = (scores.get(i) == 0) ? 0 : scores.get(i) / Math.abs(scores.get(i));
			 
			if (weakPosScore == weakScore) {
				weakMoves.add(moves.get(i));
			}
		}
		
		// revert the sign since this is the score of the opponent
		weakScore = -weakScore;
	}
	
	
	/**
	 * returns all strong moves concatenated to one string that is separated by a -
	 */
	public String getStrongMovesStr() {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<strongMoves.size(); i++) {
			sb.append(strongMoves.get(i)).append("-");
		}
		
		// remove the last delimiter
		sb.deleteCharAt(sb.length() - 1);
		
		return sb.toString();
	}
	
	
	/**
	 * returns all weak moves concatenated to one string that is separated by a -
	 */
	public String getWeakMovesStr() {
		if (weakMoves.size() == 0) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<weakMoves.size(); i++) {
			sb.append(weakMoves.get(i)).append("-");
		}
		
		// remove the last delimiter
		sb.deleteCharAt(sb.length() - 1);
		
		return sb.toString();
	}
	
	
	@Override
	public String toString() {
		return "strong score: " + strongScore + ", optimal moves: " + Arrays.toString(strongMoves.toArray()) + ", weak moves: " + Arrays.toString(weakMoves.toArray());
	}
	

	public Position getPosition() {
		return position;
	}


	public List<Integer> getStrongMoves() {
		return strongMoves;
	}


	public List<Integer> getWeakMoves() {
		return weakMoves;
	}


	public int getStrongScore() {
		return strongScore;
	}


	public int getWeakScore() {
		return weakScore;
	}
}
