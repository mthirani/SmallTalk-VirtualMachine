package smalltalk.compiler;

import org.antlr.symtab.ParameterSymbol;

public class STArg extends ParameterSymbol {
	public STArg(String name) {
		super(name);
	}

	@Override
	public String toString() {
		return getName();
	}
}
