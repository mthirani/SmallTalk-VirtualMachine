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
		STObject result = vm.nil();
		int v;
		STObject ropnd;
		switch ( primitive ) {
			case Integer_ADD:
				ropnd = ctx.stack[firstArg]; // get right operand (first arg)
				ctx.sp--; // pop ropnd
				ctx.sp--; // pop receiver
				if(receiverObj instanceof STCharacter){
					v = ((STCharacter) receiverObj).c + ((STInteger)ropnd).v;
					result = new STCharacter(vm, v);
				}
				else{
					if(receiverObj instanceof STString){
						int rec = Integer.parseInt(((STString) receiverObj).s);
						v = rec + ((STInteger)ropnd).v;
						STInteger st = new STInteger(vm, v);
						result = new STString(vm, st.toString());
					}
					else{
						STInteger receiver = (STInteger)receiverObj;
						v = receiver.v + ((STInteger)ropnd).v;
						result = new STInteger(vm, v);
					}
				}
				break;
			case Integer_SUB:
				STInteger receiverSub = (STInteger)receiverObj;
				ropnd = ctx.stack[firstArg]; // get right operand (first arg)
				ctx.sp--; // pop ropnd
				ctx.sp--; // pop receiver
				v = receiverSub.v - ((STInteger)ropnd).v;
				result = new STInteger(vm, v);
				break;
			case Integer_MULT:
				STInteger receiverMul = (STInteger)receiverObj;
				ropnd = ctx.stack[firstArg]; // get right operand (first arg)
				ctx.sp--; // pop ropnd
				ctx.sp--; // pop receiver
				v = receiverMul.v * ((STInteger)ropnd).v;
				result = new STInteger(vm, v);
				break;
			case Integer_DIV:
				STInteger receiverDiv = (STInteger)receiverObj;
				ropnd = ctx.stack[firstArg]; // get right operand (first arg)
				ctx.sp--; // pop ropnd
				ctx.sp--; // pop receiver
				v = receiverDiv.v / ((STInteger)ropnd).v;
				result = new STInteger(vm, v);
				break;
			case Integer_MOD:
				STInteger receiverMod = (STInteger)receiverObj;
				ropnd = ctx.stack[firstArg]; // get right operand (first arg)
				ctx.sp--; // pop ropnd
				ctx.sp--; // pop receiver
				v = receiverMod.v % ((STInteger)ropnd).v;
				result = new STInteger(vm, v);
				break;
			case Integer_EQ:
				STInteger receiverEq = (STInteger)receiverObj;
				ropnd = ctx.stack[firstArg]; // get right operand (first arg)
				ctx.sp--; // pop ropnd
				ctx.sp--; // pop receiver
				result = new STBoolean(vm, receiverEq.v == ((STInteger)ropnd).v);
				break;
			case Integer_LT:
				STInteger receiverLt = (STInteger)receiverObj;
				ropnd = ctx.stack[firstArg]; // get right operand (first arg)
				ctx.sp--; // pop ropnd
				ctx.sp--; // pop receiver
				result = new STBoolean(vm, receiverLt.v < ((STInteger)ropnd).v);
				break;
			case Integer_LE:
				STInteger receiverLe = (STInteger)receiverObj;
				ropnd = ctx.stack[firstArg]; // get right operand (first arg)
				ctx.sp--; // pop ropnd
				ctx.sp--; // pop receiver
				if(ropnd instanceof STInteger)
					result = new STBoolean(vm, receiverLe.v <= ((STInteger)ropnd).v);
				else if(ropnd instanceof STString){
					STString firstArgs = (STString) ropnd;
					result = new STBoolean(vm, receiverLe.v <= Integer.parseInt(firstArgs.s));
				}
				break;
			case Integer_GT:
				STInteger receiverGt = (STInteger)receiverObj;
				ropnd = ctx.stack[firstArg]; // get right operand (first arg)
				ctx.sp--; // pop ropnd
				ctx.sp--; // pop receiver
				result = new STBoolean(vm, receiverGt.v > ((STInteger)ropnd).v);
				break;
			case Integer_GE:
				STInteger receiverGe = (STInteger)receiverObj;
				ropnd = ctx.stack[firstArg]; // get right operand (first arg)
				ctx.sp--; // pop ropnd
				ctx.sp--; // pop receiver
				result = new STBoolean(vm, receiverGe.v >= ((STInteger)ropnd).v);
				break;
		}
		return result;
	}

	@Override
	public String toString() {
		return String.valueOf(v);
	}
}
