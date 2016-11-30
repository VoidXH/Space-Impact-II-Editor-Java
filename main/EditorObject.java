package main;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

import browser.BrowserPanelFolderClick;
import graphics.*;
import swingupdate.*;

/**
 * Grafikai objektumokat szerkesztõ panel.
 * @author Sgánetz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class EditorObject extends JPanel {
	private static final long serialVersionUID = 1L;

	/** Az aktuálisan szerkesztett képet tartalmazó, eredetileg megnyitott fájl. */
	private File file;
	/** Az aktuálisan szerkesztett kép. */
	private Obj image;
	/** Az eredetileg megnyitott kép. */
	private Obj originalImage;
	/** Vezérlõsáv, amire a módosító csúszkák és gombok kerülnek, a mentési lehetõségek mellé. Ez egy vízszintes sáv, ezért jó a gyári FlowLayout. */
	private JPanel controllers = new JPanel();
	/** A szerkesztett kép méreteit kijelzõ címke. */
	private JLabel size = new JLabel();
	/** A szélességet szabályzó csúszka. */
	private JSlider widthSlider = new JSlider(1, 84);
	/** A magasságot szabályzó csúszka. */
	private JSlider heightSlider = new JSlider(1, 48);

	/**
	 * Csak a kép újrarajzolása, a vezérlõsáv megtartásával.
	 */
	public void Redraw() {
		removeAll(); // Minden komponens eltávolítása a panelrõl - semmi gond, a vezérlõsáv el van tárolva
		int controllersHeight = controllers.getPreferredSize().height; // Kezelõsáv magassága - elsõ rajzolás elõtt csak ez ad értelmes adatot
		Obj.pixelSize = Math.min( // Úgy méretezze a rajzot, hogy a helynek, ahova menni fog, érintse minimum az egyik tengelyen a széleit
			(Menu.frame.getWidth() - Menu.browsers.getWidth()) / image.GetWidth(), // Szélesség, az ablak és a tallózósáv különbsége adja meg
			(Menu.browsers.getHeight() - controllersHeight) / image.GetHeight()); // Magasság, a keret ablakmagasságot adna, azért a tallózósáv
		add(SwingHelpers.CenterVertically(image.Draw(true))); // A kép újrarajzolása a panel közepére
		add(controllers, BorderLayout.SOUTH); // A vezérlõsáv módosítatlanul kerüljön vissza ugyanoda, ahol volt
		revalidate(); // Néhány mûködés (pl. rajzra kattintás) miatt újra kell hitelesíteni a panelt
	}

	/**
	 * Mentés a megadott helyre.
	 * @param path Elérési útvonal
	 */
	private void SaveAs(String path) {
		if (image.Save(path)) { // Ha sikeresen el lehet menteni
			new BrowserPanelFolderClick(file.getParentFile().getParentFile().getParentFile()).actionPerformed(null); // Fájltallózók frissítése
			JOptionPane.showMessageDialog(null, "Object saved successfully.", "Save result", JOptionPane.INFORMATION_MESSAGE); // Siker jelzése
		} else // Sikertelen mentés esetén errõl tájékoztassa a felhasználót
			JOptionPane.showMessageDialog(null, "An error ocqured while saving the object.", "Save result", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * A kép méretének kijelzése az erre szolgáló címkén.
	 */
	private void DisplaySize() {
		size.setText("Size: " + image.GetWidth() + "x" + image.GetHeight());
	}

	/**
	 * Ha nem a felhasználó méretezte át a képet, frissítse a méretet, az átméretezõ csúszkák értékét, és rajzolja újra
	 */
	private void OnResized() {
		widthSlider.setValue(image.GetWidth()); // Szélességállító csúszka értékének beállítása
		heightSlider.setValue(image.GetHeight()); // Magasságállító csúszka értékének beállítása
		DisplaySize(); // Módosult méret kijelzése
	}

	/** Változásfigyelõ a szélességet állító csúszkához. */
	private ChangeListener widthChangeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			image.SetWidth(((JSlider)e.getSource()).getValue()); // Kép szélességének módosítása
			DisplaySize(); // Megváltozott méret kijelzése
			Redraw(); // Kép újrarajzolása az új méretben
		}
	};

	/** Változásfigyelõ a magasságot állító csúszkához. */
	private ChangeListener heightChangeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			image.SetHeight(((JSlider)e.getSource()).getValue()); // Kép magasságának módosítása
			DisplaySize(); // Megváltozott méret kijelzése
			Redraw(); // Kép újrarajzolása az új méretben
		}
	};

	/** Akciófigyelõ az ürítés gombhoz. */
	private ActionListener clearAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			image.Clear(); // Az objektum ürítése
			Redraw(); // A kép felülírása a semmivel
		}
	};

	/** Akciófigyelõ a visszaállítás gombhoz. */
	private ActionListener revertAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			image = originalImage.clone(); // Az eredeti objektum visszamásolása
			OnResized(); // Ha a szerkesztésben változtak a méretek, álljanak vissza az eredetire
			Redraw(); // Eredeti kép visszarajzolása
		}
	};

	/** Akciófigyelõ a metszés gombhoz. */
	private ActionListener trimAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			image.Trim(); // Kép metszése
			OnResized(); // A metszett méret kijelzése
			Redraw(); // A metszett kép kirajzolása
		}
	};

	/** Akciófigyelõ a mentés gombhoz. */
	private ActionListener saveAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			SaveAs(file.getAbsolutePath()); // Az eredeti fájl felülírása az új rajzzal.
		}
	};

	/** Akciófigyelõ a mentés másként gombhoz. */
	private ActionListener saveAsAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int saveID = SwingHelpers.AskUserForID("Object"); // Szintazonosító bekérése a felhasználótól
			if (saveID != -1) { // Ha a felhasználó érvényes azonosítót adott meg
				String fileName = file.getAbsolutePath(); // Vegyük a megnyitott fájl nevét...
				fileName = fileName.substring(0, fileName.lastIndexOf("\\") + 1) + saveID + ".dat"; // ...és cseréljük le benne az azonosítót
				SaveAs(fileName); // Mentés az új néven
				file = new File(fileName); // Innentõl a mentés az új fájlt írja felül, ne az eredetileg megnyitottat
				originalImage = image.clone(); // Ha már a mentés szerinti eredeti fájl megváltozott, az eredeti kép is változzon a szerkesztettre
			}
		}
	};

	/** Akciófigyelõ a hardcode gombhoz */
	private ActionListener hardcodeAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int size = image.GetWidth() * image.GetHeight(); // Képméret
			StringBuilder outString = new StringBuilder("[").append(size).append("] = {"); // Kimenet, tömbformátumban
			for (int pixel = 0; pixel < size; ++pixel) // Minden pixelt...
				outString.append(image.image[pixel] == 0 ? '0' : '1').append(','); // ...adjon hozzá, vesszõvel elválasztva
			outString.setCharAt(outString.length() - 1, '}'); // Utolsó vesszõ levágása, tömb lezárása
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(outString.toString()), null); // Vágólapra másolás
			JOptionPane.showMessageDialog(null, "Hardcode copied to clipboard.", "Hardcode result", JOptionPane.INFORMATION_MESSAGE); // Infó róla
		}
	};

	/**
	 * Szerkesztõpanel létrehozása a megadott fájlhoz.
	 * @param file Egy grafikus objektumot tartalmazó fájl
	 */
	public EditorObject(File file) {
		this.file = file; // Fájl tárolása
		image = Obj.Load(file.getAbsolutePath()); // Objektum betöltése
		originalImage = image.clone(); // Objektum lemásolása, ha a felhasználó vissza szeretné állítani a kezdeti állapotot
		setLayout(new BorderLayout()); // A szerkesztõfelülethez ennyi kell: vezérlõsáv alulra, illeszkedõ tartalom a maradék helyre
		OnResized(); // Kezdõérték adása három komponensnek, közülük az egyiknek a következõ sorban kellenek a méretei
		SwingHelpers.ForceSize(size, (int)size.getPreferredSize().width + 15, (int)size.getPreferredSize().height); // Legyen hely plusz számjegyeknek
		controllers.add(size); // A méret kijelzõje kerüljön bal oldalra
		SwingHelpers.ForceSize(widthSlider, 100, widthSlider.getPreferredSize().height); // 100 szélesek legyenek a méretcsúszkák
		SwingHelpers.ForceSize(heightSlider, 100, heightSlider.getPreferredSize().height); // Alapból túl széles egy csúszka, hogy ideférjen
		controllers.add(new JLabel("Width:")); // Szélesség csúszkát jelzõ címke
		((JSlider)controllers.add(widthSlider)).addChangeListener(widthChangeListener); // Szélesség csúszka
		controllers.add(new JLabel("Height:")); // Magasság csúszkát jelzõ címke
		((JSlider)controllers.add(heightSlider)).addChangeListener(heightChangeListener); // Magasság csúszka
		((JButton)controllers.add(new JButton("Clear"))).addActionListener(clearAction); // Ürítés gomb
		((JButton)controllers.add(new JButton("Revert"))).addActionListener(revertAction); // Visszaállítás gomb
		((JButton)controllers.add(new JButton("Trim"))).addActionListener(trimAction); // Metszés gomb
		((JButton)controllers.add(new JButton("Save"))).addActionListener(saveAction); // Mentés gomb
		((JButton)controllers.add(new JButton("Save as"))).addActionListener(saveAsAction); // Mentés másként gomb
		((JButton)controllers.add(new JButton("Hardcode"))).addActionListener(hardcodeAction); // Hardcode gomb
		Redraw(); // Felület kirajzolása
	}
}