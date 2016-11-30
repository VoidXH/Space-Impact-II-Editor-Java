package browser;

import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import swingupdate.*;

/**
 * Tallózó panel. Mappák közt navigáláshoz, vagy mappán belül fájlok megnyitásához használható, de a kettõre egyszerre nem.
 * @author Sgánetz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class BrowserPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	/** Ellenségtallózó panel címe. */
	public static final String kindEnemies = "Enemies";
	/** Szinttallózó panel címe. */
	public static final String kindLevels = "Levels";
	/** Objektumtallózó panel címe. */
	public static final String kindObjects = "Objects";
	/** Az egyes gombok, így a teljes panel szélessége. */
	private static final int buttonWidth = 300;
	/** Az egyes gombok magassága. */
	private static final int buttonHeight = 25;

	/** A panel címe. */
	private String title;
	/** A mappa tartalmát mutassa? Ellenkezõ esetben mappatallózó. */
	private boolean folderContent;

	/**
	 * Új gomb hozzáadása a tallózóhoz.
	 * @param name A gomb címkéje
	 * @param listener A gomb megnyomási akciója
	 */
	void AddButton(String name, ActionListener listener) {
		JButton newButton = new JButton(name); // Gomb létrehozása
		newButton.addActionListener(listener); // Akció hozzárendelése
		SwingHelpers.ForceSize(newButton, buttonWidth, buttonHeight); // Méret erõltetése
		this.add(newButton); // Gomb panelhez adása
	}

	/** Fájlok rendezése úgy, hogy a számok normális sorrendben legyenek */
	public static Comparator<File> fixedNumericOrder = new Comparator<File>() {
		public String ComparableName(File file) { // A fájl nevét adja vissza sok vezérnullával, ha szám
			String fileName = file.getName(); // Fájlnév lekérdezése
			int dot = fileName.indexOf('.'); // Pont (azaz a kiterjesztés kezdetének) megkeresése
			if (dot != -1) // Ha van pont...
				fileName = fileName.substring(0, dot); // ...onnantól vágja le
			if (fileName.length() != 0 && fileName.matches("\\d*")) // Ha csak számok maradtak
				fileName = String.format("%020d", Integer.parseInt(fileName)); // Írja fel 20 számjegyben, vezérnullákkal
			return fileName; // Átalakított (vagy nem átalakított) fájlnév visszaadása
		}

		public int compare(File l, File r) {
			return ComparableName(l).compareTo(ComparableName(r)); // Esetlegesen átalakított fájlnevek összevetése
		}
	};

	/**
	 * A megnyitott mappa megváltoztatása.
	 * @param location Mappa elérési útvonala
	 */
	public void Open(File location) {
		this.removeAll(); // Ami eddig volt, azt törölje
		SwingHelpers.ForceSize(this.add(new JLabel(this.title, JLabel.CENTER)), buttonWidth, 25); // Cím a tetejére
		if (location == null) { // Ha a meghajtó gyökerébe ért a felhasználó és Up-ot nyomott (gyökérre a getParentFile() null-t dob)
			for (File entry : File.listRoots()) // Rajzolja ki a meghajtókat gombnak
				AddButton(entry.getAbsolutePath(), new BrowserPanelFolderClick(entry)); // Mappaként kezelje õket
		} else {
			if (!folderContent) // Ha mappák közt lehet navigálni, lehessen felfelé is menni
				AddButton("Up", new BrowserPanelFolderClick(location.getParentFile()));
			File[] contents = location.listFiles(new BrowserPanelFilter(folderContent)); // Mappa tartalmának listázása
			Arrays.sort(contents, fixedNumericOrder);
			if (contents != null) { // Ha van a mappának tartalma (nincs üres tömb, helyette null van)
				for (File entry : contents) { // Az összes mappához/fájlhoz (amit a folderContent kér) rajzoljon kiválasztó gombot
					if (folderContent) {
						String fileName = entry.getName(); // Fájlnév, le lesz vágva a kiterjesztése
						AddButton(fileName.substring(0, fileName.lastIndexOf('.')), new BrowserPanelFileClick(title, entry));
					} else
						AddButton(entry.getName(), new BrowserPanelFolderClick(entry));
				}
			}
		}
		this.revalidate(); // Panel érvényesítése (hogy mûködjön, és legyen is mit rajzolni a következõ sorban)
		this.repaint(); // Panel újrarajzolása (hogy az üres helyeken ne maradjon szemét)
	}

	/**
	 * Új tallózópanel létrehozása.
	 * @param title A panel címe
	 * @param folderContent A mappa tartalmát mutassa-e? Ellenkezõ esetben mappatallózó lesz
	 * @param location A megjelenítendõ mappa
	 */
	public BrowserPanel(String title, boolean folderContent, File location) {
		this.title = title;
		this.folderContent = folderContent;
		this.setLayout(new BoxLayout(this, 1)); // Egymás alá rajzolja a dolgokat, és lehessen benne kitöltetlen rész (így nem GridLayout)
		Open(location); // Az átadott mappa megnyitása
	}
}