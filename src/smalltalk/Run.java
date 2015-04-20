package smalltalk;

import org.antlr.v4.runtime.ANTLRInputStream;
import smalltalk.compiler.Compiler;
import smalltalk.compiler.STSymbolTable;
import smalltalk.vm.SystemDictionary;
import smalltalk.vm.VirtualMachine;
import smalltalk.vm.primitive.STObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/** Main entry to the Smalltalk interpreter */
public class Run {
	public static void main(String[] args) throws Exception {
		int fi = 0;
		boolean trace = false;
		if (args.length > 0 && args[0].equals("-trace")) {
			fi = 1;
			trace = true;
		}
		String fileName = null;
		if ( args.length>=1 ) fileName = args[fi];
		compileAndExecute(fileName, trace, trace);
	}

	public static STObject compileAndExecute(String fileName, boolean genDbg, boolean trace) {
		STSymbolTable symtab = compileCore(genDbg);
		compile(symtab, fileName, genDbg);
		return exec(symtab, trace);
	}

	/** Compile the core image.st, which includes Object. Also clear the
	 *  {@link SystemDictionary} for this thread.
	 */
	public static STSymbolTable compileCore(boolean genDbg) {
		return compile(null, "smalltalk/image.st", genDbg);
	}

	public static STSymbolTable compile(STSymbolTable symtab, String fileName, boolean genDbg) {
		Compiler c;
		if ( symtab!=null ) {
			c = new Compiler(symtab);
		}
		else {
			c = new Compiler();
		}
		c.genDbg = genDbg;

		URL imageURL = getImageURL(fileName);
		try {
			ANTLRInputStream imageInput = new ANTLRInputStream(imageURL.openStream());
			imageInput.name = imageURL.getFile();
			symtab = c.compile(imageInput);
			// TODO: semantic checks for unknown vars/fields
		}
		catch (IOException e ) {
			throw new RuntimeException("can't load "+imageURL, e);
		}
		if ( c.errors.size()>0 ) {
			throw new RuntimeException("compile errors: "+c.errors.toString(),null);
		}
		return symtab;
	}

	public static STSymbolTable compileString(STSymbolTable symtab, String input, boolean genDbg) {
		Compiler c;
		if ( symtab!=null ) {
			c = new Compiler(symtab);
		}
		else {
			c = new Compiler();
		}
		c.genDbg = genDbg;

		ANTLRInputStream imageInput = new ANTLRInputStream(input);
		imageInput.name = "<string>";
		symtab = c.compile(imageInput);
		// TODO: semantic checks for unknown vars/fields
		if ( c.errors.size()>0 ) {
			throw new RuntimeException("Image compile errors: "+c.errors.toString(),null);
		}
		return symtab;
	}

	/** Execute program contained in the compiled code among the
	 *  symbols of the symbol table.
	 *
	 *  Return the value left on the stack after invoking the main method.
	 */
	public static STObject exec(STSymbolTable symtab, boolean trace) {
		VirtualMachine vm = new VirtualMachine(symtab);
		vm.trace = trace;
		return vm.execMain();
	}

	public static URL getImageURL(String fileName) {
		URL url;
		File dir = new File(fileName);
		if ( dir.exists() ) {
			try {
				url = dir.toURI().toURL();
			}
			catch (MalformedURLException mue) {
				throw new IllegalArgumentException("bad filename: "+fileName);
			}
		}
		else { // try in classpath
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			url = cl.getResource(fileName);
			if ( url==null ) {
				throw new IllegalArgumentException("No such image file: "+
												   fileName);
			}
		}
		return url;
	}
}
