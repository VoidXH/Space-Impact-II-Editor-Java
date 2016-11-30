package swingupdate;

import java.awt.*;
import javax.swing.*;

/**
 * Segédfüggvények gyûjteménye Swing-hez.
 * @author Sgánetz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class SwingHelpers {
	/**
	 * Komponens méretének erõltetése.
	 * @param component A komponens
	 * @param x Szélesség
	 * @param y Magasság
	 */
	public static void ForceSize(Component component, int x, int y) {
		Dimension size = new Dimension(x, y); // A függvények egy része csak dimenziót fogad el
		component.setMinimumSize(size); // pl. BoxLayout
		component.setPreferredSize(size); // pl. FlowLayout, GridLayout
		component.setMaximumSize(size); // BoxLayoutnál kell a minimum mellé
	}

	/**
	 * Komponens korlátainak javítása, ha több helyet foglalna, mint szükséges.
	 * @param component A komponens
	 */
	public static void FixBounds(Component component) {
		ForceSize(component, component.getPreferredSize().width, component.getPreferredSize().height); // A szükséges méret ráerõltetése
	}

	/**
	 * Abszolút elrendezésû panelek korlátait javítja, mivel azok nem számolják ki maguknak.
	 * @param panel A panel
	 */
	public static void FixNullBounds(JPanel panel) {
		int xMax = 0, yMax = 0; // Cél méret, mindkét tengelyen a legnagyobbat keressük
		Component[] components = panel.getComponents(); // Az összes komponenst...
		for (Component component : components) { // ...meg kell vizsgálni
			int x = component.getX() + component.getWidth(), y = component.getY() + component.getHeight(); // Komponens végpontjai, maximumkeresés ezekbõl
			if (xMax < x)
				xMax = x;
			if (yMax < y)
				yMax = y;
		}
		panel.setPreferredSize(new Dimension(xMax, yMax)); // A kiszámolt méret beállítása
	}

	/**
	 * Komponens elhelyezése a rendelkezésre álló tér közepén függõlegesen.
	 * @param component A komponens
	 * @return Egy olyan panel, amiben az átadott komponens középen helyezkedik el
	 */
	public static JPanel CenterVertically(Component component) {
		JPanel centered = new JPanel(); // Egy új panel létrehozása, amiben középen fog elhelyezkedni a komponens
		centered.setLayout(new BoxLayout(centered, BoxLayout.Y_AXIS)); // A függõlegesen egymás alá építkezõ BoxLayout erre a legalkalmasabb
		centered.add(Box.createGlue()); // Automata margó hozzáadása felülre
		centered.add(component); // Tartalom hozzáadása középre
		centered.add(Box.createGlue()); // Automata margó hozzáadása alulra
		return centered; // Visszatérés a középen elhelyezett tartalommal
	}

	/**
	 * Mivel a Java elõjeles bájtokat használ, elõjel nélkülit pedig nem tud, valahol javítani kell a bájtokat. Ezzel lehet. 
	 * @param b Bemenõ elõjeles bájt
	 * @return Kimenõ elõjel nélküli bájt nagyobb méreten
	 */
	public static int GetSByte(byte b) {
		return b >= 0 ? b : b + 256; // A -128-as nulladik bitet kell pozitívnak venni
	}

	/**
	 * Egy elõjel nélküli bájtban elférõ azonosító bekérése a felhasználótól.
	 * @param name Az azonosító neve
	 * @return Sikeres bemenet esetén az azonosító, ellenkezõ esetben -1
	 */
	public static int AskUserForID(String name) {
		String newID = JOptionPane.showInputDialog(null, "Enter the new " + name.toLowerCase() + " ID", name + " ID"); // Új azonosító bekérése a felhasználótól
		if (newID == null || newID.isEmpty()) // Ha nem adott meg semmit, tudjon róla
			JOptionPane.showMessageDialog(null, "Error: no ID given.", "Save result", JOptionPane.ERROR_MESSAGE);
		else if (!newID.matches("\\d*")) // Ha nem egész számot adott meg, tudjon róla
			JOptionPane.showMessageDialog(null, "Error: \"" + newID + "\" is not a whole number between 0 and 255.", "Save result",
				JOptionPane.ERROR_MESSAGE);
		else {
			int saveID = Integer.parseInt(newID); // Azonosító számmá alakítása
			if (saveID > 255) // Ha túl nagy számot adott meg, tudjon róla
				JOptionPane.showMessageDialog(null, "Error: the ID can't be larger than 255.", "Save result", JOptionPane.ERROR_MESSAGE);
			else
				return saveID; // ID helyesen bekérve
		}
		return -1; // Bármilyen hiba esetén -1-et adjon vissza
	}
}