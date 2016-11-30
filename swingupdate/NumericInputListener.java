package swingupdate;

import java.awt.event.*;
import javax.swing.*;

/**
 * Csak sz�mszer� �rt�ket enged be�rni egy sz�vegbeviteli mez�be.
 * @author Sg�netz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class NumericInputListener implements KeyListener {
	/** Be�t�tt sz�mok. Hogy k�v�lr�l is l�that� legyen a "sz�veg", az�rt StringBuilder. */
	StringBuilder value;

	/**
	 * Egy StringBuilder referencia �tv�tele, hogy legyen "kimenet".
	 * @param value A StringBuilder, amibe a be�t�tt sz�mok ker�lnek
	 */
	public NumericInputListener(StringBuilder value) {
		this.value = value;
	}

	// A KeyListener kihaszn�latlan f�ggv�nyei
	public void keyPressed(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}

	/**
	 * Billenty�esem�ny felenged�sekor t�rt�n� esem�ny
	 * @param e Billenty�esem�ny
	 */
	public void keyReleased(KeyEvent e) {
		JTextField source = ((JTextField)e.getSource()); // A forr�si sz�vegmez�
		String text = source.getText(); // Az eddig be�rt tartalom
		int lastChar = text.length() - 1; // Az utols� karakter helye
		while (lastChar > -1 && (text.charAt(lastChar) < '0' || text.charAt(lastChar) > '9')) // Am�g van nem sz�m karakter a v�g�n...
			text = text.substring(0, lastChar--); // ...t�nj�n el
		source.setText(text); // A lev�gott tartalom vissza�r�sa
		value.delete(0, value.length()); // A kimenet �r�t�se
		value.append(text); // Ki�r�s a kimenetre
	}
}