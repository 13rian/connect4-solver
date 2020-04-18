package ch.wenkst.connect4.connect4_nply;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ch.wenkst.sw_utils.logging.Log;

public class TestPositionParser {
	private static Log log = Log.getLogger(TestPositionParser.class);
	

	/**
	 * reads in a file with some test positions
	 * @param filePath 	path of the file with the test positions
	 * @return			a list with all the test positions form the file or an empty list if an error occurred
	 */
	public List<TestPosition> positionsFromFile(String filePath) {
		List<TestPosition> testPositions = new ArrayList<>();

		try {
			File positionFile = new File(filePath);
			Scanner scanner = new Scanner(positionFile);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] parts = line.split(" ");
				String moveSequence = parts[0].trim();
				int score = Integer.parseInt(parts[1]);
				TestPosition testPosition = new TestPosition(moveSequence, score);
				testPositions.add(testPosition);
			}

			scanner.close();
		} catch (Exception e) {
			log.severe("error parsing the position file", e);
			return new ArrayList<TestPosition>();
		}

		return testPositions;
	}
}
