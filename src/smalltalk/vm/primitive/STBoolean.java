package smalltalk.vm.primitive;

import smalltalk.compiler.STBlock;
import smalltalk.vm.VirtualMachine;

public class STBoolean extends STObject {
	public final boolean b;

	public STBoolean(VirtualMachine vm, boolean b) {
		super(vm.lookupClass("Boolean"));
		this.b = b;
	}

	public static STObject perform(BlockContext ctx, int nArgs, Primitive primitive) {
		VirtualMachine vm = ctx.vm;
		vm.assertNumOperands(nArgs+1);
		int firstArg = ctx.sp - nArgs + 1;
		STObject receiverObj = ctx.stack[firstArg-1];
		STBoolean receiver = (STBoolean)receiverObj;
		STObject result = vm.nil();
		STObject ropnd;
		switch ( primitive ) {
			case Boolean_IFTRUE:
				ropnd = ctx.stack[firstArg];
				ctx.sp -= 2;										//1 argument and 1 receiver
				if(ropnd instanceof BlockDescriptor && receiver.b){
					vm.pushctx = new BlockContext(vm, (BlockDescriptor) ropnd);
					result = null;
				}
				else
					result = vm.nil();
				break;
			case Boolean_IFTRUE_IFFALSE:
				STObject blk1 = ctx.stack[firstArg];				//Block1 as the first argument
				STObject blk2 = ctx.stack[firstArg + 1];			//Block2 as the second argument
				ctx.sp -= 3;										//2 arguments and 1 receiver
				if(blk1 instanceof BlockDescriptor && receiver.b){					//If Receiver is true then evaluate the first Block
					vm.pushctx = new BlockContext(vm, (BlockDescriptor) blk1);
					result = null;
				}
				else{																//If Receiver is false then evaluate the second Block
					vm.pushctx = new BlockContext(vm, (BlockDescriptor) blk2);
					result = null;
				}
				break;
			case Boolean_NOT:
				ctx.sp--;											//Only 1 receiver which needs to be converted
				if(receiver.b)
					result = vm.newBoolean(false);
				else
					result = vm.newBoolean(true);
				break;
		}
		return result;
	}

	@Override
	public String toString() {
		return String.valueOf(b);
	}
}
