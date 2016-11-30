package swingupdate;

import java.awt.event.*;
import javax.swing.*;

/**
 * Csak számszerû értéket enged beírni egy szövegbeviteli mezõbe.
 * @author Sgánetz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class NumericInputListener implements KeyListener {
	/** Beütött számok. Hogy kívülrõl is látható legyen a "szöveg", azért StringBuilder. */
	StringBuilder value;

	/**
	 * Egy StringBuilder referencia átvétele, hogy legyen "kimenet".
	 * @param value A StringBuilder, amibe a beütött számok kerülnek
	 */
	public NumericInputListener(StringBuilder value) {
		this.value = value;
	}

	// A KeyListener kihasználatlan függvényei
	public void keyPressed(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}

	/**
	 * Billentyûesemény felengedésekor történõ esemény
	 * @param e Billentyûesemény
	 */
	public void keyReleased(KeyEvent e) {
		JTextField source = ((JTextField)e.getSource()); // A forrási szövegmezõ
		String text = source.getText(); // Az eddig beírt tartalom
		int lastChar = text.length() - 1; // Az utolsó karakter helye
		while (lastChar > -1 && (text.charAt(lastChar) < '0' || text.charAt(lastChar) > '9')) // Amíg van nem szám karakter a végén...
			text = text.substring(0, lastChar--); // ...tûnjön el
		source.setText(text); // A levágott tartalom visszaírása
		value.delete(0, value.length()); // A kimenet ürítése
		value.append(text); // Kiírás a kimenetre
	}
}