package swingupdate;

import javax.swing.*;
import javax.swing.event.*;

/**
 * Csúszka, ami a bal oldalán kijelzi az értékét
 * @author Sgánetz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class SliderWithDisplay extends JPanel {
	private static final long serialVersionUID = 1L;

	/** A kijelzõs csúszka csúszka része. */
	public JSlider slider;

	/**
	 * Új kijelzõs csúszka létrehozása.
	 * @param min Minimális érték
	 * @param max Maximális érték
	 * @param value Jelenlegi érték
	 * @param width Szélesség
	 */
	public SliderWithDisplay(int min, int max, int value, int width) {
		JLabel display = new JLabel(new Integer(max).toString()); // A maximummal inicializálódjon a címke, hogy ez legyen a mérete (ne mozogjon el a csúszka)
		SwingHelpers.FixBounds(display); // Méret megkötése
		display.setText(new Integer(value).toString()); // Most már kikerülhet a valós érték a címkére
		add(display); // Kijelzõ kijelzése ^^
		slider = new JSlider(min, max, value); // Csúszka létrehozása
		SwingHelpers.ForceSize(slider, width, (int)slider.getPreferredSize().getHeight()); // Szélesség ráerõltetése
		slider.addChangeListener(new ChangeListener() { // Esemény hozzáadása, ami frissíti a címkét, ha megváltozik a csúszka értéke
			public void stateChanged(ChangeEvent e) {
				display.setText(new Integer(slider.getValue()).toString()); // Kijelzett érték frissítése
			}
		});
		add(slider); // Csúszka megjelenítése
	}
}