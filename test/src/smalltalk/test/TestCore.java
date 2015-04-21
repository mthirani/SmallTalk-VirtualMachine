package smalltalk.test;

import org.junit.Test;

public class TestCore extends BaseTest {
	@Test public void testEmpty() {
		String input = "";
		String expecting = "nil";
		execAndCheck(input, expecting);
	}

	@Test public void testNil() {
		String input = "^nil";
		String expecting = "nil";
		execAndCheck(input, expecting);
	}

	@Test public void testTrue() {
		String input = "^true";
		String expecting = "true";
		execAndCheck(input, expecting);
	}

	@Test public void testFalse() {
		String input = "^false";
		String expecting = "false";
		execAndCheck(input, expecting);
	}

	@Test public void testInt() {
		String input =
			"^34";
		String expecting = "34";
		execAndCheck(input, expecting);
	}

	@Test public void testNegInt() {
		String input =
			"^-34";
		String expecting = "-34";
		execAndCheck(input, expecting);
	}

	@Test public void testFloat() {
		String input =
			"^123456.789"; // float only handles 3 digits after '.' it seems
		String expecting = "123456.78906";
		execAndCheck(input, expecting);
	}

	@Test public void testNegFloat() {
		String input =
			"^-123456.789"; // float only handles 3 digits after '.' it seems? wtf? 3.14159 works.
		String expecting = "-123456.78906";
		execAndCheck(input, expecting);
	}

	@Test public void testIntOp() {
		execAndCheck("^1+2", "3");
		execAndCheck("^1 - 2", "-1");
		execAndCheck("^2*4", "8");
		execAndCheck("^4/2", "2");
		execAndCheck("^4/3", "1"); // truncates
		execAndCheck("^10=10", "true");
		execAndCheck("^1=2", "false");
		execAndCheck("^1<2", "true");
		execAndCheck("^1>2", "false");
		execAndCheck("^1<=2", "true");
		execAndCheck("^1>=2", "false");
		execAndCheck("^5 mod: 4", "1");
		execAndCheck("^5 hash", "5");
	}

	@Test public void testSelf() {
		String input =
			"^self";
		String expecting = "a MainClass";
		execAndCheck(input, expecting);
	}

	@Test public void testBoolean() {
		String input =
			"^true asString, false asString";
		String expecting = "truefalse";
		execAndCheck(input, expecting);
	}

	@Test public void testArrayLiteral() {
		String input =
			"^{1. 2. 3. 4.}";
		String expecting = "{1. 2. 3. 4}";
		execAndCheck(input, expecting);
	}

	@Test public void testArrayLiteralBiggerThanDefaultOperandStackSizeOf10() {
		String input =
			"^{1. 2. 3. 4. 5. 6. 7. 8. 9. 1. 2. 3. 4. 5. 6. 7. 8. 9. 1. 2. 3. 4. 5. 6. 7. 8. 9. 1. 2. 3. 4. 5. 6. 7. 8. 9}";
		String expecting = "{1. 2. 3. 4. 5. 6. 7. 8. 9. 1. 2. 3. 4. 5. 6. 7. 8. 9. 1. 2. 3. 4. 5. 6. 7. 8. 9. 1. 2. 3. 4. 5. 6. 7. 8. 9}";
		execAndCheck(input, expecting);
	}

	@Test public void testArrayExprsLiteral() {
		String input =
			"^{1. 2+3. 4 asString, $a}";
		String expecting = "{1. 5. 4$a}";
		execAndCheck(input, expecting);
	}

	@Test public void testNestedArrayLiteral() {
		String input =
			"^{1. {2. 3}. 4.}";
		String expecting = "{1. {2. 3}. 4}";
		execAndCheck(input, expecting);
	}

	@Test public void testHeteroArrayLiteral() {
		String input =
			"^{1. 3.14159. true. 'hi'}";
		String expecting = "{1. 3.14159. true. hi}";
		execAndCheck(input, expecting);
	}

	@Test public void testAssign() {
		String input =
			"| x |\n" +
			"x := 1." +
			"^x";
		String expecting = "1";
		execAndCheck(input, expecting);
	}

	@Test public void testStringIdentifyEquals() {
		String input =
			"| x y |\n" +
			"x := 'hi'." +
			"y := 'hi'." +
			"^x==y"; // string literals are usually same object
		String expecting = "true";
		execAndCheck(input, expecting);
	}

	@Test public void testStringEquals() {
		String input =
			"| x y |\n" +
			"x := 'hi'." +
			"y := 'hi'." +
			"^x=y"; // string literals are usually same object
		String expecting = "true";
		execAndCheck(input, expecting);
	}

	@Test public void testStringEquals2() {
		String input =
			"| x y |\n" +
			"x := 'hi'." +
			"y := 'foo'." +
			"^x=y"; // string literals are usually same object
		String expecting = "false";
		execAndCheck(input, expecting);
	}

	@Test public void testStringNotEquals() {
		String input =
			"| x y |\n" +
			"x := 'hi'." +
			"y := 'hi'." +
			"^x~=y"; // string literals are usually same object
		String expecting = "false";
		execAndCheck(input, expecting);
	}

	@Test public void testObjectIdentifyEquals() {
		String input =
			"| x y |\n" +
			"x := 'hi'." +
			"y := 'hi'." +
			"^x==y";
		String expecting = "true";
		execAndCheck(input, expecting);
	}

	@Test public void testChar() {
		String input =
			"| x |\n" +
			"x := $a." +
			"^x";
		String expecting = "$a";
		execAndCheck(input, expecting);
	}

	@Test public void testStringCat() {
		String input =
			"| x y |\n" +
			"x := 'abc'." +
			"y := 'def'." +
			"^x, y";
		String expecting = "abcdef";
		execAndCheck(input, expecting);
	}

	@Test public void testIf() {
		execAndCheck("^(true ifTrue:[99])", "99");
		execAndCheck("^(false ifTrue:[99])", "nil");
	}

	@Test public void testIfElse() {
		execAndCheck("^(true ifTrue:[99] ifFalse:[100])", "99");
		execAndCheck("^(false ifTrue:[99] ifFalse:[100])", "100");
	}

	@Test public void testIfFalse() {
		execAndCheck("^(true ifFalse:[99])", "nil");
		execAndCheck("^(false ifFalse:[99])", "99");
	}

	@Test public void testAnd() {
		execAndCheck("^true and: true", "true");
		execAndCheck("^true and: false", "false");
		execAndCheck("^false and: true", "false");
		execAndCheck("^false and: false", "false");
	}

	@Test public void testOr() {
		execAndCheck("^true or: true", "true");
		execAndCheck("^true or: false", "true");
		execAndCheck("^false or: true", "true", true, true);
		execAndCheck("^false or: false", "false");
	}

	@Test public void testNot() {
		execAndCheck("^true not", "false");
		execAndCheck("^false not", "true");
	}
}
