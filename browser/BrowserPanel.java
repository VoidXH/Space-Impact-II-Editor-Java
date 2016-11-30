package browser;

import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import swingupdate.*;

/**
 * Tall�z� panel. Mapp�k k�zt navig�l�shoz, vagy mapp�n bel�l f�jlok megnyit�s�hoz haszn�lhat�, de a kett�re egyszerre nem.
 * @author Sg�netz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class BrowserPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	/** Ellens�gtall�z� panel c�me. */
	public static final String kindEnemies = "Enemies";
	/** Szinttall�z� panel c�me. */
	public static final String kindLevels = "Levels";
	/** Objektumtall�z� panel c�me. */
	public static final String kindObjects = "Objects";
	/** Az egyes gombok, �gy a teljes panel sz�less�ge. */
	private static final int buttonWidth = 300;
	/** Az egyes gombok magass�ga. */
	private static final int buttonHeight = 25;

	/** A panel c�me. */
	private String title;
	/** A mappa tartalm�t mutassa? Ellenkez� esetben mappatall�z�. */
	private boolean folderContent;

	/**
	 * �j gomb hozz�ad�sa a tall�z�hoz.
	 * @param name A gomb c�mk�je
	 * @param listener A gomb megnyom�si akci�ja
	 */
	void AddButton(String name, ActionListener listener) {
		JButton newButton = new JButton(name); // Gomb l�trehoz�sa
		newButton.addActionListener(listener); // Akci� hozz�rendel�se
		SwingHelpers.ForceSize(newButton, buttonWidth, buttonHeight); // M�ret er�ltet�se
		this.add(newButton); // Gomb panelhez ad�sa
	}

	/** F�jlok rendez�se �gy, hogy a sz�mok norm�lis sorrendben legyenek */
	public static Comparator<File> fixedNumericOrder = new Comparator<File>() {
		public String ComparableName(File file) { // A f�jl nev�t adja vissza sok vez�rnull�val, ha sz�m
			String fileName = file.getName(); // F�jln�v lek�rdez�se
			int dot = fileName.indexOf('.'); // Pont (azaz a kiterjeszt�s kezdet�nek) megkeres�se
			if (dot != -1) // Ha van pont...
				fileName = fileName.substring(0, dot); // ...onnant�l v�gja le
			if (fileName.length() != 0 && fileName.matches("\\d*")) // Ha csak sz�mok maradtak
				fileName = String.format("%020d", Integer.parseInt(fileName)); // �rja fel 20 sz�mjegyben, vez�rnull�kkal
			return fileName; // �talak�tott (vagy nem �talak�tott) f�jln�v visszaad�sa
		}

		public int compare(File l, File r) {
			return ComparableName(l).compareTo(ComparableName(r)); // Esetlegesen �talak�tott f�jlnevek �sszevet�se
		}
	};

	/**
	 * A megnyitott mappa megv�ltoztat�sa.
	 * @param location Mappa el�r�si �tvonala
	 */
	public void Open(File location) {
		this.removeAll(); // Ami eddig volt, azt t�r�lje
		SwingHelpers.ForceSize(this.add(new JLabel(this.title, JLabel.CENTER)), buttonWidth, 25); // C�m a tetej�re
		if (location == null) { // Ha a meghajt� gy�ker�be �rt a felhaszn�l� �s Up-ot nyomott (gy�k�rre a getParentFile() null-t dob)
			for (File entry : File.listRoots()) // Rajzolja ki a meghajt�kat gombnak
				AddButton(entry.getAbsolutePath(), new BrowserPanelFolderClick(entry)); // Mappak�nt kezelje �ket
		} else {
			if (!folderContent) // Ha mapp�k k�zt lehet navig�lni, lehessen felfel� is menni
				AddButton("Up", new BrowserPanelFolderClick(location.getParentFile()));
			File[] contents = location.listFiles(new BrowserPanelFilter(folderContent)); // Mappa tartalm�nak list�z�sa
			Arrays.sort(contents, fixedNumericOrder);
			if (contents != null) { // Ha van a mapp�nak tartalma (nincs �res t�mb, helyette null van)
				for (File entry : contents) { // Az �sszes mapp�hoz/f�jlhoz (amit a folderContent k�r) rajzoljon kiv�laszt� gombot
					if (folderContent) {
						String fileName = entry.getName(); // F�jln�v, le lesz v�gva a kiterjeszt�se
						AddButton(fileName.substring(0, fileName.lastIndexOf('.')), new BrowserPanelFileClick(title, entry));
					} else
						AddButton(entry.getName(), new BrowserPanelFolderClick(entry));
				}
			}
		}
		this.revalidate(); // Panel �rv�nyes�t�se (hogy m�k�dj�n, �s legyen is mit rajzolni a k�vetkez� sorban)
		this.repaint(); // Panel �jrarajzol�sa (hogy az �res helyeken ne maradjon szem�t)
	}

	/**
	 * �j tall�z�panel l�trehoz�sa.
	 * @param title A panel c�me
	 * @param folderContent A mappa tartalm�t mutassa-e? Ellenkez� esetben mappatall�z� lesz
	 * @param location A megjelen�tend� mappa
	 */
	public BrowserPanel(String title, boolean folderContent, File location) {
		this.title = title;
		this.folderContent = folderContent;
		this.setLayout(new BoxLayout(this, 1)); // Egym�s al� rajzolja a dolgokat, �s lehessen benne kit�ltetlen r�sz (�gy nem GridLayout)
		Open(location); // Az �tadott mappa megnyit�sa
	}
}