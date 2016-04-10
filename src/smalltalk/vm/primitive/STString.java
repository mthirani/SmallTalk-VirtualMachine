package smalltalk.vm.primitive;

import smalltalk.vm.VirtualMachine;

public class STString extends STObject {
	public final String s;

	public STString(VirtualMachine vm, char c) {
		this(vm, String.valueOf(c));
	}

	public STString(VirtualMachine vm, String s) {
		super(vm.lookupClass("String"));
		this.s = s;
	}

	public static STObject perform(BlockContext ctx, int nArgs, Primitive primitive) {
		VirtualMachine vm = ctx.vm;
		vm.assertNumOperands(nArgs+1); // ensure args + receiver
		int firstArg = ctx.sp - nArgs + 1;
		STObject receiverObj = ctx.stack[firstArg - 1];
		STObject result = vm.nil();
		switch ( primitive ) {
		}
		return result;
	}

	public STString asString() { return this; }
	public String toString() { return s; }
}
