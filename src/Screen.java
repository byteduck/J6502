import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * Created by aaron on 12/1/2016.
 */
public class Screen extends JFrame {

	private static final Color[] colors = new Color[]{
			Color.BLACK,Color.WHITE,new Color(0x880000),new Color(0xaaffee),new Color(0xcc44cc),
			new Color(0x00cc55),new Color(0x0000aa),new Color(0xeeee77),new Color(0xdd8855),
			new Color(0x664400),new Color(0xff7777),new Color(0x333333),new Color(0x777777),
			new Color(0xaaff66),new Color(0x0088ff),new Color(0xbbbbbb)
	};

	public CPU cpu;
	public Screen(CPU cpu){
		this.cpu = cpu;
		this.setLayout(new BorderLayout());
		this.add(new ScreenPanel(), BorderLayout.CENTER);
		this.add(new DebugPanel(), BorderLayout.SOUTH);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setTitle("J6502");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	class ScreenPanel extends JPanel {
		@Override
		public Dimension getPreferredSize(){
			return new Dimension(640,640);
		}
		@Override
		public void paint(Graphics g){
			for(int x = 0; x < 32; x++){
				for(int y = 0; y < 32; y++){
					int pos = x+y*32;
					g.setColor(colors[cpu.getMem(0x200+pos)%0x10]);
					g.fillRect(x*20,y*20,20,20);
				}
			}
		}
	}

	class DebugPanel extends JPanel{
		JLabel topLabel, bottomLabel;
		DebugPanel(){
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			topLabel = new JLabel("LOADING");
			topLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, topLabel.getFont().getSize()));
			bottomLabel = new JLabel("LOADING");
			bottomLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, bottomLabel.getFont().getSize()));
			add(topLabel);
			add(bottomLabel);
		}
		@Override
		public void paint(Graphics g){
			this.topLabel.setText("A = "+Debug.hex(cpu.getA(),2)+" | X = "+Debug.hex(cpu.getX(),2)+" | Y = "+Debug.hex(cpu.getY(),2)+" | PC = "+Debug.hex(cpu.PC,4)+" | SP = "+Debug.hex(cpu.getSP(),2));
			this.bottomLabel.setText("Flags: "+(cpu.C ? "C": "")+(cpu.Z ? "Z": "")/*+(cpu.I ? "I": "")+(cpu.D ? "D": "")+(cpu.B ? "B": "")+(cpu.O ? "O": "")*/+(cpu.N ? "N": ""));
			super.paint(g);
		}
	}
}
