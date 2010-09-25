import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.util.HashMap;

/**
 * A panel for outputting the strings generated from an MR.
*/
public class OutputPanel extends JPanel {
	private MeaningRepresentation mr;
	private static final String[] languages = {"en", "de"};
	
	public OutputPanel(MeaningRepresentation mr) {
		this.mr = mr;
	}
	
	public void setMR(MeaningRepresentation newMR) {
		mr = newMR;
	}
	
	protected void paintComponent(Graphics g) {
		g.setFont(new Font("Helvetica", Font.PLAIN, 12));
	
		g.clearRect(0, 0, getWidth(), getHeight());
		for (int i = 0; i < languages.length; i++) {
			String output = "?";
			try {
				output = mr.generate(languages[i]);
			}
			catch (Exception e) {
				//do nothing!
			}
			String newOutput = "";
			try {
				newOutput = new String(output.getBytes(), "UTF-8");
			}
			catch (java.io.UnsupportedEncodingException e) {
				newOutput = output;
			}
			g.drawString(newOutput, 5, 16 + 16 * i); //was 16 / 16
		}
	}
}
