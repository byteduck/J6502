import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class CPU{
	public final int MEMSIZE = 8000;
	private byte[] mem = new byte[MEMSIZE];
	private int romSize;
	private byte A,X,Y,SP = (byte)0xff;
	public int PC = 0x600;
	public boolean done;
	private Random rand = new Random();
	private long lastTime = 0;
	//boolean N,O,B = true,D,I,Z,C,ETC = true;
	boolean C,N,Z;
	/**
	 * C - Carry (holds the carry out of the MSBit in any arithmetic operation)
	 * Z - Zero (set when a mathematical operation makes zero)
	 * I - Interrupt (true if interrupts enabled)
	 * D - Decimal (use Binary-Coded decimal on ADC and SBC)
	 * B - Set when BRK (software interrupt) is executed
	 * ETC - Unused, always true.
	 * O - Overflow (set when a mathematical operation overflows a byte)
	 * N - Set if the result of the last operation's bit 7 is set to 1
	 */

	public CPU(){
		System.out.println("Starting up, hold on...");
		for(int i = 0; i < MEMSIZE; i++){
			mem[i] = 0;
		}
		System.out.println("Done!");
	}

	/**
	 * Key:
	 * Ind: operand is the value of memory at immediate two bytes
	 * Abs: operand is the immediate two bytes
	 *
	 * Something like LDA (ind,X) would work like this:
	 * Let's say the instruction is LDA ($01,X) and X is $01.
	 * The CPU looks at $01+X=$02 in the zero page, and that is the LSB of a pointer to a value.
	 * So if mem[2] = 0x02 and mem[3] = 0x03, then LDA ($01,X) *WHILE X IS 1*
	 * would load the value at 0x0302 into A.
	 */
	public void cycle(){
		while(System.nanoTime()-lastTime < 100000){}
		mem[0xfe] = (byte)rand.nextInt(256);
		int loc;
		int add;
		int rel;
		switch(getMem(PC)){
			case 0x00:break;
			case 0x01: //ORA (ind,X)
				A |= mem[getWord(getX()+readNext())];
				//ZN(A);
				break;
			case 0x05: //ORA zpg
				A |= mem[readNext()];
				//ZN(A);
				break;
			case 0x06: //ASL zpg
				mem[readNext()] <<= 1;
				break;
			case 0x08: //TODO PHP impl
				break;
			case 0x09: //ORA #
				A |= readByte();
				break;
			case 0x0a: //ASL A
				A <<= 1;
				break;
			case 0x1D: //ORA abs,X
				A = (byte)(A | mem[getXInd()]);
				//ZN(A);
				break;
			case 0x20: //JSR abs
				mem[getSP()+0x100] = (byte) (((PC+2) >> 8) & 0xFF);
				mem[getSP()+0xff] = (byte) (((byte)(PC+2)) & 0xFF);
				PC = readWord()-1;
				SP-=2;
				break;
			case 0x24: //BIT zpg TODO affect flags
				A &= mem[readByte()];
				Z = A != 0;
				break;
			case 0x2c: //BIT abs TODO affect flags
				A &= mem[readWord()];
				Z = A != 0;
				break;
			case 0x38: //SEC
				C = true;
				break;
			case 0x4c: //JMP abs
				PC = readWord()-1;
				break;
			case 0x60: //RTS
				SP+=2;
				PC = getWord(getSP()+0xff);
				break;
			case 0x69: //ADC #
				add = (A & 0xFF)+readNext();
				A = (byte) carry(add);
				break;
			case 0x85: //STA zpg
				mem[readNext()] = A;
				break;
			case 0x8d: //STA abs
				mem[readWord()] = A;
				break;
			case 0x9d: //STA abs,X
				mem[getXInd()] = A;
				break;
			case 0xa2: //LDX #
				X = readByte();
				break;
			case 0xa5: //LDA zpg
				A = mem[readNext()];
				break;
			case 0xa6: //LDX zpg
				X = mem[readNext()];
				break;
			case 0xa9: //LDA #
				A = readByte();
				break;
			case 0xad: //LDA abs
				A = mem[readWord()];
				break;
			case 0xae: //LDX abs
				X = mem[readWord()];
				break;
			case 0xc9: //CMP #
				Z = readByte() == A;
				break;
			case 0xd0: //BNE rel
				rel = getRel();
				if(!Z) PC += rel;
				break;
			case 0xe0: //CPX #
				Z = readByte() == X;
				break;
			case 0xe8: //INX
				X++;
				n(X);
				break;
			case 0xf0: //BEQ
				rel = getRel();
				if(Z) PC += rel;
				break;
			default:
				System.out.println("Unknown opcode "+Debug.hex(getMem(PC))+" @ "+Debug.hex(PC));
				break;
		}
		PC++;
		if(PC >= romSize+0x600){
			done = true;
		}
		lastTime = System.nanoTime();
	}

	/*public void ZN(byte last){
		if((last & 0xFF) == 0) Z = true;
		if(((last & 0x80) >> 7) == 1) N = true;
	}*/

	public int getMem(int pos){
		return mem[pos]&0xFF;
	}

	public byte getMemByte(int pos){
		return mem[pos];
	}

	public int getWord(int pos){
		return (mem[pos] & 0xFF)+(mem[pos+1] << 8);
	}

	public void load(InputStream rom) throws IOException {
		romSize = 0;
		while(rom.available() > 0){
			mem[0x600+romSize] = (byte)rom.read();
			romSize++;
		}
		rom.close();
	}

	private int readWord(){
		PC+=2;
		return getWord(PC-1);
	}

	private int readNext(){
		PC++;
		return getMem(PC);
	}

	private byte readByte(){
		PC++;
		return getMemByte(PC);
	}

	public int getA(){
		return A & 0xff;
	}

	public int getX(){
		return X & 0xff;
	}

	private int getXInd(){
		return getX()+readWord();
	}

	public int getY() {
		return Y & 0xff;
	}

	public int getSP() {
		return SP & 0xFF;
	}

	private int getRel(){
		byte in = readByte();
		int num = in & 0xFF;
		if(((in & 0x80) >> 7) == 1) return -(256-num);
		return num;
	}

	private int carry(int num){
		boolean flag = (num & 0x100) > 0;
		if(flag) C = true;
		if(C) num++;
		if(!flag) C = false;
		return num;
	}

	private void n(byte b){
		if((b & 0x7f) > 0) N = true; else N = false;
	}
}