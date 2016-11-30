package main;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;

import browser.BrowserPanelFolderClick;
import graphics.*;
import swingupdate.*;

/**
 * Pályákat szerkesztõ panel.
 * @author Sgánetz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class EditorLevel extends JPanel {
	private static final long serialVersionUID = 1L;

	/** Ellenségbejegyzés a pályafájlban. */
	private class Enemy {
		/** Szélességi koordináta. */
		public int x;
		/** Magassági koordináta. */
		public int y;
		/** Ellenség azonosítója */
		public int enemyID;
		/** Mozgásirány Y tengelyen. */
		public int movement;
		/** Megjelenített panel. */
		public JPanel display;

		public Enemy(int x, int y, int enemyID, int movement) {
			this.x = x;
			this.y = y;
			this.enemyID = enemyID;
			this.movement = movement;
		}

		public Enemy clone() {
			return new Enemy(x, y, enemyID, movement);
		}
	}

	/**
	 * Ellenségtömböt másol.
	 * @param source Forrás
	 * @return Másolat
	 */
	public static Enemy[] EnemyArrayClone(Enemy[] source) {
		int sourceCount = source.length; // Ellenségek száma a forrásban
		Enemy[] destination = new Enemy[sourceCount]; // Klöntömb
		for (int i = 0; i < sourceCount; ++i) // Minden ellenség...
			destination[i] = source[i].clone(); // ...átmásolása, nem referenciával, klónozással
		return destination;
	}

	/** Az aktuálisan szerkesztett pályát tartalmazó, eredetileg megnyitott fájl. */
	private File file;
	/** Jelenlegi pályaállapot. */
	private Enemy[] enemies;
	/** Betöltéskori pályaállapot. */
	private Enemy[] original;
	/** A pályán fellelhetõ ellenségek ellenségazonosítójához tartozó modellei. */
	private Map<Integer, Obj> models = new HashMap<Integer, Obj>();
	/** Ellenségeket tartalmazó mappa elérési útvonala. */
	private String enemyFolder;
	/** Grafikus objektumokat tartalmazó mappa elérési útvonala. */
	private String objectFolder;
	/** A kirajzolt pálya. */
	private JPanel level = new JPanel();

	/**
	 * Ellenségazonosítóhoz tartozó elsõ grafikus objektum gyorstárazása.
	 * @param id Ellenségazonosító
	 */
	private void LoadModel(int id) {
        if (!models.containsKey(id)) { // Ha még nincs betöltve
			try {
				byte[] enemyData = Files.readAllBytes(Paths.get(enemyFolder + id + ".dat")); // Olvassa be az ellenség adatait...
				models.put(id, Obj.Load(objectFolder + enemyData[0] + ".dat")); // ...majd az abban található elsõ animációs fázis modelljét
			} catch (IOException e) { // Ha nem sikerült beolvasni az ellenség modelljét...
				models.put(id, Obj.error.clone()); // ...hibajelzõt jelenítsen meg az ellenség helyén, de még szerkeszthetõ legyen a pálya
			}
        }
    }

	/**
	 * Pályaadatok (újra)betöltése.
	 */
	private void ReloadData() {
		try {
			byte[] data = Files.readAllBytes(file.toPath()); // Kiválasztott pálya beolvasása, 0. bájt az ellenségek száma
			int enemyCount = SwingHelpers.GetSByte(data[0]); // Ellenségek száma
			enemies = new Enemy[enemyCount]; // Ellenségek tömbjének inicializálása
			original = new Enemy[enemyCount]; // Eredeti ellenségtömb inicializálása
			int DataPointer = 0; // A vizsgált bájt száma
			for (byte i = 0; i < enemyCount; ++i) { // Ellenségeken végighaladás
				int id; // Ellenségfájl azonosítója
				enemies[i] = new Enemy( // Ellenség beolvasása
					SwingHelpers.GetSByte(data[++DataPointer]) * 256 + SwingHelpers.GetSByte(data[++DataPointer]), // Szélességi koordináta: big endian ushort
					data[++DataPointer], // Magassági koordináta (nem kell fix, mert <48)
					id = SwingHelpers.GetSByte(data[++DataPointer]), // Ellenségazonosító: gyorsan kimentjük
					SwingHelpers.GetSByte(data[++DataPointer])); // Mozgásirány Y tengelyen
				original[i] = enemies[i].clone(); // Eredeti eltárolása külön
				LoadModel(id); // Töltse be az ellenség grafikáját
			}
		} catch (Exception e) { // Betöltési hiba esetén legyen lehetõség új pályát elkezdeni szerkeszteni
			enemies = new Enemy[0];
			original = new Enemy[0];
		}
	}

	/**
	 * Pálya mentése a megadott néven.
	 * @param path Elérési útvonal
	 */
	private void SaveAs(String path) {
        Arrays.sort(enemies, new Comparator<Enemy>() { // Ellenségek rendezése pozíció, elõször is szélességi alapján
			public int compare(Enemy l, Enemy r) { // A pályán elõrébb lévõk kerüljenek elõrébb a fájlban
				return l.x != r.x ? new Integer(l.x).compareTo(new Integer(r.x)) : new Integer(l.y).compareTo(new Integer(r.y));
			}
        });
        FileOutputStream fos; // Ha ez nem itt kívül van, a precompiler úgy veszi, hogy nincs használva
		try {
			fos = new FileOutputStream(path); // Fájl megnyitása írásra
			fos.write((byte)enemies.length); // Ellenségek számának beírása
	        for (int i = 0; i < enemies.length; ++i) { // Majd ellenségenként
	        	fos.write((byte)(enemies[i].x / 256)); // Szélességi pozíció (big endian), nagyobb helyi érték
	        	fos.write((byte)(enemies[i].x % 256)); // Szélességi pozíció (big endian), kisebb helyi érték
	        	fos.write((byte)enemies[i].y); // Magassági pozíció
	        	fos.write((byte)enemies[i].enemyID); // Ellenség azonosítója
	        	fos.write((byte)(enemies[i].movement)); // Mozgás iránya a magasságtengelyen + 1
	        }
	        new BrowserPanelFolderClick(file.getParentFile().getParentFile().getParentFile()).actionPerformed(null); // Fájltallózók frissítése
	        JOptionPane.showMessageDialog(null, "Level saved successfully.", "Save result", JOptionPane.INFORMATION_MESSAGE); // Sikerjelzés
		} catch (Exception e) { // Sikertelen mentés esetén errõl tájékoztassa a felhasználót
			JOptionPane.showMessageDialog(null, "An error ocqured while saving the level.", "Save result", JOptionPane.ERROR_MESSAGE);
		}
    }

	/** Visszaállítás gomb akciója. */
	private ActionListener revertAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			enemies = EnemyArrayClone(original); // Eredeti ellenséglista visszamásolása a jelenlegibe
			Redraw(); // Pálya újrarajzolása
		}
	};

	/** Mentés gomb akciója. */
	private ActionListener saveAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			SaveAs(file.getAbsolutePath());
		}
	};

	/** Akciófigyelõ a mentés másként gombhoz. */
	private ActionListener saveAsAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int saveID = SwingHelpers.AskUserForID("Level"); // Szintazonosító bekérése a felhasználótól
			if (saveID != -1) { // Ha a felhasználó érvényes azonosítót adott meg
				String fileName = file.getAbsolutePath(); // Vegyük a megnyitott fájl nevét...
				fileName = fileName.substring(0, fileName.lastIndexOf("\\") + 1) + saveID + ".dat"; // ...és cseréljük le benne az azonosítót
				SaveAs(fileName); // Mentés az új néven
				file = new File(fileName); // Innentõl a mentés az új fájlt írja felül, ne az eredetileg megnyitottat
				original = EnemyArrayClone(enemies); // Ha már a mentés szerinti eredeti fájl megváltozott, az eredeti szint is változzon a szerkesztettre
			}
		}
	};

	/** A szint méretének beállítása, mivel az abszolút layout nem teszi ezt meg. */
	private void ResizeLevel() {
		SwingHelpers.FixNullBounds(level); // Tartalom alapján ajánlott méret kérése
		level.setPreferredSize(new Dimension(level.getPreferredSize().width, 48 * Obj.pixelSize)); // A magasság lecserélése a pálya magasságára
		((JScrollPane)level.getParent() /* Ez még csak a viewport */ .getParent()).getHorizontalScrollBar().revalidate(); // Frissüljön a görgetõsáv
	}

	/** Ellenség eltávolítása a pályáról. */
	private ActionListener removeEnemy = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			JPanel panel = (JPanel)((JPanel)((JButton)e.getSource()).getParent()).getParent(); // Megjelenített ellenség panelje
			int enemyCount = enemies.length; // Ellenségek száma
			for (int i = 0; i < enemyCount; ++i) { // Az ellenségeket vizsgálva...
				if (enemies[i].display == panel) { // ...ha valamelyiknek ez a megjelenítése, az törlendõ
					--enemyCount; // Ellenségszám csökkentése
					enemies[i] = enemies[enemyCount - 1]; // A tömb utolsó eleme kerüljön a törlendõ helyre
					enemies = Arrays.copyOf(enemies, enemyCount); // Utolsó ellenség levágása
				}
			}
			level.remove(panel); // Panel levétele a pályáról
			level.repaint(); // Pálya újrarajzolása, különben ott marad a panel
			ResizeLevel(); // Pálya újraméretezése (hogy ne lehessen túlgörgetni, a következõ görgetési próbálkozás már a pálya keretein belül lesz)
		}
	};

	/** Irányokat jelzõ nyílkarakterek, a fájlban a mozgásirány azonosítójának megfelelõ helyeken vannak az adott irányba mutató nyilak. */
	private static final String[] directions = new String[] {
		Character.toString((char)0x25B2), // Felfelé nyíl
		Character.toString((char)0x25C0), // Balra nyíl
		Character.toString((char)0x25BC)}; // Lefelé nyíl

	/** Mozgásirány megváltoztatása. */
	private ActionListener changeDirection = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			JButton button = (JButton)e.getSource(); // Kattintott gomb
			JPanel panel = (JPanel)button.getParent().getParent(); // Megjelenített ellenség panelje
			int enemyCount = enemies.length; // Ellenségek száma
			for (int i = 0; i < enemyCount; ++i) { // Az ellenségeket vizsgálva...
				if (enemies[i].display == panel) { // ...ha valamelyiknek ez a megjelenítése, az módosítandó
					enemies[i].movement = (enemies[i].movement + 1) % 3; // Körbe haladás a 3 lehetséges irányon
					button.setText(directions[enemies[i].movement]); // Aktuális mozgásirány kijelzése a gombon
				}
			}
		}
	};

	/** Ellenség mozgatási akciója. */
	private MouseMotionListener enemyMoved = new MouseMotionListener() {
		public void mouseMoved(MouseEvent e) {}
		public void mouseDragged(MouseEvent e) {
			JPanel panel = (JPanel)e.getSource(); // Megjelenített ellenség panelje
			int enemyCount = enemies.length; // Ellenségek száma
			for (int i = 0; i < enemyCount; ++i) { // Az ellenségeket vizsgálva...
				if (enemies[i].display == panel) { // ...ha valamelyiknek ez a megjelenítése, az módosítandó
					enemies[i].x = panel.getX() / Obj.pixelSize; // Szélességi pozíció frissítése
					enemies[i].y = panel.getY() / Obj.pixelSize; // Magassági pozíció frissítése
				}
			}
			ResizeLevel(); // Pálya méretének újraszámolása, hátha a legszélsõ mozdult el
		}
	};

	/**
	 * Ellenség kirajzolása a megjelenített szinten 
	 * @param enemy Ellenség
	 */
	private void DisplayEnemy(Enemy enemy) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 0)); // Konténerpanel, ne legyen párnázás
		JPanel model = models.get(enemy.enemyID).Draw(); // Ellenség kirajzolása
		panel.add(model); // Rajz hozzáadása a konténerhez
		JPanel buttons = new JPanel(new BorderLayout()); // Gombok panelje
		JButton closeButton = new JButton("X"); // Bezárás gomb
		closeButton.addActionListener(removeEnemy); // Bezárás akció hozzárendelése
		buttons.add(closeButton, BorderLayout.NORTH); // Bezárás gomb hozzáadása a gombpanel tetejére
		JButton directionButton = new JButton(directions[enemy.movement]); // Mozgásirány gomb
		directionButton.addActionListener(changeDirection); // Mozgásirány-változtatás akció hozzárendelése
		buttons.add(directionButton, BorderLayout.SOUTH); // Mozgásirány gomb hozzáadása a gombpanel aljára
		SwingHelpers.ForceSize(buttons, 50, model.getPreferredSize().height); // Gombok szélessége legyen fix, és a felsõ legyen felül, az alsó alul
		buttons.setBackground(Obj.inactiveColor); // A két gomb közti rész legyen háttérszínû
		panel.add(buttons); // Kerüljenek az ellenségtõl jobbra a gombok
		MouseMover.enableFor(panel, Obj.pixelSize); // Panel mozgatásának engedélyezése
		panel.addMouseMotionListener(enemyMoved); // Mozgás figyelése
		enemy.display = (JPanel)level.add(panel); // Megjelenített ellenség szinthez adása és ellenségben tárolása
		panel.setBounds(enemy.x * Obj.pixelSize, enemy.y * Obj.pixelSize, panel.getPreferredSize().width, panel.getPreferredSize().height); // Elhelyezés
	}

	/** Ellenség hozzáadása a pályához */
	private ActionListener addEnemyAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int enemyID = SwingHelpers.AskUserForID("Enemy"); // Ellenségazonosító bekérése a felhasználótól
			if (enemyID != -1) { // Ha a felhasználó érvényes azonosítót adott meg
				JScrollPane levelBase = (JScrollPane)level.getParent().getParent();
				levelBase.getHorizontalScrollBar().getValue();
				int enemyCount = enemies.length; // Ellenségek száma
				enemies = Arrays.copyOf(enemies, enemyCount + 1); // Új hely hozzáadása az ellenségtömb végére
				enemies[enemyCount] = new Enemy( // Új ellenség létrehozása
						levelBase.getHorizontalScrollBar().getValue() / Obj.pixelSize, // A jelenlegi képernyõ elejére kerüljön 
						0, // A pálya tetejére kerüljön
						enemyID, // Ellenség azonosítója
						1); // Eredeti mozgásirány: elõre
				LoadModel(enemyID); // Grafika betöltése, ha még nem volt ilyen ellenség
				DisplayEnemy(enemies[enemyCount]); // Új ellenség megjelenítése
				level.revalidate(); // Mûködjön is
			}
		}
	};

	/**
	 * Pálya és felület újrarajzolása.
	 */
	private void Redraw() {
		removeAll(); // Ha esetleg volt valami kirajzolva, már ne legyen
		setLayout(new BorderLayout()); // Két panel lesz, a pályaszerkesztõ, és alatta a vezérlõsáv
		JPanel controls = new JPanel(); // Vezérlõsáv
		((JButton)controls.add(new JButton("AddEnemy"))).addActionListener(addEnemyAction); // Visszaállítás gomb
		((JButton)controls.add(new JButton("Revert"))).addActionListener(revertAction); // Visszaállítás gomb
		((JButton)controls.add(new JButton("Save"))).addActionListener(saveAction); // Mentés gomb
		((JButton)controls.add(new JButton("Save as"))).addActionListener(saveAsAction); // Mentés másként gomb
		add(controls, BorderLayout.SOUTH); // Vezérlõsáv elhelyezése alul
		Obj.pixelSize = (Menu.browsers.getHeight() - controls.getPreferredSize().height - 15 /* Görgetõsáv mérete */)
			/ 48; // A kirajzolt pálya pont kitöltse a helyet
		level.removeAll(); // Ha esetleg volt valami a szintre rajzolva, már ne legyen
		JScrollPane levelBase = new JScrollPane(level, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS); // A szint alapja a szroll, de preferáltan csak vízszintesen
		level.setLayout(null); // A szint elrendezése legyen szabad
		level.setBackground(Obj.inactiveColor); // A szint háttere a játék grafikájának háttérszínével egyezzen
		for (int i = 0; i < enemies.length; ++i) // Ellenségek végigiterálása
			DisplayEnemy(enemies[i]); // És felpakolása a pályára
		ResizeLevel(); // Pálya újraméretezése (hogy használható legyen a görgetõsáv)
		add(levelBase); // Szint elhelyezése a maradék helyen
		revalidate(); // Legyen ismét használható a panel
	}

	/**
	 * Szerkesztõpanel létrehozása a megadott fájlhoz.
	 * @param file Egy szintet tartalmazó fájl
	 */
	public EditorLevel(File file) {
		this.file = file; // Fájl tárolása
		String path = file.getAbsolutePath(); // Fájlnév
		enemyFolder = path.substring(0, path.lastIndexOf('\\')); // Fájlnév levágása
		enemyFolder = enemyFolder.substring(0, enemyFolder.lastIndexOf('\\')); // Gyökérmappáig visszavágás
		objectFolder = enemyFolder + "\\objects\\"; // Objektumok mappába mozgás
		enemyFolder += "\\enemies\\"; // Ellenségek mappába mozgás
		ReloadData(); // Pályaadatok beolvasása
		Redraw(); // Kirajzolás
	}
}