package browser;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import main.Menu;

/**
 * Egy talloz�ban mapp�ra kattintva t�rt�n� esem�ny.
 * @author Sg�netz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class BrowserPanelFolderClick implements ActionListener {
	/** C�lmappa. */
	private File file;

	/**
	 * Konstruktor, ami elt�rolja a c�lmapp�t.
	 * @param file C�lmappa
	 */
	public BrowserPanelFolderClick(File file) {
		this.file = file;
	}

	/**
	 * Mapp�ra kattint�skor v�grehajtand� esem�ny.
	 */
	public void actionPerformed(ActionEvent event) {
		if (file != null && new File(file.getAbsolutePath() + "\\data\\enemies\\0.dat").exists()
			&& new File(file.getAbsolutePath() + "\\data\\levels\\0.dat").exists()
			&& new File(file.getAbsolutePath() + "\\data\\objects\\0.dat").exists()) { // Ha l�tezik egy ilyen strukt�ra, az nagyon val�sz�n�, hogy a j�t�k
			Menu.frame.getContentPane().removeAll(); // A JFrame.removeAll() a ContentPane-t is t�r�ln�
			Menu.browsers = new JPanel(new GridLayout(0, 1)); // B�ng�sz�s�v
			BrowserPanel // B�ng�sz�panelek
				enemiesPanel = new BrowserPanel(BrowserPanel.kindEnemies, true, new File(file.getAbsolutePath() + "\\data\\enemies")), // Ellens�gb�ng�sz�
				levelsPanel = new BrowserPanel(BrowserPanel.kindLevels, true, new File(file.getAbsolutePath() + "\\data\\levels")), // P�lyab�ng�sz�
				objectsPanel = new BrowserPanel(BrowserPanel.kindObjects, true, new File(file.getAbsolutePath() + "\\data\\objects")); // Objektumb�ng�sz�
			// Panelek hozz�ad�sa a s�vhoz, ha esetleg nem f�rnek ki, scrollozni kelljen, de ne v�zszintesen
			((JScrollPane)Menu.browsers.add(new JScrollPane(enemiesPanel))).setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			((JScrollPane)Menu.browsers.add(new JScrollPane(levelsPanel))).setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			((JScrollPane)Menu.browsers.add(new JScrollPane(objectsPanel))).setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			Menu.frame.add(Menu.browsers, BorderLayout.WEST); // Ker�lj�n az ablak bal oldal�ra
			Menu.frame.add(Menu.editor, BorderLayout.CENTER); // A szerkeszt� ker�lj�n vissza
			Menu.frame.revalidate(); // Keret �jra�rv�nyes�t�se (hogy m�k�dj�n, �s legyen mit kirajzolni)
			Menu.frame.repaint(); // Keret �jrarajzol�sa (hogy fel�l�rja, ami f�l� nem ker�lt �j objektum)
		} else
			((BrowserPanel)((JButton)event.getSource()).getParent()).Open(file); // A gombot tartalmaz� panel t�ltse be a v�lasztott mapp�t
	}
}