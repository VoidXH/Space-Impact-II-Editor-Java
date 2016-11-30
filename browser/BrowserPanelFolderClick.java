package browser;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import main.Menu;

/**
 * Egy tallozóban mappára kattintva történõ esemény.
 * @author Sgánetz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class BrowserPanelFolderClick implements ActionListener {
	/** Célmappa. */
	private File file;

	/**
	 * Konstruktor, ami eltárolja a célmappát.
	 * @param file Célmappa
	 */
	public BrowserPanelFolderClick(File file) {
		this.file = file;
	}

	/**
	 * Mappára kattintáskor végrehajtandó esemény.
	 */
	public void actionPerformed(ActionEvent event) {
		if (file != null && new File(file.getAbsolutePath() + "\\data\\enemies\\0.dat").exists()
			&& new File(file.getAbsolutePath() + "\\data\\levels\\0.dat").exists()
			&& new File(file.getAbsolutePath() + "\\data\\objects\\0.dat").exists()) { // Ha létezik egy ilyen struktúra, az nagyon valószínû, hogy a játék
			Menu.frame.getContentPane().removeAll(); // A JFrame.removeAll() a ContentPane-t is törölné
			Menu.browsers = new JPanel(new GridLayout(0, 1)); // Böngészõsáv
			BrowserPanel // Böngészõpanelek
				enemiesPanel = new BrowserPanel(BrowserPanel.kindEnemies, true, new File(file.getAbsolutePath() + "\\data\\enemies")), // Ellenségböngészõ
				levelsPanel = new BrowserPanel(BrowserPanel.kindLevels, true, new File(file.getAbsolutePath() + "\\data\\levels")), // Pályaböngészõ
				objectsPanel = new BrowserPanel(BrowserPanel.kindObjects, true, new File(file.getAbsolutePath() + "\\data\\objects")); // Objektumböngészõ
			// Panelek hozzáadása a sávhoz, ha esetleg nem férnek ki, scrollozni kelljen, de ne vízszintesen
			((JScrollPane)Menu.browsers.add(new JScrollPane(enemiesPanel))).setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			((JScrollPane)Menu.browsers.add(new JScrollPane(levelsPanel))).setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			((JScrollPane)Menu.browsers.add(new JScrollPane(objectsPanel))).setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			Menu.frame.add(Menu.browsers, BorderLayout.WEST); // Kerüljön az ablak bal oldalára
			Menu.frame.add(Menu.editor, BorderLayout.CENTER); // A szerkesztõ kerüljön vissza
			Menu.frame.revalidate(); // Keret újraérvényesítése (hogy mûködjön, és legyen mit kirajzolni)
			Menu.frame.repaint(); // Keret újrarajzolása (hogy felülírja, ami fölé nem került új objektum)
		} else
			((BrowserPanel)((JButton)event.getSource()).getParent()).Open(file); // A gombot tartalmazó panel töltse be a választott mappát
	}
}