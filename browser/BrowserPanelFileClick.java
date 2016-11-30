package browser;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.*;
import main.*;

/**
 * Egy talloz�ban f�jlra kattintva t�rt�n� esem�ny.
 * @author Sg�netz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class BrowserPanelFileClick implements ActionListener {
	/** F�jl t�pusa a j�t�kon bel�l. */
	private String kind;
	/** C�lf�jl. */
	private File file;

	/**
	 * Konstruktor, ami elt�rolja a f�jl adatait.
	 * @param kind F�jl t�pusa a j�t�kon bel�l
	 * @param file C�lf�jl
	 */
	public BrowserPanelFileClick(String kind, File file) {
		this.kind = kind;
		this.file = file;
	}

	/**
	 * F�jlra kattint�skor v�grehajtand� esem�ny.
	 */
	public void actionPerformed(ActionEvent event) {
		EditorEnemy.Cleanup(); // Az ellens�gszerkeszt�ben van egy id�z�t�, ami �j panel megnyit�sakor semmik�pp nem futhat, ez a f�ggv�ny lel�vi
		Menu.frame.getContentPane().removeAll(); // A JFrame.removeAll() a ContentPane-t is t�r�ln�
		Menu.frame.add(Menu.browsers, BorderLayout.WEST); // F�jlv�laszt�k megtart�sa bal oldalt
		if (kind.compareTo(BrowserPanel.kindEnemies) == 0) // Ha ellens�gtall�z�b�l j�n a f�jl...
			Menu.editor = new EditorEnemy(file); // ...ny�ljon meg az ellens�gszerkeszt�
		if (kind.compareTo(BrowserPanel.kindLevels) == 0) // Ha szinttall�z�b�l j�n a f�jl...
			Menu.editor = new EditorLevel(file); // ...ny�ljon meg a szintszerkeszt�
		if (kind.compareTo(BrowserPanel.kindObjects) == 0) // Ha objektumtall�z�b�l j�n a f�jl...
			Menu.editor = new EditorObject(file); // ...ny�ljon meg az objektumszerkeszt�
		Menu.frame.add(Menu.editor, BorderLayout.CENTER); // �j szerkeszt� k�z�pre
		Menu.frame.revalidate(); // Keret �jra�rv�nyes�t�se (hogy m�k�dj�n, �s legyen mit kirajzolni)
		Menu.frame.repaint(); // Keret �jrarajzol�sa (hogy fel�l�rja, ami f�l� nem ker�lt �j objektum)
	}
}