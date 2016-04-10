package smalltalk.test;

import org.junit.Test;

public class TestDict extends BaseTest {
	@Test public void testEmpty() {
		String input =
			"| d |\n" +
			"d := Dictionary new.\n" +
			"^d asString.";
		String expecting = "Dictionary()";
		execAndCheckWithDict(input, expecting);
	}

	@Test public void testAssoc() {
		String input =
			"^('hi' -> 'mom') asString\n";
		String expecting = "hi->mom";
		execAndCheckWithDict(input, expecting);
	}

	@Test public void testAssocHetero() {
		String input =
			"^('hi' -> 34) asString\n";
		String expecting = "hi->34";
		execAndCheckWithDict(input, expecting);
	}

	@Test public void testListAssoc() {
		String input =
			"^{'hi' -> 34. 'mom'->99} asString\n";
		String expecting = "Array(hi->34. mom->99)";
		execAndCheckWithDict(input, expecting);
	}

	@Test public void testFromListAssoc() {
		String input =
			"|d|\n" +
			"d := Dictionary newFrom: {'hi' -> 34. 'mom'->99}.\n" +
			"^d asString\n";
		String expecting = "Dictionary(mom->99. hi->34)";
		execAndCheckWithDict(input, expecting);
	}

	@Test public void testKeys() {
		String input =
			"|d|\n" +
			"d := Dictionary newFrom: {'hi' -> 34. 'mom'->99}.\n" +
			"^d keys asString\n";
		String expecting = "Array(mom. hi)";
		execAndCheckWithDict(input, expecting);
	}

	@Test public void testValues() {
		String input =
			"|d|\n" +
			"d := Dictionary newFrom: {'hi' -> 34. 'mom'->99}.\n" +
			"^d values asString\n";
		String expecting = "Array(99. 34)"; // ordered not guaranteed
		execAndCheckWithDict(input, expecting);
	}

	@Test public void testAt() {
		String input =
			"|d|\n" +
			"d := Dictionary newFrom: {'hi' -> 34. 'mom'->99}.\n" +
			"^{d at: 'hi'. d at: 'mom'}\n";
		String expecting = "{34. 99}"; // ordered not guaranteed
		execAndCheckWithDict(input, expecting);
	}
}
