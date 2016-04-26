package smalltalk.test;

import org.junit.Test;

public class TestArrays extends BaseTest {
	@Test public void newArray() {
		String input =
			"|a|\n"+
			"a := Array new.\n"+
			"^a";
		String expecting = "{nil. nil. nil. nil. nil. nil. nil. nil. nil. nil}";
		execAndCheck(input, expecting);
	}

	@Test public void newArray5() {
		String input =
			"|a|\n"+
			"a := Array new: 5.\n"+
			"^a";
		String expecting = "{nil. nil. nil. nil. nil}";
		execAndCheck(input, expecting);
	}

	@Test public void arraySize() {
		String input =
			"|a|\n"+
			"a := Array new: 5.\n"+
			"^a size.";
		String expecting = "5";
		execAndCheck(input, expecting);
	}

	@Test public void fillCharArray() {
		String input =
			"|a|\n"+
			"a := Array new: 5.\n"+
			"1 to: 5 do: [:i | a at: i put: $a + i].\n" +
			"^a";
		String expecting = "{$b. $c. $d. $e. $f}";
		execAndCheck(input, expecting);
	}

	@Test public void arrayDo() {
		String input =
			"|a n|\n"+
			"a := Array new: 5.\n"+
			"1 to: 5 do: [:i | a at: i put: i].\n" +
			"n := 0.\n" +
			"a do: [:x | n := n + x].\n" +
			"^n";
		String expecting = "15";
		execAndCheck(input, expecting);
	}

	@Test public void arrayMap() {
		String input =
			"|a n|\n"+
			"a := Array new: 5.\n"+
			"1 to: 5 do: [:i | a at: i put: i].\n" +
			"n := 0.\n" +
			"a := a map: [:x | 2*x].\n" +
			"^a";
		String expecting = "{2. 4. 6. 8. 10}";
		execAndCheck(input, expecting);
	}

	@Test public void arrayFilter() {
		String input =
			"|a n|\n"+
			"a := Array new: 8.\n"+
			"1 to: 8 do: [:i | a at: i put: i].\n" +
			"n := 0.\n" +
			"a := a filter: [:x | x<4].\n" +
			"^a asString";
		String expecting = "LinkedList(1. 2. 3)";
		//String result = compile(input, true);
		execAndCheckWithLinkedList(input, expecting);
	}

	@Test public void arrayIndex() {
		String input =
			"|a|\n"+
			"a := Array new: 5.\n"+
			"1 to: 5 do: [:i | a at: i put: $a + i].\n" +
			"(a at: 1), (a at: 2), (a at: 5)." +
			"^a.";
		String expecting = "{$b. $c. $d. $e. $f}";
		execAndCheck(input, expecting);
	}
}
