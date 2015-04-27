package smalltalk.compiler;

import org.antlr.symtab.GlobalScope;
import org.antlr.symtab.Symbol;

public class STSymbolTable {
	public final GlobalScope GLOBALS;

	public STSymbolTable() {
		this.GLOBALS = new GlobalScope(null);
	}

	public void defineGlobalSymbol(Symbol s) {
		this.GLOBALS.define(s);
	}
}
