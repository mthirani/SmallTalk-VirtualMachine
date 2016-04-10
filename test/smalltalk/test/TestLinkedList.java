package smalltalk.test;

import org.junit.Test;

public class TestLinkedList extends BaseTest {
	@Test public void testEmpty() {
		String input =
			"| ll |\n" +
			"ll := LinkedList new.\n" +
			"^ll asString.";
		String expecting = "LinkedList()";
		execAndCheckWithLinkedList(input, expecting);
	}

	@Test public void testAdd() {
		String input =
			"| ll |\n" +
			"ll := LinkedList new.\n" +
			"ll add: 'hi'.\n" +
			"ll add: 'mom'.\n" +
			"^ll asString.";
		String expecting = "LinkedList(hi. mom)";
		execAndCheckWithLinkedList(input, expecting);
	}

	@Test public void testAddFirst() {
		String input =
			"| ll |\n" +
			"ll := LinkedList new.\n" +
			"ll addFirst: 'x'.\n" +
			"ll addFirst: 'y'.\n" +
			"ll addFirst: 'z'.\n" +
			"^ll asString.";
		String expecting = "LinkedList(z. y. x)";
		execAndCheckWithLinkedList(input, expecting);
	}

	@Test public void testRemoveAll() {
		String input =
			"| ll |\n" +
			"ll := LinkedList new.\n" +
			"ll addFirst: 'x'.\n" +
			"ll addFirst: 'y'.\n" +
			"ll addFirst: 'z'.\n" +
			"ll removeAll." +
			"^ll asString";
		String expecting = "LinkedList()";
		execAndCheckWithLinkedList(input, expecting);
	}

	@Test public void testRemoveFirst() {
		String input =
			"| ll |\n" +
			"ll := LinkedList new.\n" +
			"ll add: 'x'.\n" +
			"ll add: 'y'.\n" +
			"ll removeFirst.\n" +
			"^ll asString";
		String expecting = "LinkedList(y)";
		execAndCheckWithLinkedList(input, expecting);
	}

	@Test public void testHetero() {
		String input =
			"| ll |\n" +
			"ll := LinkedList new.\n" +
			"ll add: 'hi'.\n" +
			"ll add: 13.\n" +
			"ll add: 3.4.\n" +
			"^ll asString";
		String expecting = "LinkedList(hi. 13. 3.4)";
		execAndCheckWithLinkedList(input, expecting);
	}
}
