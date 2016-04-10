package smalltalk.vm.primitive;

import smalltalk.vm.VirtualMachine;

/** A backing object for smalltalk integers */
public class STInteger extends STObject {
	public final int v;

	public STInteger(VirtualMachine vm, int v) {
		super(vm.lookupClass("Integer"));
		this.v = v;
	}

	public static STObject perform(BlockContext ctx, int nArgs, Primitive primitive) {
		VirtualMachine vm = ctx.vm;
		int firstArg = ctx.sp - nArgs + 1;
		STObject receiverObj = ctx.stack[firstArg - 1];
		STInteger receiver = (STInteger)receiverObj;
		STObject result = vm.nil();
		int v;
		STObject ropnd;
		switch ( primitive ) {
			case Integer_ADD:
				ropnd = ctx.stack[firstArg]; // get right operand (first arg)
				ctx.sp--; // pop ropnd
				ctx.sp--; // pop receiver
				v = receiver.v + ((STInteger)ropnd).v;
				result = new STInteger(vm, v);
				break;
			case Integer_SUB:
				ropnd = ctx.stack[firstArg]; // get right operand (first arg)
				ctx.sp--; // pop ropnd
				ctx.sp--; // pop receiver
				v = receiver.v - ((STInteger)ropnd).v;
				result = new STInteger(vm, v);
				break;
			case Integer_EQ:
				ropnd = ctx.stack[firstArg]; // get right operand (first arg)
				ctx.sp--; // pop ropnd
				ctx.sp--; // pop receiver
				result = new STBoolean(vm, receiver.v == ((STInteger)ropnd).v);
				break;
		}
		return result;
	}

	@Override
	public String toString() {
		return String.valueOf(v);
	}
}
