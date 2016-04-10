package smalltalk.vm.primitive;

import smalltalk.vm.VirtualMachine;

import java.text.DecimalFormat;

/** Backing class for Smalltalk Float. */
public class STFloat extends STObject {
	public final float v;

	public STFloat(VirtualMachine vm, float v) {
		super(vm.lookupClass("Float"));
		this.v = v;
	}

	public static STObject perform(BlockContext ctx, int nArgs, Primitive primitive) {
		return null;
	}

	@Override
	public String toString() {
		DecimalFormat df = new DecimalFormat("#.#####");
		return df.format(v);
	}
}
