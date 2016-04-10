package smalltalk.test;

import org.antlr.v4.runtime.Parser;
import smalltalk.compiler.STSymbolTable;
import smalltalk.compiler.SmalltalkParser;

/** This class is a compiler that provides hooks for testing */
public class CompilerWithHooks extends smalltalk.compiler.Compiler {
	public STSymbolTable getSymbolTable() { return symtab; }
	public SmalltalkParser.FileContext getFileTree() { return fileTree; }
	public Parser getParser() { return parser; }
}
