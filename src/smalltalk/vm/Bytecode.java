package smalltalk.vm;

import smalltalk.vm.primitive.STCompiledBlock;

import java.util.ArrayList;
import java.util.List;

public class Bytecode {
	public static final int MAX_OPNDS = 3; // Or single opnd indicating variable number

	public static int combineLineCharPos(int line, int charPos) {
		return line << 8 | charPos & 0xFF;
	}

	public static int lineFromCombined(int combined) {
		return (combined >> 8)/256;
	}

	public static int charPosFromCombined(int combined) {
		return combined & 0xFF;
	}

	public enum OperandType {
		NONE(0), BYTE(1), CHAR(2), ADDR(4), SHORT(2), LITERAL(2), INT(4), FLOAT(4),
		DBG_LOCATION(4) // upper 24 bits are line number; lowest eight bits are the character position within the line
		;
		public final int sizeInBytes;
		OperandType(int sizeInBytes) { this.sizeInBytes = sizeInBytes; }
	}

	public static class Instruction {
		final String name; // E.g., "pop", "new"
		final OperandType[] type = new OperandType[MAX_OPNDS];
		int n = 0;
		public Instruction(String name) {
			this(name,OperandType.NONE,OperandType.NONE,OperandType.NONE); n=0;
		}
		public Instruction(String name, OperandType a) {
			this(name,a,OperandType.NONE,OperandType.NONE); n=1;
		}
		public Instruction(String name, OperandType a, OperandType b) {
			this(name,a,b,OperandType.NONE); n=2;
		}
		public Instruction(String name, OperandType a, OperandType b, OperandType c) {
			this.name = name;
			type[0] = a;
			type[1] = b;
			type[2] = c;
			n = MAX_OPNDS;
		}
	}

	// don't use enum for efficiency; don't want code block to
	// be an array of objects (Bytecode[]). We want it to be byte[].

	// INSTRUCTION BYTECODES (byte is signed; use a short to keep 0..255)
	public static final short NIL					= 1;
	public static final short SELF					= 2;
	public static final short TRUE					= 3;
	public static final short FALSE					= 4;

	public static final short PUSH_CHAR				= 10;
	public static final short PUSH_INT				= 11;
	public static final short PUSH_FLOAT			= 12;
	public static final short PUSH_FIELD			= 13;
	public static final short PUSH_LOCAL 			= 14;
	public static final short PUSH_LITERAL			= 15;
	public static final short PUSH_GLOBAL			= 16;
	public static final short PUSH_ARRAY			= 17;
	public static final short STORE_FIELD			= 18;
	public static final short STORE_LOCAL 			= 19;
	public static final short POP					= 20;

	public static final short SEND					= 25;
	public static final short SEND_SUPER			= 26;
	public static final short BLOCK					= 27;
	public static final short BLOCK_RETURN			= 28; // finish [...]
	public static final short RETURN				= 29; // finish method

	public static final short DBG					= 30;

	/** Used for disassembly; describes instruction set */
	public static final Instruction[] instructions = new Instruction[] {
		null, // <INVALID>
		new Instruction("nil"),				// index is the opcode
		new Instruction("self"),
		new Instruction("true"),
		new Instruction("false"),

		null, null, null, null, null, 		// leave room for gap in ints

		new Instruction("push_char", OperandType.CHAR),
		new Instruction("push_int", OperandType.INT),
		new Instruction("push_float", OperandType.FLOAT),
		new Instruction("push_field", OperandType.SHORT),
		new Instruction("push_local", OperandType.SHORT, OperandType.SHORT),  // relative scope and index
		new Instruction("push_literal", OperandType.LITERAL),
		new Instruction("push_global", OperandType.LITERAL),
		new Instruction("push_array", OperandType.SHORT),
		new Instruction("store_field", OperandType.SHORT),
		new Instruction("store_local", OperandType.SHORT, OperandType.SHORT),
		new Instruction("pop"),

		null, null, null, null, 		// leave room for gap in ints

		new Instruction("send", OperandType.SHORT, OperandType.LITERAL),
		new Instruction("send_super", OperandType.SHORT, OperandType.LITERAL),
		new Instruction("block", OperandType.SHORT), // block number within method
		new Instruction("block_return"),
		new Instruction("return"),

		new Instruction("dbg", OperandType.LITERAL, OperandType.DBG_LOCATION), // filename, line:charpos in file
	};

	public static String disassemble(STCompiledBlock blk, int start) {
		StringBuilder buf = new StringBuilder();
		int i=start;
		while (blk.bytecode!=null && i<blk.bytecode.length) {
			i = disassembleInstruction(buf, blk, i);
			buf.append('\n');
		}
		return buf.toString();
	}

	public static String disassembleInstruction(STCompiledBlock blk, int ip) {
		StringBuilder buf = new StringBuilder();
		disassembleInstruction(buf, blk, ip);
		return buf.toString();
	}

	public static int disassembleInstruction(StringBuilder buf, STCompiledBlock blk, int ip) {
		byte[] code = blk.bytecode;
		int opcode = code[ip];
		if ( ip>=code.length ) {
			throw new IllegalArgumentException("ip out of range: "+ip);
		}
		Bytecode.Instruction I =
			Bytecode.instructions[opcode];
		if ( I==null ) {
			throw new IllegalArgumentException("no such instruction "+opcode+
				" at address "+ip+" of "+ blk.name+"\n"+blk.toString());
		}
		String instrName = I.name;
		if ( instrName.equals("dbg") ) {
			buf.append(String.format("%04d:  %s ", ip, instrName));
		}
		else {
			buf.append(String.format("%04d:  %-15s", ip, instrName));
		}
		ip++;
		if ( I.n==0 ) {
			buf.append("  ");
			return ip;
		}
		List<String> operands = new ArrayList<>();
		for (int i=0; i<I.n; i++) {
			switch ( I.type[i] ) {
				case NONE:
					break;
				case BYTE:
					operands.add(String.valueOf(code[ip]));
					break;
				case CHAR :
					operands.add(String.valueOf(getShort(code, ip)));
					break;
				case SHORT :
					operands.add(String.valueOf(getShort(code, ip)));
					break;
				case LITERAL:
					if ( blk.literals==null ) operands.add("<no literals>");
					else {
						int lit = getShort(code, ip);
						operands.add(String.valueOf('\''+blk.literals[lit]+'\''));
					}
					break;
				case ADDR :
				case INT :
					operands.add(String.valueOf(getInt(code, ip)));
					break;
				case FLOAT :
					float f = Float.intBitsToFloat(getInt(code, ip));
					operands.add(String.valueOf(f));
					break;
				case DBG_LOCATION :
					int lineAndPos = getInt(code, ip);
					int charPos = charPosFromCombined(lineAndPos);
					int line = lineFromCombined(lineAndPos);
					operands.add(line+":"+charPos);
					break;
				default :
					System.err.println("invalid opnd type: "+I.type[i]);
					break;
			}
			ip += I.type[i].sizeInBytes;
		}
		for (int i = 0; i < operands.size(); i++) {
			String s = operands.get(i);
			if ( i>0 ) buf.append(", ");
			buf.append( s );
		}
		return ip;
	}

	public static int getInt(byte[] memory, int index) {
		int b1 = memory[index++]&0xFF; // high byte
		int b2 = memory[index++]&0xFF;
		int b3 = memory[index++]&0xFF;
		int b4 = memory[index]&0xFF; // low byte
		return b1<<(8*3) | b2<<(8*2) | b3<<(8*1) | b4;
	}

	public static int getShort(byte[] memory, int index) {
		int b1 = memory[index++]&0xFF; // mask off sign-extended bits
		int b2 = memory[index]&0xFF;
		return b1<<(8*1) | b2;
	}

}
