package swingupdate;

import java.awt.*;
import javax.swing.*;

/**
 * Seg�df�ggv�nyek gy�jtem�nye Swing-hez.
 * @author Sg�netz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class SwingHelpers {
	/**
	 * Komponens m�ret�nek er�ltet�se.
	 * @param component A komponens
	 * @param x Sz�less�g
	 * @param y Magass�g
	 */
	public static void ForceSize(Component component, int x, int y) {
		Dimension size = new Dimension(x, y); // A f�ggv�nyek egy r�sze csak dimenzi�t fogad el
		component.setMinimumSize(size); // pl. BoxLayout
		component.setPreferredSize(size); // pl. FlowLayout, GridLayout
		component.setMaximumSize(size); // BoxLayoutn�l kell a minimum mell�
	}

	/**
	 * Komponens korl�tainak jav�t�sa, ha t�bb helyet foglalna, mint sz�ks�ges.
	 * @param component A komponens
	 */
	public static void FixBounds(Component component) {
		ForceSize(component, component.getPreferredSize().width, component.getPreferredSize().height); // A sz�ks�ges m�ret r�er�ltet�se
	}

	/**
	 * Abszol�t elrendez�s� panelek korl�tait jav�tja, mivel azok nem sz�molj�k ki maguknak.
	 * @param panel A panel
	 */
	public static void FixNullBounds(JPanel panel) {
		int xMax = 0, yMax = 0; // C�l m�ret, mindk�t tengelyen a legnagyobbat keress�k
		Component[] components = panel.getComponents(); // Az �sszes komponenst...
		for (Component component : components) { // ...meg kell vizsg�lni
			int x = component.getX() + component.getWidth(), y = component.getY() + component.getHeight(); // Komponens v�gpontjai, maximumkeres�s ezekb�l
			if (xMax < x)
				xMax = x;
			if (yMax < y)
				yMax = y;
		}
		panel.setPreferredSize(new Dimension(xMax, yMax)); // A kisz�molt m�ret be�ll�t�sa
	}

	/**
	 * Komponens elhelyez�se a rendelkez�sre �ll� t�r k�zep�n f�gg�legesen.
	 * @param component A komponens
	 * @return Egy olyan panel, amiben az �tadott komponens k�z�pen helyezkedik el
	 */
	public static JPanel CenterVertically(Component component) {
		JPanel centered = new JPanel(); // Egy �j panel l�trehoz�sa, amiben k�z�pen fog elhelyezkedni a komponens
		centered.setLayout(new BoxLayout(centered, BoxLayout.Y_AXIS)); // A f�gg�legesen egym�s al� �p�tkez� BoxLayout erre a legalkalmasabb
		centered.add(Box.createGlue()); // Automata marg� hozz�ad�sa fel�lre
		centered.add(component); // Tartalom hozz�ad�sa k�z�pre
		centered.add(Box.createGlue()); // Automata marg� hozz�ad�sa alulra
		return centered; // Visszat�r�s a k�z�pen elhelyezett tartalommal
	}

	/**
	 * Mivel a Java el�jeles b�jtokat haszn�l, el�jel n�lk�lit pedig nem tud, valahol jav�tani kell a b�jtokat. Ezzel lehet. 
	 * @param b Bemen� el�jeles b�jt
	 * @return Kimen� el�jel n�lk�li b�jt nagyobb m�reten
	 */
	public static int GetSByte(byte b) {
		return b >= 0 ? b : b + 256; // A -128-as nulladik bitet kell pozit�vnak venni
	}

	/**
	 * Egy el�jel n�lk�li b�jtban elf�r� azonos�t� bek�r�se a felhaszn�l�t�l.
	 * @param name Az azonos�t� neve
	 * @return Sikeres bemenet eset�n az azonos�t�, ellenkez� esetben -1
	 */
	public static int AskUserForID(String name) {
		String newID = JOptionPane.showInputDialog(null, "Enter the new " + name.toLowerCase() + " ID", name + " ID"); // �j azonos�t� bek�r�se a felhaszn�l�t�l
		if (newID == null || newID.isEmpty()) // Ha nem adott meg semmit, tudjon r�la
			JOptionPane.showMessageDialog(null, "Error: no ID given.", "Save result", JOptionPane.ERROR_MESSAGE);
		else if (!newID.matches("\\d*")) // Ha nem eg�sz sz�mot adott meg, tudjon r�la
			JOptionPane.showMessageDialog(null, "Error: \"" + newID + "\" is not a whole number between 0 and 255.", "Save result",
				JOptionPane.ERROR_MESSAGE);
		else {
			int saveID = Integer.parseInt(newID); // Azonos�t� sz�mm� alak�t�sa
			if (saveID > 255) // Ha t�l nagy sz�mot adott meg, tudjon r�la
				JOptionPane.showMessageDialog(null, "Error: the ID can't be larger than 255.", "Save result", JOptionPane.ERROR_MESSAGE);
			else
				return saveID; // ID helyesen bek�rve
		}
		return -1; // B�rmilyen hiba eset�n -1-et adjon vissza
	}
}