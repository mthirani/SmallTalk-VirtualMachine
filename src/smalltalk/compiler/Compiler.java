package smalltalk.compiler;

import org.antlr.symtab.ClassSymbol;
import org.antlr.symtab.Scope;
import org.antlr.symtab.Symbol;
import org.antlr.symtab.VariableSymbol;
import org.antlr.v4.codegen.*;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import smalltalk.misc.Utils;
import smalltalk.vm.Bytecode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Compiler {
	protected final STSymbolTable symtab;
	public final List<String> errors = new ArrayList<>();
	protected SmalltalkParser parser;
	protected CommonTokenStream tokens;
	protected SmalltalkParser.FileContext fileTree;
	protected String fileName;
	public boolean genDbg; // generate dbg file,line instructions

	public Compiler() {
		symtab = new STSymbolTable();
	}

	public Compiler(STSymbolTable symtab) {
		this.symtab = symtab;
	}

	public String getFileName(){
		return tokens.getSourceName();
	}
	public STSymbolTable compile(ANTLRInputStream input) {
		ParserRuleContext tree = parseClasses(input);
		defSymbols(tree);
		resolveSymbols(tree);
		CodeGenerator codeGenerator = new CodeGenerator(this);
		codeGenerator.visit(tree);

		return symtab;
	}

	// Convenience methods for code gen

	public static Code dbg(int fileName, int line, int linePos){
		return Code.of(Bytecode.DBG).join(Utils.toLiteral(fileName)).join(Utils.shortToBytes(line)).join(Utils.shortToBytes(linePos));
	}
	public static Code push_nil() 				{ return Code.of(Bytecode.NIL); }
	public static Code push_float() 				{ return Code.of(Bytecode.PUSH_FLOAT); }
	public static Code push_field(int i)    		{ return Code.of(Bytecode.PUSH_FIELD).join(Utils.shortToBytes(i));
	}
	public static Code push_local(int d, int i) 				{ return Code.of(Bytecode.PUSH_LOCAL).join(Utils.shortToBytes(d)).join(Utils.shortToBytes(i)); }
	public static Code push_literal(int v) 				{ return Code.of(Bytecode.PUSH_LITERAL).join(Utils.shortToBytes(v)); }
	public static Code push_global(int v) 				{ return Code.of(Bytecode.PUSH_GLOBAL).join(Utils.shortToBytes(v)); }
	public static Code push_array() 				{ return Code.of(Bytecode.PUSH_ARRAY); }
	public static Code push_store_field(int i) 				{ return Code.of(Bytecode.STORE_FIELD).join(Utils.shortToBytes(i)); }
	public static Code push_store_local(int d, int i) 				{ return Code.of(Bytecode.STORE_LOCAL).join(Utils.shortToBytes(d)).join(Utils.shortToBytes(i)); }
	public static Code push_send(int n, int i) 				{ return Code.of(Bytecode.SEND).join(Utils.shortToBytes(n)).join(Utils.shortToBytes(i)); }
	public static Code push_send_super(int n, int i) 				{ return Code.of(Bytecode.SEND_SUPER).join(Utils.shortToBytes(n)).join(Utils.shortToBytes(i)); }
	public static Code push_block(int blkNum) 				{ return Code.of(Bytecode.BLOCK).join(Utils.shortToBytes(blkNum)); }
	public static Code push_block_return() 				{ return Code.of(Bytecode.BLOCK_RETURN); }
	public static Code push_char(char c)			{ return Code.of(Bytecode.PUSH_CHAR).join(Utils.shortToBytes(c)); }
	public static Code push_int(int v) 			{ return Code.of(Bytecode.PUSH_INT).join(Utils.intToBytes(v)); }
	public static Code push_debug() 				{ return Code.of(Bytecode.DBG); }
	public static Code push_true() 				{ return Code.of(Bytecode.TRUE); }
	public static Code push_false() 				{ return Code.of(Bytecode.FALSE); }

	// Error support

	public void error(String msg) {
		errors.add(msg);
	}

	public void error(String msg, Exception e) {
		errors.add(msg+"\n"+ Arrays.toString(e.getStackTrace()));
	}

	public void defineFields(STClass cl, List<String> instanceVars)
	{
		if(instanceVars != null){

			int getIndex = 0;
			ClassSymbol c = cl;
			while(c.getSuperClassScope() != null){
				getIndex = getIndex + c.getSuperClassScope().getFields().size();
				c = c.getSuperClassScope();
			}
			for(String insVar: instanceVars){
				STField var = new STField(insVar);
				cl.define(var);
				var.setInsertionOrderNumber(getIndex);
				getIndex++;
			}
		}
	}

	public STMethod createMethod(String method, SmalltalkParser.MethodContext ctx)
	{
		return new STMethod(method, ctx);
	}

	public STMethod createMethod(String main, SmalltalkParser.MainContext ctx)
	{
		return new STMethod(main, ctx);
	}

	public void defineArguments(STBlock m, List<String> args)
	{
		if(args != null){
			for(String a: args){
				STArg arg = new STArg(a);
				Symbol s = m.resolve(arg.getName());
				if(s == null ||!s.getScope().equals(m))
					m.define(arg);
				else
					this.error("redefinition of " + arg + " in " + m.toQualifierString(">>"));
			}
		}
	}

	public STMethod createPrimitiveMethod(STClass currentScope, String selector, String primitiveName, SmalltalkParser.MethodContext methodNode)
	{
		STMethod stMethod = new STMethod(primitiveName, methodNode);
		currentScope.define(stMethod);
		return stMethod;
	}

	public void defineLocals(Scope currentScope, List<String> vars)
	{
		if(vars != null){
			for(String v: vars){
				STVariable var = new STVariable(v);
				Symbol s = currentScope.resolve(var.getName());
				if(s == null || !s.getScope().equals(currentScope))
					currentScope.define(var);
				else
					this.error("redefinition of " + v + " in " + currentScope.toQualifierString(">>"));
			}
		}
	}

	public STBlock createBlock(STMethod currentMethod, SmalltalkParser.BlockContext ctx) {
		return new STBlock(currentMethod, ctx);
	}

	public static Code method_return() {
		return Code.of(Bytecode.RETURN);
	}

	public static Code pop() {
		return Code.of(Bytecode.POP);
	}

	public static Code push_self() {
		return Code.of(Bytecode.SELF);
	}

	public ParserRuleContext parseClasses(ANTLRInputStream antlrInputStream) {
		ANTLRInputStream ip = new ANTLRInputStream(String.valueOf(antlrInputStream));
		SmalltalkLexer lexer = new SmalltalkLexer(ip);
		tokens = new CommonTokenStream(lexer);
		parser = new SmalltalkParser(tokens);
		fileTree = parser.file();

		return fileTree;
	}

	public void defSymbols(ParserRuleContext tree) {
		DefineSymbols def = new DefineSymbols(this);
		ParseTreeWalker p = new ParseTreeWalker();
		p.walk(def , tree);
	}

	public void resolveSymbols(ParserRuleContext tree) {
		ResolveSymbols res = new ResolveSymbols(this);
		ParseTreeWalker p = new ParseTreeWalker();
		p.walk(res , tree);
	}
}
