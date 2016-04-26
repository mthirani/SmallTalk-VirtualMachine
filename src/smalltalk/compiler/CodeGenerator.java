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

	@Override
	public Code visitArray(SmalltalkParser.ArrayContext ctx) {
		int totalElements = 0;
		Code code = Code.None;
		for(SmalltalkParser.MessageExpressionContext mec: ctx.messageExpression()){
			code = code.join(visit(mec));
			totalElements++;
		}
		code = code.join(Compiler.push_array(totalElements));

		return code;
	}

	public LinkedHashMap<String, LinkedHashSet<String>> literals;				//To add unique literals and insert the literals in sequence so used LinkedHashSet
	public HashMap<String, TreeSet<STCompiledBlock>> compiledBlocks;
	public HashMap<String, Integer> nlocals;
	public HashMap<String, Integer> nargs;
	public final Map<Scope,StringTable> blockToStrings = new HashMap<>();
	public boolean isPrimitive = false;
	public String primitiveName;
	public int blockNum;

	public CodeGenerator(Compiler compiler) {
		this.compiler = compiler;
		this.currentScope = compiler.symtab.GLOBALS;
		this.literals = new LinkedHashMap<String, LinkedHashSet<String>>();
		this.nlocals = new HashMap<String, Integer>();
		this.compiledBlocks = new HashMap<String, TreeSet<STCompiledBlock>>();
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
			if(compiler.genDbg){
				code = Code.join(code, dbgAtEndMain(ctx.stop));
			}
			code = code.join(Compiler.pop());
			code = code.join(Compiler.push_self());
			code = code.join(Compiler.method_return());
			ctx.scope.compiledBlock = getCompiledBlock(ctx.scope, code);
			popScope();
		}

		return Code.None;
	}


	@Override
	public Code visitEmptyBody(SmalltalkParser.EmptyBodyContext ctx) {
		if (ctx.localVars() != null){
			Code code = visitLocalVars(ctx.localVars());
			if(compiler.genDbg){
				code = Code.join(code, dbgAtEndBlock(ctx.stop));
			}
			return code;
		}
		else{
			if(currentScope.getName().indexOf("block") >= 0){
				Code code = Compiler.push_nil();
				if(compiler.genDbg){
					code = Code.join(dbgAtEndBlock(ctx.stop), code);
				}
				return code;
			}
			else{
				Code code = Code.None;
				if(compiler.genDbg){
					code = Code.join(code, dbgAtEndBlock(ctx.stop));
				}

				return code;
			}
		}
	}

	@Override
	public Code visitBlock(SmalltalkParser.BlockContext ctx) {
		pushScope(ctx.scope);
		int temp = blockNum++;
		String scopeName = extractScopeName(ctx.scope);
		Code e= Code.None;
		if(ctx.blockArgs() != null)
			e = visit(ctx.blockArgs());
		Code code = e.join(visit(ctx.body()));
		if(compiler.genDbg){
			code = Code.join(code, dbgAtEndBlock(ctx.stop));
		}
		code = code.join(Compiler.push_block_return());
		ctx.scope.compiledBlock = getCompiledBlock(ctx.scope, code);
		/***** Map/ Add the Compiled Block code in the specified scope block ****/
		if(compiledBlocks.containsKey(scopeName)){
			compiledBlocks.get(scopeName).add(getCompiledBlock(ctx.scope, code));
		}
		else{
			TreeSet<STCompiledBlock> stCompiledBlocks = new TreeSet<>();
			stCompiledBlocks.add(getCompiledBlock(ctx.scope, code));
			compiledBlocks.put(scopeName, stCompiledBlocks);
		}
		popScope();

		return Compiler.push_block(temp);
	}

	@Override
	public Code visitUnarySuperMsgSend(SmalltalkParser.UnarySuperMsgSendContext ctx) {
		setLiterals(currentScope.getName(), ctx.ID().getText());
		int index = findIndex(literals.get(currentScope.getName()), ctx.ID().getText());
		Code code = Compiler.push_self();
		if(compiler.genDbg)
			code = code.join(Compiler.push_send_super(0, index+1));
		else
			code = code.join(Compiler.push_send_super(0, index));

		return code;
	}

	@Override
	public Code visitClassMethod(SmalltalkParser.ClassMethodContext ctx) {
		isClassMethod = true;
		Code code = visit(ctx.method());
		isClassMethod = false;

		return code;
	}

	@Override
	public Code visitClassDef(SmalltalkParser.ClassDefContext ctx) {
		pushScope(ctx.scope);
		Code code = visitChildren(ctx);
		popScope();

		return code;
	}

	@Override
	public Code visitOperatorMethod(SmalltalkParser.OperatorMethodContext ctx) {
		isPrimitive = false;
		blockNum = 0;
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
		blockNum = 0;
		pushScope(ctx.scope);
		nlocals.put(ctx.scope.getName(), 0);
		Code code = visit(ctx.methodBlock());
		ctx.scope.compiledBlock = getCompiledBlock(ctx.scope, code);
		popScope();

		return Code.None;
	}

	@Override
	public Code visitKeywordMethod(SmalltalkParser.KeywordMethodContext ctx) {
		isPrimitive = false;
		blockNum = 0;
		pushScope(ctx.scope);
		nlocals.put(ctx.scope.getName(), 0);
		Code code = visit(ctx.methodBlock());
		nargs.put(ctx.scope.getName(), ctx.ID().size());
		ctx.scope.compiledBlock = getCompiledBlock(ctx.scope, code);
		popScope();

		return Code.None;
	}

	@Override
	public Code visitUnaryMsgSend(SmalltalkParser.UnaryMsgSendContext ctx) {
		Code code = visit(ctx.unaryExpression());
		setLiterals(currentScope.getName(), ctx.ID().getText());
		int index = findIndex(literals.get(currentScope.getName()), ctx.ID().getText());
		if(compiler.genDbg)
			code = code.join(Compiler.push_send(0, index+1));
		else
			code = code.join(Compiler.push_send(0, index));
		if (compiler.genDbg) {
			code = Code.join(dbg(ctx.ID().getSymbol()), code);
		}

		return code;
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
			String scopeName = currentScope.getName();
			String keywords = "";
			for(int i=0; i<ctx.KEYWORD().size(); i++){
				keywords = keywords + ctx.KEYWORD().get(i).toString();
			}
			setLiterals(scopeName, keywords);
			index = findIndex(literals.get(scopeName), keywords);
			if(compiler.genDbg){
				code = Code.join(code, dbg(ctx.KEYWORD(0).getSymbol()));
			}
			if(compiler.genDbg)
				code = code.join(Compiler.push_send(args, index+1));
			else
				code = code.join(Compiler.push_send(args, index));
		}

		return code;
	}

	@Override
	public Code visitBinaryExpression(SmalltalkParser.BinaryExpressionContext ctx) {
		Code code = Code.None;
		code = code.join(visit(ctx.unaryExpression(0)));
		int i = 1;
		int bop = 0;
		while(ctx.getChild(i) != null){
			code = code.join(visit(ctx.getChild(i+1)));
			if(compiler.genDbg){
				code = Code.join(dbg(ctx.bop(bop).getStart()), code);
			}
			code = code.join(visit(ctx.getChild(i)));
			i = i + 2;
			bop = bop + 1;
		}

		return code;
	}

	@Override
	public Code visitBop(SmalltalkParser.BopContext ctx) {
		Code code = Code.None;
		String scopeName = currentScope.getName();
		setLiterals(scopeName, ctx.getText());
		int index = findIndex(literals.get(scopeName), ctx.getText());
		if ( compiler.genDbg )
			code = code.join(Compiler.push_send(1, index+1));
		else
			code = code.join(Compiler.push_send(1, index));

		return code;
	}

	@Override
	public Code visitSmalltalkMethodBlock(SmalltalkParser.SmalltalkMethodBlockContext ctx) {

		SmalltalkParser.MethodContext methodNode = (SmalltalkParser.MethodContext)ctx.getParent();
		Code code = visitChildren(ctx);
		if ( compiler.genDbg ) {
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
		if(isClassMethod)
			stCompiledBlock.name = "static " + stCompiledBlock.name;
		if(nlocals.get(scope.getName()) != null){
			stCompiledBlock.nlocals = nlocals.get(scope.getName());
		}
		else
			stCompiledBlock.nlocals = 0;
		int i;
		if(literals.get(scope.getName()) != null){
			LinkedHashSet<String> literalsCompiledBlock = literals.get(scope.getName());
			i = 0;
			if (compiler.genDbg){
				stCompiledBlock.literals = new String[literalsCompiledBlock.size()+1];
				stCompiledBlock.literals[i] = compiler.getFileName();
				i++;
			}
			else
				stCompiledBlock.literals = new String[literalsCompiledBlock.size()];
			for(String  literal: literalsCompiledBlock){
				stCompiledBlock.literals[i] = literal;
				i++;
			}
		}
		else{
			if (compiler.genDbg){
				stCompiledBlock.literals = new String[1];
				stCompiledBlock.literals[0] = compiler.getFileName();
			}else
				stCompiledBlock.literals = new String[0];
		}
		if(compiledBlocks.get(scope.getName()) != null){
			TreeSet<STCompiledBlock> stCompiledBlocks = compiledBlocks.get(scope.getName());
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
		if (compiler.genDbg) {
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
			if(s instanceof STVariable || s instanceof STArg){				//Inserted s instanceof STArg
				Scope scope = currentScope;
				int i = s.getInsertionOrderNumber();
				int d = 0;				//this is delta from current scope to s.scope
				while(!s.getScope().equals(scope)){
					scope = scope.getEnclosingScope();
					d++;
				}
				return Compiler.push_store_local(d, i);
			}
			else{
				return Code.None;
			}
		}
	}

	@Override
	public Code visitId(SmalltalkParser.IdContext ctx) {
		if(ctx.sym instanceof STVariable){
			int local = ctx.sym.getInsertionOrderNumber();
			Scope scope = currentScope;
			int d = 0;
			while(!ctx.sym.getScope().equals(scope)){
				scope = scope.getEnclosingScope();
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
					Scope scope = currentScope;
					int d = 0;
					while(!ctx.sym.getScope().equals(scope)){
						scope = scope.getEnclosingScope();
						d++;
					}

					return Compiler.push_local(d, local);
				}
				else
				{
					int index = 0;
					String scopeName = currentScope.getName();
					setLiterals(scopeName, ctx.ID().getText());
					index = findIndex(literals.get(scopeName), ctx.ID().getText());
					if(compiler.genDbg)
						return Compiler.push_global(index+1);
					else
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
		String scopeName = currentScope.getName();

		if(ctx.getText().equals("nil"))
			return Compiler.push_nil();
		else if(ctx.getText().equals("self"))
			return Compiler.push_self();
		else if(ctx.getText().equals("true"))
			return Compiler.push_true();
		else if(ctx.getText().equals("false"))
			return Compiler.push_false();
		else if(ctx.getText().startsWith("$"))
			return Compiler.push_char(ctx.getText().charAt(1));
		else if(ctx.getText().startsWith("'")){
			setLiterals(scopeName, ctx.getText().substring(1,ctx.getText().length()-1));
			int index = findIndex(literals.get(scopeName), ctx.getText().substring(1,ctx.getText().length()-1));
			if ( compiler.genDbg )
				return Compiler.push_literal(index+1);
			else
				return Compiler.push_literal(index);
		}
		else{
			if(ctx.getText().indexOf(".") >= 0)
				return Compiler.push_float(new Float(ctx.getText()));
			else
				return Compiler.push_int(new Integer(ctx.getText()));
		}
	}

	@Override
	public Code visitReturn(SmalltalkParser.ReturnContext ctx) {
		Code e = visit(ctx.messageExpression());
		if ( compiler.genDbg ) {
			e = Code.join(e, dbg(ctx.start));
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

	public Code dbg(int line, int charPos) {			//lineFromCombined method in  Bytecode java file has been modified: Added /256 in return
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