package smalltalk.test;

import org.antlr.symtab.Symbol;
import org.antlr.symtab.SymbolWithScope;
import org.antlr.symtab.Utils;
import org.antlr.symtab.VariableSymbol;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.xpath.XPath;
import org.junit.Test;
import smalltalk.compiler.SmalltalkParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestIDLookup extends BaseTest {
	@Test public void testNoRefs() {
		String input =
		"class T [ ]";
		CompilerWithHooks compiler = parseAndDefineSymbols(input);

		String expectingLvalues = "";
		assertEquals(expectingLvalues, getLValues(compiler));

		String expectingRefs = "";
		assertEquals(expectingRefs, getIDRefs(compiler));
	}

	@Test public void testAssignment() {
		String input =
		"|x y| x := y.";
		CompilerWithHooks compiler = parseAndDefineSymbols(input);

		String expectingLvalues = "global>>MainClass>>main>>x";
		assertEquals(expectingLvalues, getLValues(compiler));

		String expectingRefs = "global>>MainClass>>main>>y";
		assertEquals(expectingRefs, getIDRefs(compiler));
	}

	@Test public void testRefToFieldAndLocal() {
		String input =
		"class T [" +
		"    |x|" +
		"    foo [ |y| x := y. ]" +
		"]";
		CompilerWithHooks compiler = parseAndDefineSymbols(input);

		String expectingLvalues = "global>>T>>x";
		assertEquals(expectingLvalues, getLValues(compiler));

		String expectingRefs = "global>>T>>foo>>y";
		assertEquals(expectingRefs, getIDRefs(compiler));
	}

	@Test public void testRefToFieldAndLocalNested() {
		String input =
		"class T [" +
		"    |x|" +
		"    foo [ |y| [x := y.] ]" +
		"]";
		CompilerWithHooks compiler = parseAndDefineSymbols(input);

		String expectingLvalues = "global>>T>>x";
		assertEquals(expectingLvalues, getLValues(compiler));

		String expectingRefs = "global>>T>>foo>>y";
		assertEquals(expectingRefs, getIDRefs(compiler));
	}

	@Test public void testRefToFieldAndLocalAndArgNested() {
		String input =
		"class T [" +
		"    |x|" +
		"    foo: arg [ |y| [x := y + arg.] ]" +
		"]";
		CompilerWithHooks compiler = parseAndDefineSymbols(input);

		String expectingRefs = "global>>T>>foo:>>y, global>>T>>foo:>>arg";
		assertEquals(expectingRefs, getIDRefs(compiler));
	}

	public String getIDRefs(CompilerWithHooks compiler) {
		ParseTree tree = compiler.getFileTree();
		Parser parser = compiler.getParser();
		Collection<ParseTree> idRefsInExprs = XPath.findAll(tree, "//id", parser);
		List<Symbol> vars = new ArrayList<>();
		for (ParseTree idRuleNode : idRefsInExprs) {
			Symbol var = ((SmalltalkParser.IdContext)idRuleNode).sym;
			assertNotNull(var);
			vars.add(var);
		}
		List<String> names = new ArrayList<>();
		for (Symbol v : vars) {
			if ( v instanceof VariableSymbol ) {
				names.add(((VariableSymbol) v).getFullyQualifiedName(">>"));
			}
			else if ( v instanceof SymbolWithScope ) {
				names.add(((SymbolWithScope) v).getFullyQualifiedName(">>"));
			}
		}
		return Utils.join(names, ", ");
	}

	public String getLValues(CompilerWithHooks compiler) {
		ParseTree tree = compiler.getFileTree();
		Parser parser = compiler.getParser();
		Collection<ParseTree> idRefsOnLeftOfAssignment = XPath.findAll(tree, "//lvalue", parser);
		List<VariableSymbol> vars = new ArrayList<>();
		for (ParseTree idRuleNode : idRefsOnLeftOfAssignment) {
			VariableSymbol var = ((SmalltalkParser.LvalueContext)idRuleNode).sym;
			assertNotNull("id Node "+idRuleNode.getText()+" has no symbol pointer", var);
			vars.add(var);
		}
		List<String> names = Utils.map(vars, v -> v.getFullyQualifiedName(">>"));
		return Utils.join(names, ", ");
	}

}
