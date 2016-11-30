package main;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import browser.*;
import graphics.*;

/**
 * A main f�ggv�ny helye, ami l�trehozza a szerkeszt� ablak�t, valamint a men� k�t r�sz�nek (tall�z�s�v �s szerkeszt�panel) referenci�i
 * @author Sg�netz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class Menu {
	/** Ablakkeret. */
	public static JFrame frame;
	/** Tall�z�s�v */
	public static JPanel browsers;
	/** Szerkeszt�panel */
	public static JPanel editor;

	/**
	 * Bel�p�si pont, a szerkeszt� ablak�nak l�trehoz�sa.
	 * @param args Parancssori argumentumok (nem lesznek haszn�lva)
	 */
	public static void main(String[] args) {
		frame = new JFrame("Space Impact II Editor"); // Ablak l�trehoz�sa
		// F�jlkeres� panel
		JScrollPane browserBar = // Kezdetben egyetlen f�jlkeres� a j�t�kmapp�t tall�zni
				new JScrollPane( // Scrollozhat� legyen, ha esetleg nem f�r ki az aktu�lis mappa minden almapp�ja
				new BrowserPanel("Find the game folder", false, new File(".") // A panel c�me legyen az instrukci�
				.getAbsoluteFile() // Java h�lyes�g jav�t�sa #1: csak �gy lehet felfel� l�pni
				.getParentFile()), // Java h�lyes�g jav�t�sa #2: a mappa benne lenne saj�t mag�ban
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, // Ha kell, jelenjen meg a f�gg�leges g�rget�s�v
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // Ne jelenjen meg a v�zszintes g�rget�s�v
		frame.add(browserBar, BorderLayout.WEST); // Ker�lj�n az ablak bal oldal�ra
		// Szerkeszt� panel
		editor = new JPanel(); // Panel l�trehoz�sa
		editor.setLayout(new BoxLayout(editor, BoxLayout.Y_AXIS)); // A k�vetkez� sorokban hozz�adott komponenseket egym�s al� rajzolja
		editor.add(Box.createGlue()); // Automata marg� hozz�ad�sa fel�lre (hogy f�gg�legesen k�z�pen legyen a tartalom)
		editor.add(Obj.space.Draw()); // A j�t�k log�j�nak fels� fel�t rajzolja ki
		editor.add(new Obj(1, 1).Draw()); // Egy pixelnyi sor rajzol�sa a k�t k�p k�z�
		editor.add(Obj.impact.Draw()); // A j�t�k log�j�nak als� fel�t rajzolja ki
		editor.add(Box.createGlue()); // Automata marg� hozz�ad�sa alulra
		editor.setBackground(Obj.inactiveColor); // Legyen a teljes panel h�tt�rsz�n�
		editor.setPreferredSize(new Dimension(980, 720)); // Ezzel lesz meg az 1280x720-as alap ablakm�ret, de ha nem lehet ekkora, nem baj
		frame.add(editor, BorderLayout.CENTER); // Szerkeszt�ablak hozz�ad�sa, a layout �tm�retezhet� (k�z�ps�) r�sz�re
		// Ablakl�trehoz�s befejez� l�p�sei
		frame.pack(); // Ablak bem�retez�se
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Kil�p�s az ablak kil�p�s gombj�val
		frame.setVisible(true); // Megjelen�t�s
	}
}