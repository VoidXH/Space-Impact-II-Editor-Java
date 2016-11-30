package swingupdate;

import javax.swing.*;
import javax.swing.event.*;

/**
 * Cs�szka, ami a bal oldal�n kijelzi az �rt�k�t
 * @author Sg�netz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class SliderWithDisplay extends JPanel {
	private static final long serialVersionUID = 1L;

	/** A kijelz�s cs�szka cs�szka r�sze. */
	public JSlider slider;

	/**
	 * �j kijelz�s cs�szka l�trehoz�sa.
	 * @param min Minim�lis �rt�k
	 * @param max Maxim�lis �rt�k
	 * @param value Jelenlegi �rt�k
	 * @param width Sz�less�g
	 */
	public SliderWithDisplay(int min, int max, int value, int width) {
		JLabel display = new JLabel(new Integer(max).toString()); // A maximummal inicializ�l�djon a c�mke, hogy ez legyen a m�rete (ne mozogjon el a cs�szka)
		SwingHelpers.FixBounds(display); // M�ret megk�t�se
		display.setText(new Integer(value).toString()); // Most m�r kiker�lhet a val�s �rt�k a c�mk�re
		add(display); // Kijelz� kijelz�se ^^
		slider = new JSlider(min, max, value); // Cs�szka l�trehoz�sa
		SwingHelpers.ForceSize(slider, width, (int)slider.getPreferredSize().getHeight()); // Sz�less�g r�er�ltet�se
		slider.addChangeListener(new ChangeListener() { // Esem�ny hozz�ad�sa, ami friss�ti a c�mk�t, ha megv�ltozik a cs�szka �rt�ke
			public void stateChanged(ChangeEvent e) {
				display.setText(new Integer(slider.getValue()).toString()); // Kijelzett �rt�k friss�t�se
			}
		});
		add(slider); // Cs�szka megjelen�t�se
	}
}