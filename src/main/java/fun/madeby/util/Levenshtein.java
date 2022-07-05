package fun.madeby.util;

import static java.util.Objects.isNull;

/**
 * Created by Gra_m on 2022 07 05
 */

public final class Levenshtein {

	public static int levenshteinDistance(final String lhs, final String rhs) {
		if (lhs.equalsIgnoreCase(rhs)) return 0;
		if (lhs.isEmpty()) return rhs.length();
		if (rhs.isEmpty()) return lhs.length();

		// create matrix

		int[][] result = new int[lhs.length()][rhs.length()];

		// initialise old:
		for (int i = 0; i < lhs.length(); i++)
			result[i][0] = i;
		for (int i = 0; i < rhs.length(); i++)
			result[0][i] = i;

		// initialis new:
		//static IntStream range(int startInclusive,
		//int endExclusive)

		// this version fails as subst (renaming 'edit') is not counted if the difference is at the end. John Johx
		int subst;
					for (int j = 1; j < rhs.length(); j++) {
						for (int i = 1; i < lhs.length(); i++) {
							if (lhs.charAt(i - 1) == rhs.charAt(j - 1))
								subst = 0;
							else
								subst = 1;
							int deletion = result[i - 1][j] + 1;
							int insertion = result[i][j - 1] + 1;
							int substitution = result[i - 1][j - 1] + subst;
							result[i][j] = Math.min(Math.min(deletion, insertion), substitution);
						}
					}
				return result[lhs.length() - 1][rhs.length() - 1];

			}
		}
