import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class CPU{
	public final int MEMSIZE = 8000;
	private byte[] mem = new byte[MEMSIZE];
	private int romSize;
	private byte A,X,Y,SP;
	public int PC = 0x600;
	public boolean done;
	private Random rand = new Random();
	private long lastTime = 0;

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
	 */
	public void cycle(){
		while(System.nanoTime()-lastTime < 100000){}
		mem[0xfe] = (byte)rand.nextInt(256);
		int loc;
		switch(getMem(PC)){
			case 0x00:break;
			case 0x01: //ORA (ind,X)

				break;
			case 0x05: //ORA zpg
				break;
			case 0x1D: //ORA abs,X
				A = (byte)(A | mem[getXInd()]);
				break;
			case 0x4c: //JMP abs
				PC = readWord()-1;
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
			case 0x8d: //STA abs
				mem[readWord()] = A;
				break;
			default:
				System.out.println("Unknown opcode "+Debug.hex(getMem(PC)));
				break;
		}
		PC++;
		if(PC >= romSize+0x600){
			done = true;
		}
		lastTime = System.nanoTime();
	}

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
			romSize++;
			mem[0x600+romSize] = (byte)rom.read();
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
}