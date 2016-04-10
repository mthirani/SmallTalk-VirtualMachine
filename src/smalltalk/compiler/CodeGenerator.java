package smalltalk.compiler;

import org.antlr.symtab.ClassSymbol;
import org.antlr.symtab.Scope;
import org.antlr.symtab.StringTable;
import org.antlr.symtab.Symbol;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;
import smalltalk.vm.primitive.Primitive;
import smalltalk.vm.primitive.STCompiledBlock;
import smalltalk.vm.primitive.STFloat;

import java.util.*;

/** Fill STBlock, STMethod objects in Symbol table with bytecode,
 * {@link smalltalk.vm.primitive.STCompiledBlock}.
 */
public class CodeGenerator extends SmalltalkBaseVisitor<Code> {
	public Scope currentScope;

	/** With which compiler are we generating code? */
	public final Compiler compiler;
	public boolean isClassMethod = false;
	public LinkedHashMap<String, LinkedHashSet<String>> literals;
	public LinkedHashMap<String, LinkedHashSet<STCompiledBlock>> compiledBlocks;
	public HashMap<String, Integer> nlocals;
	public HashMap<String, Integer> nargs;
	public final Map<Scope,StringTable> blockToStrings = new HashMap<>();
	public boolean isPrimitive = false;
	public String primitiveName;

	public CodeGenerator(Compiler compiler) {
		this.compiler = compiler;
		this.currentScope = compiler.symtab.GLOBALS;
		this.literals = new LinkedHashMap<String, LinkedHashSet<String>>();
		this.nlocals = new HashMap<String, Integer>();
		this.compiledBlocks = new LinkedHashMap<String, LinkedHashSet<STCompiledBlock>>();
		this.nargs =  new HashMap<String, Integer>();
	}

	/** This and defaultResult() critical to getting code to bubble up the
	 *  visitor call stack when we don't implement every method.
	 */
	@Override
	protected Code aggregateResult(Code aggregate, Code nextResult) {
		if ( aggregate!=Code.None ) {
			if ( nextResult!=Code.None ) {
				return aggregate.join(nextResult);
			}
			return aggregate;
		}
		else {
			return nextResult;
		}
	}

	@Override
	protected Code defaultResult() {
		return Code.None;
	}

	@Override
	public Code visitFile(SmalltalkParser.FileContext ctx) {
		visitChildren(ctx);
		return Code.None;
	}

	@Override
	public Code visitMain(SmalltalkParser.MainContext ctx) {
		if(ctx.scope != null){
			pushScope(ctx.scope);
			nlocals.put(ctx.scope.getName(), 0);
			Code code = visitChildren(ctx);
			code = code.join(Compiler.pop());
			code = code.join(Compiler.push_self());
			code = code.join(Compiler.method_return());
			ctx.scope.compiledBlock = getCompiledBlock(ctx.scope, code);
		}

		return Code.None;
	}


	@Override
	public Code visitEmptyBody(SmalltalkParser.EmptyBodyContext ctx) {
		if (ctx.localVars() != null){
			Code c = visitLocalVars(ctx.localVars());
			return c;
		}
		else{
			if(currentScope.getName().indexOf("block") >= 0){
				Code code = Compiler.push_nil();
				return code;
			}
			else{
				return Code.None;
			}
		}
	}

	@Override
	public Code visitBlock(SmalltalkParser.BlockContext ctx) {
		pushScope(ctx.scope);
		int blockNum;
		Code e= Code.None;
		if(ctx.blockArgs() != null)
			e = visit(ctx.blockArgs());
		Code code = e.join(visit(ctx.body()));
		code = code.join(Compiler.push_block_return());
		/***** Extract the scope name ****/
		String scopeName = extractScopeName(ctx.scope);

		/***** Map/ Add the Compiled Block code in the specified scope block ****/
		if(compiledBlocks.containsKey(scopeName)){
			blockNum = compiledBlocks.get(scopeName).size();
			compiledBlocks.get(scopeName).add(getCompiledBlock(ctx.scope, code));
		}
		else{
			LinkedHashSet<STCompiledBlock> stCompiledBlocks = new LinkedHashSet<>();
			stCompiledBlocks.add(getCompiledBlock(ctx.scope, code));
			compiledBlocks.put(scopeName, stCompiledBlocks);
			blockNum = 0;
		}
		ctx.scope.compiledBlock = getCompiledBlock(ctx.scope, code);
		popScope();

		return Compiler.push_block(blockNum);
	}

	@Override
	public Code visitClassDef(SmalltalkParser.ClassDefContext ctx) {
		isClassMethod = true;
		pushScope(ctx.scope);
		Code code = visitChildren(ctx);
		popScope();
		isClassMethod = false;

		return code;
	}

	@Override
	public Code visitOperatorMethod(SmalltalkParser.OperatorMethodContext ctx) {
		isPrimitive = false;
		pushScope(ctx.scope);
		nlocals.put(ctx.scope.getName(), 0);
		Code code = visit(ctx.methodBlock());
		ctx.scope.compiledBlock = getCompiledBlock(ctx.scope, code);
		popScope();

		return Code.None;
	}

	@Override
	public Code visitNamedMethod(SmalltalkParser.NamedMethodContext ctx) {
		isPrimitive = false;
		pushScope(ctx.scope);
		nlocals.put(ctx.scope.getName(), 0);
		Code code = visit(ctx.methodBlock());
		ctx.scope.compiledBlock = getCompiledBlock(ctx.scope, code);
		popScope();

		return Code.None;
	}

	@Override
	public Code visitFullBody(SmalltalkParser.FullBodyContext ctx) {
		Code c = Code.None;
		if(ctx.localVars() != null)
			c = c.join(visitLocalVars(ctx.localVars()));
		int count = 1;
		for(SmalltalkParser.StatContext s: ctx.stat()){
			c = c.join(visit(s));
			if(count != ctx.stat().size())
				c = c.join(Compiler.pop());
			count++;
		}

		return c;
	}

	@Override
	public Code visitPrimitiveMethodBlock(@NotNull SmalltalkParser.PrimitiveMethodBlockContext ctx) {
		/*STPrimitiveMethod p = (STPrimitiveMethod)currentScope.resolve(ctx.selector);
		STCompiledBlock blk = new STCompiledBlock(p);
		primitiveName = ctx.SYMBOL().getText(); // e.g., Integer_ADD
		primitiveName = primitiveName.substring(1,primitiveName.length());
		Primitive primitive = Primitive.valueOf(primitiveName);*/
		isPrimitive = true;
		nargs.put(currentScope.getName(), ctx.args.size());
		primitiveName = ctx.selector;

		return Code.None;
	}


	@Override
	public Code visitLocalVars(SmalltalkParser.LocalVarsContext ctx) {
		nlocals.put(currentScope.getName(), ctx.ID().size());

		return Code.None;
	}

	@Override
	public Code visitKeywordSend(SmalltalkParser.KeywordSendContext ctx) {
		int index;
		Code code = Code.None;
		for(SmalltalkParser.BinaryExpressionContext bec: ctx.binaryExpression())
			code = code.join(visit(bec));
		if(ctx.args.size() != 0){
			int args = ctx.args.size();
//			String scopeName = extractScopeName(currentScope);
			String scopeName = currentScope.getName();
			String keywords = "";
			for(int i=0; i<ctx.KEYWORD().size(); i++){
				keywords = keywords + ctx.KEYWORD().get(i).toString();
			}
			setLiterals(scopeName, keywords);
			index = findIndex(literals.get(scopeName), keywords);
			code = code.join(Compiler.push_send(args, index));
		}
		return code;
	}

	@Override
	public Code visitBinaryExpression(SmalltalkParser.BinaryExpressionContext ctx) {
		Code code = Code.None;
		code = code.join(visit(ctx.unaryExpression(0)));
		int i = 1;
		while(ctx.getChild(i) != null){
			code = code.join(visit(ctx.getChild(i+1)));
			code = code.join(visit(ctx.getChild(i)));
			i = i + 2;
		}

		return code;
	}

	@Override
	public Code visitBop(SmalltalkParser.BopContext ctx) {
		Code code = Code.None;
		//String scopeName = extractScopeName(currentScope);		//Uncomment this one
		String scopeName = currentScope.getName();
		setLiterals(scopeName, ctx.getText());
		int index = findIndex(literals.get(scopeName), ctx.getText());
		code = code.join(Compiler.push_send(1, index));

		return code;
	}

	@Override
	public Code visitSmalltalkMethodBlock(SmalltalkParser.SmalltalkMethodBlockContext ctx) {
		SmalltalkParser.MethodContext methodNode = (SmalltalkParser.MethodContext)ctx.getParent();
		Code code = visitChildren(ctx);
		if ( compiler.genDbg ) { // put dbg in front of push_self
			code = Code.join(code, dbgAtEndBlock(ctx.stop));
		}
		if ( ctx.body() instanceof SmalltalkParser.FullBodyContext ) {
			code = code.join(Compiler.pop()); // visitFullBody() doesn't have last pop; we toss here but use with block_return in visitBlock
		}
		code = code.join(Compiler.push_self());
		code = code.join(Compiler.method_return());
		methodNode.scope.compiledBlock = getCompiledBlock(methodNode.scope, code);

		return code;
	}

	private STCompiledBlock getCompiledBlock(STBlock scope, Code code) {
		STCompiledBlock stCompiledBlock = new STCompiledBlock(scope);
		stCompiledBlock.bytecode = code.bytes();
		stCompiledBlock.isClassMethod = isClassMethod;
		if(isPrimitive){
			stCompiledBlock.name = primitiveName;
			stCompiledBlock.qualifiedName = scope.getEnclosingScope().getName() + ">>" + primitiveName;
		}
		else{
			stCompiledBlock.name = scope.getName();
			stCompiledBlock.qualifiedName = scope.getQualifiedName(">>");
		}
		if(nlocals.get(scope.getName()) != null){
			stCompiledBlock.nlocals = nlocals.get(scope.getName());
		}
		else
			stCompiledBlock.nlocals = 0;
		int i;
		if(literals.get(scope.getName()) != null){
			LinkedHashSet<String> literalsCompiledBlock = literals.get(scope.getName());
			stCompiledBlock.literals = new String[literalsCompiledBlock.size()];
			i = 0;
			for(String  literal: literalsCompiledBlock){
				stCompiledBlock.literals[i] = literal;
				i++;
			}
		}
		else
			stCompiledBlock.literals = new String[0];
		if(compiledBlocks.get(scope.getName()) != null){
			LinkedHashSet<STCompiledBlock> stCompiledBlocks = compiledBlocks.get(scope.getName());
			stCompiledBlock.blocks = new STCompiledBlock[stCompiledBlocks.size()];
			i = 0;
			for(STCompiledBlock stCompiledBlock1: stCompiledBlocks){
				stCompiledBlock.blocks[i] = stCompiledBlock1;
				i++;
			}
		}
		if(nargs.get(scope.getName()) != null){
			stCompiledBlock.nargs = nargs.get(scope.getName());
		}
		else
			stCompiledBlock.nargs = 0;

		return stCompiledBlock;
	}

	@Override
	public Code visitAssign(SmalltalkParser.AssignContext ctx) {
		Code e = visit(ctx.messageExpression());
		Code store = store(ctx.lvalue().ID().getText());
		Code code = e.join(store);
		if ( compiler.genDbg ) {
			code = dbg(ctx.start).join(code);
		}
		return code;
	}

	private Code store(String text) {
		Symbol s = currentScope.resolve(text);
		if(s instanceof STField){
			int i = s.getInsertionOrderNumber();
			return Compiler.push_store_field(i);
		}
		else{
			if(s instanceof STVariable){
				Scope scope = currentScope;
				int i = s.getInsertionOrderNumber();
				int d = 0;				//this is delta from current scope to s.scope
				while(!s.getScope().equals(scope)){
					scope = currentScope.getEnclosingScope();
					d++;
				}
				return Compiler.push_store_local(d, i);
			}
			else{

			}
		}

		return Code.None;
	}

	@Override
	public Code visitId(SmalltalkParser.IdContext ctx) {
		if(ctx.sym instanceof STVariable){
			int local = ctx.sym.getInsertionOrderNumber();
			Scope scope = currentScope;
			int d = 0;
			if(!ctx.sym.getScope().equals(scope)){
				scope = currentScope.getEnclosingScope();
				d++;
			}

			return Compiler.push_local(d, local);
		}
		else{
			if(ctx.sym instanceof STField){
				return Compiler.push_field(ctx.sym.getInsertionOrderNumber());
			}
			else
			{
				if(ctx.sym instanceof STArg){
					int local = ctx.sym.getInsertionOrderNumber();
					return Compiler.push_local(0, local);
				}
				else
				{
					int index = 0;
					//String scopeName = extractScopeName(currentScope);
					String scopeName = currentScope.getName();
					setLiterals(scopeName, ctx.ID().getText());
					index = findIndex(literals.get(scopeName), ctx.ID().getText());

					return Compiler.push_global(index);
				}
			}
		}
	}

	@Override
	public Code visitBlockArgs(SmalltalkParser.BlockArgsContext ctx) {
		int args = ctx.ID().size();
		nargs.put(currentScope.getName(), args);

		return Code.None;
	}

	@Override
	public Code visitLiteral(SmalltalkParser.LiteralContext ctx) {
		/***** Extract the scope name ****/
		//String scopeName = extractScopeName(currentScope);		//Uncomment this one
		String scopeName = currentScope.getName();

		if(ctx.getText().equals("nil"))
			return Compiler.push_nil();
		else if(ctx.getText().equals("self"))
			return Compiler.push_self();
		else if(ctx.getText().equals("super"))
			return Compiler.push_send_super();
		else if(ctx.getText().equals("true"))
			return Compiler.push_true();
		else if(ctx.getText().equals("false"))
			return Compiler.push_false();
		else if(ctx.getText().startsWith("$"))
			return Compiler.push_char(ctx.getText().charAt(0));
		else if(ctx.getText().startsWith("'")){
			setLiterals(scopeName, ctx.getText().substring(1,ctx.getText().length()-1));
			int index = findIndex(literals.get(scopeName), ctx.getText().substring(1,ctx.getText().length()-1));
			return Compiler.push_literal(index);
		}
		else
			return Compiler.push_int(new Integer(ctx.getText()));
	}

	@Override
	public Code visitReturn(SmalltalkParser.ReturnContext ctx) {
		Code e = visit(ctx.messageExpression());
		if ( compiler.genDbg ) {
			e = Code.join(e, dbg(ctx.start)); // put dbg after expression as that is when it executes
		}
		Code code = e.join(Compiler.method_return());

		return code;
	}

	public void pushScope(Scope scope) {
		currentScope = scope;
	}

	public void popScope()
	{
		currentScope = currentScope.getEnclosingScope();
	}

	public int getLiteralIndex(String s) {
		return 0;
	}

	public Code dbgAtEndMain(Token t) {
		int charPos = t.getCharPositionInLine() + t.getText().length();
		return dbg(t.getLine(), charPos);
	}

	public Code dbgAtEndBlock(Token t) {
		int charPos = t.getCharPositionInLine() + t.getText().length();
		charPos -= 1; // point at ']'
		return dbg(t.getLine(), charPos);
	}

	public Code dbg(Token t) {
		return dbg(t.getLine(), t.getCharPositionInLine());
	}

	public Code dbg(int line, int charPos) {
		return Compiler.dbg(getLiteralIndex(compiler.getFileName()), line, charPos);
	}

	private String extractScopeName(Scope currentScope){
		String scopeName;
		if(currentScope.getName().indexOf("-") >= 0){
			int index = currentScope.getName().indexOf("-");
			scopeName = currentScope.getName().substring(0,index);
		}
		else
			scopeName = currentScope.getName();

		return scopeName;
	}

	private int findIndex(LinkedHashSet<String> strings, String find) {
		int index = 0;
		for(String l: strings){
			if(l.equals(find)){
				return index;
			}
			index++;
		}

		return index;
	}

	private void setLiterals(String scopeName, String value){
		if(literals.get(scopeName) != null){
			literals.get(scopeName).add(value);
		}
		else{
			LinkedHashSet<String> keywordliterals = new LinkedHashSet<>();
			keywordliterals.add(value);
			literals.put(scopeName, keywordliterals);
		}
	}
}