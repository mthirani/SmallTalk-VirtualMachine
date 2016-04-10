package smalltalk.vm.primitive;

import smalltalk.vm.VirtualMachine;

/** */
public class STBoolean extends STObject {
	public final boolean b;

	public STBoolean(VirtualMachine vm, boolean b) {
		super(vm.lookupClass("Boolean"));
		this.b = b;
	}

	public static STObject perform(BlockContext ctx, int nArgs, Primitive primitive) {
		return null;
	}

	@Override
	public String toString() {
		return String.valueOf(b);
	}
}
