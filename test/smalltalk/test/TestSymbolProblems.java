package smalltalk.test;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class TestSymbolProblems extends BaseTest {
	@Test public void testClassRedef() {
		String input =
		"class T [ ]" +
		"class T [ ]";
		String expecting = "global>>T";
		String[] errors = {"redefinition of T"};
		check(input, expecting, errors);
	}

	@Test public void testMethodRedef() {
		String input =
		"class T [" +
		"    foo [ ]" +
		"    g [ ]" +
		"    foo [ ]" +
		"]";
		String expecting = "global>>T, T>>foo, T>>g";
		String[] errors = {"redefinition of method foo in global>>T"};
		check(input, expecting, errors);
	}

	@Test public void testArgRedef() {
		String input =
		"class T [" +
		"    at: x put: x [ ]" +
		"]";
		String expecting = "global>>T, T>>at:put:, at:put:>>x";
		String[] errors = {"redefinition of x in global>>T>>at:put:"};
		check(input, expecting, errors);
	}

	@Test public void testVarRedef() {
		String input =
		"class T [" +
		"    f [ |x x| ]" +
		"]";
		String expecting = "global>>T, T>>f, f>>x";
		String[] errors = {"redefinition of x in global>>T>>f"};
		check(input, expecting, errors);
	}

	@Test public void testArgVarRedef() {
		String input =
		"class T [" +
		"    at: x [ |x| ]" +
		"]";
		String expecting = "global>>T, T>>at:, at:>>x";
		String[] errors = {"redefinition of x in global>>T>>at:"};
		check(input, expecting, errors);
	}

	@Test public void testNestedVarRedef() {
		String input =
		"class T [" +
		"    at: x [ [|x x|] ]" +
		"]";
		String expecting = "global>>T, T>>at:, at:>>x, at:>>at:-block0, at:-block0>>x";
		String[] errors = {"redefinition of x in global>>T>>at:>>at:-block0"};
		check(input, expecting, errors);
	}

	@Test public void testUnknownAssignVar() {
		String input =
		"x := 1.";
		String expecting = "global>>MainClass, MainClass>>main";
		String[] errors = {"unknown variable x in global>>MainClass>>main"};
		check(input, expecting, errors);
	}

	@Test public void testUnknownAssignVarInFunc() {
		String input =
		"class T [" +
		"    f [ x := 1. ]" +
		"]";
		String expecting = "global>>T, T>>f";
		String[] errors = {"unknown variable x in global>>T>>f"};
		check(input, expecting, errors);
	}

	@Test public void testUnknownAssignVarInFuncBlock() {
		String input =
		"class T [" +
		"    f [ [x := 1.] ]" +
		"]";
		String expecting = "global>>T, T>>f, f>>f-block0";
		String[] errors = {"unknown variable x in global>>T>>f>>f-block0"};
		check(input, expecting, errors);
	}

	@Test public void testUnknownSymInExprInFuncBlock() {
		String input =
		"class T [" +
		"    f [ |x| [x := y.] ]" + // y might be global class later
		"]";
		String expecting = "global>>T, T>>f, f>>x, f>>f-block0";
		String[] errors = {};
		check(input, expecting, errors);
	}

	public void check(String input, String expecting, String[] errors) {
		CompilerWithHooks compiler = parseAndDefineSymbols(input);
		String result = compiler.getSymbolTable().GLOBALS.toTestString(", ", ">>");
		assertEquals(expecting, result);
		assertEquals(Arrays.asList(errors), compiler.errors);
	}
}
