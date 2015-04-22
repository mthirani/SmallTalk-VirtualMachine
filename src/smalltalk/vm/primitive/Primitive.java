package smalltalk.vm.primitive;

import smalltalk.vm.VirtualMachine;

/** This is the list of primitive methods (like "native" in java
 *  speak) that SmallTalk code can invoke.  For example, in image.st
 *  you'll see:
 *
 * 		Integer superClass: #Number [
 *  		+ y <primitive:#Integer_ADD>
 * 			...
 * 		]
 *
 *  The VM executes PrimitiveMethod.perform(..., Integer_ADD) upon
 *  "+" message, which then (almost always) routes to a perform() in
 *  an implementation object like STArray.
 *
 *  To define a new primitive, add enum element here pointing to static
 *  perform method.  The perform() method returns a value that {@link
 *  smalltalk.vm.VirtualMachine#sendMessage} pushes onto stack if
 *  non-null.
 */
public enum Primitive {
	Object_Class_ERROR(STMetaClassObject::perform),
	Object_Class_BASICNEW(STMetaClassObject::perform), // class method
	Object_PRINT(STObject::perform),
	Object_ASSTRING(STObject::perform),
	Object_CLASSNAME(STObject::perform),
	Object_SAME(STObject::perform),
	Object_HASH(STObject::perform),

	Character_ASINTEGER(STCharacter::perform),
	Character_Class_NEW(STCharacter::perform),

	Integer_ADD(STInteger::perform), // +
	Integer_SUB(STInteger::perform),
	Integer_MULT(STInteger::perform),
	Integer_DIV(STInteger::perform),
	Integer_LT(STInteger::perform),
	Integer_LE(STInteger::perform),
	Integer_GT(STInteger::perform),
	Integer_GE(STInteger::perform),
	Integer_EQ(STInteger::perform),
	Integer_MOD(STInteger::perform),
	Integer_ASFLOAT(STInteger::perform),

	Float_ADD(STFloat::perform), // +
	Float_SUB(STFloat::perform),
	Float_MULT(STFloat::perform),
	Float_DIV(STFloat::perform),
	Float_LT(STFloat::perform),
	Float_LE(STFloat::perform),
	Float_GT(STFloat::perform),
	Float_GE(STFloat::perform),
	Float_EQ(STFloat::perform),
	Float_ASINTEGER(STFloat::perform),

	Boolean_IFTRUE_IFFALSE(STBoolean::perform),
	Boolean_IFTRUE(STBoolean::perform),
	Boolean_NOT(STBoolean::perform),

	String_Class_NEW(STString::perform),
	String_CAT(STString::perform),
	String_EQ(STString::perform),
	String_ASARRAY(STString::perform),

	BlockDescriptor_VALUE(BlockDescriptor::perform),
	BlockDescriptor_VALUE_1_ARG(BlockDescriptor::perform),
	BlockDescriptor_VALUE_2_ARGS(BlockDescriptor::perform),

	Array_Class_NEW(STArray::perform),
	Array_SIZE(STArray::perform),
	Array_AT(STArray::perform),
	Array_AT_PUT(STArray::perform),

	TranscriptStream_SHOW(VirtualMachine::TranscriptStream_SHOW)
	;

	/** Effectively a pointer to a static perform method */
	private final PrimitiveImplPtr performer;

	Primitive(PrimitiveImplPtr performer) {
		this.performer = performer;
	}

	public STObject perform(BlockContext ctx, int nArgs) {
		return performer.perform(ctx, nArgs, this);
	}
}
