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

	@Test public void testBasicNew() {
		String input =
			"class T [\n" +
			"    f [^'hi']\n" +
			"]\n" +
			"^T basicNew f\n";
		String expecting = "hi";
		execAndCheck(input, expecting);
	}

	@Test public void testNew() {
		String input =
			"class T [\n" +
			"    f [^'hi']\n" +
			"]\n" +
			"^T new f\n"; // should be same as basicNew
		String expecting = "hi";
		execAndCheck(input, expecting);
	}

	@Test public void testInit() {
		String input =
			"class T [\n" +
			"    |x|\n" +
			"    initialize [x:=1]\n"+
			"    f [^x]\n" +
			"]\n" +
			"^T new f\n"; // 'new' calls 'initialize' after 'basicNew'
		String expecting = "1";
		execAndCheck(input, expecting);
	}

	@Test public void testInitWithValue() {
		String input =
			"class T [\n" +
			"    |x|\n" +
			"    initialize: v [x:=v]\n"+
			"    f [^x]\n" +
			"]\n" +
			"^(T new: 99) f\n";
		String expecting = "99";
		execAndCheck(input, expecting);
	}

	@Test public void testClassMethod() {
		/*
		0000:  dbg '<string>', 5:11             MainClass>>main[][]
		0007:  dbg '<string>', 5:3              MainClass>>main[][]
		0014:  push_global    'T'               MainClass>>main[][class T]
		0017:  send           0, 'factory'      MainClass>>main[][], T>>factory[][]
		0000:  dbg '<string>', 2:19             MainClass>>main[][], T>>factory[][]
		0007:  self                             MainClass>>main[][], T>>factory[][class T]
		0008:  send           0, 'new'          MainClass>>main[][], T>>factory[][], Object>>new[][]
		0000:  dbg 'image.st', 20:22            MainClass>>main[][], T>>factory[][], Object>>new[][]
		0007:  dbg 'image.st', 20:13            MainClass>>main[][], T>>factory[][], Object>>new[][]
		0014:  self                             MainClass>>main[][], T>>factory[][], Object>>new[][class T]
		0015:  send           0, 'basicNew'     MainClass>>main[][], T>>factory[][], Object>>new[][a T]
		0020:  send           0, 'initialize'   MainClass>>main[][], T>>factory[][], Object>>new[][], Object>>initialize[][]
		0000:  self                             MainClass>>main[][], T>>factory[][], Object>>new[][], Object>>initialize[][a T]
		0001:  dbg 'image.st', 29:16            MainClass>>main[][], T>>factory[][], Object>>new[][], Object>>initialize[][a T]
		0008:  return                           MainClass>>main[][], T>>factory[][], Object>>new[][a T]
		0025:  dbg 'image.st', 20:7             MainClass>>main[][], T>>factory[][], Object>>new[][a T]
		0032:  return                           MainClass>>main[][], T>>factory[][a T]
		0013:  dbg '<string>', 2:13             MainClass>>main[][], T>>factory[][a T]
		0020:  return                           MainClass>>main[][a T]
		0022:  send           0, 'asString'     MainClass>>main[][], T>>asString[][]
		0000:  push_literal   'blort'           MainClass>>main[][], T>>asString[]['blort']
		0003:  dbg '<string>', 3:14             MainClass>>main[][], T>>asString[]['blort']
		0010:  return                           MainClass>>main[]['blort']
		0027:  dbg '<string>', 5:0              MainClass>>main[]['blort']
		0034:  return
		 */
		String input =
			"class T [\n" +
			"    factory [^self new]\n" +
			"    asString [^'blort']\n"+
			"]\n" +
			"^T factory asString\n";
		String expecting = "blort";
		execAndCheck(input, expecting);
	}
}
