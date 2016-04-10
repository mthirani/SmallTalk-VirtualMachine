package smalltalk.test;

import org.junit.Test;

public class TestClasses extends BaseTest {
	@Test public void testClassAndNew() {
		String input =
			"class T [ ]\n" +
			"^T new";
		String expecting = "a T";
		execAndCheck(input, expecting);
	}

	@Test public void testClassAndNew2() {
		String input =
			"class T [\n" +
			"    |x|\n" +
			"    initialize [ super initialize. x:=1. ]\n" + // define ctor's with initialize
			"    value [ ^x ]\n" +
			"]\n" +
			"^(T new) value";
		String expecting = "1";
		execAndCheck(input, expecting);
	}

	@Test public void testClassAndNewArg() {
		String input =
			"class T [\n" +
			"    | _x |\n" +
			"    initialize: x [ self initialize. _x:=x. ]\n" +
			"    value [ ^_x ]\n" +
			"]\n" +
			"^(T new: 99) value";
		String expecting = "99";
		execAndCheck(input, expecting);
	}

	@Test public void testInheritMethodAndField() {
		String input =
			"class T [\n" +
			"    | _x |\n" +
			"    initialize: x [ self initialize. _x:=x. ]\n" +
			"    value [ ^_x ]\n" +
			"]\n" +
			"class U : T [\n" +
			"]\n" +
			"^(U new: 99) value";
		String expecting = "99";
		execAndCheck(input, expecting);
	}

	@Test public void testOverrideMethodAndInheritField() {
		String input =
			"class T [\n" +
			"    | _x |\n" +
			"    initialize: x [ self initialize. _x:=x. ]\n" +
			"    value [ ^_x ]\n" +
			"]\n" +
			"class U : T [\n" +
			"    value [ ^_x + 1 ]\n" +
			"]\n" +
			"^(U new: 99) value";
		String expecting = "100";
		execAndCheck(input, expecting);
	}

	@Test public void testUnary() {
		String input =
			"class T [\n" +
			"    value [ ^99 ]\n" +
			"]\n" +
			"^(T new) value";
		String expecting = "99";
		execAndCheck(input, expecting);
	}

	@Test public void testBinary() {
		String input =
			"class T [\n" +
			"    | _x |\n" +
			"    initialize: x [ self initialize. _x:=x. ]\n" +
			"    + y [ ^_x+y ]\n" +
			"]\n" +
			"^(T new: 1) + 99";
		String expecting = "100";
		execAndCheck(input, expecting);
	}

	@Test public void testKeyword1Arg() {
		String input =
			"class T [\n" +
			"    | _x |\n" +
			"    initialize: x [ self initialize. _x:=x. ]\n" +
			"    foo: y [ ^_x+y ]\n" +
			"]\n" +
			"^(T new: 1) foo: 99";
		String expecting = "100";
		execAndCheck(input, expecting);
	}

	@Test public void testKeyword2Arg() {
		String input =
			"class T [\n" +
			"    foo: a bar: b [ ^a+b ]\n" +
			"]\n" +
			"^(T new) foo: 99 bar: 99";
		String expecting = "198";
		execAndCheck(input, expecting);
	}
}
