package main;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import browser.*;
import graphics.*;

/**
 * A main függvény helye, ami létrehozza a szerkesztõ ablakát, valamint a menü két részének (tallózósáv és szerkesztõpanel) referenciái
 * @author Sgánetz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class Menu {
	/** Ablakkeret. */
	public static JFrame frame;
	/** Tallózósáv */
	public static JPanel browsers;
	/** Szerkesztõpanel */
	public static JPanel editor;

	/**
	 * Belépési pont, a szerkesztõ ablakának létrehozása.
	 * @param args Parancssori argumentumok (nem lesznek használva)
	 */
	public static void main(String[] args) {
		frame = new JFrame("Space Impact II Editor"); // Ablak létrehozása
		// Fájlkeresõ panel
		JScrollPane browserBar = // Kezdetben egyetlen fájlkeresõ a játékmappát tallózni
				new JScrollPane( // Scrollozható legyen, ha esetleg nem fér ki az aktuális mappa minden almappája
				new BrowserPanel("Find the game folder", false, new File(".") // A panel címe legyen az instrukció
				.getAbsoluteFile() // Java hülyeség javítása #1: csak így lehet felfelé lépni
				.getParentFile()), // Java hülyeség javítása #2: a mappa benne lenne saját magában
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, // Ha kell, jelenjen meg a függõleges görgetõsáv
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // Ne jelenjen meg a vízszintes görgetõsáv
		frame.add(browserBar, BorderLayout.WEST); // Kerüljön az ablak bal oldalára
		// Szerkesztõ panel
		editor = new JPanel(); // Panel létrehozása
		editor.setLayout(new BoxLayout(editor, BoxLayout.Y_AXIS)); // A következõ sorokban hozzáadott komponenseket egymás alá rajzolja
		editor.add(Box.createGlue()); // Automata margó hozzáadása felülre (hogy függõlegesen középen legyen a tartalom)
		editor.add(Obj.space.Draw()); // A játék logójának felsõ felét rajzolja ki
		editor.add(new Obj(1, 1).Draw()); // Egy pixelnyi sor rajzolása a két kép közé
		editor.add(Obj.impact.Draw()); // A játék logójának alsó felét rajzolja ki
		editor.add(Box.createGlue()); // Automata margó hozzáadása alulra
		editor.setBackground(Obj.inactiveColor); // Legyen a teljes panel háttérszínû
		editor.setPreferredSize(new Dimension(980, 720)); // Ezzel lesz meg az 1280x720-as alap ablakméret, de ha nem lehet ekkora, nem baj
		frame.add(editor, BorderLayout.CENTER); // Szerkesztõablak hozzáadása, a layout átméretezhetõ (középsõ) részére
		// Ablaklétrehozás befejezõ lépései
		frame.pack(); // Ablak beméretezése
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Kilépés az ablak kilépés gombjával
		frame.setVisible(true); // Megjelenítés
	}
}