package smalltalk.test;

import org.antlr.symtab.GlobalScope;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.ParserRuleContext;
import smalltalk.Run;
import smalltalk.compiler.Compiler;
import smalltalk.compiler.STSymbolTable;
import smalltalk.vm.VirtualMachine;
import smalltalk.vm.primitive.STMetaClassObject;
import smalltalk.vm.primitive.STObject;

import static junit.framework.TestCase.assertEquals;

public class BaseTest {
	public GlobalScope parseAndGetGlobalScope(String input) {
		return parseAndDefineSymbols(input).getSymbolTable().GLOBALS;
	}

	public CompilerWithHooks parseAndDefineSymbols(String input) {
		CompilerWithHooks compiler = new CompilerWithHooks();
		ParserRuleContext tree = compiler.parseClasses(new ANTLRInputStream(input));
		if ( tree!=null ) {
			compiler.defSymbols(tree);
			compiler.resolveSymbols(tree);
		}
		return compiler;
	}

	public void execAndCheck(String input, String expecting,
							 boolean withLinkedList, boolean withDict,
							 boolean trace, boolean genDbg) {
		STSymbolTable symtab = Run.compileCore(genDbg);
		if ( withLinkedList ) {
			Run.compile(symtab, "smalltalk/test/linkedlist.st", genDbg);
		}
		if ( withDict ) {
			Run.compile(symtab, "smalltalk/test/dict.st", genDbg);
		}
		Run.compileString(symtab, input, genDbg);
		STObject result = Run.exec(symtab, trace);
		assertEquals(expecting, result.toString());
	}

	public void execAndCheckWithLinkedList(String input, String expecting, boolean trace, boolean genDbg) {
		boolean withLinkedList = true;
		boolean withDict = false;
		execAndCheck(input, expecting, withLinkedList, withDict, trace, genDbg);
	}

	public void execAndCheckWithLinkedList(String input, String expecting) {
		boolean genDbg = true;
		boolean trace = false;
		execAndCheckWithLinkedList(input, expecting, trace, genDbg);
	}

	public void execAndCheckWithDict(String input, String expecting, boolean trace, boolean genDbg) {
		boolean withLinkedList = true;
		boolean withDict = true;
		execAndCheck(input, expecting, withLinkedList, withDict, trace, genDbg);
	}

	public void execAndCheckWithDict(String input, String expecting) {
		boolean genDbg = true;
		boolean trace = false;
		execAndCheckWithDict(input, expecting, trace, genDbg);
	}

	public void execAndCheck(String input, String expecting, boolean trace, boolean genDbg) {
		boolean withLinkedList = false;
		boolean withDict = false;
		execAndCheck(input, expecting, withLinkedList, withDict, trace, genDbg);
	}

	public void execAndCheck(String input, String expecting, boolean genDbg) {
		boolean withLinkedList = false;
		boolean trace = false;
		boolean withDict = false;
		execAndCheck(input, expecting, withLinkedList, withDict, trace, genDbg);
	}

	public void execAndCheck(String input, String expecting) {
		boolean genDbg = true;
		execAndCheck(input, expecting, genDbg);
	}

	public String compile(String input) {
		return compile(input, false);
	}

	public String compile(String input, boolean genDbg) {
		StringBuilder code = new StringBuilder();
		smalltalk.compiler.Compiler c = new Compiler();
		c.genDbg = genDbg;
		ANTLRInputStream ais = new ANTLRInputStream(input);
		STSymbolTable symtab = c.compile(ais);
		VirtualMachine vm = new VirtualMachine(symtab);
		for (STObject o : vm.systemDict.getObjects()) {
			if ( o instanceof STMetaClassObject ) {
				STMetaClassObject meta = (STMetaClassObject)o;
				code.append(meta.toTestString());
			}
		}
		return code.toString();
	}
}
