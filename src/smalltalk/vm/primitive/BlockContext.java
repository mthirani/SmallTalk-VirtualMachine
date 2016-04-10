package smalltalk.vm.primitive;

import smalltalk.vm.VirtualMachine;

/** "The interpreter uses contexts to represent the state of its execution of
 *   CompiledMethods and blocks. A context can be a MethodContext or a
 *   BlockContext. A MethodContext represents the execution of a
 *   CompiledMethod that was invoked by a message." --Bluebook, page 580.
 *   http://stephane.ducasse.free.fr/FreeBooks/BlueBook/Bluebook.pdf
 *
 *  Like Pharo, but unlike the original Smalltalk, we use a single context
 *  object, BlockContext, to represent both block and method context. It's
 *  just simpler. Pharo uses MethodContext but we use BlockContext (after
 *  much deliberation!)
 *
 *  This object represents the execution of a {@link STCompiledBlock} in response
 *  to a message send. This context holds the locals (with arguments first)
 *  and operand stack for computation.  It knows where it is within the method
 *  code block (field ip) and what receiver it's executing the method for.
 *
 *  The VM does not have an explicit invocation stack. The implicit invocation
 *  stack follows the invokingContext chain upwards, just like we
 *  do when resolving symbols.
 *
 *  "A Little SmallTalk" uses Context and (poorly named) Interpreter objects
 *  to represent the execution context of a method. see pdf page 148.
 *  http://sdmeta.gforge.inria.fr/FreeBooks/LittleSmalltalk/ALittleSmalltalk.pdf
 *
 *  This object is not exposed to ST programs as an STObject, as is typical,
 *  for simplicity reasons.
 */
public class BlockContext {
	public static final int INITIAL_STACK_SIZE = 10;

	/** An indicator object that indicates a method has already returned
	 *  so that we can return again later from inside a closure that is
	 *  hanging around somewhere.
	 */
	public static final BlockContext RETURNED = new BlockContext();

	/** In what VM am I executing? */
	public VirtualMachine vm;		//final

	// ----- STATE OF EXECUTION -----

	/** The context that was active when this context was created; i.e.,
	 *  who invoked me? This is not final because we need to set this to
	 *  RETURNED after this context finishes.
	 */
	public BlockContext invokingContext;

	/** The receiver of the message that resulted in this context */
	public STObject receiver;		//final

	/** The compiled code associated with this context */
	public STCompiledBlock compiledBlock;		//final

	/** All arguments and local variables associated with this block */
	public STObject[] locals;		//final

	/** The instruction pointer that points into compiledBlock.bytcodes */
	public int ip = 0;

	/** The operand stack for this context */
	public STObject[] stack;

	/** The operand stack pointer for this context; points at stack top */
	public int sp = -1;

	// ----- BLOCK CONTEXT INFO -----

	/*  The following two fields are used only by block executions.
	 *
	 *  "A BlockContext represents a block encountered in a CompiledMethod
	 *   [and evaluated]. A BlockContext refers to the MethodContext whose
	 *   CompiledMethod contained the block it represents." -- bluebook p 581 in pdf.
	 *  http://stephane.ducasse.free.fr/FreeBooks/BlueBook/Bluebook.pdf
	 *
	 *  Because blocks are embedded within methods/blocks, this
	 *  object knows which block or method surrounds it and
	 *  also knows the outermost surrounding context: the surrounding
	 *  method. For example, the following method has two embedded
	 *  blocks.
	 *
	 *  foo [
	 *     [ "block 1 starts"
	 *        [^99 "block 2"] value
	 *     ] value
	 *  ]
	 *
	 *  The outermost context is the BlockContext we create for foo when
	 *  it's invoked. Block [[^99] value] has both enclosingContext and
	 *  enclosingMethodContext (both point at the foo context). The
	 *  innermost block, [^99], has:
	 *  	1.  enclosingContext pointing at the (block) BlockContext created
	 *         	during evaluation of block [[^99] value]
	 *      2.  enclosingMethodContext pointing at the foo BlockContext
	 *
	 *  A (block) context object is created from a {@link BlockDescriptor}
	 *  when we hit it with a "value" message to evaluate itself.
	 *
	 *  See bluebook p 580+ in pdf.
	 *  http://stephane.ducasse.free.fr/FreeBooks/BlueBook/Bluebook.pdf
	 *
	 *  See http://pharobooks.gforge.inria.fr/PharoByExampleTwo-Eng/latest/Block.pdf
	 *  See http://sdmeta.gforge.inria.fr/FreeBooks/LittleSmalltalk/ALittleSmalltalk.pdf
	 */

	/** The immediately surrounding/enclosing method or block.
	 *  If this block is a block within the outermost block (i.e., the method),
	 *  the enclosing context is the same as the enclosing method context.
	 *
	 *  This is not the invokingContext, which says who called us.
	 *
	 *  This is called "caller" by bluebook p580 and
	 *  "creator" by little smalltalk p 192 in pdf
	 */
	public BlockContext enclosingContext;

	/** A shortcut up the enclosingContext chain to the method in which the
	 *  block associated with this context is defined.  We need to locate
	 *  the method context that created us so that we can return properly
	 *  upon METHOD_RETURN bytecode. To perform a return, we unwind the
	 *  stack until we reach one level above enclosingMethodContext and
	 *  then push the return result on that context's stack.
	 *
	 *  Smalltalk code can also create a block and pass it as
	 *  a parameter to some distant method's invocation
	 *  context. If the distant method evaluates the block and the
	 *  block has a method return instruction, it's like an
	 *  exception--we must unroll to the method context that
	 *  created the block and then return from it.  Moreover, if
	 *  the enclosingMethodContext has returned when this block
	 *  tries to return, we still a pointer to the method so we
	 *  can respond appropriately with an error.
	 *
	 *  All enclosingMethodContext for blocks defined within method m
	 *  point at m.
	 *
	 *  This is called "home" by bluebook p580
	 */
	public BlockContext enclosingMethodContext;

	// ----- DEBUGGING (dbg instruction) -----
	public String currentFile;
	public int currentLine;
	public int currentCharPos;
	public int prev_ip = -1; // what was the last instruction? ip points at next to execute not currently executing

	private BlockContext() { // used to just to create RETURNED
		vm = null;
		receiver = null;
		compiledBlock = null;
		locals = null;
	}

	/** Create a context from a STCompiledBlock and a receiver object */
	public BlockContext(VirtualMachine vm, STCompiledBlock compiledBlock, STObject receiver) {
	}

	/** Create a BlockContext from a {@link BlockDescriptor} as a
	 *  result of a "value" message sent to force evaluation. The
	 *  descriptor says which block to execute and knows the
	 *  surrounding context.  All of this is copied to the
	 *  BlockContext.
	 *
	 *  To execute "[...] value", we execute BLOCK followed by
	 *  SEND bytecodes. BLOCK creates a descriptor and pushes it
	 *  onto the stack of the currently active context. SEND looks
	 *  up the "value" selector in the BlockDescriptor and finds a
	 *  primitive method.  The VM does the usual thing and creates
	 *  a method context to invoke the primitive method and starts
	 *  executing the "native" java code of the primitive method
	 *  via performPrimitive().
	 *
	 *  The receiver for the value method is the block descriptor.
	 *  The receiver in the block context must be the receiver of
	 *  the enclosing method context (that defined the block).
	 *  This way methods invoked within a block such as "self
	 *  msg."  interpret self as the right object (the receiver of
	 *  the enclosing method).
	 */
	public BlockContext(VirtualMachine vm, BlockDescriptor descriptor) {
	}

	public void push(STObject o) {
	}
	public STObject pop() { return null; }
	public STObject top() { return null; }

	/** If there is no enclosing context, we must be a method. */
	public boolean isBlock() { return false; }

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		BlockContext c = this;
		STMetaClassObject enclosingClass = c.compiledBlock.enclosingClass;
		String s;
		if ( enclosingClass!=null ) {
			s = enclosingClass.name + ">>" + c.compiledBlock.name + c.vm.pLocals(c) + c.vm.pContextWorkStack(c);
		}
		else {
			s = c.compiledBlock.qualifiedName + c.vm.pLocals(c) + c.vm.pContextWorkStack(c);
		}
		buf.append(s);
		return buf.toString();
	}
}
