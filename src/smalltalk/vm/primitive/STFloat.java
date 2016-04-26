package smalltalk.vm.primitive;

import smalltalk.vm.VirtualMachine;

import java.text.DecimalFormat;

/** Backing class for Smalltalk Float. */
public class STFloat extends STObject {
	public final float v;

	public STFloat(VirtualMachine vm, float v) {
		super(vm.lookupClass("Float"));
		this.v = v;
	}

	public static STObject perform(BlockContext ctx, int nArgs, Primitive primitive) {
		VirtualMachine vm = ctx.vm;
		int firstArg = ctx.sp - nArgs + 1;
		STObject receiverObj = ctx.stack[firstArg - 1];
		STObject result = vm.nil();
		float v;
		STObject ropnd;
		switch ( primitive ) {
			case Float_ADD:
				ropnd = ctx.stack[firstArg]; // get right operand (first arg)
				ctx.sp--; // pop ropnd
				ctx.sp--; // pop receiver
				STFloat receiver = (STFloat)receiverObj;
				v = receiver.v + ((STFloat)ropnd).v;
				result = new STFloat(vm, v);
				break;
			case Float_SUB:
				STFloat receiverSub = (STFloat)receiverObj;
				ropnd = ctx.stack[firstArg]; // get right operand (first arg)
				ctx.sp--; // pop ropnd
				ctx.sp--; // pop receiver
				v = receiverSub.v - ((STFloat)ropnd).v;
				result = new STFloat(vm, v);
				break;
			case Float_MULT:
				STFloat receiverMul = (STFloat)receiverObj;
				ropnd = ctx.stack[firstArg]; // get right operand (first arg)
				ctx.sp--; // pop ropnd
				ctx.sp--; // pop receiver
				v = receiverMul.v * ((STFloat)ropnd).v;
				result = new STFloat(vm, v);
				break;
			case Float_DIV:
				STFloat receiverDiv = (STFloat)receiverObj;
				ropnd = ctx.stack[firstArg]; // get right operand (first arg)
				ctx.sp--; // pop ropnd
				ctx.sp--; // pop receiver
				v = receiverDiv.v / ((STFloat)ropnd).v;
				result = new STFloat(vm, v);
				break;
			case Float_EQ:
				STFloat receiverEq = (STFloat)receiverObj;
				ropnd = ctx.stack[firstArg]; // get right operand (first arg)
				ctx.sp--; // pop ropnd
				ctx.sp--; // pop receiver
				result = new STBoolean(vm, receiverEq.v == ((STFloat)ropnd).v);
				break;
			case Float_LT:
				STFloat receiverLt = (STFloat)receiverObj;
				ropnd = ctx.stack[firstArg]; // get right operand (first arg)
				ctx.sp--; // pop ropnd
				ctx.sp--; // pop receiver
				result = new STBoolean(vm, receiverLt.v < ((STFloat)ropnd).v);
				break;
			case Float_LE:
				STFloat receiverLe = (STFloat)receiverObj;
				ropnd = ctx.stack[firstArg]; // get right operand (first arg)
				ctx.sp--; // pop ropnd
				ctx.sp--; // pop receiver
				result = new STBoolean(vm, receiverLe.v <= ((STFloat)ropnd).v);
				break;
			case Float_GT:
				STFloat receiverGt = (STFloat)receiverObj;
				ropnd = ctx.stack[firstArg]; // get right operand (first arg)
				ctx.sp--; // pop ropnd
				ctx.sp--; // pop receiver
				result = new STBoolean(vm, receiverGt.v > ((STFloat)ropnd).v);
				break;
			case Float_GE:
				STFloat receiverGe = (STFloat)receiverObj;
				ropnd = ctx.stack[firstArg]; // get right operand (first arg)
				ctx.sp--; // pop ropnd
				ctx.sp--; // pop receiver
				result = new STBoolean(vm, receiverGe.v >= ((STFloat)ropnd).v);
				break;
			case Object_ASSTRING:
				STFloat receiver1 = (STFloat)receiverObj;
				ctx.sp--;
				result = receiver1.asString();
				break;
		}
		return result;
	}

	@Override
	public String toString() {
		DecimalFormat df = new DecimalFormat("#.#####");
		return df.format(v);
	}
}
