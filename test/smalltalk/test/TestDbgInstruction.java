package smalltalk.test;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestDbgInstruction extends BaseTest {
	@Test public void testReturnWithDbgInstruction() {
		String input =
			"^x==nil\n";
		String expecting =
			"name: MainClass\n" +
			"superClass: \n" +
			"fields: \n" +
			"methods:\n" +
			"    name: main\n" +
			"    qualifiedName: MainClass>>main\n" +
			"    nargs: 0\n" +
			"    nlocals: 0\n" +
			"    literals: '<unknown>','x','=='\n" +
			"    0000:  dbg '<unknown>', 1:2\n" +
			"    0007:  push_global    'x'\n" +
			"    0010:  nil              \n" +
			"    0011:  send           1, '=='\n" +
			"    0016:  dbg '<unknown>', 1:0\n" +
			"    0023:  return           \n" +
			"    0024:  dbg '<unknown>', 1:7\n" +
			"    0031:  pop              \n" +
			"    0032:  self             \n" +
			"    0033:  return           \n";
		boolean genDbg = true;
		String result = compile(input, genDbg);
		assertEquals(expecting, result);
	}

	@Test public void testCondBlockWithOperator() {
		String input =
			"[p~~nil] ifTrue: [ ]\n";
		//   012345678901234567890
		//   0         1         2
		String expecting =
			"name: MainClass\n" +
			"superClass: \n" +
			"fields: \n" +
			"methods:\n" +
			"    name: main\n" +
			"    qualifiedName: MainClass>>main\n" +
			"    nargs: 0\n" +
			"    nlocals: 0\n" +
			"    literals: '<unknown>','ifTrue:'\n" +
			"    0000:  block          0\n" +
			"    0003:  block          1\n" +
			"    0006:  dbg '<unknown>', 1:9\n" +
			"    0013:  send           1, 'ifTrue:'\n" +
			"    0018:  dbg '<unknown>', 1:20\n" +
			"    0025:  pop              \n" +
			"    0026:  self             \n" +
			"    0027:  return           \n" +
			"    blocks:\n" +
			"        name: main-block0\n" +
			"        qualifiedName: main>>main-block0\n" +
			"        nargs: 0\n" +
			"        nlocals: 0\n" +
			"        literals: '<unknown>','p','~~'\n" +
			"        0000:  dbg '<unknown>', 1:2\n" +
			"        0007:  push_global    'p'\n" +
			"        0010:  nil              \n" +
			"        0011:  send           1, '~~'\n" +
			"        0016:  dbg '<unknown>', 1:7\n" +
			"        0023:  block_return     \n" +
			"\n" +
			"        name: main-block1\n" +
			"        qualifiedName: main>>main-block1\n" +
			"        nargs: 0\n" +
			"        nlocals: 0\n" +
			"        literals: '<unknown>'\n" +
			"        0000:  dbg '<unknown>', 1:17\n" +
			"        0007:  nil              \n" +
			"        0008:  dbg '<unknown>', 1:19\n" +
			"        0015:  block_return     \n";
		boolean genDbg = true;
		String result = compile(input, genDbg);
		assertEquals(expecting, result);
	}

	@Test public void testTestExprDbgInstructions() {
		String input =
			"x==nil\n";
		String expecting =
			"name: MainClass\n" +
			"superClass: \n" +
			"fields: \n" +
			"methods:\n" +
			"    name: main\n" +
			"    qualifiedName: MainClass>>main\n" +
			"    nargs: 0\n" +
			"    nlocals: 0\n" +
			"    literals: '<unknown>','x','=='\n" +
			"    0000:  dbg '<unknown>', 1:1\n" +
			"    0007:  push_global    'x'\n" +
			"    0010:  nil              \n" +
			"    0011:  send           1, '=='\n" +
			"    0016:  dbg '<unknown>', 1:6\n" +
			"    0023:  pop              \n" +
			"    0024:  self             \n" +
			"    0025:  return           \n";
		boolean genDbg = true;
		String result = compile(input, genDbg);
		assertEquals(expecting, result);
	}

	@Test public void testTestSendDbgInstructions() {
		String input =
			"3 asString\n";
		String expecting =
			"name: MainClass\n" +
			"superClass: \n" +
			"fields: \n" +
			"methods:\n" +
			"    name: main\n" +
			"    qualifiedName: MainClass>>main\n" +
			"    nargs: 0\n" +
			"    nlocals: 0\n" +
			"    literals: '<unknown>','asString'\n" +
			"    0000:  dbg '<unknown>', 1:2\n" +
			"    0007:  push_int       3\n" +
			"    0012:  send           0, 'asString'\n" +
			"    0017:  dbg '<unknown>', 1:10\n" +
			"    0024:  pop              \n" +
			"    0025:  self             \n" +
			"    0026:  return           \n";
		boolean genDbg = true;
		String result = compile(input, genDbg);
		assertEquals(expecting, result);
	}

	@Test public void testTestSendDbgWithMultiArgs() {
		String input =
			"1 to: 10 do: [5+6]\n";
		String expecting =
			"name: MainClass\n" +
			"superClass: \n" +
			"fields: \n" +
			"methods:\n" +
			"    name: main\n" +
			"    qualifiedName: MainClass>>main\n" +
			"    nargs: 0\n" +
			"    nlocals: 0\n" +
			"    literals: '<unknown>','to:do:'\n" +
			"    0000:  push_int       1\n" +
			"    0005:  push_int       10\n" +
			"    0010:  block          0\n" +
			"    0013:  dbg '<unknown>', 1:2\n" +
			"    0020:  send           2, 'to:do:'\n" +
			"    0025:  dbg '<unknown>', 1:18\n" +
			"    0032:  pop              \n" +
			"    0033:  self             \n" +
			"    0034:  return           \n" +
			"    blocks:\n" +
			"        name: main-block0\n" +
			"        qualifiedName: main>>main-block0\n" +
			"        nargs: 0\n" +
			"        nlocals: 0\n" +
			"        literals: '<unknown>','+'\n" +
			"        0000:  dbg '<unknown>', 1:15\n" +
			"        0007:  push_int       5\n" +
			"        0012:  push_int       6\n" +
			"        0017:  send           1, '+'\n" +
			"        0022:  dbg '<unknown>', 1:17\n" +
			"        0029:  block_return     \n";
		boolean genDbg = true;
		String result = compile(input, genDbg);
		assertEquals(expecting, result);
	}

	@Test public void testTestSendDbgWithNestedSends() {
		String input =
			"1 to: 'abc' size do: [ ]\n";
		//   0123456789012345678901234
		//   0         1         2
		String expecting =
			"name: MainClass\n" +
			"superClass: \n" +
			"fields: \n" +
			"methods:\n" +
			"    name: main\n" +
			"    qualifiedName: MainClass>>main\n" +
			"    nargs: 0\n" +
			"    nlocals: 0\n" +
			"    literals: '<unknown>','abc','size','to:do:'\n" +
			"    0000:  push_int       1\n" +
			"    0005:  dbg '<unknown>', 1:12\n" +
			"    0012:  push_literal   'abc'\n" +
			"    0015:  send           0, 'size'\n" +
			"    0020:  block          0\n" +
			"    0023:  dbg '<unknown>', 1:2\n" +
			"    0030:  send           2, 'to:do:'\n" +
			"    0035:  dbg '<unknown>', 1:24\n" +
			"    0042:  pop              \n" +
			"    0043:  self             \n" +
			"    0044:  return           \n" +
			"    blocks:\n" +
			"        name: main-block0\n" +
			"        qualifiedName: main>>main-block0\n" +
			"        nargs: 0\n" +
			"        nlocals: 0\n" +
			"        literals: '<unknown>'\n" +
			"        0000:  dbg '<unknown>', 1:21\n" +
			"        0007:  nil              \n" +
			"        0008:  dbg '<unknown>', 1:23\n" +
			"        0015:  block_return     \n";
		boolean genDbg = true;
		String result = compile(input, genDbg);
		assertEquals(expecting, result);
	}

	@Test public void testLotsBlocks() {
		String input =
			"class T [\n" +
			" do: blk [\n" +
			"  |p|\n" +
			"  p:=head.\n" +
			"  [p~~nil] whileTrue: [ blk value: (p value). p := p nextLink ]\n" +
			" ]\n" +
			"]\n";
		//   01234567890123456789012345678901234567890123456789012345678901234567890
		//   0         1         2         3         4         5         6         7
		String expecting =
			"name: T\n" +
			"superClass: \n" +
			"fields: \n" +
			"methods:\n" +
			"    name: do:\n" +
			"    qualifiedName: T>>do:\n" +
			"    nargs: 1\n" +
			"    nlocals: 1\n" +
			"    literals: '<unknown>','head','whileTrue:'\n" +
			"    0000:  dbg '<unknown>', 4:2\n" +
			"    0007:  push_global    'head'\n" +
			"    0010:  store_local    0, 1\n" +
			"    0015:  pop              \n" +
			"    0016:  block          0\n" +
			"    0019:  block          1\n" +
			"    0022:  dbg '<unknown>', 5:11\n" +
			"    0029:  send           1, 'whileTrue:'\n" +
			"    0034:  dbg '<unknown>', 6:1\n" +
			"    0041:  pop              \n" +
			"    0042:  self             \n" +
			"    0043:  return           \n" +
			"    blocks:\n" +
			"        name: do:-block0\n" +
			"        qualifiedName: do:>>do:-block0\n" +
			"        nargs: 0\n" +
			"        nlocals: 0\n" +
			"        literals: '<unknown>','~~'\n" +
			"        0000:  dbg '<unknown>', 5:4\n" +
			"        0007:  push_local     1, 1\n" +
			"        0012:  nil              \n" +
			"        0013:  send           1, '~~'\n" +
			"        0018:  dbg '<unknown>', 5:9\n" +
			"        0025:  block_return     \n" +
			"\n" +
			"        name: do:-block1\n" +
			"        qualifiedName: do:>>do:-block1\n" +
			"        nargs: 0\n" +
			"        nlocals: 0\n" +
			"        literals: '<unknown>','value','value:','nextLink'\n" +
			"        0000:  push_local     1, 0\n" +
			"        0005:  dbg '<unknown>', 5:38\n" +
			"        0012:  push_local     1, 1\n" +
			"        0017:  send           0, 'value'\n" +
			"        0022:  dbg '<unknown>', 5:28\n" +
			"        0029:  send           1, 'value:'\n" +
			"        0034:  pop              \n" +
			"        0035:  dbg '<unknown>', 5:46\n" +
			"        0042:  dbg '<unknown>', 5:53\n" +
			"        0049:  push_local     1, 1\n" +
			"        0054:  send           0, 'nextLink'\n" +
			"        0059:  store_local    1, 1\n" +
			"        0064:  dbg '<unknown>', 5:62\n" +
			"        0071:  block_return     \n";
		boolean genDbg = true;
		String result = compile(input, genDbg);
		assertEquals(expecting, result);
	}

	/*
	   do: blk [
       |p|
       p:=head.
       [p~~nil] whileTrue: [ blk value: (p value). p := p nextLink ]
   ]

	 */
}
