package fun.madeby.util;



/**
 * Created by Gra_m on 2022 07 05
 */

public final class Levenshtein {

	public static int levenshteinDistance(final String original, final String destination) {
		if (original.equalsIgnoreCase(destination)) return 0;
		if (original.isEmpty()) return destination.length();
		if (destination.isEmpty()) return original.length();

		// todo FIXED Levenshtein create matrix with extra length on each side so there is somewhere to look back at the data from.
		int[][] result = new int[original.length() + 1][destination.length() + 1];

		// todo FIXED not sure if +1 necessary, or recommended:
		for (int j = 0; j < original.length() + 1; j++)
			result[j][0] = j;
		for (int i = 0; i < destination.length() + 1; i++)
			result[0][i] = i;

		// initialis new:
		//static IntStream range(int startInclusive,
		//int endExclusive)
	//	IntStream.range(0, original.length() ).forEach(i-> result[i][0] = i);
	//	IntStream.range(0, destination.length() ).forEach(i-> result[0][i] = i);



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
							System.out.println("originalAxis: " + originalAxis + "destinationAxis: " + destinationAxis + " currentDiagonalMatrix: " + diagonal);
							// populate diagonalMatrixResult
							int min = Math.min(Math.min(destinationAxis, originalAxis), diagonal);
							if(o==d)
								System.out.println("Useful only for words with same lengths: o==d " + o  + " 'DiagonalAxisResult': " + min);
								
							result[o][d] = min;
						}
					}


				// todo FIXED Levenshtein, because of the extra space the final diagonal reference has been calculated and can be returned.
				return result[original.length()][destination.length()];


			}
		}
