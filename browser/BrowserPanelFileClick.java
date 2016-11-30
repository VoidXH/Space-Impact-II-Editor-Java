package browser;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.*;
import main.*;

/**
 * Egy tallozóban fájlra kattintva történõ esemény.
 * @author Sgánetz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class BrowserPanelFileClick implements ActionListener {
	/** Fájl típusa a játékon belül. */
	private String kind;
	/** Célfájl. */
	private File file;

	/**
	 * Konstruktor, ami eltárolja a fájl adatait.
	 * @param kind Fájl típusa a játékon belül
	 * @param file Célfájl
	 */
	public BrowserPanelFileClick(String kind, File file) {
		this.kind = kind;
		this.file = file;
	}

	/**
	 * Fájlra kattintáskor végrehajtandó esemény.
	 */
	public void actionPerformed(ActionEvent event) {
		EditorEnemy.Cleanup(); // Az ellenségszerkesztõben van egy idõzítõ, ami új panel megnyitásakor semmiképp nem futhat, ez a függvény lelövi
		Menu.frame.getContentPane().removeAll(); // A JFrame.removeAll() a ContentPane-t is törölné
		Menu.frame.add(Menu.browsers, BorderLayout.WEST); // Fájlválasztók megtartása bal oldalt
		if (kind.compareTo(BrowserPanel.kindEnemies) == 0) // Ha ellenségtallózóból jön a fájl...
			Menu.editor = new EditorEnemy(file); // ...nyíljon meg az ellenségszerkesztõ
		if (kind.compareTo(BrowserPanel.kindLevels) == 0) // Ha szinttallózóból jön a fájl...
			Menu.editor = new EditorLevel(file); // ...nyíljon meg a szintszerkesztõ
		if (kind.compareTo(BrowserPanel.kindObjects) == 0) // Ha objektumtallózóból jön a fájl...
			Menu.editor = new EditorObject(file); // ...nyíljon meg az objektumszerkesztõ
		Menu.frame.add(Menu.editor, BorderLayout.CENTER); // Új szerkesztõ középre
		Menu.frame.revalidate(); // Keret újraérvényesítése (hogy mûködjön, és legyen mit kirajzolni)
		Menu.frame.repaint(); // Keret újrarajzolása (hogy felülírja, ami fölé nem került új objektum)
	}
}