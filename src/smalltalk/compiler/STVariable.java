package smalltalk.compiler;

import org.antlr.symtab.VariableSymbol;

public class STVariable extends VariableSymbol {
	public STVariable(String name) {
		super(name);
	}

	@Override
	public String toString() {
		return getName();
	}
}
