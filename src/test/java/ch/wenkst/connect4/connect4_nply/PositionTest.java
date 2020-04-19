package ch.wenkst.connect4.connect4_nply;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.Random;

import org.junit.jupiter.api.Test;

import ch.wenkst.connect4.connect4_nply.configuration.AppConfig;
import ch.wenkst.connect4.connect4_nply.game.Position;

public class PositionTest {
	@Test
	public void mirrorPositions() {
		// create p1 and p2 where p2 is the mirror image of p2
		Random random = new Random(Instant.now().toEpochMilli());

		for (int j=0; j<100; j++) {
			// play 8 random moves
			Position p1 = new Position();
			Position p2 = new Position();
			for (int i=0; i<8; i++) {
				int move = random.nextInt(AppConfig.boardWidth);
				int mirroredMove = AppConfig.boardWidth - 1 - move;

				p1.play(move);				// play the random move in position 1
				p2.play(mirroredMove);		// play the mirrored move in position 2
				
				if (p1.isWon()) {
					break;
				}
			}

			Position p1Mirror = p1.mirror();

			assertEquals(p2.getPosition(), p1Mirror.getPosition(), "mirrored positions are equal");
			assertEquals(p2.getDiskMask(), p1Mirror.getDiskMask(), "mirrored disk masks are equal");
		}
	}
	
	
	@Test
	public void moveSequenceTest() {
		String moveSequence = "1112234444566777";
		Position position = new Position();
		position.fromMoveSequence(moveSequence);
		
		Position expectedPosition = new Position();
		expectedPosition.play(0);
		expectedPosition.play(0);
		expectedPosition.play(0);
		expectedPosition.play(1);
		expectedPosition.play(1);
		expectedPosition.play(2);
		expectedPosition.play(3);
		expectedPosition.play(3);
		expectedPosition.play(3);
		expectedPosition.play(3);
		expectedPosition.play(4);
		expectedPosition.play(5);
		expectedPosition.play(5);
		expectedPosition.play(6);
		expectedPosition.play(6);
		expectedPosition.play(6);
		
		assertEquals(expectedPosition.getPosition(), position.getPosition(), "position from move sequence equal");
		assertEquals(expectedPosition.getDiskMask(), position.getDiskMask(), "disk mask from move sequence equal");
	}
}
