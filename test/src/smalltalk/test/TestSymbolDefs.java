package smalltalk.test;

import org.antlr.symtab.GlobalScope;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestSymbolDefs extends BaseTest {
	@Test public void testEmpty() {
		String input = "";
		String expecting = "";
		check(input, expecting);
	}

	@Test public void testMainButNoSymbols() {
		String input = "Transcript show: 'hi'.";
		String expecting = "global>>MainClass, MainClass>>main";
		check(input, expecting);
	}

	@Test public void testGlobals() {
		String input = "|c|";
		String expecting = "global>>MainClass, MainClass>>main, main>>c";
		check(input, expecting);
	}

	@Test public void testEmptyClass() {
		String input = "class T [ ]";
		String expecting = "global>>T";
		check(input, expecting);
	}

	@Test public void test2Classes() {
		String input =
			"class T [ ]\n" +
			"class U [ ]";
		String expecting = "global>>T, global>>U";
		check(input, expecting);
	}

	@Test public void testField() {
		String input = "class T [ |x| ]";
		String expecting = "global>>T, T>>x";
		check(input, expecting);
	}

	@Test public void testFields() {
		String input = "class T [ |x y z| ]";
		String expecting = "global>>T, T>>x, T>>y, T>>z";
		check(input, expecting);
	}

	@Test public void testEmptyMethod() {
		String input =
			"class T [" +
			"    foo [ ]" +
			"]";
		String expecting = "global>>T, T>>foo";
		check(input, expecting);
	}

	@Test public void testMethodLocals() {
		String input =
			"class T [" +
			"    foo [ |x| ]" +
			"]";
		String expecting = "global>>T, T>>foo, foo>>x";
		check(input, expecting);
	}

	@Test public void testMethodLocals2() {
		String input =
			"class T [" +
			"    foo [ |x y| ]" +
			"]";
		String expecting = "global>>T, T>>foo, foo>>x, foo>>y";
		check(input, expecting);
	}

	@Test public void testMethodLocals3() {
		String input =
			"class T [" +
			"    foo [ |x y| ]" +
			"    bar [ |a b c| ]" +
			"]";
		String expecting = "global>>T, T>>foo, T>>bar, foo>>x, foo>>y, bar>>a, bar>>b, bar>>c";
		check(input, expecting);
	}

	@Test public void testEmptyMethods() {
		String input =
			"class T [" +
			"    foo [ ]" +
			"    bar [ ]" +
			"]";
		String expecting = "global>>T, T>>foo, T>>bar";
		check(input, expecting);
	}

	@Test public void testInheritance() {
		String input =
			"class T [ ]\n" +
			"class U : T [ ]";
		String expecting = "global>>T, global>>U";
		check(input, expecting);
	}

	@Test public void testInheritanceAllowsUnknownSuper() {
		String input =
			"class T [ ]\n" +
			"class U : XXX [ ]";
		String expecting = "global>>T, global>>U";
		check(input, expecting);
	}

	@Test public void testFieldsAndMethod() {
		String input =
			"class T [" +
			"    | x y | " +
			"    foo [ ]" +
			"    bar [ ]" +
			"]";
		String expecting = "global>>T, T>>x, T>>y, T>>foo, T>>bar";
		check(input, expecting);
	}

	@Test public void testVarsInDiffScopeThanFields() {
		String input =
			"class T [" +
			"    | x y |" +
			"    foo [ |x| ]" +
			"    bar [ |y| ]" +
			"]";
		String expecting = "global>>T, T>>x, T>>y, T>>foo, T>>bar, foo>>x, bar>>y";
		check(input, expecting);
	}

	@Test public void testArgsInDiffScopeThanFields() {
		String input =
			"class T [" +
			"    | x y |" +
			"    foo: x [ ]" +
			"    bar: y [ ]" +
			"]";
		String expecting = "global>>T, T>>x, T>>y, T>>foo:, T>>bar:, foo:>>x, bar:>>y";
		check(input, expecting);
	}

	@Test public void testVarsInSameScopeAsLocals() {
		String input =
			"class T [" +
			"    foo: x [ |y| ]" +
			"    bar: y [ |z| ]" +
			"]";
		String expecting = "global>>T, T>>foo:, T>>bar:, foo:>>x, foo:>>y, bar:>>y, bar:>>z";
		check(input, expecting);
	}

	@Test public void testArgNestedVarRedef() {
		String input =
			"class T [" +
			"    at: x [ [|x|] ]" +
			"]";
		String expecting = "global>>T, T>>at:, at:>>x, at:>>at:-block0, at:-block0>>x";
		check(input, expecting);
	}

	@Test public void testArgNestedVarRedefIn2Blocks() {
		String input =
			"class T [" +
			"    at: x [ [|x|]. [|x|] ]" +
			"]";
		String expecting = "global>>T, T>>at:, at:>>x, at:>>at:-block0, at:>>at:-block1, at:-block0>>x, at:-block1>>x";
		check(input, expecting);
	}

	@Test public void test2ClassesWithSameMethod() {
		String input =
			"class T [ foo [] ]\n" +
			"class U [ foo [] ]";
		String expecting = "global>>T, global>>U, T>>foo, U>>foo";
		check(input, expecting);
	}

	@Test public void test2ClassesWithSameFields() {
		String input =
			"class T [ |x y| ]\n" +
			"class U [ |y x| ]";
		String expecting = "global>>T, global>>U, T>>x, T>>y, U>>y, U>>x";
		check(input, expecting);
	}

	public void check(String input, String expecting) {
		GlobalScope globals = parseAndGetGlobalScope(input);
		String result = globals.toTestString(", ", ">>");
		assertEquals(expecting, result);
	}
}
