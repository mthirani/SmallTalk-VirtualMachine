package smalltalk.vm;

import smalltalk.compiler.STSymbolTable;
import smalltalk.vm.exceptions.BlockCannotReturn;
import smalltalk.vm.exceptions.IndexOutOfRange;
import smalltalk.vm.exceptions.InternalVMException;
import smalltalk.vm.exceptions.MessageNotUnderstood;
import smalltalk.vm.exceptions.MismatchedBlockArg;
import smalltalk.vm.exceptions.StackUnderflow;
import smalltalk.vm.exceptions.TypeError;
import smalltalk.vm.exceptions.UndefinedGlobal;
import smalltalk.vm.exceptions.UnknownClass;
import smalltalk.vm.exceptions.UnknownField;
import smalltalk.vm.exceptions.VMException;
import smalltalk.vm.primitive.BlockContext;
import smalltalk.vm.primitive.BlockDescriptor;
import smalltalk.vm.primitive.Primitive;
import smalltalk.vm.primitive.STArray;
import smalltalk.vm.primitive.STBoolean;
import smalltalk.vm.primitive.STCompiledBlock;
import smalltalk.vm.primitive.STFloat;
import smalltalk.vm.primitive.STInteger;
import smalltalk.vm.primitive.STMetaClassObject;
import smalltalk.vm.primitive.STNil;
import smalltalk.vm.primitive.STObject;
import smalltalk.vm.primitive.STString;

/** A VM for a subset of Smalltalk.
 *
 *  3 HUGE simplicity factors in this implementation: we ignore GC,
 *  efficiency, and don't expose execution contexts to smalltalk programmers.
 *
 *  Because of the shared {@link SystemDictionary#objects} list (ThreadLocal)
 *  in SystemDictionary, each VirtualMachine must run in its own thread
 *  if you want multiple.
 */
public class VirtualMachine {
	/** The dictionary of global objects including class meta objects */
	public final SystemDictionary systemDict; // singleton

	/** "This is the active context itself. It is either a BlockContext
	 *  or a BlockContext." BlueBook p 605 in pdf.
	 */
	public BlockContext ctx;

	/** Trace instructions and show stack during exec? */
	public boolean trace = false;

	public VirtualMachine(STSymbolTable symtab) {
		// create system dictionary and predefined nil, true, false, Transcript
		// convert symbol table ClassSymbols to STMetaClassObjects
	}

	/** look up MainClass>>main and execute it */
	public STObject execMain() {
		// ...
		return exec(mainObject,main);
	}

	/** Begin execution of the bytecodes in method relative to a receiver
	 *  (self) and within a particular VM. exec() creates an initial
	 *  method context to simulate a call to the method passed in.
	 *
	 *  Return the value left on the stack after invoking the method,
	 *  or return self/receiver if there's nothing on the stack.
	 */
	public STObject exec(STObject self, STCompiledBlock method) {
		ctx = null;
		BlockContext initialContext = new BlockContext(this, method, self);
		pushContext(initialContext);
		// fetch-decode-execute loop
		while ( true ) {
			if ( trace ) traceInstr(); // show instr first then stack after to show results
			int op = 0; // ...
			switch ( op ) {
				case Bytecode.NIL:
					break;
			}
			if ( trace ) traceStack(); // show stack *after* execution
		}
		return ctx!=null ? ctx.receiver : null;
	}

	public void error(String type, String msg) throws VMException {
		error(type, null, msg);
	}

	public void error(String type, Exception e, String msg) throws VMException {
		String stack = getVMStackString();
		switch ( type ) {
			case "MessageNotUnderstood":
				throw new MessageNotUnderstood(msg,stack);
			case "IndexOutOfRange":
				throw new IndexOutOfRange(msg,stack);
			case "BlockCannotReturn":
				throw new BlockCannotReturn(msg,stack);
			case "StackUnderflow":
				throw new StackUnderflow(msg,stack);
			case "UndefinedGlobal":
				throw new UndefinedGlobal(msg,stack);
			case "MismatchedBlockArg":
				throw new MismatchedBlockArg(msg,stack);
			case "InternalVMException":
				throw new InternalVMException(e,msg,stack);
			case "UnknownClass":
				throw new UnknownClass(msg,stack);
			case "TypeError":
				throw new TypeError(msg,stack);
			case "UnknownField":
				throw new UnknownField(msg,stack);
			default :
				throw new VMException(msg,stack);
		}
	}

	public void error(String msg) throws VMException {
		error("unknown", msg);
	}

	public void pushContext(BlockContext ctx) {
		ctx.invokingContext = this.ctx;
		this.ctx = ctx;
	}

	public void popContext() { ctx = ctx.invokingContext; }

	public static STObject TranscriptStream_SHOW(BlockContext ctx, int nArgs, Primitive primitive) {
		VirtualMachine vm = ctx.vm;
		vm.assertNumOperands(nArgs + 1); // ensure args + receiver
		int firstArg = ctx.sp - nArgs + 1;
		STObject receiverObj = ctx.stack[firstArg - 1];
		vm.assertEqualBackingTypes(receiverObj, "TranscriptStream");
		STObject arg = ctx.stack[firstArg];
		System.out.println(arg.asString());
		ctx.sp -= nArgs + 1; // pop receiver and arg
		return receiverObj;  // leave receiver on stack for primitive methods
	}

	public STMetaClassObject lookupClass(String id) {
		return systemDict.lookupClass(id);
	}

	public STObject newInstance(String className, Object ctorArg) {
		return null;
	}

	public STObject newInstance(STMetaClassObject metaclass, Object ctorArg) {
		return null;
	}

	public STObject newInstance(STMetaClassObject metaclass) {
		return null;
	}

	public STInteger newInteger(int v) {
		return null;
	}

	public STFloat newFloat(float v) {
		return null;
	}

	public STString newString(String s) {
		return null;
	}

	public STBoolean newBoolean(boolean b) {
		return null;
	}

	public STNil nil() {
		return new STNil(this);
	}

	// D e b u g g i n g

	void trace() {
		traceInstr();
		traceStack();
	}

	void traceInstr() {
		String instr = Bytecode.disassembleInstruction(ctx.compiledBlock, ctx.ip);
		System.out.printf("%-40s", instr);
	}

	void traceStack() {
		BlockContext c = ctx;
		List<String> a = new ArrayList<>();
		while ( c!=null ) {
			a.add( c.toString() );
			c = c.invokingContext;
		}
		System.out.println(Utils.join(a,", "));
	}

	public String getVMStackString() {
		StringBuilder stack = new StringBuilder();
		BlockContext c = ctx;
		while ( c!=null ) {
			int ip = c.prev_ip;
			if ( ip<0 ) ip = c.ip;
			String instr = Bytecode.disassembleInstruction(c.compiledBlock, ip);
			String location = c.currentFile+":"+c.currentLine+":"+c.currentCharPos;
			String mctx = c.compiledBlock.qualifiedName + pLocals(c) + pContextWorkStack(c);
			String s = String.format("    at %50s%-20s executing %s\n",
									 mctx,
									 String.format("(%s)",location),
									 instr);
			stack.append(s);
			c = c.invokingContext;
		}
		return stack.toString();
	}

	String pContextWorkStack(BlockContext ctx) {
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		for (int i=0; i<=ctx.sp; i++) {
			if ( i>0 ) buf.append(", ");
			pValue(buf, ctx.stack[i]);
		}
		buf.append("]");
		return buf.toString();
	}

	String pLocals(BlockContext ctx) {
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		for (int i=0; i<ctx.locals.length; i++) {
			if ( i>0 ) buf.append(", ");
			pValue(buf, ctx.locals[i]);
		}
		buf.append("]");
		return buf.toString();
	}

	void pValue(StringBuilder buf, STObject v) {
		if ( v==null ) buf.append("null");
		else if ( v==nil() ) buf.append("nil");
		else if ( v instanceof STString) buf.append("'"+v.asString()+"'");
		else if ( v instanceof BlockDescriptor) {
			BlockDescriptor blk = (BlockDescriptor) v;
			buf.append(blk.block.name);
		}
		else if ( v instanceof STMetaClassObject ) {
			buf.append(v.toString());
		}
		else {
			STObject r = v.asString(); //getAsString(v);
			buf.append(r.toString());
		}
	}
}
