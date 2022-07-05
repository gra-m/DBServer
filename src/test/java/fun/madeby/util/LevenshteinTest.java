package fun.madeby.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LevenshteinTest {

	@Test
	public void testLevenstein_0_distance() {
		int result = Levenshtein.levenshteinDistance("John", "John");
		assertEquals(0, result);
	}

	@Test
	public void testLevenstein_1_distanceEnd() {
		int result = Levenshtein.levenshteinDistance("Johx", "John");
		assertEquals(1, result);
	}

	@Test
	public void testLevenstein_1_distanceBegin() {
		int result = Levenshtein.levenshteinDistance("Cohn", "John");
		assertEquals(1, result);
	}

	@Test
	public void testLevenstein_empty_strings() {
		int result = Levenshtein.levenshteinDistance("", "");
		assertEquals(0, result);
	}


}