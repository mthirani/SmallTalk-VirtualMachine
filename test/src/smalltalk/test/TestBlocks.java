package smalltalk.test;

import org.junit.Test;
import smalltalk.vm.exceptions.BlockCannotReturn;

import static org.junit.Assert.assertEquals;

public class TestBlocks extends BaseTest {
	@Test public void testBlockDescriptor() {
		String input =
			"^[99]";
		String expecting = "a BlockDescriptor";
		execAndCheck(input, expecting);
	}

	@Test public void testBlockValueOperator() {
		String input =
			"^[99] value"; // block return value is the value of the last expression, 99
		String expecting = "99";
		execAndCheck(input, expecting);
	}

	@Test public void testBlockValueWithArgOperator() {
		String input =
			"^[:x | x] value: 99";
		String expecting = "99";
		execAndCheck(input, expecting);
	}

	@Test public void testClosure() {
		String input =
			"class T [\n" +
			"    f [|x| x := 1. ^[x := 5] ]\n" + // return block that assigns to local x
			"]\n" +
			"|t|\n" +
			"t := T new.\n" +
			"^t f value"; // Send f to t then evaluate the block that comes back.
			              // Even though f has returned, the [x := 5] can access x
		                  // Result is last expr of [x := 5] or 5.
		String expecting = "5";
		execAndCheck(input, expecting);
	}

	@Test public void testRemoteMethodCanSetMyLocal() {
		String input =
		"class T [\n" +
		"    f [|x| self g: [x := 5]. ^x]\n" +  // pass block to g that assigns value to local x
		"    g: blk [ blk value ]\n"+           // eval block [x:=5] then return to f, which returns x
		"]\n" +
		"^T new f"; // make a T then call f
		String expecting = "5";
		execAndCheck(input, expecting);
	}

	@Test public void testRemoteMethodCanSetMyArg() {
		String input =
		"class T [\n" +
		"    f: x [self g: [x := 5]. ^x]\n" + // pass block to g that assigns value to arg x
		"    g: blk [ blk value ]\n"+         // eval block [x:=5] then return to f, which returns x
		"]\n" +
		"^T new f: 1"; // make a T then call f
		String expecting = "5";
		execAndCheck(input, expecting);
	}

	@Test public void testRemoteMethodCanSetMyField() {
		String input =
		"class T [\n" +
		"    | x |\n" +
		"    initialize [x := 1]\n" +
		"    f [(U new) g: [x := 5]. ^x]\n" + // pass block to g that assigns value to arg x
		"]\n" +
		"class U [\n" +
		"    g: blk [ blk value ]\n"+         // eval block [x:=5] then return to f, which returns x
		"]\n" +
		"^T new f"; // make a T then call f
		String expecting = "5";
		execAndCheck(input, expecting);
	}

	@Test public void testSendBlockBackToSameMethodAndSetLocal() {
		String input =
		"class T [\n" +
		"    f: blk pass: p [\n" +
		"       |x|\n" +
		"       p=1 ifTrue: [self g: [x:=5]]\n" + // pass 1: send to g, which sends back to us
		"           ifFalse:[blk value].\n" +     // pass 2: eval blk [x:=5] passed back from g
		"       ^x\n" +                           // should be 5 not nil
		"    ]\n" +
		"    g: blk [ self f: blk pass: 2]\n"+   // send blk back to f for eval
		"]\n" +
		"^T new f: nil pass: 1"; // make a T then call a:b:
		String expecting = "5";
		execAndCheck(input, expecting);
	}

	@Test public void testRemoteReturn() {
		String input =
			"class T [\n" +
			"    f [ self g: [^99]. ^1]\n" + // send a block to g that returns 99 from f. the ^1 is dead code
			"    g: blk [ blk value ]\n" +   // eval block that forces f to return 99.
											 // VM unrolls method invocation stack to caller of f upon evaluating [^99]
			"]\n" +
			"|t|\n" +
			"t := T new.\n" +
			"^t f"; // Send f to t
		String expecting = "99";
		execAndCheck(input, expecting);
	}

	@Test public void testRemoteReturnFromNestedBlock() {
		String input =
			"class T [\n" +
			"    f [ [self g: [^99]] value. ^1]\n" + // eval code that sends a block to g that returns 99 from f. the ^1 is dead code
			"    g: blk [ blk value ]\n" +   // eval block that forces f to return 99.
											 // VM unrolls method invocation stack to caller of f upon evaluating [^99]
			"]\n" +
			"|t|\n" +
			"t := T new.\n" +
			"^t f"; // Send f to t
		String expecting = "99";
		execAndCheck(input, expecting);
	}

	@Test public void testAttemptDoubleReturn() {
		String input =
			"class T [\n" +
			"    f [ self g: [^[^99]]. ^1]\n" + // send a block that returns a block that returns 99 from f to g. the ^1 is dead code
			"    g: blk [ blk value ]\n" +      // eval block that forces f to return 99.
											    // VM unrolls method invocation stack to caller of f upon evaluating [^99]
			"]\n" +
			"|t|\n" +
			"t := T new.\n" +
			"^t f value"; // Send f to t then evaluate block that comes back, [^99],
			        	  // which tries to return from its surrounding method, f, again.
		String expecting =
			"BlockCannotReturn: T>>f-block1 can't trigger return again from method T>>f\n" +
			"    at                             f-block0>>f-block1[][](<string>:2:19)      executing 0012:  return           \n" +
			"    at                             MainClass>>main[a T][](<string>:7:3)       executing 0052:  send           0, 'value'\n";
		String result = "";
		try {
			execAndCheck(input, expecting);
		}
		catch (BlockCannotReturn bcr) {
			result = bcr.toString();
		}
		assertEquals(expecting, result);
	}

	@Test public void returnFromNestedCallViaBlock() {
		String input =
			"class Test [\n" +
			"  test [\n" +
			"    self foo: [^99].\n" +	// pass block with "return from test" to foo:
			"  ]\n" +
			"  foo: blk [\n" +
			"    self bar: blk\n" +		// pass that same block along to bar:
			"  ]\n" +
			"  bar: blk [\n" +
			"    blk value.\n" +		// eval block, forcing return from test;
										// unwind stack out of bar, foo and then return from test
			"  ]\n" +
			"]\n" +
			"^Test new test";
		String expecting = "99";
		execAndCheck(input, expecting);
	}
}
