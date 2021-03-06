package utility;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class TextLCD extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4927024550703654533L;
	private static JTextArea lcd;
	private static String[] displayText = {"", "", "", "", "", "", "", ""};
	private static Keys buttons;

	public TextLCD() {
		setPreferredSize(new Dimension(178, 128));
		setBackground(new Color(190, 210, 170));
		setBounds(80, 57, 178, 135);
		
		lcd = new JTextArea(10, 19);
		lcd.setBackground(new Color(190, 210, 170));
		lcd.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		lcd.setLineWrap(true);
		lcd.setFont(new Font("Courier", 0, 15));
		lcd.setEditable(false);
		clear();
		add(lcd);
		buttons = Brick.getKeys();
	}

	public String inputName() {
		String inName = "ev3";
		clear();
		drawString("EV3 calibration", 0, 0);
		drawString("Give your robot", 0, 1);
		drawString("a name:", 0, 2);
		inName = inputString(true, "EV3", 3);
		if (inName != null)
		{
			drawString("> " + inName, 0, 4);
			Delay.msDelay(500);
		}
		return inName;
	}

	public String inputString(boolean type, String name, int row) {
		String alpha = " ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz-0123456789";
		if (type == false) alpha = "0123456789.";
		int chr = 0; // characterposition in the alpha string
		int pos = 0; // position of the character
		int lngt = name.length();
		String input = "";
		String[] in = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
		for (int i = 0; i < name.length(); i++) {
			in[i] = name.substring(i, i+1);
			drawString(in[i], i, row);
		}
		chr = alpha.indexOf(in[0]);
		Delay.msDelay(150);
		while(true)
		{
			int button;
			do
			{
				button = buttons.getButtons(10);
			}
			
			while (button == 0);
			if (button == Keys.ID_UP) 
			{
				chr = chr + 1;
				if (chr>=alpha.length()) chr = 0;
				drawString(alpha.substring(chr, chr+1), pos, row, true);
				in[pos] = alpha.substring(chr, chr+1);
			}
			if (button == Keys.ID_DOWN) 
			{
				chr = chr - 1;
				if (chr<0) chr = alpha.length()-1;
				drawString(alpha.substring(chr, chr+1), pos, row, true);
				in[pos] = alpha.substring(chr, chr+1);
			}
			if (button == Keys.ID_RIGHT) 
			{
				in[pos] = alpha.substring(chr, chr+1);
				drawString(alpha.substring(chr, chr+1), pos, row, false);
				name = ""; // rebuild the name
				for (int i = 0; i < 15; i++) {
					name = name + in[i];
				}
				name = name.trim(); // remove trailing spaces
				lngt = name.length();
				System.out.println("naam: " + name + " / lengte: " + lngt);
								
				pos = pos + 1;
				if (pos == lngt) // if the cursor is behind the name
				{
					chr = 0; // insert a space
					lngt++; // and increase the length of the string
				}
				if (pos > lngt) // if the cursor is after the trailing space
				{
					pos = 0; // move to the start of the string
				}
				if (in[pos] != "") chr = alpha.indexOf(in[pos]); // pick up the value of the new position 
				drawString(alpha.substring(chr, chr+1), pos, row, true); // write it again with the marker underneath
			}
			if (button == Keys.ID_LEFT) 
			{
				in[pos] = alpha.substring(chr, chr+1);
				drawString(alpha.substring(chr, chr+1), pos, row, false);
				pos = pos - 1;
				if (pos < 0) pos = lngt-1;
				if (in[pos] != "") chr = alpha.indexOf(in[pos]);
				drawString(alpha.substring(chr, chr+1), pos, row, true);
			}
			if (button == Keys.ID_ENTER) 
			{
				in[pos] = alpha.substring(chr, chr+1);
				for (int i = 0; i < in.length; i++) {
					input = input+in[i];
				}
				input = input.trim();
				return input;
			}
			if (button == Keys.ID_ESCAPE) return null;
		}
	}

	public void drawString(String message, int col, int row) {
		int max = message.length();
		String oldMsg = displayText[row];
		String newMsg = "";
		for (int i = 0; i < 18; i++) {
			if (i >= col && i < col + max) {
				newMsg = newMsg + message.substring(i - col, i - col + 1);
			} else {
				newMsg = newMsg + oldMsg.substring(i, i+1);
			}
		}
		displayText[row] = newMsg + "\n";
		refreshLCD();
	}

	public void screenWait(int time, int row) {
		for (int i = 0; i < 9; i++) {
			drawString("* ", i*2, row);
			Delay.msDelay(time/9);
		}
		System.out.println("");
	}

	public void clear() {
		for (int i = 0; i < 8; i++) {
			displayText[i] = "                  \n";
		}
		refreshLCD();
	}

	private void refreshLCD() {
		StringBuilder builder = new StringBuilder();
		for(String s : displayText) {
			builder.append(s);
		}
		String text = builder.toString();
		lcd.setText(text);
	}

	/** Take double value input from the EV3 brick.
	* @param digits The number of integer digits (before the decimal point): Maximum 6
	* @param floats The number of decimal figures (after the decimal point): Maximum 4
	* @param value The default value (double) that the user can start from.
	* @param pos The starting position to display the value.
	* @param row The row to display the value.
	*/
	
	public String inputNumber(int digits, int floats, double value, int pos, int row) {
		double[] increments = {100000, 10000, 1000, 100, 10, 1, 0.0, 0.1, 0.01, 0.001, 0.0001, 0.00001};
		double[] increment = Arrays.copyOfRange(increments, 6-digits, 11);
		int limit = digits + 1 + floats; // total number of position for the double format
		int col = pos + digits - 1; // position of the 'cursor' (before de decimal point
		String decimalValue = String.format("%1." + floats + "f", value); // double value with correct format
		int lead = limit - decimalValue.length(); // leading space
	
		drawString(String.format(Locale.CANADA, "%1$," + limit + "." + floats + "f", value), pos, row);
		decimalValue = decimalValue.substring(col - lead, col - lead + 1);
//		drawString(decimalValue, col, row);
		drawString("^", col, row+1);
		while(true)
		{
			int button;
			do
			{
				button = buttons.getButtons(10);
			}
			
			while (button == 0);
			if (button == Keys.ID_UP) 
			{
				value = value + increment[col];
			} 
			if (button == Keys.ID_DOWN) 
			{
				value = value - increment[col];
//				if (value < 0) value = value + increment[col];
			} 
			if (button == Keys.ID_RIGHT) 
			{
//				lcd.drawString(" ", col + pos, row);
				drawString(" ", col + pos, row+1);
				col = col + 1;
//				System.out.println("col: " + col + " / limit: " + limit);
				if (col == limit) col = lead;
				if (col == digits) col = digits + 1;
			} 
			if (button == Keys.ID_LEFT) 
			{
//				lcd.drawString(" ", col + pos, row);
				drawString(" ", col + pos, row+1);
				col = col - 1;
				if (col == digits) col = digits - 1; // skip the decimal point
				if (decimalValue == "_" || col < 0) col = limit - 1; // if at the start, go to the end
			}
			decimalValue = String.format("%1." + floats + "f", value);
			if (decimalValue.length() > limit) {
				value = value - increment[col];
				decimalValue = String.format("%1." + floats + "f", value);
			}
			lead = limit - decimalValue.length();
			drawString(String.format(Locale.CANADA, "%1$" + limit + "." + floats + "f", value), pos, row);
			if (col - lead < 0) 
			{
				decimalValue = "_";
			} else 
			{
				decimalValue = decimalValue.substring(col - lead, col - lead + 1);
			}
//			LCD.drawString(decimalValue, col + pos, row, true);
			drawString("^", col + pos, row+1); // remove +1 for real EV3
			Delay.msDelay(200);
			if (button == Keys.ID_ENTER) 
			{
				return String.format(Locale.CANADA, "%1$" + limit + "." + floats + "f", value);
			}
			if (button == Keys.ID_ESCAPE) return null;
		}
	}

	public void drawString(String message, int col, int row, boolean b) {
		int max = message.length();
		String oldMsg = displayText[row];
		String newMsg = "";
		String oldSel = displayText[row+1].replace('^', ' ');
		String newSel = "";
		for (int i = 0; i < 18; i++) {
			if (i >= col && i < col + max) {
				newMsg = newMsg + message.substring(i - col, i - col + 1);
				newSel = newSel + "^";
			} else {
				newMsg = newMsg + oldMsg.substring(i, i+1);
				System.out.println(oldSel.substring(i, i+1));
				newSel = newSel + oldSel.substring(i, i+1);
			}
		}
//		if (b == true) newMsg = newMsg.toUpperCase();
		displayText[row] = newMsg + "\n";
		if (b == true) displayText[row+1] = newSel;
		refreshLCD();
	}

	/**
	 * Draw a single char on the LCD at specified x,y co-ordinate.
	 * @param c Character to display
	 * @param x X location
	 * @param y Y location
	 */
	public void drawChar(char c, int x, int y) {
		String oldMsg = displayText[y];
		String newMsg = oldMsg.substring(0,x) + c + oldMsg.substring(x+1);
		displayText[y] = newMsg + "\n";
		refreshLCD();
	}
	
	public void drawInt(int i, int x, int y) {
		drawString(""+i, x, y);
	}

	/**
	 * Clear an LCD display row
	 * @param y the row to clear
	 */
	public void clear(int i) {
		displayText[i] = "                  \n";
		refreshLCD();
	}

	public void refresh() {
		StringBuilder builder = new StringBuilder();
		for(String s : displayText) {
			builder.append(s);
		}
		String text = builder.toString();
		lcd.setText(text);
	}
}