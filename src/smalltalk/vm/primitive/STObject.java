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
	public final STObject[] fields;

	STObject(STMetaClassObject metaclass) {
		this.metaclass = metaclass;
		fields = null;
		// Create empty slot for each field directly defined by metaclass
		// plus any fields inherited from super class.
		// Note: native backing objects like STBoolean do not have smalltalk-visible fields
		// so nfields == 0 and therefore vm can be null.
	}

	/** Which fields are directly defined? null if no fields */
	public STObject[] getFields() {
		return fields;
	}

	/** What kind of object am I? Analogous to Java's Object.getClass() */
	public STMetaClassObject getSTClass() {
		return null;
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
		vm.assertNumOperands(nArgs+1); // ensure args + receiver
		// index of 1st arg on opnd stack; use only if arg(s) present for primitive
		int firstArg = ctx.sp - nArgs + 1; 
		STObject receiver = ctx.stack[firstArg-1];
		STObject result;
		switch ( primitive ) {
			case Object_ASSTRING:
				ctx.sp--; // pop receiver
				// if asString not overridden in Smalltalk, create an STString
				// from the *java* object's toString(); see STObject.asString()
				result = receiver.asString();
				break;
			case Object_CLASSNAME :
				break;
			case Object_SAME : // SmallTalk = op.  same as == in Java (same object)
				break;
			case Object_HASH:
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
