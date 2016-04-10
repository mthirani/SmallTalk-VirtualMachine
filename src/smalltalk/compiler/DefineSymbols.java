package smalltalk.compiler;

import org.antlr.symtab.Scope;
import org.antlr.symtab.Symbol;
import org.antlr.symtab.Utils;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefineSymbols extends SmalltalkBaseListener {
	public final Compiler compiler;
	public STMethod currentMethod;
	public Scope currentScope; // block or method

	public DefineSymbols(Compiler compiler) {
		this.compiler = compiler;
		currentScope = compiler.symtab.GLOBALS;
	}

	@Override
	public void enterClassDef(SmalltalkParser.ClassDefContext ctx) {
		String className = ctx.ID(0).getText();
		String superClassName = null;
		if ( ctx.ID(1)!=null ) {
			superClassName = ctx.ID(1).getText();
		}
		else if ( !className.equals("Object") ) {
			superClassName = "Object";
		}
		List<String> instanceVars = null;
		if ( ctx.instanceVars()!=null ) {
			instanceVars = new ArrayList<>();
			List<TerminalNode> instanceVarNodes = ctx.instanceVars().localVars().ID();
			for (TerminalNode varNode : instanceVarNodes) {
				instanceVars.add(varNode.getText());
			}
//			System.out.println("\tinstance vars: "+instanceVars);
		}
		if ( currentScope.getSymbol(className)!=null || className.equals("MainClass") ) {
			compiler.error("redefinition of "+className);
			return;
		}
		STClass cl = new STClass(className, superClassName, compiler.symtab);
		currentScope.define(cl);
		compiler.defineFields(cl, instanceVars);
		ctx.scope = cl;
		pushScope(cl);
	}

	@Override
	public void exitClassDef(SmalltalkParser.ClassDefContext ctx) {
		popScope();
	}

	@Override
	public void enterMain(SmalltalkParser.MainContext ctx) {
		if ( ctx.body().getChildCount()==0 ) return;
		// pretend user defined "class MainClass [main [...]]"
		// define MainClass
		STClass cl = new STClass("MainClass", "Object", compiler.symtab);
		ctx.classScope = cl;
		currentScope.define(cl);
		pushScope(cl);
		// define main method
		STMethod m = compiler.createMethod("main", ctx);
		ctx.scope = m;
		currentScope.define(m);
		currentMethod = m;
		pushScope(m);
	}

	@Override
	public void exitMain(SmalltalkParser.MainContext ctx) {
		if ( ctx.body().getChildCount()==0 ) return;
		popScope(); // pop main method
		popScope(); // pop MainClass
	}

	@Override
	public void exitClassMethod(SmalltalkParser.ClassMethodContext ctx) {
		ctx.method().scope.isClassMethod = true;
	}

	@Override
	public void enterNamedMethod(SmalltalkParser.NamedMethodContext ctx) {
		ctx.methodBlock().selector = ctx.ID().getText();
		ctx.methodBlock().args = Collections.emptyList();
	}

	@Override
	public void enterOperatorMethod(final SmalltalkParser.OperatorMethodContext ctx) {
		ctx.methodBlock().selector = ctx.bop().getText();
		ctx.methodBlock().args = new ArrayList<String>(){{add(ctx.ID().getText());}};
	}

	@Override
	public void enterKeywordMethod(SmalltalkParser.KeywordMethodContext ctx) {
		List<String> vars = getTextValues(ctx.KEYWORD());
		ctx.methodBlock().selector = Utils.join(vars, "");
		ctx.methodBlock().args = getTextValues(ctx.ID());
	}

	@Override
	public void enterSmalltalkMethodBlock(SmalltalkParser.SmalltalkMethodBlockContext ctx) {
		SmalltalkParser.MethodContext methodNode =
			(SmalltalkParser.MethodContext) Utils.getAncestor(ctx, SmalltalkParser.RULE_method);
		Symbol existingSymbol = currentScope.getSymbol(ctx.selector);
		if ( existingSymbol!=null ) {
			compiler.error("redefinition of method "+ctx.selector+" in "+currentScope.toQualifierString(">>"));
			methodNode.scope = null;
			return;
		}

		STMethod m = compiler.createMethod(ctx.selector, methodNode);
		currentScope.define(m); // must define before defining variables so that method is hooked into scope tree
		compiler.defineArguments(m, ctx.args);
		methodNode.scope = m;
		currentMethod = m;
		pushScope(m);
	}

	@Override
	public void enterPrimitiveMethodBlock(SmalltalkParser.PrimitiveMethodBlockContext ctx) {
		if ( currentScope.getSymbol(ctx.selector)!=null ) {
			compiler.error("redefinition of primitive "+ctx.selector+" in "+currentScope.toQualifierString(">>"));
		}
		SmalltalkParser.MethodContext methodNode = (SmalltalkParser.MethodContext)ctx.getParent();
		String primitiveName = ctx.SYMBOL().getText();
		primitiveName = primitiveName.substring(1); // Strip # from #Foo
		STMethod m =
			compiler.createPrimitiveMethod((STClass) currentScope, ctx.selector, primitiveName,
										   methodNode);
		if(((STClass) currentScope).resolveMethod(m.getName()).getName() == null)
			currentScope.define(m);
		compiler.defineArguments(m, ctx.args);
		methodNode.scope = m;
		currentMethod = m;
		// no need to push; no body for primitives.
	}

	@Override
	public void exitSmalltalkMethodBlock(SmalltalkParser.SmalltalkMethodBlockContext ctx) {
		SmalltalkParser.MethodContext methodNode =
			(SmalltalkParser.MethodContext) Utils.getAncestor(ctx, SmalltalkParser.RULE_method);
		if ( methodNode.scope != null ) {
			popScope(); // pop out of method scope
		}
	}

	@Override
	public void enterFullBody(SmalltalkParser.FullBodyContext ctx) {
		if ( ctx.localVars()!=null ) {
			List<String> vars = getTextValues(ctx.localVars().ID());
			compiler.defineLocals(currentScope, vars);
		}
	}

	@Override
	public void enterEmptyBody(SmalltalkParser.EmptyBodyContext ctx) {
		if ( ctx.localVars()!=null ) {
			List<String> vars = getTextValues(ctx.localVars().ID());
			compiler.defineLocals(currentScope, vars);
		}
	}

	@Override
	public void enterBlock(SmalltalkParser.BlockContext ctx) {
		List<String> args = Collections.emptyList();
		if ( ctx.blockArgs()!=null && ctx.blockArgs().ID()!=null ) {
			args = getTextValues(ctx.blockArgs().ID());
		}
		STBlock blk = compiler.createBlock(currentMethod, ctx);
		currentScope.define(blk); // must occur before defining variables
		compiler.defineArguments(blk, args);
		ctx.scope = blk;
		pushScope(blk);
	}

	@Override
	public void exitBlock(SmalltalkParser.BlockContext ctx) {
		popScope();
	}

	public static List<String> getTextValues(List<TerminalNode> nodes) {
		return Utils.map(nodes, TerminalNode::getText);
	}

	public void pushScope(Scope scope) {
		currentScope = scope;
	}

	public void popScope() {
		currentScope = currentScope.getEnclosingScope();
	}
}
