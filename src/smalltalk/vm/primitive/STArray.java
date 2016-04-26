package smalltalk.vm.primitive;

import org.antlr.symtab.Utils;
import smalltalk.vm.VirtualMachine;

import java.util.Arrays;
import java.util.List;

/** */
public class STArray extends STObject {
	public STObject[] elements;		//final

	public STArray(VirtualMachine vm, int n, STObject fill) {
		super(vm.lookupClass("Array"));
		elements = new STObject[n];
		STString s1 = (STString) fill;
		for(int i=0; i<n; i++){
			elements[i] = new STCharacter(vm, s1.s.charAt(i));
		}
	}

	public STArray(VirtualMachine vm, int n) {
		super(vm.lookupClass("Array"));
		elements = new STObject[n];
		for(int i=1; i<=n; i++){
			elements[i-1] = vm.ctx.stack[vm.ctx.sp - n + i];
		}
	}

	public STArray(VirtualMachine vm, int n, boolean args) {
		super(vm.lookupClass("Array"));
		if(!args){
			elements = new STObject[10];
			for(int i=0; i<=9; i++){
				elements[i] = vm.nil();
			}
		}
		else{
			elements = new STObject[n];
			for(int i=0; i<n; i++){
				elements[i] = vm.nil();
			}
		}
	}

	public static STObject perform(BlockContext ctx, int nArgs, Primitive primitive) {
		VirtualMachine vm = ctx.vm;
		vm.assertNumOperands(nArgs+1);
		int firstArg = ctx.sp - nArgs + 1;
		STObject receiver = ctx.stack[firstArg-1];
		STObject result = null;
		switch ( primitive ) {
			case Object_ASSTRING:
				ctx.sp--; // pop receiver
				result = receiver.asString();
				break;
			case Array_Class_NEW:
				if(nArgs == 0){
					ctx.sp--;
					result = new STArray(vm, nArgs, false);
				}
				else{
					if(ctx.stack[firstArg] instanceof STInteger){
						STInteger ropnd = (STInteger) ctx.stack[firstArg];
						ctx.sp -= 2;
						result = new STArray(vm, ropnd.v, true);
					}else if(ctx.stack[firstArg] instanceof STString){
						STString ropnd = (STString) ctx.stack[firstArg];
						ctx.sp -= 2;
						result = new STArray(vm, Integer.parseInt(ropnd.s), true);
					}
				}
				break;
			case Array_SIZE:
				ctx.sp--;
				STArray ropnd = (STArray) receiver;
				int count = ropnd.elements.length;
				result = new STInteger(vm, count);
				break;
			case Array_AT:
				STArray rAt = (STArray) receiver;
				STInteger indexAt = (STInteger) ctx.stack[firstArg];		//fetch the first argument from stack which will be the index to put the value in
				result = rAt.elements[indexAt.v - 1];
				ctx.sp -= 2;
				break;
			case Array_AT_PUT:
				STArray r = (STArray) receiver;
				STInteger index = (STInteger) ctx.stack[firstArg];		//fetch the first argument from stack which will be the index to put the value in
				r.elements[index.v - 1] = ctx.stack[firstArg + 1];
				result = r;
				ctx.sp -= 3;
				break;
		}
		return result;
	}

	@Override
	public String toString() {
		int count = 1;
		String returnState = "{";
		for(STObject stObject: elements){
			if(count == elements.length)
				returnState = returnState + stObject;
			else
				returnState = returnState + stObject + ". ";
			count++;
		}
		returnState = returnState + "}";

		return returnState;
	}
}
