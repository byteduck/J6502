/**
 * Created by aaron on 12/1/2016.
 */
public class Debug{
	public static String hex(int n) {
		return String.format("0x%02X", n);
	}
	public static String hex(int n,int length) {
		return String.format("0x%0"+length+"X", n);
	}
}
