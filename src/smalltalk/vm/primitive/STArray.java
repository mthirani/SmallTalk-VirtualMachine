package smalltalk.vm.primitive;

import org.antlr.symtab.Utils;
import smalltalk.vm.VirtualMachine;

import java.util.Arrays;
import java.util.List;

/** */
public class STArray extends STObject {
	public STObject[] elements;		//final

	STArray(VirtualMachine vm, int n, STObject fill) {
		super(vm.lookupClass("Array"));
	}

	public static STObject perform(BlockContext ctx, int nArgs, Primitive primitive) {
		return null;
	}

	@Override
	public String toString() {
		return "";
	}
}
