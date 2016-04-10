package smalltalk.compiler;

//import com.sun.tools.javac.code.Symbol;
import org.antlr.symtab.MethodSymbol;
import org.antlr.symtab.Scope;
import org.antlr.v4.runtime.ParserRuleContext;
import smalltalk.vm.primitive.STCompiledBlock;
import smalltalk.vm.primitive.STMetaClassObject;

/** A block is an anonymous method defined within a method or another block.
 *  Ala gnu impl., blocks aren't stored en masse inline.
 *
 *  Blocks are tracked by enclosing method's {@link STCompiledBlock} object.
 *
 *  A method does not have a block that holds the locals. We mix the
 *  arguments and locals together to be consistent with blocks
 *  like [:x | |a b| ...] that have both arguments and locals.
 *
 *  Block/method symbols also keep a reference to an {@link STCompiledBlock}
 *  object that results from compilation. The VM collects these compiled
 *  blocks and creates {@link STMetaClassObject} objects for each
 *  {@link //Symbol.ClassSymbol}.
 *
 *  In some sense, a block should subclass a method symbol instead of
 *  method subclassing block. But, method is somehow a "larger" concept
 *  than a block and can be seen as a specialization. As far as implementation,
 *  however, it would be better to have the block subclass the method to
 *  add the index field. That is how I originally had it but it got confusing
 *  from a usage point of view. For example, look at the name and argument type
 *  of this method:
 *
 *  STCompiledBlock getCompiledBlock(STMethod blk, ...)
 *
 *  Weird, right? Now, STBlock pretty much has all fields and I view a
 *  method as a block with a name.
 */
public class STBlock extends MethodSymbol {
	/** The block number within the surrounding method or block.
	 *  To push a code block onto the operand stack at runtime,
	 *  we push use index as an argument of the BLOCK instruction.
	 *  It's also used to conjure up the method name; see getSelector().
	 */
	public final int index;

	public int numNestedBlocks;

	public STCompiledBlock compiledBlock;

	/** Used by subclass STMethod */
	protected STBlock(String name, ParserRuleContext tree) {
		super(name);
		setDefNode(tree);
		index = -1;
	}

	/** Create a block object within a specific method */
	public STBlock(STMethod method, ParserRuleContext tree) {
		super(method.getName() + "-block" + method.numNestedBlocks);
		setDefNode(tree);
		index = method.numNestedBlocks++;
	}

	public boolean isMethod() { return false; }

	public String getSelector() {
		return getName();
	}

	public int nargs() {
		if(compiledBlock != null)
			return compiledBlock.nargs;
		else
			return 0;
	}

	public int nlocals() {
		return compiledBlock.nlocals;
	}

	/** Given the name of a local variable or argument, return the index from 0.
	 *  The arguments come first and then the locals. For example,
	 *  at: x put: y [|a| ...]
	 *  has  indexes x@0, y@1, a@x.
	 */
	public int getLocalIndex(String name) {
		return 0;
	}

	/** Look for name in current block; keep looking upwards in
	 *  enclosingScope until found; return how many scopes we had to
	 *  jump to find name. 0 indicates same scope.
	 */
	public int getRelativeScopeCount(String name) {
		return 0;
	}
}
