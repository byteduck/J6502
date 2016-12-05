import java.io.FileInputStream;
import java.io.IOException;

public class J6502{
	public static void main(String[] args) throws IOException {
		CPU cpu = new CPU();
		cpu.load(new FileInputStream("program.bin"));
		Screen screen = new Screen(cpu);
		while(!cpu.done){
			cpu.cycle();
			screen.repaint();
		}
	}
}