package smalltalk.test;

import org.antlr.v4.runtime.misc.Utils;
import org.junit.Test;
import smalltalk.Run;
import smalltalk.compiler.Compiler;
import smalltalk.compiler.STSymbolTable;
import smalltalk.vm.VirtualMachine;
import smalltalk.vm.primitive.STMetaClassObject;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestCodeGen extends BaseTest {
	@Test public void testEmpty() {
		String input = "";
		String expecting = "";
		String result = compile(input);
		assertEquals(expecting, result);
	}

	@Test public void testEmptyClass() {
		String input =
				"class T [" +
						"]";
		String expecting =
				"name: T\n" +
						"superClass: \n" +
						"fields: \n" +
						"methods:\n";
		String result = compile(input);
		assertEquals(expecting, result);
	}

	@Test public void testEmptyMethod() {
		String input =
				"class T [\n" +
						"	f [ ]" +
						"]";
		String expecting =
				"name: T\n" +
						"superClass: \n" +
						"fields: \n" +
						"methods:\n" +
						"    name: f\n" +
						"    qualifiedName: T>>f\n" +
						"    nargs: 0\n" +
						"    nlocals: 0\n" +
						"    literals: \n" +
						"    0000:  self             \n" +
						"    0001:  return           \n";
		String result = compile(input);
		assertEquals(expecting, result);
	}

	@Test public void testAssignToLocalInMain() {
		String input =
				"|x y| x := y.";
		String expecting =
				"name: MainClass\n" +
						"superClass: \n" +
						"fields: \n" +
						"methods:\n" +
						"    name: main\n" +
						"    qualifiedName: MainClass>>main\n" +
						"    nargs: 0\n" +
						"    nlocals: 2\n" +
						"    literals: \n" +
						"    0000:  push_local     0, 1\n" +
						"    0005:  store_local    0, 0\n" +
						"    0010:  pop              \n" +
						"    0011:  self             \n" +
						"    0012:  return           \n";
		String result = compile(input);
		assertEquals(expecting, result);
	}

	@Test public void testHelloWorld() {
		String input =
				"Transcript show: 'hello'.";
		String expecting =
				"name: MainClass\n" +
						"superClass: \n" +
						"fields: \n" +
						"methods:\n" +
						"    name: main\n" +
						"    qualifiedName: MainClass>>main\n" +
						"    nargs: 0\n" +
						"    nlocals: 0\n" +
						"    literals: 'Transcript','hello','show:'\n" +
						"    0000:  push_global    'Transcript'\n" +
						"    0003:  push_literal   'hello'\n" +
						"    0006:  send           1, 'show:'\n" +
						"    0011:  pop              \n" +
						"    0012:  self             \n" +
						"    0013:  return           \n";
		String result = compile(input);
		assertEquals(expecting, result);
	}

	@Test public void testDefinePrimitive() {
		String input =
				"class String : Object [\n" +
						"   asArray <primitive:#String_ASARRAY>\n" +
						"]\n";
		String expecting =
				"name: String\n" +
						"superClass: \n" +
						"fields: \n" +
						"methods:\n" +
						"    name: asArray\n" +
						"    qualifiedName: String>>asArray\n" +
						"    nargs: 0\n" +
						"    nlocals: 0\n" +
						"    literals: \n";
		String result = compile(input);
		assertEquals(expecting, result);
	}

	@Test public void testDefinePrimitiveOperator() {
		String input =
				"class String : Object [\n" +
						"   , other <primitive:#String_CAT>\n" +
						"]\n";
		String expecting =
				"name: String\n" +
						"superClass: \n" +
						"fields: \n" +
						"methods:\n" +
						"    name: ,\n" +
						"    qualifiedName: String>>,\n" +
						"    nargs: 1\n" +
						"    nlocals: 0\n" +
						"    literals: \n";
		String result = compile(input);
		assertEquals(expecting, result);
	}

	@Test public void testAssignToField() {
		String input =
				"class T [\n" +
						"	|x|\n" +
						"	foo [|y| x := y.]\n" +
						"]\n";
		String expecting =
				"name: T\n" +
						"superClass: \n" +
						"fields: x\n" +
						"methods:\n" +
						"    name: foo\n" +
						"    qualifiedName: T>>foo\n" +
						"    nargs: 0\n" +
						"    nlocals: 1\n" +
						"    literals: \n" +
						"    0000:  push_local     0, 0\n" +
						"    0005:  store_field    0\n" +
						"    0008:  pop              \n" +
						"    0009:  self             \n" +
						"    0010:  return           \n";
		String result = compile(input);
		assertEquals(expecting, result);
	}

	@Test public void testPopBetweenExpressions() {
		String input =
				"class T [\n" +
						"	|x|\n" +
						"	foo [|y| x := y. x := y]\n" +
						"]\n";
		String expecting =
				"name: T\n" +
						"superClass: \n" +
						"fields: x\n" +
						"methods:\n" +
						"    name: foo\n" +
						"    qualifiedName: T>>foo\n" +
						"    nargs: 0\n" +
						"    nlocals: 1\n" +
						"    literals: \n" +
						"    0000:  push_local     0, 0\n" +
						"    0005:  store_field    0\n" +
						"    0008:  pop              \n" +
						"    0009:  push_local     0, 0\n" +
						"    0014:  store_field    0\n" +
						"    0017:  pop              \n" +
						"    0018:  self             \n" +
						"    0019:  return           \n";
		String result = compile(input);
		assertEquals(expecting, result);
	}

	@Test public void testPopBetweenSends() {
		String input =
				"false ifTrue: [^99].\n" +
						"true ifTrue: [^100].\n" +
						"^1";
		String expecting =
				"name: MainClass\n" +
						"superClass: \n" +
						"fields: \n" +
						"methods:\n" +
						"    name: main\n" +
						"    qualifiedName: MainClass>>main\n" +
						"    nargs: 0\n" +
						"    nlocals: 0\n" +
						"    literals: 'ifTrue:'\n" +
						"    0000:  false            \n" +
						"    0001:  block          0\n" +
						"    0004:  send           1, 'ifTrue:'\n" +
						"    0009:  pop              \n" +
						"    0010:  true             \n" +
						"    0011:  block          1\n" +
						"    0014:  send           1, 'ifTrue:'\n" +
						"    0019:  pop              \n" +
						"    0020:  push_int       1\n" +
						"    0025:  return           \n" +
						"    0026:  pop              \n" +
						"    0027:  self             \n" +
						"    0028:  return           \n" +
						"    blocks:\n" +
						"        name: main-block0\n" +
						"        qualifiedName: main>>main-block0\n" +
						"        nargs: 0\n" +
						"        nlocals: 0\n" +
						"        literals: \n" +
						"        0000:  push_int       99\n" +
						"        0005:  return           \n" +
						"        0006:  block_return     \n" +
						"\n" +
						"        name: main-block1\n" +
						"        qualifiedName: main>>main-block1\n" +
						"        nargs: 0\n" +
						"        nlocals: 0\n" +
						"        literals: \n" +
						"        0000:  push_int       100\n" +
						"        0005:  return           \n" +
						"        0006:  block_return     \n";
		String result = compile(input);
		assertEquals(expecting, result);
	}

	@Test public void testTestField() {
		String input =
				"class T [\n" +
						"	|x|\n" +
						"	isEmpty [^x==nil]\n" +
						"]\n";
		String expecting =
				"name: T\n" +
						"superClass: \n" +
						"fields: x\n" +
						"methods:\n" +
						"    name: isEmpty\n" +
						"    qualifiedName: T>>isEmpty\n" +
						"    nargs: 0\n" +
						"    nlocals: 0\n" +
						"    literals: '=='\n" +
						"    0000:  push_field     0\n" +
						"    0003:  nil              \n" +
						"    0004:  send           1, '=='\n" +
						"    0009:  return           \n" +
						"    0010:  pop              \n" +
						"    0011:  self             \n" +
						"    0012:  return           \n";
		String result = compile(input);
		assertEquals(expecting, result);
	}

	@Test public void testInheritFieldAndHaveField() {
		String input =
				"class T [\n" +
						"    | x |\n" +
						"]\n" +
						"class U : T [\n" +
						"    | y |\n" +
						"    value [ ^x + y ]\n" +
						"]\n";
		String expecting =
				"name: T\n"+
						"superClass: \n"+
						"fields: x\n"+
						"methods:\n"+
						"name: U\n"+
						"superClass: T\n"+
						"fields: x,y\n"+
						"methods:\n"+
						"    name: value\n"+
						"    qualifiedName: U>>value\n"+
						"    nargs: 0\n"+
						"    nlocals: 0\n"+
						"    literals: '+'\n"+
						"    0000:  push_field     0\n"+
						"    0003:  push_field     1\n"+
						"    0006:  send           1, '+'\n"+
						"    0011:  return           \n"+
						"    0012:  pop              \n"+
						"    0013:  self             \n"+
						"    0014:  return           \n";
		String result = compile(input);
		assertEquals(expecting, result);
	}

	@Test public void testMethods() {
		String input =
				"class T [\n" +
						"	foo [ ^99 ]\n" +
						"	bar [ ^100 ]\n" +
						"]\n";
		String expecting =
				"name: T\n" +
						"superClass: \n" +
						"fields: \n" +
						"methods:\n" +
						"    name: bar\n" +
						"    qualifiedName: T>>bar\n" +
						"    nargs: 0\n" +
						"    nlocals: 0\n" +
						"    literals: \n" +
						"    0000:  push_int       100\n" +
						"    0005:  return           \n" +
						"    0006:  pop              \n" + // dead code
						"    0007:  self             \n" + // dead code
						"    0008:  return           \n" + // dead code
						"\n" +
						"    name: foo\n" +
						"    qualifiedName: T>>foo\n" +
						"    nargs: 0\n" +
						"    nlocals: 0\n" +
						"    literals: \n" +
						"    0000:  push_int       99\n" +
						"    0005:  return           \n" +
						"    0006:  pop              \n" + // dead code
						"    0007:  self             \n" + // dead code
						"    0008:  return           \n";  // dead code
		String result = compile(input);
		assertEquals(expecting, result);
	}

	@Test public void testOperator() {
		String input =
				"class T [\n" +
						"	foo [|x| x := 1 + 5 * 10.]\n" +
						"]\n";
		String expecting =
				"name: T\n" +
						"superClass: \n" +
						"fields: \n" +
						"methods:\n" +
						"    name: foo\n" +
						"    qualifiedName: T>>foo\n" +
						"    nargs: 0\n" +
						"    nlocals: 1\n" +
						"    literals: '+','*'\n" +
						"    0000:  push_int       1\n" +
						"    0005:  push_int       5\n" +
						"    0010:  send           1, '+'\n" +
						"    0015:  push_int       10\n" +
						"    0020:  send           1, '*'\n" +
						"    0025:  store_local    0, 0\n" +
						"    0030:  pop              \n" +
						"    0031:  self             \n" +
						"    0032:  return           \n";
		String result = compile(input);
		assertEquals(expecting, result);
	}

	@Test public void testBlock() {
		String input =
				"|x y| x := [^99].";
		String expecting =
				"name: MainClass\n" +
						"superClass: \n" +
						"fields: \n" +
						"methods:\n" +
						"    name: main\n" +
						"    qualifiedName: MainClass>>main\n" +
						"    nargs: 0\n" +
						"    nlocals: 2\n" +
						"    literals: \n" +
						"    0000:  block          0\n" +
						"    0003:  store_local    0, 0\n" +
						"    0008:  pop              \n" +
						"    0009:  self             \n" +
						"    0010:  return           \n" +
						"    blocks:\n" +
						"        name: main-block0\n" +
						"        qualifiedName: main>>main-block0\n" +
						"        nargs: 0\n" +
						"        nlocals: 0\n" +
						"        literals: \n" +
						"        0000:  push_int       99\n" +
						"        0005:  return           \n" +
						"        0006:  block_return     \n";
		String result = compile(input);
		assertEquals(expecting, result);
	}

	@Test public void test2Blocks() {
		String input =
				"[true] whileTrue: [ ].";
		String expecting =
				"name: MainClass\n" +
						"superClass: \n" +
						"fields: \n" +
						"methods:\n" +
						"    name: main\n" +
						"    qualifiedName: MainClass>>main\n" +
						"    nargs: 0\n" +
						"    nlocals: 0\n" +
						"    literals: 'whileTrue:'\n" +
						"    0000:  block          0\n" +
						"    0003:  block          1\n" +
						"    0006:  send           1, 'whileTrue:'\n" +
						"    0011:  pop              \n" +
						"    0012:  self             \n" +
						"    0013:  return           \n" +
						"    blocks:\n" +
						"        name: main-block0\n" +
						"        qualifiedName: main>>main-block0\n" +
						"        nargs: 0\n" +
						"        nlocals: 0\n" +
						"        literals: \n" +
						"        0000:  true             \n" +
						"        0001:  block_return     \n" +
						"\n" +
						"        name: main-block1\n" +
						"        qualifiedName: main>>main-block1\n" +
						"        nargs: 0\n" +
						"        nlocals: 0\n" +
						"        literals: \n" +
						"        0000:  nil              \n" +
						"        0001:  block_return     \n";
		String result = compile(input);
		assertEquals(expecting, result);
	}

	@Test public void testToDoLoop() {
		String input =
				"1 to: 5 do: [:i | Transcript show: i].";
		String expecting =
				"name: MainClass\n" +
						"superClass: \n" +
						"fields: \n" +
						"methods:\n" +
						"    name: main\n" +
						"    qualifiedName: MainClass>>main\n" +
						"    nargs: 0\n" +
						"    nlocals: 0\n" +
						"    literals: 'to:do:'\n" +
						"    0000:  push_int       1\n" +
						"    0005:  push_int       5\n" +
						"    0010:  block          0\n" +
						"    0013:  send           2, 'to:do:'\n" +
						"    0018:  pop              \n" +
						"    0019:  self             \n" +
						"    0020:  return           \n" +
						"    blocks:\n" +
						"        name: main-block0\n" +
						"        qualifiedName: main>>main-block0\n" +
						"        nargs: 1\n" +
						"        nlocals: 0\n" +
						"        literals: 'Transcript','show:'\n" +
						"        0000:  push_global    'Transcript'\n" +
						"        0003:  push_local     0, 0\n" +
						"        0008:  send           1, 'show:'\n" +
						"        0013:  block_return     \n";
		String result = compile(input);
		assertEquals(expecting, result);
	}

	/** Load and compile smalltalk/test/linkedlist.st then compare
	 *  all gen'd bytecode/literals.
	 */
	@Test public void testFullLinkedList() throws IOException {
		boolean genDbg = false;
		STSymbolTable symtab = Run.compileCore(genDbg);
		Run.compile(symtab, "smalltalk/test/linkedlist.st", genDbg);
		VirtualMachine vm = new VirtualMachine(symtab);
		STMetaClassObject linkedListClass = vm.lookupClass("LinkedList");
		String expectedOutputFileName = Run.getImageURL("smalltalk/test/linkedlist.st-teststring.txt").getFile();
		char[] _expecting = Utils.readFile(expectedOutputFileName);
		String expecting = new String(_expecting);
		String result = linkedListClass.toTestString();
		assertEquals(expecting, result);        STMetaClassObject linkClass = vm.lookupClass("Link");
		expectedOutputFileName = Run.getImageURL("smalltalk/test/link-teststring.txt").getFile();
		_expecting = Utils.readFile(expectedOutputFileName);
		expecting = new String(_expecting);
		result = linkClass.toTestString();
		assertEquals(expecting, result);
	}

	/** Load and compile smalltalk/test/linkedlist.st then compare
	 *  all gen'd bytecode/literals.
	 */
	@Test public void testFullLinkedListWithDbg() throws IOException {
		boolean genDbg = true;
		STSymbolTable symtab = Run.compileCore(genDbg);
		Run.compile(symtab, "smalltalk/test/linkedlist.st", genDbg);
		List<STMetaClassObject> metaObjects = Compiler.getMetaObjects(symtab);
		STMetaClassObject linkedListClass =
				org.antlr.symtab.Utils.findFirst(metaObjects, meta -> meta.name.equals("LinkedList"));
		String expectedOutputFileName = Run.getImageURL("smalltalk/test/linkedlist.st-teststring-dbg.txt").getFile();
		char[] _expecting = Utils.readFile(expectedOutputFileName);
		String expecting = new String(_expecting);
		String result = linkedListClass.toTestString();
		System.out.println(result);
		assertEquals(expecting, result);
	}

}