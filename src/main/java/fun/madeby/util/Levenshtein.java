package fun.madeby.util;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * Created by Gra_m on 2022 07 05
 */

public final class Levenshtein {
	private static String original;
	private static String destination;
	private static int [][] result;
	private static Logger LOGGER;

	static{
		try {
			LOGGER = LoggerSetUp.setUpLogger("Levenshtein");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static int levenshteinDistance(final String original, final String destination, Boolean print) {
		LOGGER.finest("@Levenshtein levenshteinDistance(String original, String destination, Boolean print)");
		if (original.equalsIgnoreCase(destination)) return 0;
		if (original.isEmpty()) return destination.length();
		if (destination.isEmpty()) return original.length();

		// todo FIXED Levenshtein create matrix with extra length on each side so there is somewhere to look back at the data from.
		int[][] result = new int[original.length() + 1][destination.length() + 1];

		// todo FIXED not sure if +1 necessary, or recommended:
		// initialise with IntStream new static IntStream range(int startInclusive, int endExclusive)
		IntStream.range(0, original.length() + 1 ).forEach(j-> result[j][0] = j);
		IntStream.range(0, destination.length() + 1 ).forEach(i-> result[0][i] = i);

		int editRequired;
					for (int o = 1; o <= original.length(); o++) { //todo FIXED Levenshtein now go up to entire length with extra position
						for (int d = 1; d <= destination.length(); d++) {
							if (original.charAt(o - 1) == destination.charAt(d - 1))
								editRequired = 0;
							else
								editRequired = 1;

							int originalAxis = result[o][d - 1] + editRequired;
							int destinationAxis = result[o - 1][d] + editRequired;
							int diagonal = result[o - 1][d - 1] + editRequired;
							LOGGER.finest("levenshteinDistance(String original, String destination, Boolean print)\n->originalAxis: " + originalAxis + " destinationAxis: " + destinationAxis + " currentDiagonalMatrix: " + diagonal);

							// populate diagonalMatrixResult
							int min = Math.min(Math.min(destinationAxis, originalAxis), diagonal);
							//if(o==d) System.out.println("Printing diagonal; describes the Ldistance perfectly when the words have the same lengths: " + o  + " 'DiagonalAxisResult': " + min); // todo left in for reference
								
							result[o][d] = min;
						}
					}

				// todo FIXED Levenshtein, because of the extra space the final diagonal reference has been calculated and can be returned.
				if (print) {
					Levenshtein.original = original;
					Levenshtein.destination = destination;
					Levenshtein.result = result;
					//printBasicOriginalTop();
					printDynamicProgrammingTable();
				}
				return result[original.length()][destination.length()];
			}

	/*private static void printBasicDestinationTop() {
		for (int o = 0; o <= original.length(); o++) {
			for (int d = 0; d <= destination.length(); d++) {
				System.out.printf("| " + result[o][d] + "%s" + " |" + "%s",
						result[o][d] >= 0  && result[o][d] < 10 ? " " : "",
						d == destination.length() ? "\n" : "");
			}
		}
	}*/

	private static void printBasicOriginalTop() {
		for (int d = 0; d <= destination.length(); d++) {
			for (int o = 0; o <= original.length(); o++) {
				System.out.printf("| " + result[o][d] + "%s" + " |" + "%s",
						result[o][d] >= 0  && result[o][d] < 10 ? " " : "",
						o == original.length() ? "\n" : "");
			}
		}
	}

	private static void printDynamicProgrammingTable() {
		StringBuilder destinationPrintReady = new StringBuilder (destination);
		int originalCharCount = 0;
		boolean trigger = true;
		System.out.println("printDynamicProgrammingTable():");
		printTopRow();

		for (int d = 0; d <= destination.length(); d++) {
			if (originalCharCount < destination.length())
				trigger = true;
			for (int o = 0; o <= original.length(); o++) {
				if (o == 0 && trigger) {
					if (d == 0) {
						System.out.print("| " + "\"\"" + " || 0  |");
						continue;
					}
					System.out.printf("| " + "%c" + "  |",
							destinationPrintReady.charAt(originalCharCount));
					originalCharCount++;
					trigger = false;
				}
				System.out.printf("| " + result[o][d] + "%s" + " |" + "%s",
						result[o][d] >= 0  && result[o][d] < 10 ? " " : "",
						o == original.length() ? "\n" : "");
			}
		}
	}

	private static void printTopRow() {
		StringBuilder originalPrintReady = new StringBuilder (original);
		System.out.print("|    || \"\" |");
		for(int i = 0; i < originalPrintReady.length(); i++) {
			System.out.printf("| " + "%c" + "  |" + "%s",
					originalPrintReady.charAt(i),
					(i != originalPrintReady.length() -1) ? "" : "\n");
		}
	}
}
