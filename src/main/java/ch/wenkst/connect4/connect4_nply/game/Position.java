package ch.wenkst.connect4.connect4_nply.game;

import java.io.Serializable;

import ch.wenkst.connect4.connect4_nply.configuration.AppConfig;

/**
 * holds one connect4 position, it is represented by the following bit pattern
 * 5 12 19 26 33 40 47
 * 4 11 18 25 32 39 46
 * 3 10 17 24 31 38 45
 * 2  9 16 23 30 37 44
 * 1  8 15 22 29 36 43
 * 0  7 14 21 28 35 42
 * 
 * the position is saved with 2 integers, one for the disks of the current player and one
 * for the non-empty spots
 */
public class Position implements Serializable {	
	private static long bottomMask = bottomRowMask(); 									// mask of bottom row
	private static long boardMask = bottomMask * ((1L << AppConfig.boardHeight)-1);		// mask of the whole board

	private long position = 0; 				// disks of the current player
	private long diskMask = 0; 				// non-empty spots on the board
	
	
	public Position() {
		
	}	
	
	public Position(long position, long diskMask) {
		this.position = position;
		this.diskMask = diskMask;
	}
	
	
	/**
	 * copy constructor
	 * @param that
	 */
	public Position(Position that) {
	    this(that.getPosition(), that.getDiskMask());
	}
	

	/**
	 * returns true if the passed column is playable
	 * @param col 	index of the column (0-6)
	 * @return 		true if the move is legal, false if it is illegal
	 */
	public boolean legalMove(int col) {
		return (diskMask & topMask(col)) == 0;
	}


	/**
	 * puts the disk of the current player in the passed column
	 * @param col 	board column
	 */
	public void play(int col) {
		position ^= diskMask; 						// get the position of the opponent
		diskMask |= diskMask + bottomMask(col);  	// add the move to the disk mask
	}
	
	
	/**
	 * plays the passed move mask
	 * @param moveMask 	move mask
	 */
	public void play(long moveMask) {
		position ^= diskMask; 						// get the position of the opponent
		diskMask |= diskMask + moveMask;  			// add the move to the disk mask
	}


	/**
	 * creates a position form the passed sequence of moves (starting from 1)
	 * @param moveSequence		a sequence of moves that define a position
	 * @return 					true if the position could be created and is not winning
	 */
	public boolean fromMoveSequence(String moveSequence) {		
		for (int i = 0; i < moveSequence.length(); i++) {
			String columnStr = String.valueOf(moveSequence.charAt(i));
			int column = Integer.parseInt(columnStr) - 1;
			
			// check if the move is invalid
			if (column < 0 || column >= AppConfig.boardWidth || !legalMove(column) || isWinningMove(column)) {
				return false;
			}
			
			// play the move
			play(column);
		}

		return true;
	}
	
	
	/**
	 * creates a position form the passed sequence of column indices (starting from 0)
	 * @param columnSequence	a sequence of columns that define a position
	 * @return 					true if the position could be created and is not winning
	 */
	public boolean fromColumnSequence(String columnSequence) {		
		for (int i = 0; i < columnSequence.length(); i++) {
			String columnStr = String.valueOf(columnSequence.charAt(i));
			int column = Integer.parseInt(columnStr);
			
			// check if the move is invalid
			if (column < 0 || column >= AppConfig.boardWidth || !legalMove(column) || isWinningMove(column)) {
				return false;
			}
			
			// play the move
			play(column);
		}

		return true;
	}
	
	
	/**
	 * returns true if the current player can win or the opponent can win with his next move
	 * @return 	true if the next move is forced, false otherwise
	 */
	public boolean isWon() {
		// check if the current player can win with the next move
		if (canWinNext()) {
			return true;
		}
		
		// check if the current player can make a move that does not lose after the opponent played a move
		if (nonLosingMoves() == 0) {
			return true;
		}
		
		return false;
	}


	/**
	 * returns true if the position is winning for the current player if the passed
	 * column is played
	 * @param col 	board column
	 * @return
	 */
	public boolean isWinningMove(int col) {
		return (winningMask() & legalMovesMask() & columnMask(col)) > 0;
	}



	/**
	 * returns a unique key of the position
	 * @return 	 integer key representing the position
	 */
	public long toKey() {
		return position + diskMask;
	}
	
	
	/**
	 * returns a position that is mirrored on the y-axis
	 * @return
	 */
	public Position mirror() {
		Position mirroredPosition = new Position(mirrorBoardNumber(position), mirrorBoardNumber(diskMask));
		return mirroredPosition;
	}
	
	
	/**
	 * mirrors the passed number representing a board 
	 * @param number 	any number representing some board configuration (position, board mask, disk maksk etc)
	 * @return
	 */
	private long mirrorBoardNumber(long number) {		
		long mirroredNumber = 0;
		
		// left half of the board
		for (int col=0; col < (AppConfig.boardWidth+1)/2 - 1; col++) {
			mirroredNumber += (number & columnMask(col)) << ((AppConfig.boardWidth - (2*col + 1)) * (AppConfig.boardHeight+1));
		}
		
		// right half of the board
		for (int col=0; col < (AppConfig.boardWidth+1)/2 - 1; col++) {
			mirroredNumber += (number & columnMask(AppConfig.boardWidth - col-1)) >> ((AppConfig.boardWidth - (2*col + 1)) * (AppConfig.boardHeight+1));
		}
		
		// center row
		if (AppConfig.boardWidth % 2 != 0) {
			int col = (AppConfig.boardWidth)/2;
			mirroredNumber += (number & columnMask(col));
		}
		
		return mirroredNumber;
	}
	
	
	/**
	 * returns a string representation of the board
	 * 1: red
	 * 2: yellow
	 */
	public String toString() {
		// get the player who's move it is
		int currentPlayer = 1;
		int opponentPlayer = 2;
		if (getMoveCount() % 2 == 1) {
			currentPlayer = 2;
			opponentPlayer = 1;
		}
		
		// get the two positions
		long currentPosition = position;
		long opponentPosition = position ^ diskMask;
		
		StringBuilder posStr = new StringBuilder();
		for (int h=AppConfig.boardHeight-1; h>-1; h--) {
			for (int w=0; w<AppConfig.boardWidth; w++) {
				int shift = h + w*(AppConfig.boardHeight + 1);
				
				long mask = 1L << shift;
				if ((currentPosition & mask) > 0) {
					posStr.append(currentPlayer + " ");
				
				} else if ((opponentPosition & mask) > 0) {
					posStr.append(opponentPlayer + " ");
					
				} else {
					posStr.append("0 ");
				}
				// System.out.println(shift);
			}
			posStr.append("\n");
		}
		
		return posStr.toString();
	}
	

	///////////////////////////////////////////////////////////////////////////////////////////////
	// 								anticipate losing moves 									 //
	///////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * return true if current player can win with his next move
	 */
	public boolean canWinNext() {
		return (winningMask() & legalMovesMask()) > 0;
	}


    /**
     * returns a mask of all moves that can be played which do not lose after the opponent
     * played. this method will only find moves that block opponents winning chances
     * and will sometimes miss direct winning moves
	 * @return
	 */
	public long nonLosingMoves() {
		// assert(!canWinNext());
		long legalMoves = legalMovesMask();
		long winningOpponentMoves = winningOpponentMask();
		long forcedMoves = legalMoves & winningOpponentMoves; 	// moves that need to be played to avoid losing
		if (forcedMoves > 0) {
			// check if there is more than one forced move
			if ((forcedMoves & (forcedMoves - 1)) > 0) {
				// the opponent has two winning moves, current player will lose for sure
				return 0; 
				
			} else {
				// only one forces move
				legalMoves = forcedMoves;    
			}
		}
		
		// avoid to play below an opponent winning spot
		return legalMoves & ~(winningOpponentMoves >> 1);  
	}


	/**
	 * returns a bitmask with all possible winning moves for the current player
	 * @return
	 */
    private long winningMask() {
    	return winningMoveMask(position, diskMask);
    }


    /**
     * returns a bitmask with all possible winning moves for the opponent player
     * @return
     */
    private long winningOpponentMask() {
    	return winningMoveMask(position ^ diskMask, diskMask);
    }

	
	/**
	 * returns a bitmask with all winning moves for the passed position
	 * to detect a potential row of 4 the following 4 cases need to be checked: 
	 * - row of three on right / top side, 1110
	 * - row of three on left / bottom side, 0111
	 * - hole right / bottom, 1101
	 * - hole left / bottom, 1011
	 * potential vertical rows of 4 can only be played on top, therefore there is only
	 * one case:
	 * - row of three top, 1110
	 * @param position		the position of the current player
	 * @param diskMask		the mask containing all disks
	 * @return
	 */
	private long winningMoveMask(long position, long diskMask) {
		// vertical (check if a row on top can be completed, no holes possible)
		long r = (position << 1) & (position << 2) & (position << 3);

		// horizontal
		long p = (position << (AppConfig.boardHeight+1)) & (position << 2*(AppConfig.boardHeight+1));
		r |= p & (position << 3*(AppConfig.boardHeight+1));	// position right
		r |= p & (position >> (AppConfig.boardHeight+1));	// hole right
		
		p = (position >> (AppConfig.boardHeight+1)) & (position >> 2*(AppConfig.boardHeight+1));
		r |= p & (position << (AppConfig.boardHeight+1));	// position left
		r |= p & (position >> 3*(AppConfig.boardHeight+1)); // hole left

		// diagonal \
		p = (position << AppConfig.boardHeight) & (position << 2*AppConfig.boardHeight);
		r |= p & (position << 3*AppConfig.boardHeight);
		r |= p & (position >> AppConfig.boardHeight);
		p = (position >> AppConfig.boardHeight) & (position >> 2*AppConfig.boardHeight);
		r |= p & (position << AppConfig.boardHeight);
		r |= p & (position >> 3*AppConfig.boardHeight);

		// diagonal /
		p = (position << (AppConfig.boardHeight+2)) & (position << 2*(AppConfig.boardHeight+2));
		r |= p & (position << 3*(AppConfig.boardHeight+2));
		r |= p & (position >> (AppConfig.boardHeight+2));
		p = (position >> (AppConfig.boardHeight+2)) & (position >> 2*(AppConfig.boardHeight+2));
		r |= p & (position << (AppConfig.boardHeight+2));
		r |= p & (position >> 3*(AppConfig.boardHeight+2));

		return r & (boardMask ^ diskMask);
	}
	
	
	/**
	 * returns a bitmask with all legal moves
	 * @return
	 */
    private long legalMovesMask() {
    	return (diskMask + bottomMask) & boardMask;
    }
    
    
    
	///////////////////////////////////////////////////////////////////////////////////////////////
	// 								exploration move ordering 									 //
	///////////////////////////////////////////////////////////////////////////////////////////////
	
    /**
     * score function for move exploration order. moves that would result in more winning
     * chances get a higher score. the score is equal to the number of possible winning moves
     * after the move was played
     * @param moveMask		move in the bitmask format
     */
	public byte moveScore(long moveMask) {
		return (byte) Long.bitCount(winningMoveMask(position | moveMask, diskMask));
	}
	
	

	///////////////////////////////////////////////////////////////////////////////////////////////
	// 								mask methods calculation 									 //
	///////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * returns a bitmask with all cells of the passed column
	 * @param col 	board column
	 * @return
	 */
	public static long columnMask(int col) {
		return ((1L << AppConfig.boardHeight)-1) << col*(AppConfig.boardHeight+1);
	}
    
    
	/**
	 * returns a bitmask with the top cell of the passed column
	 * @param col 	board column
	 * @return
	 */
	private long topMask(int col) {
		return (1L << (AppConfig.boardHeight - 1)) << col*(AppConfig.boardHeight+1);
	}


	/**
	 * returns a bitmask with the bottom cell of the passed column
	 * @param col 	board column
	 * @return
	 */
	private long bottomMask(int col) {
		return 1L << col*(AppConfig.boardHeight+1);
	}
	
	
	/**
	 * returns a bitmask with the bottom row
	 * @return
	 */
	private static long bottomRowMask() {
		long result = 0;
		for (int col=0; col<AppConfig.boardWidth; col++) {
			result += 1L << col*(AppConfig.boardHeight+1);
		}
		return result;
	}	


	public byte getMoveCount() {
		return (byte) Long.bitCount(diskMask);
	}


	public long getPosition() {
		return position;
	}


	public long getDiskMask() {
		return diskMask;
	}
}
