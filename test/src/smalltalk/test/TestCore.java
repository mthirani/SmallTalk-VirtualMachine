package smalltalk.test;

import org.junit.Test;
import smalltalk.vm.exceptions.ClassMessageSentToInstance;
import smalltalk.vm.exceptions.MessageNotUnderstood;

import static org.junit.Assert.assertEquals;

public class TestCore extends BaseTest {
	@Test public void testEmpty() {
		/*
		<empty trace>
		 */
		String input = "";
		String expecting = "nil";
		execAndCheck(input, expecting);
	}

	@Test public void testNil() {
		/*
		0000:  nil                              MainClass>>main[][nil]
		0001:  dbg '<string>', 1:0              MainClass>>main[][nil]
		0008:  return
		 */
		String input = "^nil";
		String expecting = "nil";
		execAndCheck(input, expecting);
	}

	@Test public void testTrue() {
		/*
		0000:  true                             MainClass>>main[][true]
		0001:  dbg '<string>', 1:0              MainClass>>main[][true]
		0008:  return
		 */
		String input = "^true";
		String expecting = "true";
		execAndCheck(input, expecting);
	}

	@Test public void testFalse() {
		/*
		0000:  false                            MainClass>>main[][false]
		0001:  dbg '<string>', 1:0              MainClass>>main[][false]
		0008:  return
		 */
		String input = "^false";
		String expecting = "false";
		execAndCheck(input, expecting);
	}

	@Test public void testInt() {
		/*
		0000:  push_int       34                MainClass>>main[][34]
		0005:  dbg '<string>', 1:0              MainClass>>main[][34]
		0012:  return
		 */
		String input =
			"^34";
		String expecting = "34";
		execAndCheck(input, expecting);
	}

	@Test public void testNegInt() {
		/*
		0000:  push_int       -34               MainClass>>main[][-34]
		0005:  dbg '<string>', 1:0              MainClass>>main[][-34]
		0012:  return
		 */
		String input =
			"^-34";
		String expecting = "-34";
		execAndCheck(input, expecting);
	}

	@Test public void testFloat() {
		/*
		0000:  push_float     123456.79         MainClass>>main[][123456.78906]
		0005:  dbg '<string>', 1:0              MainClass>>main[][123456.78906]
		0012:  return
		 */
		String input =
			"^123456.789"; // float only handles 3 digits after '.' it seems
		String expecting = "123456.78906";
		execAndCheck(input, expecting);
	}

	@Test public void testNegFloat() {
		/*
		0000:  push_float     -123456.79        MainClass>>main[][-123456.78906]
		0005:  dbg '<string>', 1:0              MainClass>>main[][-123456.78906]
		0012:  return
		 */
		String input =
			"^-123456.789"; // float only handles 3 digits after '.' it seems? wtf? 3.14159 works.
		String expecting = "-123456.78906";
		execAndCheck(input, expecting);
	}

	@Test public void testIntOp() {
		/*
		0000:  dbg '<string>', 1:2              MainClass>>main[][]
		0007:  push_int       1                 MainClass>>main[][1]
		0012:  push_int       2                 MainClass>>main[][1, 2]
		0017:  send           1, '+'            MainClass>>main[][3]
		0022:  dbg '<string>', 1:0              MainClass>>main[][3]
		0029:  return
		0000:  dbg '<string>', 1:3              MainClass>>main[][]
		0007:  push_int       1                 MainClass>>main[][1]
		0012:  push_int       2                 MainClass>>main[][1, 2]
		0017:  send           1, '-'            MainClass>>main[][-1]
		0022:  dbg '<string>', 1:0              MainClass>>main[][-1]
		0029:  return
		0000:  dbg '<string>', 1:2              MainClass>>main[][]
		0007:  push_int       2                 MainClass>>main[][2]
		0012:  push_int       4                 MainClass>>main[][2, 4]
		0017:  send           1, '*'            MainClass>>main[][8]
		0022:  dbg '<string>', 1:0              MainClass>>main[][8]
		0029:  return
		0000:  dbg '<string>', 1:2              MainClass>>main[][]
		0007:  push_int       4                 MainClass>>main[][4]
		0012:  push_int       2                 MainClass>>main[][4, 2]
		0017:  send           1, '/'            MainClass>>main[][2]
		0022:  dbg '<string>', 1:0              MainClass>>main[][2]
		0029:  return
		0000:  dbg '<string>', 1:2              MainClass>>main[][]
		0007:  push_int       4                 MainClass>>main[][4]
		0012:  push_int       3                 MainClass>>main[][4, 3]
		0017:  send           1, '/'            MainClass>>main[][1]
		0022:  dbg '<string>', 1:0              MainClass>>main[][1]
		0029:  return
		0000:  dbg '<string>', 1:3              MainClass>>main[][]
		0007:  push_int       10                MainClass>>main[][10]
		0012:  push_int       10                MainClass>>main[][10, 10]
		0017:  send           1, '='            MainClass>>main[][true]
		0022:  dbg '<string>', 1:0              MainClass>>main[][true]
		0029:  return
		0000:  dbg '<string>', 1:2              MainClass>>main[][]
		0007:  push_int       1                 MainClass>>main[][1]
		0012:  push_int       2                 MainClass>>main[][1, 2]
		0017:  send           1, '='            MainClass>>main[][false]
		0022:  dbg '<string>', 1:0              MainClass>>main[][false]
		0029:  return
		0000:  dbg '<string>', 1:2              MainClass>>main[][]
		0007:  push_int       1                 MainClass>>main[][1]
		0012:  push_int       2                 MainClass>>main[][1, 2]
		0017:  send           1, '<'            MainClass>>main[][true]
		0022:  dbg '<string>', 1:0              MainClass>>main[][true]
		0029:  return
		0000:  dbg '<string>', 1:2              MainClass>>main[][]
		0007:  push_int       1                 MainClass>>main[][1]
		0012:  push_int       2                 MainClass>>main[][1, 2]
		0017:  send           1, '>'            MainClass>>main[][false]
		0022:  dbg '<string>', 1:0              MainClass>>main[][false]
		0029:  return
		0000:  dbg '<string>', 1:2              MainClass>>main[][]
		0007:  push_int       1                 MainClass>>main[][1]
		0012:  push_int       2                 MainClass>>main[][1, 2]
		0017:  send           1, '<='           MainClass>>main[][true]
		0022:  dbg '<string>', 1:0              MainClass>>main[][true]
		0029:  return
		0000:  dbg '<string>', 1:2              MainClass>>main[][]
		0007:  push_int       1                 MainClass>>main[][1]
		0012:  push_int       2                 MainClass>>main[][1, 2]
		0017:  send           1, '>='           MainClass>>main[][false]
		0022:  dbg '<string>', 1:0              MainClass>>main[][false]
		0029:  return
		0000:  push_int       5                 MainClass>>main[][5]
		0005:  push_int       4                 MainClass>>main[][5, 4]
		0010:  dbg '<string>', 1:3              MainClass>>main[][5, 4]
		0017:  send           1, 'mod:'         MainClass>>main[][1]
		0022:  dbg '<string>', 1:0              MainClass>>main[][1]
		0029:  return
		0000:  dbg '<string>', 1:3              MainClass>>main[][]
		0007:  push_int       5                 MainClass>>main[][5]
		0012:  send           0, 'hash'         MainClass>>main[][], Integer>>hash[][]
		0000:  self                             MainClass>>main[][], Integer>>hash[][5]
		0001:  dbg 'image.st', 129:10           MainClass>>main[][], Integer>>hash[][5]
		0008:  return                           MainClass>>main[][5]
		0017:  dbg '<string>', 1:0              MainClass>>main[][5]
		0024:  return
		 */
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
		/*
		0000:  self                             MainClass>>main[][a MainClass]
		0001:  dbg '<string>', 1:0              MainClass>>main[][a MainClass]
		0008:  return
		 */
		String input =
			"^self";
		String expecting = "a MainClass";
		execAndCheck(input, expecting);
	}

	@Test public void testBoolean() {
		/*
		0000:  dbg '<string>', 1:14             MainClass>>main[][]
		0007:  dbg '<string>', 1:6              MainClass>>main[][]
		0014:  true                             MainClass>>main[][true]
		0015:  send           0, 'asString'     MainClass>>main[]['true']
		0020:  dbg '<string>', 1:22             MainClass>>main[]['true']
		0027:  false                            MainClass>>main[]['true', false]
		0028:  send           0, 'asString'     MainClass>>main[]['true', 'false']
		0033:  send           1, ','            MainClass>>main[]['truefalse']
		0038:  dbg '<string>', 1:0              MainClass>>main[]['truefalse']
		0045:  return
		 */
		String input =
			"^true asString, false asString";
		String expecting = "truefalse";
		execAndCheck(input, expecting);
	}

	@Test public void testArrayLiteral() {
		/*
		0000:  push_int       1                 MainClass>>main[][1]
		0005:  push_int       2                 MainClass>>main[][1, 2]
		0010:  push_int       3                 MainClass>>main[][1, 2, 3]
		0015:  push_int       4                 MainClass>>main[][1, 2, 3, 4]
		0020:  push_array     4                 MainClass>>main[][{1. 2. 3. 4}]
		0023:  dbg '<string>', 1:0              MainClass>>main[][{1. 2. 3. 4}]
		0030:  return
		 */
		String input =
			"^{1. 2. 3. 4.}";
		String expecting = "{1. 2. 3. 4}";
		execAndCheck(input, expecting);
	}

	@Test public void testArrayLiteralBiggerThanDefaultOperandStackSizeOf10() {
		/*
		0000:  push_int       1                 MainClass>>main[][1]
		0005:  push_int       2                 MainClass>>main[][1, 2]
		0010:  push_int       3                 MainClass>>main[][1, 2, 3]
		0015:  push_int       4                 MainClass>>main[][1, 2, 3, 4]
		0020:  push_int       5                 MainClass>>main[][1, 2, 3, 4, 5]
		0025:  push_int       6                 MainClass>>main[][1, 2, 3, 4, 5, 6]
		0030:  push_int       7                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7]
		0035:  push_int       8                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8]
		0040:  push_int       9                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9]
		0045:  push_int       1                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1]
		0050:  push_int       2                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2]
		0055:  push_int       3                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3]
		0060:  push_int       4                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4]
		0065:  push_int       5                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5]
		0070:  push_int       6                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6]
		0075:  push_int       7                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7]
		0080:  push_int       8                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8]
		0085:  push_int       9                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9]
		0090:  push_int       1                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1]
		0095:  push_int       2                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2]
		0100:  push_int       3                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3]
		0105:  push_int       4                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4]
		0110:  push_int       5                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5]
		0115:  push_int       6                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6]
		0120:  push_int       7                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7]
		0125:  push_int       8                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8]
		0130:  push_int       9                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9]
		0135:  push_int       1                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1]
		0140:  push_int       2                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2]
		0145:  push_int       3                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3]
		0150:  push_int       4                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4]
		0155:  push_int       5                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5]
		0160:  push_int       6                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6]
		0165:  push_int       7                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7]
		0170:  push_int       8                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8]
		0175:  push_int       9                 MainClass>>main[][1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9]
		0180:  push_array     36                MainClass>>main[][{1. 2. 3. 4. 5. 6. 7. 8. 9. 1. 2. 3. 4. 5. 6. 7. 8. 9. 1. 2. 3. 4. 5. 6. 7. 8. 9. 1. 2. 3. 4. 5. 6. 7. 8. 9}]
		0183:  dbg '<string>', 1:0              MainClass>>main[][{1. 2. 3. 4. 5. 6. 7. 8. 9. 1. 2. 3. 4. 5. 6. 7. 8. 9. 1. 2. 3. 4. 5. 6. 7. 8. 9. 1. 2. 3. 4. 5. 6. 7. 8. 9}]
		0190:  return
		 */
		String input =
			"^{1. 2. 3. 4. 5. 6. 7. 8. 9. 1. 2. 3. 4. 5. 6. 7. 8. 9. 1. 2. 3. 4. 5. 6. 7. 8. 9. 1. 2. 3. 4. 5. 6. 7. 8. 9}";
		String expecting = "{1. 2. 3. 4. 5. 6. 7. 8. 9. 1. 2. 3. 4. 5. 6. 7. 8. 9. 1. 2. 3. 4. 5. 6. 7. 8. 9. 1. 2. 3. 4. 5. 6. 7. 8. 9}";
		execAndCheck(input, expecting);
	}

	@Test public void testArrayExprsLiteral() {
		/*
		0000:  push_int       1                 MainClass>>main[][1]
		0005:  dbg '<string>', 1:6              MainClass>>main[][1]
		0012:  push_int       2                 MainClass>>main[][1, 2]
		0017:  push_int       3                 MainClass>>main[][1, 2, 3]
		0022:  send           1, '+'            MainClass>>main[][1, 5]
		0027:  dbg '<string>', 1:20             MainClass>>main[][1, 5]
		0034:  dbg '<string>', 1:12             MainClass>>main[][1, 5]
		0041:  push_int       4                 MainClass>>main[][1, 5, 4]
		0046:  send           0, 'asString'     MainClass>>main[][1, 5, '4']
		0051:  push_char      97                MainClass>>main[][1, 5, '4', $a]
		0054:  send           1, ','            MainClass>>main[][1, 5, '4$a']
		0059:  push_array     3                 MainClass>>main[][{1. 5. 4$a}]
		0062:  dbg '<string>', 1:0              MainClass>>main[][{1. 5. 4$a}]
		0069:  return
		 */
		String input =
			"^{1. 2+3. 4 asString, $a}";
		String expecting = "{1. 5. 4$a}";
		execAndCheck(input, expecting);
	}

	@Test public void testNestedArrayLiteral() {
		/*
		0000:  push_int       1                 MainClass>>main[][1]
		0005:  push_int       2                 MainClass>>main[][1, 2]
		0010:  push_int       3                 MainClass>>main[][1, 2, 3]
		0015:  push_array     2                 MainClass>>main[][1, {2. 3}]
		0018:  push_int       4                 MainClass>>main[][1, {2. 3}, 4]
		0023:  push_array     3                 MainClass>>main[][{1. {2. 3}. 4}]
		0026:  dbg '<string>', 1:0              MainClass>>main[][{1. {2. 3}. 4}]
		0033:  return
		 */
		String input =
			"^{1. {2. 3}. 4.}";
		String expecting = "{1. {2. 3}. 4}";
		execAndCheck(input, expecting);
	}

	@Test public void testHeteroArrayLiteral() {
		/*
		0000:  push_int       1                 MainClass>>main[][1]
		0005:  push_float     3.14159           MainClass>>main[][1, 3.14159]
		0010:  true                             MainClass>>main[][1, 3.14159, true]
		0011:  push_literal   'hi'              MainClass>>main[][1, 3.14159, true, 'hi']
		0014:  push_array     4                 MainClass>>main[][{1. 3.14159. true. hi}]
		0017:  dbg '<string>', 1:0              MainClass>>main[][{1. 3.14159. true. hi}]
		0024:  return
		 */
		String input =
			"^{1. 3.14159. true. 'hi'}";
		String expecting = "{1. 3.14159. true. hi}";
		execAndCheck(input, expecting);
	}

	@Test public void testAssign() {
		/*
		0000:  dbg '<string>', 2:0              MainClass>>main[nil][]
		0007:  push_int       1                 MainClass>>main[nil][1]
		0012:  store_local    0, 0              MainClass>>main[1][1]
		0017:  pop                              MainClass>>main[1][]
		0018:  push_local     0, 0              MainClass>>main[1][1]
		0023:  dbg '<string>', 2:7              MainClass>>main[1][1]
		0030:  return
		 */
		String input =
			"| x |\n" +
			"x := 1." +
			"^x";
		String expecting = "1";
		execAndCheck(input, expecting);
	}

	@Test public void testStringIdentifyEquals() {
		// it's ok to fail this one if your implementation doesn't share strings;
		// i share strings per block with:
		//public STString[] literalsAsSTStrings; // Cache STString objects for literals
		// in STCompiledBlock. probably a dumb implementation
		/*
		0000:  dbg '<string>', 2:0              MainClass>>main[nil, nil][]
		0007:  push_literal   'hi'              MainClass>>main[nil, nil]['hi']
		0010:  store_local    0, 0              MainClass>>main['hi', nil]['hi']
		0015:  pop                              MainClass>>main['hi', nil][]
		0016:  dbg '<string>', 2:10             MainClass>>main['hi', nil][]
		0023:  push_literal   'hi'              MainClass>>main['hi', nil]['hi']
		0026:  store_local    0, 1              MainClass>>main['hi', 'hi']['hi']
		0031:  pop                              MainClass>>main['hi', 'hi'][]
		0032:  dbg '<string>', 2:22             MainClass>>main['hi', 'hi'][]
		0039:  push_local     0, 0              MainClass>>main['hi', 'hi']['hi']
		0044:  push_local     0, 1              MainClass>>main['hi', 'hi']['hi', 'hi']
		0049:  send           1, '=='           MainClass>>main['hi', 'hi'][true]
		0054:  dbg '<string>', 2:20             MainClass>>main['hi', 'hi'][true]
		0061:  return
		 */
		String input =
			"| x y |\n" +
			"x := 'hi'." +
			"y := 'hi'." +
			"^x==y"; // string literals are usually same object
		String expecting = "true";
		execAndCheck(input, expecting);
	}

	@Test public void testStringEquals() {
		/*
		0000:  dbg '<string>', 2:0              MainClass>>main[nil, nil][]
		0007:  push_literal   'hi'              MainClass>>main[nil, nil]['hi']
		0010:  store_local    0, 0              MainClass>>main['hi', nil]['hi']
		0015:  pop                              MainClass>>main['hi', nil][]
		0016:  dbg '<string>', 2:10             MainClass>>main['hi', nil][]
		0023:  push_literal   'hi'              MainClass>>main['hi', nil]['hi']
		0026:  store_local    0, 1              MainClass>>main['hi', 'hi']['hi']
		0031:  pop                              MainClass>>main['hi', 'hi'][]
		0032:  dbg '<string>', 2:22             MainClass>>main['hi', 'hi'][]
		0039:  push_local     0, 0              MainClass>>main['hi', 'hi']['hi']
		0044:  push_local     0, 1              MainClass>>main['hi', 'hi']['hi', 'hi']
		0049:  send           1, '='            MainClass>>main['hi', 'hi'][true]
		0054:  dbg '<string>', 2:20             MainClass>>main['hi', 'hi'][true]
		0061:  return
		 */
		String input =
			"| x y |\n" +
			"x := 'hi'." +
			"y := 'hi'." +
			"^x=y"; // string literals are usually same object
		String expecting = "true";
		execAndCheck(input, expecting);
	}

	@Test public void testStringEquals2() {
		/*
		0000:  dbg '<string>', 2:0              MainClass>>main[nil, nil][]
		0007:  push_literal   'hi'              MainClass>>main[nil, nil]['hi']
		0010:  store_local    0, 0              MainClass>>main['hi', nil]['hi']
		0015:  pop                              MainClass>>main['hi', nil][]
		0016:  dbg '<string>', 2:10             MainClass>>main['hi', nil][]
		0023:  push_literal   'foo'             MainClass>>main['hi', nil]['foo']
		0026:  store_local    0, 1              MainClass>>main['hi', 'foo']['foo']
		0031:  pop                              MainClass>>main['hi', 'foo'][]
		0032:  dbg '<string>', 2:23             MainClass>>main['hi', 'foo'][]
		0039:  push_local     0, 0              MainClass>>main['hi', 'foo']['hi']
		0044:  push_local     0, 1              MainClass>>main['hi', 'foo']['hi', 'foo']
		0049:  send           1, '='            MainClass>>main['hi', 'foo'][false]
		0054:  dbg '<string>', 2:21             MainClass>>main['hi', 'foo'][false]
		0061:  return
		 */
		String input =
			"| x y |\n" +
			"x := 'hi'." +
			"y := 'foo'." +
			"^x=y";
		String expecting = "false";
		execAndCheck(input, expecting);
	}

	@Test public void testStringNotEquals() {
		/*
		0000:  dbg '<string>', 2:0              MainClass>>main[nil, nil][]
		0007:  push_literal   'hi'              MainClass>>main[nil, nil]['hi']
		0010:  store_local    0, 0              MainClass>>main['hi', nil]['hi']
		0015:  pop                              MainClass>>main['hi', nil][]
		0016:  dbg '<string>', 2:10             MainClass>>main['hi', nil][]
		0023:  push_literal   'hi'              MainClass>>main['hi', nil]['hi']
		0026:  store_local    0, 1              MainClass>>main['hi', 'hi']['hi']
		0031:  pop                              MainClass>>main['hi', 'hi'][]
		0032:  dbg '<string>', 2:22             MainClass>>main['hi', 'hi'][]
		0039:  push_local     0, 0              MainClass>>main['hi', 'hi']['hi']
		0044:  push_local     0, 1              MainClass>>main['hi', 'hi']['hi', 'hi']
		0049:  send           1, '~='           MainClass>>main['hi', 'hi'][], Object>>~=['hi'][]
		0000:  dbg 'image.st', 42:26            MainClass>>main['hi', 'hi'][], Object>>~=['hi'][]
		0007:  dbg 'image.st', 42:14            MainClass>>main['hi', 'hi'][], Object>>~=['hi'][]
		0014:  self                             MainClass>>main['hi', 'hi'][], Object>>~=['hi']['hi']
		0015:  push_local     0, 0              MainClass>>main['hi', 'hi'][], Object>>~=['hi']['hi', 'hi']
		0020:  send           1, '='            MainClass>>main['hi', 'hi'][], Object>>~=['hi'][true]
		0025:  send           0, 'not'          MainClass>>main['hi', 'hi'][], Object>>~=['hi'][false]
		0030:  dbg 'image.st', 42:7             MainClass>>main['hi', 'hi'][], Object>>~=['hi'][false]
		0037:  return                           MainClass>>main['hi', 'hi'][false]
		0054:  dbg '<string>', 2:20             MainClass>>main['hi', 'hi'][false]
		0061:  return
		 */
		String input =
			"| x y |\n" +
			"x := 'hi'." +
			"y := 'hi'." +
			"^x~=y"; // string literals are usually same object
		String expecting = "false";
		execAndCheck(input, expecting);
	}

	@Test public void testObjectIdentifyEquals() {
		/*
		0000:  dbg '<string>', 2:0              MainClass>>main[nil, nil][]
		0007:  push_literal   'hi'              MainClass>>main[nil, nil]['hi']
		0010:  store_local    0, 0              MainClass>>main['hi', nil]['hi']
		0015:  pop                              MainClass>>main['hi', nil][]
		0016:  dbg '<string>', 2:10             MainClass>>main['hi', nil][]
		0023:  push_literal   'hi'              MainClass>>main['hi', nil]['hi']
		0026:  store_local    0, 1              MainClass>>main['hi', 'hi']['hi']
		0031:  pop                              MainClass>>main['hi', 'hi'][]
		0032:  dbg '<string>', 2:22             MainClass>>main['hi', 'hi'][]
		0039:  push_local     0, 0              MainClass>>main['hi', 'hi']['hi']
		0044:  push_local     0, 1              MainClass>>main['hi', 'hi']['hi', 'hi']
		0049:  send           1, '=='           MainClass>>main['hi', 'hi'][true]
		0054:  dbg '<string>', 2:20             MainClass>>main['hi', 'hi'][true]
		0061:  return
		 */
		String input =
			"| x y |\n" +
			"x := 'hi'." +
			"y := 'hi'." +
			"^x==y";
		String expecting = "true";
		execAndCheck(input, expecting);
	}

	@Test public void testChar() {
		/*
		0000:  dbg '<string>', 2:0              MainClass>>main[nil][]
		0007:  push_char      97                MainClass>>main[nil][$a]
		0010:  store_local    0, 0              MainClass>>main[$a][$a]
		0015:  pop                              MainClass>>main[$a][]
		0016:  push_local     0, 0              MainClass>>main[$a][$a]
		0021:  dbg '<string>', 2:8              MainClass>>main[$a][$a]
		0028:  return
		 */
		String input =
			"| x |\n" +
			"x := $a." +
			"^x";
		String expecting = "$a";
		execAndCheck(input, expecting);
	}

	@Test public void testStringCat() {
		/*
		0000:  dbg '<string>', 2:0              MainClass>>main[nil, nil][]
		0007:  push_literal   'abc'             MainClass>>main[nil, nil]['abc']
		0010:  store_local    0, 0              MainClass>>main['abc', nil]['abc']
		0015:  pop                              MainClass>>main['abc', nil][]
		0016:  dbg '<string>', 2:11             MainClass>>main['abc', nil][]
		0023:  push_literal   'def'             MainClass>>main['abc', nil]['def']
		0026:  store_local    0, 1              MainClass>>main['abc', 'def']['def']
		0031:  pop                              MainClass>>main['abc', 'def'][]
		0032:  dbg '<string>', 2:24             MainClass>>main['abc', 'def'][]
		0039:  push_local     0, 0              MainClass>>main['abc', 'def']['abc']
		0044:  push_local     0, 1              MainClass>>main['abc', 'def']['abc', 'def']
		0049:  send           1, ','            MainClass>>main['abc', 'def']['abcdef']
		0054:  dbg '<string>', 2:22             MainClass>>main['abc', 'def']['abcdef']
		0061:  return
		 */
		String input =
			"| x y |\n" +
			"x := 'abc'." +
			"y := 'def'." +
			"^x, y";
		String expecting = "abcdef";
		execAndCheck(input, expecting);
	}

	@Test public void testIf() {
		/*
		0000:  true                             MainClass>>main[][true]
		0001:  block          0                 MainClass>>main[][true, main-block0]
		0004:  dbg '<string>', 1:7              MainClass>>main[][true, main-block0]
		0011:  send           1, 'ifTrue:'      MainClass>>main[][], MainClass>>main-block0[][]
		0000:  push_int       99                MainClass>>main[][], MainClass>>main-block0[][99]
		0005:  dbg '<string>', 1:17             MainClass>>main[][], MainClass>>main-block0[][99]
		0012:  block_return                     MainClass>>main[][99]
		0016:  dbg '<string>', 1:0              MainClass>>main[][99]
		0023:  return
		0000:  false                            MainClass>>main[][false]
		0001:  block          0                 MainClass>>main[][false, main-block0]
		0004:  dbg '<string>', 1:8              MainClass>>main[][false, main-block0]
		0011:  send           1, 'ifTrue:'      MainClass>>main[][nil]
		0016:  dbg '<string>', 1:0              MainClass>>main[][nil]
		0023:  return
		 */
		execAndCheck("^(true ifTrue:[99])", "99");
		execAndCheck("^(false ifTrue:[99])", "nil");
	}

	@Test public void testIfElse() {
		/*
		0000:  true                             MainClass>>main[][true]
		0001:  block          0                 MainClass>>main[][true, main-block0]
		0004:  block          1                 MainClass>>main[][true, main-block0, main-block1]
		0007:  dbg '<string>', 1:7              MainClass>>main[][true, main-block0, main-block1]
		0014:  send           2, 'ifTrue:ifFalse:'MainClass>>main[][], MainClass>>main-block0[][]
		0000:  push_int       99                MainClass>>main[][], MainClass>>main-block0[][99]
		0005:  dbg '<string>', 1:17             MainClass>>main[][], MainClass>>main-block0[][99]
		0012:  block_return                     MainClass>>main[][99]
		0019:  dbg '<string>', 1:0              MainClass>>main[][99]
		0026:  return
		0000:  false                            MainClass>>main[][false]
		0001:  block          0                 MainClass>>main[][false, main-block0]
		0004:  block          1                 MainClass>>main[][false, main-block0, main-block1]
		0007:  dbg '<string>', 1:8              MainClass>>main[][false, main-block0, main-block1]
		0014:  send           2, 'ifTrue:ifFalse:'MainClass>>main[][], MainClass>>main-block1[][]
		0000:  push_int       100               MainClass>>main[][], MainClass>>main-block1[][100]
		0005:  dbg '<string>', 1:32             MainClass>>main[][], MainClass>>main-block1[][100]
		0012:  block_return                     MainClass>>main[][100]
		0019:  dbg '<string>', 1:0              MainClass>>main[][100]
		0026:  return
		 */
		execAndCheck("^(true ifTrue:[99] ifFalse:[100])", "99");
		execAndCheck("^(false ifTrue:[99] ifFalse:[100])", "100");
	}

	@Test public void testIfFalse() {
		/*
		0000:  true                             MainClass>>main[][true]
		0001:  block          0                 MainClass>>main[][true, main-block0]
		0004:  dbg '<string>', 1:7              MainClass>>main[][true, main-block0]
		0011:  send           1, 'ifFalse:'     MainClass>>main[][], Boolean>>ifFalse:[main-block0][]
		0000:  dbg 'image.st', 100:24           MainClass>>main[][], Boolean>>ifFalse:[main-block0][]
		0007:  self                             MainClass>>main[][], Boolean>>ifFalse:[main-block0][true]
		0008:  send           0, 'not'          MainClass>>main[][], Boolean>>ifFalse:[main-block0][false]
		0013:  push_local     0, 0              MainClass>>main[][], Boolean>>ifFalse:[main-block0][false, main-block0]
		0018:  dbg 'image.st', 100:28           MainClass>>main[][], Boolean>>ifFalse:[main-block0][false, main-block0]
		0025:  send           1, 'ifTrue:'      MainClass>>main[][], Boolean>>ifFalse:[main-block0][nil]
		0030:  dbg 'image.st', 100:18           MainClass>>main[][], Boolean>>ifFalse:[main-block0][nil]
		0037:  return                           MainClass>>main[][nil]
		0016:  dbg '<string>', 1:0              MainClass>>main[][nil]
		0023:  return
		0000:  false                            MainClass>>main[][false]
		0001:  block          0                 MainClass>>main[][false, main-block0]
		0004:  dbg '<string>', 1:8              MainClass>>main[][false, main-block0]
		0011:  send           1, 'ifFalse:'     MainClass>>main[][], Boolean>>ifFalse:[main-block0][]
		0000:  dbg 'image.st', 100:24           MainClass>>main[][], Boolean>>ifFalse:[main-block0][]
		0007:  self                             MainClass>>main[][], Boolean>>ifFalse:[main-block0][false]
		0008:  send           0, 'not'          MainClass>>main[][], Boolean>>ifFalse:[main-block0][true]
		0013:  push_local     0, 0              MainClass>>main[][], Boolean>>ifFalse:[main-block0][true, main-block0]
		0018:  dbg 'image.st', 100:28           MainClass>>main[][], Boolean>>ifFalse:[main-block0][true, main-block0]
		0025:  send           1, 'ifTrue:'      MainClass>>main[][], Boolean>>ifFalse:[main-block0][], MainClass>>main-block0[][]
		0000:  push_int       99                MainClass>>main[][], Boolean>>ifFalse:[main-block0][], MainClass>>main-block0[][99]
		0005:  dbg '<string>', 1:19             MainClass>>main[][], Boolean>>ifFalse:[main-block0][], MainClass>>main-block0[][99]
		0012:  block_return                     MainClass>>main[][], Boolean>>ifFalse:[main-block0][99]
		0030:  dbg 'image.st', 100:18           MainClass>>main[][], Boolean>>ifFalse:[main-block0][99]
		0037:  return                           MainClass>>main[][99]
		0016:  dbg '<string>', 1:0              MainClass>>main[][99]
		0023:  return
		 */
		execAndCheck("^(true ifFalse:[99])", "nil");
		execAndCheck("^(false ifFalse:[99])", "99");
	}

	@Test public void testAnd() {
		/*
		0000:  true                             MainClass>>main[][true]
		0001:  true                             MainClass>>main[][true, true]
		0002:  dbg '<string>', 1:6              MainClass>>main[][true, true]
		0009:  send           1, 'and:'         MainClass>>main[][], Boolean>>and:[true][]
		0000:  self                             MainClass>>main[][], Boolean>>and:[true][true]
		0001:  block          0                 MainClass>>main[][], Boolean>>and:[true][true, and:-block0]
		0004:  dbg 'image.st', 102:12           MainClass>>main[][], Boolean>>and:[true][true, and:-block0]
		0011:  send           1, 'ifTrue:'      MainClass>>main[][], Boolean>>and:[true][], Boolean>>and:-block0[][]
		0000:  push_local     1, 0              MainClass>>main[][], Boolean>>and:[true][], Boolean>>and:-block0[][true]
		0005:  block          1                 MainClass>>main[][], Boolean>>and:[true][], Boolean>>and:-block0[][true, and:-block1]
		0008:  dbg 'image.st', 102:24           MainClass>>main[][], Boolean>>and:[true][], Boolean>>and:-block0[][true, and:-block1]
		0015:  send           1, 'ifTrue:'      MainClass>>main[][], Boolean>>and:[true][], Boolean>>and:-block0[][], Boolean>>and:-block1[][]
		0000:  true                             MainClass>>main[][], Boolean>>and:[true][], Boolean>>and:-block0[][], Boolean>>and:-block1[][true]
		0001:  dbg 'image.st', 102:33           MainClass>>main[][], Boolean>>and:[true][], Boolean>>and:-block0[][], Boolean>>and:-block1[][true]
		0008:  return                           MainClass>>main[][true]
		0014:  dbg '<string>', 1:0              MainClass>>main[][true]
		0021:  return
		0000:  true                             MainClass>>main[][true]
		0001:  false                            MainClass>>main[][true, false]
		0002:  dbg '<string>', 1:6              MainClass>>main[][true, false]
		0009:  send           1, 'and:'         MainClass>>main[][], Boolean>>and:[false][]
		0000:  self                             MainClass>>main[][], Boolean>>and:[false][true]
		0001:  block          0                 MainClass>>main[][], Boolean>>and:[false][true, and:-block0]
		0004:  dbg 'image.st', 102:12           MainClass>>main[][], Boolean>>and:[false][true, and:-block0]
		0011:  send           1, 'ifTrue:'      MainClass>>main[][], Boolean>>and:[false][], Boolean>>and:-block0[][]
		0000:  push_local     1, 0              MainClass>>main[][], Boolean>>and:[false][], Boolean>>and:-block0[][false]
		0005:  block          1                 MainClass>>main[][], Boolean>>and:[false][], Boolean>>and:-block0[][false, and:-block1]
		0008:  dbg 'image.st', 102:24           MainClass>>main[][], Boolean>>and:[false][], Boolean>>and:-block0[][false, and:-block1]
		0015:  send           1, 'ifTrue:'      MainClass>>main[][], Boolean>>and:[false][], Boolean>>and:-block0[][nil]
		0020:  dbg 'image.st', 102:40           MainClass>>main[][], Boolean>>and:[false][], Boolean>>and:-block0[][nil]
		0027:  block_return                     MainClass>>main[][], Boolean>>and:[false][nil]
		0016:  pop                              MainClass>>main[][], Boolean>>and:[false][]
		0017:  false                            MainClass>>main[][], Boolean>>and:[false][false]
		0018:  dbg 'image.st', 103:7            MainClass>>main[][], Boolean>>and:[false][false]
		0025:  return                           MainClass>>main[][false]
		0014:  dbg '<string>', 1:0              MainClass>>main[][false]
		0021:  return
		0000:  false                            MainClass>>main[][false]
		0001:  true                             MainClass>>main[][false, true]
		0002:  dbg '<string>', 1:7              MainClass>>main[][false, true]
		0009:  send           1, 'and:'         MainClass>>main[][], Boolean>>and:[true][]
		0000:  self                             MainClass>>main[][], Boolean>>and:[true][false]
		0001:  block          0                 MainClass>>main[][], Boolean>>and:[true][false, and:-block0]
		0004:  dbg 'image.st', 102:12           MainClass>>main[][], Boolean>>and:[true][false, and:-block0]
		0011:  send           1, 'ifTrue:'      MainClass>>main[][], Boolean>>and:[true][nil]
		0016:  pop                              MainClass>>main[][], Boolean>>and:[true][]
		0017:  false                            MainClass>>main[][], Boolean>>and:[true][false]
		0018:  dbg 'image.st', 103:7            MainClass>>main[][], Boolean>>and:[true][false]
		0025:  return                           MainClass>>main[][false]
		0014:  dbg '<string>', 1:0              MainClass>>main[][false]
		0021:  return
		0000:  false                            MainClass>>main[][false]
		0001:  false                            MainClass>>main[][false, false]
		0002:  dbg '<string>', 1:7              MainClass>>main[][false, false]
		0009:  send           1, 'and:'         MainClass>>main[][], Boolean>>and:[false][]
		0000:  self                             MainClass>>main[][], Boolean>>and:[false][false]
		0001:  block          0                 MainClass>>main[][], Boolean>>and:[false][false, and:-block0]
		0004:  dbg 'image.st', 102:12           MainClass>>main[][], Boolean>>and:[false][false, and:-block0]
		0011:  send           1, 'ifTrue:'      MainClass>>main[][], Boolean>>and:[false][nil]
		0016:  pop                              MainClass>>main[][], Boolean>>and:[false][]
		0017:  false                            MainClass>>main[][], Boolean>>and:[false][false]
		0018:  dbg 'image.st', 103:7            MainClass>>main[][], Boolean>>and:[false][false]
		0025:  return                           MainClass>>main[][false]
		0014:  dbg '<string>', 1:0              MainClass>>main[][false]
		0021:  return
		 */
		execAndCheck("^true and: true", "true");
		execAndCheck("^true and: false", "false");
		execAndCheck("^false and: true", "false");
		execAndCheck("^false and: false", "false");
	}

	@Test public void testOr() {
		/*
		0000:  true                             MainClass>>main[][true]
		0001:  true                             MainClass>>main[][true, true]
		0002:  dbg '<string>', 1:6              MainClass>>main[][true, true]
		0009:  send           1, 'or:'          MainClass>>main[][], Boolean>>or:[true][]
		0000:  self                             MainClass>>main[][], Boolean>>or:[true][true]
		0001:  block          0                 MainClass>>main[][], Boolean>>or:[true][true, or:-block0]
		0004:  dbg 'image.st', 106:12           MainClass>>main[][], Boolean>>or:[true][true, or:-block0]
		0011:  send           1, 'ifTrue:'      MainClass>>main[][], Boolean>>or:[true][], Boolean>>or:-block0[][]
		0000:  true                             MainClass>>main[][], Boolean>>or:[true][], Boolean>>or:-block0[][true]
		0001:  dbg 'image.st', 106:22           MainClass>>main[][], Boolean>>or:[true][], Boolean>>or:-block0[][true]
		0008:  return                           MainClass>>main[][true]
		0014:  dbg '<string>', 1:0              MainClass>>main[][true]
		0021:  return
		0000:  true                             MainClass>>main[][true]
		0001:  false                            MainClass>>main[][true, false]
		0002:  dbg '<string>', 1:6              MainClass>>main[][true, false]
		0009:  send           1, 'or:'          MainClass>>main[][], Boolean>>or:[false][]
		0000:  self                             MainClass>>main[][], Boolean>>or:[false][true]
		0001:  block          0                 MainClass>>main[][], Boolean>>or:[false][true, or:-block0]
		0004:  dbg 'image.st', 106:12           MainClass>>main[][], Boolean>>or:[false][true, or:-block0]
		0011:  send           1, 'ifTrue:'      MainClass>>main[][], Boolean>>or:[false][], Boolean>>or:-block0[][]
		0000:  true                             MainClass>>main[][], Boolean>>or:[false][], Boolean>>or:-block0[][true]
		0001:  dbg 'image.st', 106:22           MainClass>>main[][], Boolean>>or:[false][], Boolean>>or:-block0[][true]
		0008:  return                           MainClass>>main[][true]
		0014:  dbg '<string>', 1:0              MainClass>>main[][true]
		0021:  return
		0000:  false                            MainClass>>main[][false]
		0001:  true                             MainClass>>main[][false, true]
		0002:  dbg '<string>', 1:7              MainClass>>main[][false, true]
		0009:  send           1, 'or:'          MainClass>>main[][], Boolean>>or:[true][]
		0000:  self                             MainClass>>main[][], Boolean>>or:[true][false]
		0001:  block          0                 MainClass>>main[][], Boolean>>or:[true][false, or:-block0]
		0004:  dbg 'image.st', 106:12           MainClass>>main[][], Boolean>>or:[true][false, or:-block0]
		0011:  send           1, 'ifTrue:'      MainClass>>main[][], Boolean>>or:[true][nil]
		0016:  pop                              MainClass>>main[][], Boolean>>or:[true][]
		0017:  push_local     0, 0              MainClass>>main[][], Boolean>>or:[true][true]
		0022:  block          1                 MainClass>>main[][], Boolean>>or:[true][true, or:-block1]
		0025:  dbg 'image.st', 107:9            MainClass>>main[][], Boolean>>or:[true][true, or:-block1]
		0032:  send           1, 'ifTrue:'      MainClass>>main[][], Boolean>>or:[true][], Boolean>>or:-block1[][]
		0000:  true                             MainClass>>main[][], Boolean>>or:[true][], Boolean>>or:-block1[][true]
		0001:  dbg 'image.st', 107:19           MainClass>>main[][], Boolean>>or:[true][], Boolean>>or:-block1[][true]
		0008:  return                           MainClass>>main[][true]
		0014:  dbg '<string>', 1:0              MainClass>>main[][true]
		0021:  return
		0000:  false                            MainClass>>main[][false]
		0001:  false                            MainClass>>main[][false, false]
		0002:  dbg '<string>', 1:7              MainClass>>main[][false, false]
		0009:  send           1, 'or:'          MainClass>>main[][], Boolean>>or:[false][]
		0000:  self                             MainClass>>main[][], Boolean>>or:[false][false]
		0001:  block          0                 MainClass>>main[][], Boolean>>or:[false][false, or:-block0]
		0004:  dbg 'image.st', 106:12           MainClass>>main[][], Boolean>>or:[false][false, or:-block0]
		0011:  send           1, 'ifTrue:'      MainClass>>main[][], Boolean>>or:[false][nil]
		0016:  pop                              MainClass>>main[][], Boolean>>or:[false][]
		0017:  push_local     0, 0              MainClass>>main[][], Boolean>>or:[false][false]
		0022:  block          1                 MainClass>>main[][], Boolean>>or:[false][false, or:-block1]
		0025:  dbg 'image.st', 107:9            MainClass>>main[][], Boolean>>or:[false][false, or:-block1]
		0032:  send           1, 'ifTrue:'      MainClass>>main[][], Boolean>>or:[false][nil]
		0037:  pop                              MainClass>>main[][], Boolean>>or:[false][]
		0038:  false                            MainClass>>main[][], Boolean>>or:[false][false]
		0039:  dbg 'image.st', 108:7            MainClass>>main[][], Boolean>>or:[false][false]
		0046:  return                           MainClass>>main[][false]
		0014:  dbg '<string>', 1:0              MainClass>>main[][false]
		0021:  return
		 */
		execAndCheck("^true or: true", "true");
		execAndCheck("^true or: false", "true");
		execAndCheck("^false or: true", "true", true, true);
		execAndCheck("^false or: false", "false");
	}

	@Test public void testNot() {
		/*
		0000:  dbg '<string>', 1:6              MainClass>>main[][]
		0007:  true                             MainClass>>main[][true]
		0008:  send           0, 'not'          MainClass>>main[][false]
		0013:  dbg '<string>', 1:0              MainClass>>main[][false]
		0020:  return
		0000:  dbg '<string>', 1:7              MainClass>>main[][]
		0007:  false                            MainClass>>main[][false]
		0008:  send           0, 'not'          MainClass>>main[][true]
		0013:  dbg '<string>', 1:0              MainClass>>main[][true]
		0020:  return
		 */
		execAndCheck("^true not", "false");
		execAndCheck("^false not", "true");
	}

	@Test public void testHelloWorld() {
		/*
		0000:  push_global    'Transcript'      MainClass>>main[][a TranscriptStream]
		0003:  push_literal   'hi'              MainClass>>main[][a TranscriptStream, 'hi']
		0006:  dbg '<string>', 1:11             MainClass>>main[][a TranscriptStream, 'hi']
		0013:  send           1, 'show:'        hi
		MainClass>>main[][a TranscriptStream]
		0018:  dbg '<string>', 1:21             MainClass>>main[][a TranscriptStream]
		0025:  pop                              MainClass>>main[][]
		0026:  self                             MainClass>>main[][a MainClass]
		0027:  return
		 */
		String input =
			"Transcript show: 'hi'\n";
		String expecting = "a MainClass";
		execAndCheck(input, expecting);
	}

	@Test public void testBasicNew() {
		/*
		0000:  dbg '<string>', 4:12             MainClass>>main[][]
		0007:  dbg '<string>', 4:3              MainClass>>main[][]
		0014:  push_global    'T'               MainClass>>main[][class T]
		0017:  send           0, 'basicNew'     MainClass>>main[][a T]
		0022:  send           0, 'f'            MainClass>>main[][], T>>f[][]
		0000:  push_literal   'hi'              MainClass>>main[][], T>>f[]['hi']
		0003:  dbg '<string>', 2:7              MainClass>>main[][], T>>f[]['hi']
		0010:  return                           MainClass>>main[]['hi']
		0027:  dbg '<string>', 4:0              MainClass>>main[]['hi']
		0034:  return
		 */
		String input =
			"class T [\n" +
			"    f [^'hi']\n" +
			"]\n" +
			"^T basicNew f\n";
		String expecting = "hi";
		execAndCheck(input, expecting);
	}

	@Test public void testNew() {
		/*
		0000:  dbg '<string>', 4:7              MainClass>>main[][]
		0007:  dbg '<string>', 4:3              MainClass>>main[][]
		0014:  push_global    'T'               MainClass>>main[][class T]
		0017:  send           0, 'new'          MainClass>>main[][], Object>>new[][]
		0000:  dbg 'image.st', 20:22            MainClass>>main[][], Object>>new[][]
		0007:  dbg 'image.st', 20:13            MainClass>>main[][], Object>>new[][]
		0014:  self                             MainClass>>main[][], Object>>new[][class T]
		0015:  send           0, 'basicNew'     MainClass>>main[][], Object>>new[][a T]
		0020:  send           0, 'initialize'   MainClass>>main[][], Object>>new[][], Object>>initialize[][]
		0000:  self                             MainClass>>main[][], Object>>new[][], Object>>initialize[][a T]
		0001:  dbg 'image.st', 29:16            MainClass>>main[][], Object>>new[][], Object>>initialize[][a T]
		0008:  return                           MainClass>>main[][], Object>>new[][a T]
		0025:  dbg 'image.st', 20:7             MainClass>>main[][], Object>>new[][a T]
		0032:  return                           MainClass>>main[][a T]
		0022:  send           0, 'f'            MainClass>>main[][], T>>f[][]
		0000:  push_literal   'hi'              MainClass>>main[][], T>>f[]['hi']
		0003:  dbg '<string>', 2:7              MainClass>>main[][], T>>f[]['hi']
		0010:  return                           MainClass>>main[]['hi']
		0027:  dbg '<string>', 4:0              MainClass>>main[]['hi']
		0034:  return
		 */
		String input =
			"class T [\n" +
			"    f [^'hi']\n" +
			"]\n" +
			"^T new f\n"; // should be same as basicNew
		String expecting = "hi";
		execAndCheck(input, expecting);
	}

	@Test public void testInit() {
		/*
		0000:  dbg '<string>', 6:7              MainClass>>main[][]
		0007:  dbg '<string>', 6:3              MainClass>>main[][]
		0014:  push_global    'T'               MainClass>>main[][class T]
		0017:  send           0, 'new'          MainClass>>main[][], Object>>new[][]
		0000:  dbg 'image.st', 20:22            MainClass>>main[][], Object>>new[][]
		0007:  dbg 'image.st', 20:13            MainClass>>main[][], Object>>new[][]
		0014:  self                             MainClass>>main[][], Object>>new[][class T]
		0015:  send           0, 'basicNew'     MainClass>>main[][], Object>>new[][a T]
		0020:  send           0, 'initialize'   MainClass>>main[][], Object>>new[][], T>>initialize[][]
		0000:  dbg '<string>', 3:16             MainClass>>main[][], Object>>new[][], T>>initialize[][]
		0007:  push_int       1                 MainClass>>main[][], Object>>new[][], T>>initialize[][1]
		0012:  store_field    0                 MainClass>>main[][], Object>>new[][], T>>initialize[][1]
		0015:  dbg '<string>', 3:20             MainClass>>main[][], Object>>new[][], T>>initialize[][1]
		0022:  pop                              MainClass>>main[][], Object>>new[][], T>>initialize[][]
		0023:  self                             MainClass>>main[][], Object>>new[][], T>>initialize[][a T]
		0024:  return                           MainClass>>main[][], Object>>new[][a T]
		0025:  dbg 'image.st', 20:7             MainClass>>main[][], Object>>new[][a T]
		0032:  return                           MainClass>>main[][a T]
		0022:  send           0, 'f'            MainClass>>main[][], T>>f[][]
		0000:  push_field     0                 MainClass>>main[][], T>>f[][1]
		0003:  dbg '<string>', 4:7              MainClass>>main[][], T>>f[][1]
		0010:  return                           MainClass>>main[][1]
		0027:  dbg '<string>', 6:0              MainClass>>main[][1]
		0034:  return
		 */
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
		/*
		0000:  dbg '<string>', 6:13             MainClass>>main[][]
		0007:  push_global    'T'               MainClass>>main[][class T]
		0010:  push_int       99                MainClass>>main[][class T, 99]
		0015:  dbg '<string>', 6:4              MainClass>>main[][class T, 99]
		0022:  send           1, 'new:'         MainClass>>main[][], Object>>new:[99][]
		0000:  dbg 'image.st', 26:13            MainClass>>main[][], Object>>new:[99][]
		0007:  self                             MainClass>>main[][], Object>>new:[99][class T]
		0008:  send           0, 'new'          MainClass>>main[][], Object>>new:[99][], Object>>new[][]
		0000:  dbg 'image.st', 20:22            MainClass>>main[][], Object>>new:[99][], Object>>new[][]
		0007:  dbg 'image.st', 20:13            MainClass>>main[][], Object>>new:[99][], Object>>new[][]
		0014:  self                             MainClass>>main[][], Object>>new:[99][], Object>>new[][class T]
		0015:  send           0, 'basicNew'     MainClass>>main[][], Object>>new:[99][], Object>>new[][a T]
		0020:  send           0, 'initialize'   MainClass>>main[][], Object>>new:[99][], Object>>new[][], Object>>initialize[][]
		0000:  self                             MainClass>>main[][], Object>>new:[99][], Object>>new[][], Object>>initialize[][a T]
		0001:  dbg 'image.st', 29:16            MainClass>>main[][], Object>>new:[99][], Object>>new[][], Object>>initialize[][a T]
		0008:  return                           MainClass>>main[][], Object>>new:[99][], Object>>new[][a T]
		0025:  dbg 'image.st', 20:7             MainClass>>main[][], Object>>new:[99][], Object>>new[][a T]
		0032:  return                           MainClass>>main[][], Object>>new:[99][a T]
		0013:  push_local     0, 0              MainClass>>main[][], Object>>new:[99][a T, 99]
		0018:  dbg 'image.st', 26:17            MainClass>>main[][], Object>>new:[99][a T, 99]
		0025:  send           1, 'initialize:'  MainClass>>main[][], Object>>new:[99][], T>>initialize:[99][]
		0000:  dbg '<string>', 3:19             MainClass>>main[][], Object>>new:[99][], T>>initialize:[99][]
		0007:  push_local     0, 0              MainClass>>main[][], Object>>new:[99][], T>>initialize:[99][99]
		0012:  store_field    0                 MainClass>>main[][], Object>>new:[99][], T>>initialize:[99][99]
		0015:  dbg '<string>', 3:23             MainClass>>main[][], Object>>new:[99][], T>>initialize:[99][99]
		0022:  pop                              MainClass>>main[][], Object>>new:[99][], T>>initialize:[99][]
		0023:  self                             MainClass>>main[][], Object>>new:[99][], T>>initialize:[99][a T]
		0024:  return                           MainClass>>main[][], Object>>new:[99][a T]
		0030:  dbg 'image.st', 26:7             MainClass>>main[][], Object>>new:[99][a T]
		0037:  return                           MainClass>>main[][a T]
		0027:  send           0, 'f'            MainClass>>main[][], T>>f[][]
		0000:  push_field     0                 MainClass>>main[][], T>>f[][99]
		0003:  dbg '<string>', 4:7              MainClass>>main[][], T>>f[][99]
		0010:  return                           MainClass>>main[][99]
		0032:  dbg '<string>', 6:0              MainClass>>main[][99]
		0039:  return
		 */
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
			"    class factory [^self new]\n" +
			"    asString [^'blort']\n"+
			"]\n" +
			"^T factory asString\n";
		String expecting = "blort";
		execAndCheck(input, expecting);
	}

	@Test public void testClassPrimitiveMessageOnInstanceError() {
		/*
		0000:  dbg '<string>', 1:3              MainClass>>main[][]
		0007:  push_int       99                MainClass>>main[][99]
		0012:  send           0, 'new'  <======= OOOOPS!! cannot send class message "new" to an Integer instance
		 */
		String input =
			"99 new\n";
		String expecting =
			"ClassMessageSentToInstance: new is a class method sent to instance of Integer\n" +
			"    at                              MainClass>>main[][99](<string>:1:3)       executing 0012:  send           0, 'new'\n";
		String result = "";
		try {
			execAndCheck(input, expecting);
		}
		catch (ClassMessageSentToInstance te) {
			result = te.toString();
		}
		assertEquals(expecting, result);
	}

	@Test public void testClassMessageOnInstanceError() {
		/*
		0000:  dbg '<string>', 5:7              MainClass>>main[][]
		0007:  dbg '<string>', 5:3              MainClass>>main[][]
		0014:  push_global    'T'               MainClass>>main[][class T]
		0017:  send           0, 'new'          MainClass>>main[][], Object>>new[][]
		0000:  dbg 'image.st', 20:22            MainClass>>main[][], Object>>new[][]
		0007:  dbg 'image.st', 20:13            MainClass>>main[][], Object>>new[][]
		0014:  self                             MainClass>>main[][], Object>>new[][class T]
		0015:  send           0, 'basicNew'     MainClass>>main[][], Object>>new[][a T]
		0020:  send           0, 'initialize'   MainClass>>main[][], Object>>new[][], Object>>initialize[][]
		0000:  self                             MainClass>>main[][], Object>>new[][], Object>>initialize[][a T]
		0001:  dbg 'image.st', 29:16            MainClass>>main[][], Object>>new[][], Object>>initialize[][a T]
		0008:  return                           MainClass>>main[][], Object>>new[][a T]
		0025:  dbg 'image.st', 20:7             MainClass>>main[][], Object>>new[][a T]
		0032:  return                           MainClass>>main[][a T]
		0022:  send           0, 'factory'      <======= OOOOPS!! cannot send class message "factor" to a T instance
		 */
		String input =
			"class T [\n" +
			"    class factory [^self new]\n" +
			"    asString [^'blort']\n"+
			"]\n" +
			"^T new factory";
		String expecting =
			"ClassMessageSentToInstance: factory is a class method sent to instance of T\n" +
			"    at                             MainClass>>main[][a T](<string>:5:3)       executing 0022:  send           0, 'factory'\n";
		String result = "";
		try {
			execAndCheck(input, expecting);
		}
		catch (ClassMessageSentToInstance te) {
			result = te.toString();
		}
		assertEquals(expecting, result);
	}

	@Test public void testInstanceMessageOnClassError() {
		/*
		0000:  dbg '<string>', 4:3              MainClass>>main[][]
		0007:  push_global    'T'               MainClass>>main[][class T]
		0010:  send           0, 'asString'
		 */
		String input =
			"class T [\n" +
			"    asString [^'blort']\n"+
			"]\n" +
			"^T asString";
		String expecting =
			"MessageNotUnderstood: asString is an instance method sent to class object T\n" +
			"    at                         MainClass>>main[][class T](<string>:4:3)       executing 0010:  send           0, 'asString'\n";
		String result = "";
		try {
			execAndCheck(input, expecting);
		}
		catch (MessageNotUnderstood te) {
			result = te.toString();
		}
		assertEquals(expecting, result);
	}
}
