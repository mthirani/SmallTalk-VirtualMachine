package smalltalk.vm.primitive;

import smalltalk.vm.VirtualMachine;
import smalltalk.vm.exceptions.InternalVMException;

/** A Smalltalk instance. All fields initialized to nil.
 *  We combine all fields from all inherited classes into this one.  There is
 *  one STObject for every Smalltalk object.
 *
 *  This is unlike Timothy Budd's impl. See Fig 12.2 page 154 of PDF. He has
 *  a superObject chain.  Each object at depth 3 has 3 actual impl objects,
 *  one per depth.
 */
public class STObject {
	/** What kind of object am I? */
	public final STMetaClassObject metaclass;

	/** Which smalltalk-visible fields are defined all the way up the superclass chain? */
	public STObject[] fields;

	public STObject(STMetaClassObject metaclass) {
		this.metaclass = metaclass;
		fields = null;
	}

	/** Which fields are directly defined? null if no fields */
	public STObject[] getFields() {
		return fields;
	}

	/** What kind of object am I? Analogous to Java's Object.getClass() */
	public STMetaClassObject getSTClass() {
		return metaclass;
	}

	/** Analogous to Java's toString() */
	public STString asString() {
		if ( metaclass==null ) {
			throw new InternalVMException(null, "object "+toString()+" has null metaclass", null);
		}
		return metaclass.vm.newString(toString());
	}

	/** Implement a primitive method in active context ctx.
	 *  A non-null return value should be pushed onto operand stack by the VM.
	 *  Primitive methods do not bother pushing a `BlockContext` object as
	 *  they are executing in Java not Smalltalk.
	 */
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
			case Object_CLASSNAME:
				ctx.sp--;
				result = new STString(vm, receiver.getSTClass().getName());
				break;
			case Object_SAME:
				STObject x = receiver;
				STObject y = ctx.stack[firstArg];
				ctx.sp -= 2;
				result = vm.newBoolean(x.toString().equals(y.toString()));
				break;
			case Object_HASH:
				ctx.sp--;
				result = receiver;
				break;
			case Object_Class_BASICNEW:
				ctx.sp--;
				result = new STObject((STMetaClassObject) receiver);
				break;
			case TranscriptStream_SHOW:
				ctx.sp--;
				result = receiver.asString();
				break;
		}
		return result;
	}

	@Override
	public String toString() {
		if ( metaclass==null ) return "<no classdef>";
		return "a "+metaclass.getName();
	}
}
