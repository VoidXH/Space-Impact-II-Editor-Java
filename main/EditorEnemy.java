package main;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.Timer;
import javax.swing.*;
import javax.swing.event.*;
import browser.*;
import graphics.*;
import swingupdate.*;

/**
 * Ellenségeket szerkesztõ panel.
 * @author Sgánetz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class EditorEnemy extends JPanel {
	private static final long serialVersionUID = 1L;

	/** Az aktuálisan szerkesztett ellenséget tartalmazó, eredetileg megnyitott fájl. */
	private File file;

	/** Mentés, mint ez az azonosító. A típus azért StringBuilder, hogy referenciaként át lehessen adni. */
	private StringBuilder saveAsID;
	/** Elsõ animációs fázis modelljének azonosítója szövegként. A típus azért StringBuilder, hogy referenciaként át lehessen adni. */
	private StringBuilder modelID_str;
	/** Elsõ animációs fázis modelljének azonosítója. */
	private int modelID;
	/** Szélesség. */
	private int x;
	/** Magasság. */
	private int y;
	/** Animációs. */
	private int anims;
	/** Életek száma. */
	private int lives;
	/** Beúszva a pályára vízszintesen helyben marad-e? */
	private int floats;
	/** Lövések közti idõ. */
	private int shotTime;
	/** Mozog-e felfelé? */
	private int moveUp;
	/** Mozog-e lefelé? */
	private int moveDown;
	/** Mozog-e, amíg nem ért be a pályára? */
	private int moveAnyway;
	/** Felsõ mozgáshatár. */
	private int moveMin;
	/** Alsó mozgáshatár. */
	private int moveMax;

	/** Kezelõpanel, az ellenség állítható tulajdonságaival. */
	private JPanel controls = new JPanel();
	/** Méretet kijelzõ címke */
	private JLabel size = new JLabel();
	/** Kirajzolt animációs fázis. */
	private JPanel drawn = new JPanel();
	/** Lehetséges megjelenések, animációs fázisok. */
	private Obj[] animPhases;
	/** Jelenleg megjelenített animációs fázis. */
	private int animPhase;

	/** Animációváltó idõzítõ. */
	private static Timer animChanger = null;

	/**
	 * Az animációváltó lelövése, ha fut.
	 */
	public static void Cleanup() {
		if (animChanger != null) // Ha egy elõzõ példánynak még futna idõzítõje...
			animChanger.cancel(); // ...ne fusson tovább, mert az Obj kirajzolása nem thread-safe, nem kullogna el a háttérben GC-re várva ez az idõzítõ,
								  // és még a pixelméretet is felülírja a háttérben, ami rosszul hat ki egy esetlegesen elkezdett rajzolásra
	}

	/**
	 * Animációs fázis kirajzolása.
	 */
	private void DrawAnimPhase() {
		this.drawn.removeAll(); // Rajzpanel ürítése
		int drawSpaceX = (Menu.frame.getWidth() - Menu.browsers.getWidth()) / 2, drawSpaceY = Menu.browsers.getHeight(); // A megjelenített elenség mérete
		Obj.pixelSize = Math.min( // Úgy méretezze a rajzot, hogy a helynek, ahova menni fog, érintse minimum az egyik tengelyen a széleit
			drawSpaceX / animPhases[animPhase].GetWidth(), // Szélesség, az ablak és a tallózósáv különbsége adja meg
			drawSpaceY / animPhases[animPhase].GetHeight()); // Magasság, a keret ablakmagasságot adna, azért a tallózósáv
		JPanel drawn = SwingHelpers.CenterVertically(animPhases[animPhase].Draw()); // Függõlegesen középen legyen a kirajzolt ellenség
		SwingHelpers.ForceSize(drawn, drawSpaceX, drawSpaceY); // A megjelenítõ panel szélessége ne változzon az animációs fázissal
		this.drawn.add(drawn); // Új rajz hozzáadása a rajzpanelhez
		this.drawn.revalidate(); // Rajzpanel újrahitelesítése, különben nem rajzolódik ki
	}

	/**
	 * Az összes animációs fázis újbóli betöltése.
	 */
	private void ReloadAnimations() {
		modelID = Integer.parseInt(modelID_str.toString()); // Elsõ animációs fázis objektumának azonosítója
        animPhases = new Obj[anims]; // Az összes animációs fázis grafikáinak helye
        String objectFolder = file.getAbsolutePath(); // Objektummappa kezdete (jelenlegi fájlnév)
        objectFolder = objectFolder.substring(0, objectFolder.lastIndexOf('\\')); // Levágás a mappáig
        objectFolder = objectFolder.substring(0, objectFolder.lastIndexOf('\\')) /* Levágott jelenlegi mappanév */ + "\\objects\\"; // Célmappa
        for (int phase = 0; phase < anims; ++phase) // Minden fázisra...
        	animPhases[phase] = Obj.Load(objectFolder + new Integer(modelID + phase).toString() + ".dat"); // ...töltse be a modellt
        animPhase = x = y = 0; // Animációs fázis és méretek nullázása
        for (int anim = 0; anim < anims; ++anim) { // Ellenség méretének meghatározása: a legnagyobb az animációk közt
            if (x < animPhases[anim].GetWidth())
                x = animPhases[anim].GetWidth();
            if (y < animPhases[anim].GetHeight())
                y = animPhases[anim].GetHeight();
        }
        size.setText(x + "x" + y + " (automatically calculated)"); // Méret kijelzése
	}

	/**
	 * Az ellenség minden adatának újbóli betöltése.
	 */
	private void ReloadData() {
		String fileName = file.getAbsolutePath(); // A fájl neve
		int AfterBackslash = fileName.lastIndexOf('\\') + 1; // Fájlnév kezdetének helye a fájl elérési útvonalában
        saveAsID = new StringBuilder(fileName.substring(AfterBackslash, fileName.lastIndexOf('.'))); // Fájlnév, azaz mentési azonosító
        byte[] data; // A fájl tartalma
		try {
			data = Files.readAllBytes(file.toPath()); // Fájl beolvasása
		} catch (IOException e) {
			add(new JLabel("Error while reading the file.")); // A felhasználó értesítése, hogy a fájlt nem lehet beolvasni
			return; // Visszatérés, nem allokált tömbbõl olvasással csak gyûlne a hiba
		}
        modelID = data[0]; // Elsõ animációs fázis objektumának azonosítója
        modelID_str = new StringBuilder(new Integer(modelID).toString()); // Elsõ animációs fázis objektumának azonosítója szövegként
        anims = data[1]; // Animációs fázisok száma
        lives = data[2]; // Életek
        floats = data[3]; // Beúszva a pályára helyben marad-e
        shotTime = data[4]; // Lövések közti idõ
        moveUp = data[5]; // Mozog-e felfelé
        moveDown = data[6]; // Mozog-e lefelé
        moveAnyway = data[7]; // Mozog-e pályán kívül
        moveMin = data[8]; // Felsõ mozgáshatár
        moveMax = data[9]; // Alsó mozgáshatár
        ReloadAnimations(); // Animációs fázisok betöltése
        removeAll(); // Ha volt bármi a panelen, tûnjön el
		setLayout(new BorderLayout()); // A szerkesztõfelülethez ennyi kell: vezérlõk balra, ellenség megjelenítése jobbra
		DrawAnimPhase(); // Animációs fázis megrajzolása
		add(drawn, BorderLayout.EAST); // Megrajzolt animációs fázis hozzáadása jobb oldalra
		add(controls); // Vezérlõfelület hozzáadása a méretezhetõ területre
		revalidate();
	}

	/**
	 * Vezérlõ hozzáadása a vezérlõpanelhez.
	 * @param title Vezérlõ neve
	 * @param component Vezérlõ komponens
	 * @return Vezérlõ komponens
	 */
	private Component AddControl(String title, Component component) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 5)); // Új, sorfolytonos, balra igazított panel létrehozása
		panel.setAlignmentX(.048f); // Körülbelül margóba húzza a jelölõnégyzeteket is
		panel.add(new JLabel(title + ":")); // Vezérlõ megjelölése
		panel.add(component); // Vezérlõ komponens elhelyezése
		SwingHelpers.ForceSize(panel, Menu.frame.getWidth() - Menu.browsers.getWidth() - drawn.getWidth(), panel.getPreferredSize().height); // Teljes szélesség
		controls.add(panel); // Hozzáadás a vezérlõpanelhez
		return component; // Vezérlõ komponens visszaadása, hogy lehessen tovább kezelni ugyanabban a sorban
	}

	/** Animációváltó idõzítõ. */
	private class AnimationChanger extends TimerTask {
		public void run() {
			animPhase = (animPhase + 1) % anims; // Körkörösen a következõ fázisba lépés
			String modelID_string = modelID_str.toString(); // A kezdõanimáció mezõ tartalma
			if (modelID_string.matches("\\d*")) { // Ha szám van a kezdõanimáció mezõben (pl. egy billentyût nyomva tartva nincs az)
				int newModel = Integer.parseInt(modelID_string); // A mezõbe írt modellazonosító
				if (modelID != newModel) { // Ha megváltozott a kezdõanimáció
					modelID = newModel; // Az ellenség adatai közé is kerüljön be
					ReloadAnimations(); // Animációk újratöltése
				}
			}
			DrawAnimPhase(); // Az új fázis kirajzolása
		}
	}

	/** Animációk számának megváltoztatása csúszkával */
	private ChangeListener animsChanged = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			anims = ((JSlider)e.getSource()).getValue(); // Érték tárolása
			ReloadAnimations(); // Animálás újrakezdése a megváltozott animációszámmal
		}
	};

	/** Életek megváltoztatása csúszkával */
	private ChangeListener livesChanged = new ChangeListener() { public void stateChanged(ChangeEvent e) {
			lives = ((JSlider)e.getSource()).getValue(); } };
	/** Lebegés megváltoztatása jelölõnégyzettel */
	private ChangeListener floatsChanged = new ChangeListener() { public void stateChanged(ChangeEvent e) {
			floats = ((JCheckBox)e.getSource()).isSelected() ? 1 : 0; } };
	/** Lövések idõközének megváltoztatása csúszkával */
	private ChangeListener shotTimeChanged = new ChangeListener() { public void stateChanged(ChangeEvent e) {
			shotTime = ((JSlider)e.getSource()).getValue(); } };
	/** Felfelé mozgás megváltoztatása jelölõnégyzettel */
	private ChangeListener moveUpChanged = new ChangeListener() { public void stateChanged(ChangeEvent e) {
			moveUp = ((JCheckBox)e.getSource()).isSelected() ? 1 : 0; } };
	/** Lefelé mozgás megváltoztatása jelölõnégyzettel */
	private ChangeListener moveDownChanged = new ChangeListener() { public void stateChanged(ChangeEvent e) {
			moveDown = ((JCheckBox)e.getSource()).isSelected() ? 1 : 0; } };
	/** Csak a képernyõn mozgás megváltoztatása jelölõnégyzettel */
	private ChangeListener moveAnywayChanged = new ChangeListener() { public void stateChanged(ChangeEvent e) {
			moveAnyway = ((JCheckBox)e.getSource()).isSelected() ? 0 : 1; } };
	/** Felsõ mozgáshatár megváltoztatása csúszkával */
	private ChangeListener moveMinChanged = new ChangeListener() { public void stateChanged(ChangeEvent e) {
			moveMin = ((JSlider)e.getSource()).getValue(); } };
	/** Alsó mozgáshatár megváltoztatása csúszkával */
	private ChangeListener moveMaxChanged = new ChangeListener() { public void stateChanged(ChangeEvent e) {
			moveMax = ((JSlider)e.getSource()).getValue(); } };

	/**
	 * Vezérlõsáv létrehozása.
	 */
	private void CreateControls() {
		controls.removeAll(); // Ha már egyszer létre lett hozva a vezérlõsáv, írja felül
		controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS)); // Felülrõl lefelé, FlowLayoutként kerülnek kirajzolásra a vezérlõk
		controls.add(Box.createGlue()); // Automata margó hozzáadása felülre (hogy függõlegesen középen legyen a tartalom)
		AddControl("Enemy ID", new JTextField(saveAsID.toString(), 3)).addKeyListener(new NumericInputListener(saveAsID)); // Mentési ID
		AddControl("Model ID", new JTextField(modelID_str.toString(), 3)).addKeyListener(new NumericInputListener(modelID_str)); // Kezdõmodell
		AddControl("Size", size); // Méretkijelzõ
		((SliderWithDisplay)AddControl("Animation phases", new SliderWithDisplay(1, 15, anims, 50))).slider.addChangeListener(animsChanged); // Animációk
		((SliderWithDisplay)AddControl("Lives", new SliderWithDisplay(1, 127, lives, 150))).slider.addChangeListener(livesChanged); // Életek
		((JCheckBox)controls.add(new JCheckBox("Floats", floats == 1))).addChangeListener(floatsChanged); // Lebegés
		((SliderWithDisplay)AddControl("Shot cooldown", new SliderWithDisplay(0, 80, shotTime, 100))).slider.addChangeListener(shotTimeChanged); // Lövésköz
		((JCheckBox)controls.add(new JCheckBox("Move upwards", moveUp == 1))).addChangeListener(moveUpChanged); // Felfelé mozgás
		((JCheckBox)controls.add(new JCheckBox("Move downwards", moveDown == 1))).addChangeListener(moveDownChanged); // Lefelé mozgás
		((JCheckBox)controls.add(new JCheckBox("Only move on screen", moveAnyway == 0))).addChangeListener(moveAnywayChanged); // Csak a képernyõn mozgás
		((SliderWithDisplay)AddControl("Top position", new SliderWithDisplay(0, 47, moveMin, 100))).slider.addChangeListener(moveMinChanged); // Felsõ határ
		((SliderWithDisplay)AddControl("Bottom position", new SliderWithDisplay(0, 47, moveMax, 100))).slider.addChangeListener(moveMaxChanged); // Alsó határ
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 5)); // Panel a két gombnak, hogy sorban legyenek
		((JButton)buttons.add(new JButton("Save"))).addActionListener(new ActionListener() { // Mentés gomb
			public void actionPerformed(ActionEvent e) {
				if (Integer.parseInt(saveAsID.toString()) > 255) // Ha túl nagy számot adott meg, tudjon róla
					JOptionPane.showMessageDialog(null, "Error: the ID can't be larger than 255.", "Save result", JOptionPane.ERROR_MESSAGE);
				else { // Ha 0-255 közti egész számot adott meg
					String fileName = file.getAbsolutePath(); // Vegyük a megnyitott fájl nevét...
					fileName = fileName.substring(0, fileName.lastIndexOf("\\") + 1) + saveAsID + ".dat"; // ...és cseréljük le benne az azonosítót
					try { // Mentés megpróbálása az új néven
						FileOutputStream fos = new FileOutputStream(fileName); // Írandó fájl megnyitása, szándékosan ilyen változónévvel
						fos.write(new byte[] {Byte.parseByte(modelID_str.toString()), (byte)anims, (byte)lives, (byte)floats, (byte)shotTime, (byte)moveUp,
					            (byte)moveDown, (byte)moveAnyway, (byte)moveMin, (byte)moveMax}); // Bájttömb kiírása
						fos.close(); // Fájl bezárása
						new BrowserPanelFolderClick(file.getParentFile().getParentFile().getParentFile()).actionPerformed(null); // Fájltallózók frissítése
						JOptionPane.showMessageDialog(null, "Enemy saved successfully.", "Save result", JOptionPane.INFORMATION_MESSAGE); // Sikerjelzés
					} catch (Exception expecto_patronum) { // Sikertelen mentés esetén errõl tájékoztassa a felhasználót
						JOptionPane.showMessageDialog(null, "An error ocqured while saving the enemy.", "Save result", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		((JButton)buttons.add(new JButton("Revert"))).addActionListener(new ActionListener() { // Visszaállítás gomb
			public void actionPerformed(ActionEvent e) {
				ReloadData(); // Ellenség teljes újratöltése
				CreateControls(); // Vezérlõk újra létrehozása (egyszerûbb, mint értékeket frissíteni)
			}
		});
		controls.add(buttons); // Gombok megjelenítése
		controls.add(Box.createGlue()); // Automata margó hozzáadása alulra
	}
	/**
	 * Szerkesztõpanel létrehozása a megadott fájlhoz.
	 * @param file Egy ellenséget tartalmazó fájl
	 */
	public EditorEnemy(File file) {
		this.file = file;
		ReloadData(); // Fájl betöltése és a panel létrehozása
		SwingHelpers.ForceSize(size, size.getPreferredSize().width + 10, size.getPreferredSize().height); // Létrejött a méretpanel, férjen el benne még szám
		CreateControls(); // Vezérlõk létrehozása
		Cleanup(); // Az animációváltó lelövése, ha fut
		animChanger = new Timer(); // Új idõzítõ létrehozása...
		animChanger.schedule(new AnimationChanger(), 500, 500); // ...hogy fél másodpercenként a következõ animációs fázist mutassa
	}
}