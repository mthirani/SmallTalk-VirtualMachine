package smalltalk.compiler;

import org.antlr.symtab.Symbol;
import org.antlr.symtab.VariableSymbol;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;

/** Set the symbol references in the parse tree nodes for ID and lvalues.
 *  Check that the left-hand side of assignments are variables. Other
 *  unknown symbols could simply be references to type names that will
 *  be compiled later. Mostly done to verify scopes/symbols in
 *  {@link smalltalk.test.TestIDLookup}.
 */
public class ResolveSymbols extends SetScope {
	public ResolveSymbols(Compiler compiler) {
		super(compiler);
	}

	@Override
	public void enterId(@NotNull SmalltalkParser.IdContext ctx) {
		ctx.sym = currentScope.resolve(ctx.getStart().getText());
	}

	@Override
	public void enterLvalue(@NotNull SmalltalkParser.LvalueContext ctx) {
		ctx.sym = checkIDExists(ctx.getStart());
	}

	public VariableSymbol checkIDExists(Token ID) {
		Symbol sym = currentScope.resolve(ID.getText());
		if ( sym==null ) {
			compiler.error("unknown variable "+ID.getText()+" in "+currentScope.toQualifierString(">>"));
			// we assume it's a classname if not on left hand size
			return null;
		}
		if ( !(sym instanceof VariableSymbol) ) {
			compiler.error("symbol "+ID.getText()+
							   " is not a variable/argument in "+
							   currentScope.toQualifierString(">>"));
			sym = null;
		}
		return (VariableSymbol)sym;
	}
}
