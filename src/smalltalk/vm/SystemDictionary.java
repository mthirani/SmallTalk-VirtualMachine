package smalltalk.vm;

import smalltalk.compiler.STClass;
import smalltalk.compiler.STMethod;
import smalltalk.compiler.STSymbolTable;
import smalltalk.vm.primitive.STMetaClassObject;
import smalltalk.vm.primitive.STObject;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** Stores predefined system objects like nil, true, false, Transcript as well
 *  as all {@link smalltalk.vm.primitive.STMetaClassObject}'s.  You should
 *  create just one instance of STNil for nil and an instance of STBoolean
 *  for true and for false.
 */
public class SystemDictionary {
	// All metaclass info and any predefined global objects like nil, true, ...
	protected final Map<String,STObject> objects = new LinkedHashMap<>();
	public STSymbolTable stSymbolTable;
	public final VirtualMachine vm;

	public SystemDictionary(VirtualMachine vm, STSymbolTable stSymbolTable) {
		this.vm = vm;
		this.stSymbolTable = stSymbolTable;
		initPredefinedObjects();
		//symtabToSystemDictionary(stSymbolTable);			//Newly added
	}

	/** Convert the symbol table with classes, methods, and compiled code
	 *  (as computed by the compiler) into a system dictionary that has
	 *  meta-objects.
	 *
	 *  This method assumes that the compiler has augmented the symbol table
	 *  symbols such as {@link STMethod} with pointers to the
	 *  {@link smalltalk.vm.primitive.STCompiledBlock}s.
	 */
	public void symtabToSystemDictionary(STSymbolTable symtab) {

	}

	/** Define predefined object Transcript. */
	public void initPredefinedObjects() {
		/*STClass stclass = new STClass("TranscriptStream", "", stSymbolTable);
		STMetaClassObject stMetaClassObject = new STMetaClassObject(vm, stclass);
		objects.put("TranscriptStream", stMetaClassObject);*/
	}

	public STObject lookup(String id) {
		return objects.get(id);
	}

	public STMetaClassObject lookupClass(String id) {
		if(id.equals("TranscriptStream")){
			STClass stclass = new STClass("TranscriptStream", "", stSymbolTable);
			STMetaClassObject stMetaClassObject = new STMetaClassObject(vm, stclass);
			return stMetaClassObject;
		}
		STMetaClassObject stMetaClassObject = null;
		if(objects.get(id) != null)
			stMetaClassObject = objects.get(id).getSTClass();

		return stMetaClassObject;
	}

	public void defineMetaObject(String name, STMetaClassObject meta) {
		objects.put(name, meta);
	}

	public Collection<STObject> getObjects() { return this.objects.values(); }

	public void define(String id, STObject v) {
		objects.put(id, v);
	}
}
