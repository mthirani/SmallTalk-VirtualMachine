package smalltalk.vm;

import org.antlr.symtab.ClassSymbol;
import org.antlr.symtab.Symbol;
import org.antlr.symtab.Utils;
import org.stringtemplate.v4.ST;
import smalltalk.compiler.*;
import smalltalk.vm.exceptions.*;
import smalltalk.vm.primitive.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

	/* BlockContext created to indicate if there is any pushContext object created by any of the primitive methods */
	public BlockContext pushctx;

	/** Trace instructions and show stack during exec? */
	public boolean trace = false;

	/**Extract the args which needs to be pushed to new active BlockContext **/
	public STObject []extractObjs;

	public VirtualMachine(STSymbolTable symtab) {
		systemDict = new SystemDictionary(this, symtab);
		for (Symbol s : symtab.GLOBALS.getSymbols()) {
			if ( s instanceof ClassSymbol ) {
				systemDict.define(s.getName(), new STMetaClassObject(this,(STClass)s));		//ClassSymbols are converted to STMetaClassObjects
			}
		}
		STObject transcript = new STObject(systemDict.lookupClass("TranscriptStream"));
		systemDict.define("Transcript", transcript);
	}

	/** look up MainClass>>main and execute it */
	public STObject execMain() {
		STObject mainObject;
		if(systemDict.lookupClass("MainClass") != null)
			mainObject = systemDict.lookupClass("MainClass");
		else
			return nil();
		STCompiledBlock main = ((STMetaClassObject)mainObject).methods.get("main");

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
		while ( true ) {
			pushctx = null;
			int depth;
			int localIndex;
			if ( trace ) traceInstr(); // show instr first then stack after to show results
			int op = ctx.compiledBlock.bytecode[ctx.ip];
			boolean isDBG = false;
			switch ( op ) {
				case Bytecode.NIL:
					ctx.push(nil());
					consumeByte(ctx.ip);
					break;
				case Bytecode.TRUE:
					ctx.push(stBool(true));
					consumeByte(ctx.ip);
					break;
				case Bytecode.FALSE:
					ctx.push(stBool(false));
					consumeByte(ctx.ip);
					break;
				case Bytecode.PUSH_INT:
					ctx.prev_ip = ctx.ip;
					consumeByte(ctx.ip);
					int value = getInt(ctx.ip);
					consumeInt(ctx.ip);
					ctx.push(newInteger(value));
					break;
				case Bytecode.PUSH_FLOAT:
					consumeByte(ctx.ip);
					int valueF = getInt(ctx.ip);
					Float f = Float.intBitsToFloat(valueF);
					consumeFloat(ctx.ip);
					ctx.push(newFloat(f));
					break;
				case Bytecode.PUSH_CHAR:
					consumeByte(ctx.ip);
					int ch = getShort(ctx.ip);
					consumeShort(ctx.ip);
					ctx.push(newCharacter(ch));
					break;
				case Bytecode.PUSH_ARRAY:
					consumeByte(ctx.ip);
					int nargs = getShort(ctx.ip);
					consumeShort(ctx.ip);
					STArray stArray = new STArray(this, nargs);
					ctx.sp = ctx.sp - nargs;
					ctx.push(stArray);
					break;
				case Bytecode.PUSH_LITERAL:
					consumeByte(ctx.ip);
					int index = getShort(ctx.ip);
					String literalValue = ctx.compiledBlock.literals[index];
					consumeShort(ctx.ip);
					ctx.push(newString(literalValue));
					break;
				case Bytecode.PUSH_GLOBAL:
					consumeByte(ctx.ip);
					int global = getShort(ctx.ip);
					String litVal = ctx.compiledBlock.literals[global];
					consumeShort(ctx.ip);
					STObject glbl = this.lookupClass(litVal);
					if(litVal.equals("Transcript"))
						ctx.push(new STObject((STMetaClassObject) glbl));
					else
						ctx.push(glbl);
					break;
				case Bytecode.PUSH_LOCAL:
					consumeByte(ctx.ip);
					depth = getShort(ctx.ip);
					consumeShort(ctx.ip);
					localIndex = getShort(ctx.ip);
					consumeShort(ctx.ip);
					BlockContext plocal = getBlockContext(depth);
					ctx.push(plocal.locals[localIndex]);
					break;
				case Bytecode.PUSH_FIELD:
					consumeByte(ctx.ip);
					int fieldIndex = getShort(ctx.ip);
					consumeShort(ctx.ip);
					//STObject rec = ctx.receiver;
					STMetaClassObject targetObj = this.lookupClass(ctx.receiver.metaclass.getName());
					/*** Newly Added ***/
					if(targetObj.fields.size() != 0){
						ctx.push(targetObj.fields.get(fieldIndex));
						//ctx.push(newString(targetObj.fields.get(fieldIndex)));
					}
					else{
						if(ctx.receiver.metaclass.superClass != null){
							targetObj = this.lookupClass(ctx.receiver.metaclass.superClass.getName());
							ctx.push(targetObj.fields.get(fieldIndex));
							//ctx.push(newString(targetObj.fields.get(fieldIndex)));
						}
					}
					/*** Newly Added ***/
					//ctx.push(newString(targetObj.fields.get(fieldIndex)));		Earlier this line was just there
					break;
				case Bytecode.STORE_LOCAL:
					consumeByte(ctx.ip);
					depth = getShort(ctx.ip);
					consumeShort(ctx.ip);
					localIndex = getShort(ctx.ip);
					consumeShort(ctx.ip);
					BlockContext slocal = getBlockContext(depth);
					slocal.locals[localIndex] = ctx.top();
					break;
				case Bytecode.STORE_FIELD:
					consumeByte(ctx.ip);
					int fldIndx = getShort(ctx.ip);
					consumeShort(ctx.ip);
					STMetaClassObject target = this.lookupClass(ctx.receiver.metaclass.getName());
					/*** Newly Added ***/
					if(target.fields.size() != 0)
						//target.fields.set(fldIndx, ctx.top().toString());
						//((ArrayList) target.fields).set(fldIndx, ctx.top());
						target.fields.set(fldIndx, ctx.top());
					else{
						if(ctx.receiver.metaclass.superClass != null){
							target = this.lookupClass(ctx.receiver.metaclass.superClass.getName());
							//target.fields.set(fldIndx, ctx.top().toString());
							//((ArrayList) target.fields).set(fldIndx, ctx.top());
							target.fields.set(fldIndx, ctx.top());
						}
					}
					/*** Newly Added ***/
					//target.fields.set(fldIndx, ctx.top().toString());			Earlier this line was just there
					break;
				case Bytecode.SEND:
					ctx.prev_ip = ctx.ip;
					consumeByte(ctx.ip);
					int args = getShort(ctx.ip);
					consumeShort(ctx.ip);
					int lit = getShort(ctx.ip);
					consumeShort(ctx.ip);
					String literal = ctx.compiledBlock.literals[lit];
					Primitive p = getPrimtive(literal, args);
					if(p != null){										//If the message is Primitive
						STObject stObj = p.perform(ctx, args);
						if(pushctx != null)								//If any block descriptor object found
							pushContext(pushctx);						//Pushing a new Block Context if pushctx is not null
						else
							ctx.push(stObj);
						if(p.equals(Primitive.BlockDescriptor_VALUE_1_ARG) || p.equals(Primitive.BlockDescriptor_VALUE_2_ARGS))
							ctx.setLocals(extractObjs, args);
					}
					else{												//If the message is not primitive
						extractObjs = getSTObjectArgs(ctx, args);
						STObject recieve = ctx.stack[ctx.sp - args];	//Extract the Receiver of the message
						STCompiledBlock st = getSTCompiledBlock(recieve, literal);
						errorHandling(st, ctx, recieve);
						ctx.sp = ctx.sp - args - 1;						//Modified the invoking ctx stack pointer
						BlockContext bctx = new BlockContext(this, st, recieve);			//Create a new BlockContext based on the receive instance type
						pushContext(bctx);
						ctx.setLocals(extractObjs, args);					//Set the locals of newly created BlockContext
					}
					break;
				case Bytecode.SEND_SUPER:
					consumeByte(ctx.ip);
					int arg = getShort(ctx.ip);
					consumeShort(ctx.ip);
					int litIndex = getShort(ctx.ip);
					consumeShort(ctx.ip);
					String litValue = ctx.compiledBlock.literals[litIndex];
					STObject recieve = ctx.stack[ctx.sp - arg];	//Extract the Receiver of the message which should be a meta class object
					//STCompiledBlock st = getSTCompiledBlock(ctx.compiledBlock.enclosingClass.superClass, litValue);
					STCompiledBlock st = getSTCompiledBlock(ctx.compiledBlock.enclosingClass.superClass, litValue);
					ctx.sp = ctx.sp - arg - 1;						//Modified the invoking ctx stack pointer
					BlockContext bctx = new BlockContext(this, st, recieve);			//Create a new BlockContext based on the receive instance type
					pushContext(bctx);
					break;
				case Bytecode.SELF:
					consumeByte(ctx.ip);
					if(ctx.receiver instanceof STMetaClassObject && ((STMetaClassObject) ctx.receiver).getName().equals("MainClass"))
						ctx.push(new STObject((STMetaClassObject) ctx.receiver));
					else
						ctx.push(ctx.receiver);
					break;
				case Bytecode.BLOCK:
					consumeByte(ctx.ip);
					int blk = getShort(ctx.ip);
					consumeShort(ctx.ip);
					BlockContext getEncBlk = ctx;					//Get the Enclosing BlockContext which holds all the block numbers
					while(getEncBlk.enclosingContext != null){
						getEncBlk = getEncBlk.enclosingContext;
					}
					BlockDescriptor blkDes = new BlockDescriptor(getEncBlk.compiledBlock.blocks[blk], ctx, getEncBlk);
					ctx.push(blkDes);
					break;
				case Bytecode.POP:
					consumeByte(ctx.ip);
					ctx.pop();
					break;
				case Bytecode.BLOCK_RETURN:
					STObject blkObj = ctx.pop();
					popContext();
					ctx.push(blkObj);
					break;
				case Bytecode.RETURN:
					STObject ret = ctx.pop();
					boolean pop = false;
					if(ctx.enclosingMethodContext != null){
						if(ctx.invokingContext.receiver != ctx.enclosingContext.receiver){
							String triggerBlock = getTriggerBlockName(ctx);
							ctx.prev_ip = ctx.ip;
							error("BlockCannotReturn", triggerBlock + " can't trigger return again from method " + ctx.enclosingMethodContext.compiledBlock.qualifiedName);
						}
						ctx = ctx.enclosingMethodContext;
					}
					if(ctx.invokingContext != null){
						pop = true;
						popContext();
					}
					if(pop)
						ctx.push(ret);
					else
						ctx.receiver = ret;
					break;
				case Bytecode.DBG:
					ctx.prev_ip = ctx.ip;
					isDBG = true;
					consumeByte(ctx.ip);
					int fileName = getShort(ctx.ip);
					ctx.currentFile = ctx.compiledBlock.literals[fileName];
					consumeShort(ctx.ip);
					ctx.currentLine = getShort(ctx.ip);
					consumeShort(ctx.ip);
					ctx.currentCharPos = getShort(ctx.ip);
					consumeShort(ctx.ip);
					break;
			}
			if ( trace ) traceStack(); // show stack *after* execution
			op = ctx.compiledBlock.bytecode[ctx.ip];
			if((ctx.sp == -1) && (!isDBG) && (op == Bytecode.RETURN))
				break;
		}
		return ctx!=null ? ctx.receiver : null;
	}

	private String getTriggerBlockName(BlockContext ctx) {
		String stClass = ctx.receiver.getSTClass().getName();
		String blk = ctx.toString().substring(ctx.toString().indexOf(">>"), ctx.toString().length() - 4);
		return (stClass + blk);
	}

	private void errorHandling(STCompiledBlock st, BlockContext ctx, STObject recieve) {
		if(st.isClassMethod){
			if(recieve instanceof STInteger){
				String name = getCompiledBlockName(st);
				error("ClassMessageSentToInstance", name + " is a class method sent to instance of Integer");
			}
			if(recieve instanceof STFloat){
				String name = getCompiledBlockName(st);
				error("ClassMessageSentToInstance", name + " is a class method sent to instance of Float");
			}
			if(recieve instanceof STCharacter){
				String name = getCompiledBlockName(st);
				error("ClassMessageSentToInstance", name + " is a class method sent to instance of Float");
			}
			if(recieve instanceof STObject){
				String name = getCompiledBlockName(st);
				if(!name.equals("new") && !name.equals("new:") && !(recieve.toString().startsWith("class")))
					error("ClassMessageSentToInstance", name + " is a class method sent to instance of " + recieve.getSTClass().name);
			}
		}
		else{
			if(recieve instanceof STObject){
				String name = getCompiledBlockName(st);
				if(recieve.toString().startsWith("class"))
					error("MessageNotUnderstood", name + " is an instance method sent to class object " + recieve.getSTClass().name);
			}
		}
	}

	private String getCompiledBlockName(STCompiledBlock st) {
		if(st.name.indexOf("static") >=0 ){
			return (st.name.substring(st.name.indexOf("static") + 7, st.name.length()));
		}
		else
			return st.name;
	}

	private STCompiledBlock getSTCompiledBlock(STObject recieve, String literal) {
		//STMetaClassObject s;
		if(!recieve.getSTClass().getName().equals("MainClass")){
			if(this.lookupClass(recieve.getSTClass().getName()).methods.get(literal) != null)
				return this.lookupClass(recieve.getSTClass().getName()).methods.get(literal);
			/*** Newly Added ***/
			else{
				if(recieve.metaclass != null){
					STMetaClassObject s = this.lookupClass(recieve.metaclass.superClass.getName());
					if(s.methods.get(literal) != null){
						return s.methods.get(literal);
					}
				}
			}
			/*** Newly Added ***/
		}
		if(this.lookupClass("Object").methods.get(literal) != null){
			return this.lookupClass("Object").methods.get(literal);
		}
		if(this.lookupClass("Collection").methods.get(literal) != null){
			return this.lookupClass("Collection").methods.get(literal);
		}
		return null;
	}

	public STObject[] getSTObjectArgs(BlockContext ctx, int args) {
		STObject []elements = new STObject[args];
		for(int i=1; i<=args; i++){
			elements[i-1] = ctx.stack[ctx.sp - args + i];
		}
		return elements;
	}

	private BlockContext getBlockContext(int depth) {
		BlockContext ctx = this.ctx;
		for(int i = 0; i < depth; i++){
			ctx = ctx.enclosingContext;
		}

		return ctx;
	}

	public Primitive getPrimtive(String literal, int args){
		if(ctx.stack[ctx.sp - args] instanceof STInteger)
			return getIntegerPrimitive(literal);
		else if(ctx.stack[ctx.sp - args] instanceof STFloat)
			return getFloatPrimtive(literal);
		else if(ctx.stack[ctx.sp - args] instanceof STBoolean)
			return getBooleanPrimtive(literal);
		else if(ctx.stack[ctx.sp - args] instanceof STString)
			return getStringPrimtive(literal);
		else if(ctx.stack[ctx.sp - args] instanceof STCharacter)
			return getCharacterPrimtive(literal);
		else if(ctx.stack[ctx.sp - args] instanceof STArray)
			return getArrayPrimitive(literal, args);
		else if(ctx.stack[ctx.sp - args] instanceof BlockDescriptor)
			return getBlockObjectPrimtive(literal, args);
		else if(ctx.stack[ctx.sp - args] instanceof STObject)
			return getObjectPrimtive(literal, args);
		else
			return null;
	}

	private Primitive getFloatPrimtive(String literal) {
		if(literal.equals("+"))
			return Primitive.Float_ADD;
		if(literal.equals("-"))
			return Primitive.Float_SUB;
		if(literal.equals("*"))
			return Primitive.Float_MULT;
		if(literal.equals("/"))
			return Primitive.Float_DIV;
		if(literal.equals("="))
			return Primitive.Float_EQ;
		if(literal.equals("<"))
			return Primitive.Float_LT;
		if(literal.equals("<="))
			return Primitive.Float_LE;
		if(literal.equals(">"))
			return Primitive.Float_GT;
		if(literal.equals(">="))
			return Primitive.Float_GE;
		if(literal.equals("asString"))
			return Primitive.Object_ASSTRING;

		return null;
	}

	private Primitive getCharacterPrimtive(String literal) {
		if(literal.equals("asInteger"))
			return Primitive.Character_ASINTEGER;
		if(literal.equals("asString"))
			return Primitive.Object_ASSTRING;
		if(literal.equals("+"))
			return Primitive.Integer_ADD;

		return null;
	}

	private Primitive getBlockObjectPrimtive(String literal, int args) {
		if(literal.equals("value"))
			return Primitive.BlockDescriptor_VALUE;
		if(literal.equals("value:") && args == 1)
			return Primitive.BlockDescriptor_VALUE_1_ARG;
		if(literal.equals("value:") && args == 2)
			return Primitive.BlockDescriptor_VALUE_2_ARGS;

		return null;
	}

	private Primitive getObjectPrimtive(String literal, int args) {
		/*if(literal.equals("asString"))
			return Primitive.Object_ASSTRING;*/
		if(literal.equals("basicNew"))
			return Primitive.Object_Class_BASICNEW;
		if(literal.equals("show:"))
			return Primitive.TranscriptStream_SHOW;
		/*if(literal.equals("size"))
			return Primitive.Array_SIZE;
		if(literal.equals("at:put:"))
			return Primitive.Array_AT_PUT;
		if(literal.equals("at:"))
			return Primitive.Array_AT;*/
		if(ctx.stack[ctx.sp - args] instanceof STMetaClassObject){
			String name = ((STMetaClassObject) ctx.stack[ctx.sp - args]).getName();
			if(name.equals("Array"))
				return getArrayPrimitive(literal, args);
		}

		return null;
	}

	private Primitive getArrayPrimitive(String literal, int args) {
		if(literal.equals("new:"))
			return Primitive.Array_Class_NEW;
		if(literal.equals("size"))
			return Primitive.Array_SIZE;
		if(literal.equals("at:put:"))
			return Primitive.Array_AT_PUT;
		if(literal.equals("at:"))
			return Primitive.Array_AT;
		if(literal.equals("className"))
			return Primitive.Object_CLASSNAME;

		return null;
	}

	public Primitive getStringPrimtive(String literal){
		if(literal.equals("asString"))
			return Primitive.Object_ASSTRING;
		if(literal.equals("new:"))
			return Primitive.String_Class_NEW;
		if(literal.equals(","))
			return Primitive.String_CAT;
		if(literal.equals("==") || literal.equals("="))
			return Primitive.String_EQ;
		if(literal.equals("asArray"))
			return Primitive.String_ASARRAY;
		if(literal.equals("+"))
			return Primitive.Integer_ADD;

		return null;
	}

	public Primitive getBooleanPrimtive(String literal){
		if(literal.equals("ifTrue:"))
			return Primitive.Boolean_IFTRUE;
		if(literal.equals("ifTrue:ifFalse:"))
			return Primitive.Boolean_IFTRUE_IFFALSE;
		if(literal.equals("not"))
			return Primitive.Boolean_NOT;
		if(literal.equals("asString"))
			return Primitive.Object_ASSTRING;

		return null;
	}

	public Primitive getIntegerPrimitive(String literal){
		if(literal.equals("+"))
			return Primitive.Integer_ADD;
		if(literal.equals("-"))
			return Primitive.Integer_SUB;
		if(literal.equals("*"))
			return Primitive.Integer_MULT;
		if(literal.equals("/"))
			return Primitive.Integer_DIV;
		if(literal.equals("="))
			return Primitive.Integer_EQ;
		if(literal.equals("<"))
			return Primitive.Integer_LT;
		if(literal.equals("<="))
			return Primitive.Integer_LE;
		if(literal.equals(">"))
			return Primitive.Integer_GT;
		if(literal.equals(">="))
			return Primitive.Integer_GE;
		if(literal.equals("mod:"))
			return Primitive.Integer_MOD;
		if(literal.equals("asString"))
			return Primitive.Object_ASSTRING;

		return null;
	}
	public void error(String type, String msg) throws VMException {
		error(type, null, msg);
	}

	public void error(String type, Exception e, String msg) throws VMException {
		String stack = getVMStackString();
		switch ( type ) {
			case "MessageNotUnderstood":
				throw new MessageNotUnderstood(msg,stack);
			case "ClassMessageSentToInstance":
				throw new ClassMessageSentToInstance(msg,stack);
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

	public void popContext() {
		ctx = ctx.invokingContext;
	}

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

	public void assertNumOperands(int i) {
	}

	private void assertEqualBackingTypes(STObject r, String t){
	}

	public STMetaClassObject lookupClass(String id) {
		return systemDict.lookupClass(id);
	}

	public STInteger newInteger(int v) {
		return new STInteger(this, v);
	}

	public STFloat newFloat(float v) {
		return new STFloat(this, v);
	}

	public STCharacter newCharacter(int v) {
		return new STCharacter(this, v);
	}

	public STString newString(String s) {
		return new STString(this, s);
	}

	public STBoolean newBoolean(boolean b) {
		return new STBoolean(this, b);
	}

	public STNil nil() {
		return new STNil(this);
	}

	public STBoolean stBool(boolean b) {
		return new STBoolean(this, b);
	}

	public int consumeShort(int index) {
		int x = getShort(index);
		ctx.ip += Bytecode.OperandType.SHORT.sizeInBytes;
		return x;
	}

	public int consumeInt(int index) {
		int x = getShort(index);
		ctx.ip += Bytecode.OperandType.INT.sizeInBytes;
		return x;
	}

	public int consumeFloat(int index) {
		int x = getShort(index);
		ctx.ip += Bytecode.OperandType.FLOAT.sizeInBytes;
		return x;
	}

	public int consumeLiterals(int index) {
		int x = getShort(index);
		ctx.ip += Bytecode.OperandType.LITERAL.sizeInBytes;
		return x;
	}

	public int consumeDBG(int index) {
		int x = getShort(index);
		ctx.ip += Bytecode.OperandType.DBG_LOCATION.sizeInBytes;
		return x;
	}

	public int consumeByte(int index) {
		int x = getShort(index);
		ctx.ip += Bytecode.OperandType.BYTE.sizeInBytes;
		return x;
	}

	// get short operand out of bytecode sequence
	public int getShort(int index) {
		byte[] code = ctx.compiledBlock.bytecode;
		return Bytecode.getShort(code, index);
	}

	//get out of bytecode sequence
	public int getInt(int index) {
		byte[] code = ctx.compiledBlock.bytecode;
		return Bytecode.getInt(code, index);
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
		Collections.reverse(a);
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

	public String pContextWorkStack(BlockContext ctx) {
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		for (int i=0; i<=ctx.sp; i++) {
			if ( i>0 ) buf.append(", ");
			pValue(buf, ctx.stack[i]);
		}
		buf.append("]");
		return buf.toString();
	}

	public String pLocals(BlockContext ctx) {
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