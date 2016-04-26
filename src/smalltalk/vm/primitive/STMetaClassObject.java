package smalltalk.vm.primitive;

import org.antlr.symtab.FieldSymbol;
import org.antlr.symtab.MethodSymbol;
import org.stringtemplate.v4.ST;
import smalltalk.compiler.STClass;
import smalltalk.compiler.STMethod;
import smalltalk.vm.VirtualMachine;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.antlr.symtab.Utils.map;

/** A meta class that has info about ST classes like Java's Class class.
 *
 *  Expose this as a (meta) object in VM but it has little functionality and
 *  is mainly for uniformity that classes are also objects.
 */
public class STMetaClassObject extends STObject {
	public final VirtualMachine vm;
	public final String name;
	public final STMetaClassObject superClass;
	public final List<STObject> fields;							//Changed from public final List<String> fields;
	public final Map<String,STCompiledBlock> methods;

	public STMetaClassObject(VirtualMachine vm, STClass classSymbol) {
		super(null); // metaclass for a metaclass is 'this' but 'this' doesn't exist yet; see override of getSTClass()
		this.vm = vm;
		if(classSymbol.getSuperClassScope() != null)
			this.superClass = new STMetaClassObject(vm, (STClass) classSymbol.getSuperClassScope());
		else
			this.superClass = null;
		this.name = classSymbol.getName();
		fields = new ArrayList<>();
		// make space for ALL fields, including inherited ones
		for (FieldSymbol f : classSymbol.getDefinedFields()) {
			fields.add(new STString(vm, f.getName()));				//Changed from fields.add(f.getName());
		}
		// for all methods defined in classSymbol, map method name to its compiled method
		methods = new HashMap<>();
		for (MethodSymbol m : classSymbol.getDefinedMethods()) {
			STCompiledBlock m1 = ((STMethod)m).compiledBlock;
			m1.enclosingClass = this;			//Added by Mayank for setting the enclosingClass for all blocks
			//methods.put(m.getName(), ((STMethod)m).compiledBlock);
			methods.put(m.getName(), m1);
		}
		// set enclosingClass for all nested blocks within method
	}

	@Override
	public STMetaClassObject getSTClass() {
		return this;
	}

	public static STObject perform(BlockContext ctx, int nArgs, Primitive primitive) {
		VirtualMachine vm = ctx.vm;
		ctx.vm.assertNumOperands(nArgs+1); // ensure args + receiver
		int firstArg = ctx.sp - nArgs + 1;
		STObject receiver = ctx.stack[firstArg-1];
		STObject result = vm.nil();
		//if ( firstArg-1 >= 0 ) receiver = ctx.stack[firstArg-1];
		switch ( primitive ) {
			case Object_Class_BASICNEW:
				ctx.sp--;
				result = new STObject((STMetaClassObject) receiver);
				break;
			case Object_Class_ERROR:
				vm.error(ctx.stack[firstArg].asString().toString());
				break;
			case TranscriptStream_SHOW:
				ctx.sp--;
				result = receiver.asString();
				break;
		}
		return result;
	}

	public String getName() { return name; }

	public STCompiledBlock resolveMethod(String name) {
		return methods.get(name);
	}

	public int getNumberOfFields() {
		return fields.size();
	}

	public String toTestString() {
		ST template = new ST(
			"name: <name>\n" +
			"superClass: <superClass.name>\n" +
			"fields: <fields; separator={,}>\n" +
			"methods:\n" +
			"    <methods; separator={<\\n>}>"
		);
		template.add("name", name);
		template.add("superClass", superClass);
		template.add("fields", fields);
		template.add("methods", map(methods.values(), STCompiledBlock::toTestString));
		return template.render();
	}

	@Override
	public String toString() {
		return "class "+name;
	}
}
