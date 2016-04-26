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
		vm.assertNumOperands(nArgs+1);
		int firstArg = ctx.sp - nArgs + 1;
		STObject receiverObj = ctx.stack[firstArg - 1];
		STString receiver = (STString)receiverObj;
		STObject result = vm.nil();
		String s;
		STObject ropnd;
		switch ( primitive ) {
			case String_CAT:
				ropnd = ctx.stack[firstArg];
				ctx.sp -= 2;
				s = receiver.s + new STString(vm, ropnd.toString());
				result = new STString(vm, s);
				break;
			case String_ASARRAY:
				ropnd = receiver;
				ctx.sp--;
				result = new STArray(vm, receiver.s.length(), ropnd);
				break;
			case String_EQ:
				STObject x = receiver;
				STObject y = ctx.stack[firstArg];
				ctx.sp -= 2;
				if(x.toString().equals(y.toString()))
					result = vm.newBoolean(true);
				else
					result = vm.newBoolean(false);
				break;
			case String_Class_NEW:
				STObject recv = receiver;
				STObject arg = ctx.stack[firstArg];
				ctx.sp -= 2;
				if(arg instanceof STString)
					result = new STString(vm, ((STString) arg).s);
				else if(arg instanceof STCharacter){
					int val = ((STCharacter) arg).c;
					result = new STString(vm, ((char) val));
				}
				break;
		}
		return result;
	}

	public STString asString() { return this; }
	public String toString() { return s; }
}
