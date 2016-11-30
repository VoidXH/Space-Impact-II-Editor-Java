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
 * P�ly�kat szerkeszt� panel.
 * @author Sg�netz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class EditorLevel extends JPanel {
	private static final long serialVersionUID = 1L;

	/** Ellens�gbejegyz�s a p�lyaf�jlban. */
	private class Enemy {
		/** Sz�less�gi koordin�ta. */
		public int x;
		/** Magass�gi koordin�ta. */
		public int y;
		/** Ellens�g azonos�t�ja */
		public int enemyID;
		/** Mozg�sir�ny Y tengelyen. */
		public int movement;
		/** Megjelen�tett panel. */
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
	 * Ellens�gt�mb�t m�sol.
	 * @param source Forr�s
	 * @return M�solat
	 */
	public static Enemy[] EnemyArrayClone(Enemy[] source) {
		int sourceCount = source.length; // Ellens�gek sz�ma a forr�sban
		Enemy[] destination = new Enemy[sourceCount]; // Kl�nt�mb
		for (int i = 0; i < sourceCount; ++i) // Minden ellens�g...
			destination[i] = source[i].clone(); // ...�tm�sol�sa, nem referenci�val, kl�noz�ssal
		return destination;
	}

	/** Az aktu�lisan szerkesztett p�ly�t tartalmaz�, eredetileg megnyitott f�jl. */
	private File file;
	/** Jelenlegi p�lya�llapot. */
	private Enemy[] enemies;
	/** Bet�lt�skori p�lya�llapot. */
	private Enemy[] original;
	/** A p�ly�n fellelhet� ellens�gek ellens�gazonos�t�j�hoz tartoz� modellei. */
	private Map<Integer, Obj> models = new HashMap<Integer, Obj>();
	/** Ellens�geket tartalmaz� mappa el�r�si �tvonala. */
	private String enemyFolder;
	/** Grafikus objektumokat tartalmaz� mappa el�r�si �tvonala. */
	private String objectFolder;
	/** A kirajzolt p�lya. */
	private JPanel level = new JPanel();

	/**
	 * Ellens�gazonos�t�hoz tartoz� els� grafikus objektum gyorst�raz�sa.
	 * @param id Ellens�gazonos�t�
	 */
	private void LoadModel(int id) {
        if (!models.containsKey(id)) { // Ha m�g nincs bet�ltve
			try {
				byte[] enemyData = Files.readAllBytes(Paths.get(enemyFolder + id + ".dat")); // Olvassa be az ellens�g adatait...
				models.put(id, Obj.Load(objectFolder + enemyData[0] + ".dat")); // ...majd az abban tal�lhat� els� anim�ci�s f�zis modellj�t
			} catch (IOException e) { // Ha nem siker�lt beolvasni az ellens�g modellj�t...
				models.put(id, Obj.error.clone()); // ...hibajelz�t jelen�tsen meg az ellens�g hely�n, de m�g szerkeszthet� legyen a p�lya
			}
        }
    }

	/**
	 * P�lyaadatok (�jra)bet�lt�se.
	 */
	private void ReloadData() {
		try {
			byte[] data = Files.readAllBytes(file.toPath()); // Kiv�lasztott p�lya beolvas�sa, 0. b�jt az ellens�gek sz�ma
			int enemyCount = SwingHelpers.GetSByte(data[0]); // Ellens�gek sz�ma
			enemies = new Enemy[enemyCount]; // Ellens�gek t�mbj�nek inicializ�l�sa
			original = new Enemy[enemyCount]; // Eredeti ellens�gt�mb inicializ�l�sa
			int DataPointer = 0; // A vizsg�lt b�jt sz�ma
			for (byte i = 0; i < enemyCount; ++i) { // Ellens�geken v�gighalad�s
				int id; // Ellens�gf�jl azonos�t�ja
				enemies[i] = new Enemy( // Ellens�g beolvas�sa
					SwingHelpers.GetSByte(data[++DataPointer]) * 256 + SwingHelpers.GetSByte(data[++DataPointer]), // Sz�less�gi koordin�ta: big endian ushort
					data[++DataPointer], // Magass�gi koordin�ta (nem kell fix, mert <48)
					id = SwingHelpers.GetSByte(data[++DataPointer]), // Ellens�gazonos�t�: gyorsan kimentj�k
					SwingHelpers.GetSByte(data[++DataPointer])); // Mozg�sir�ny Y tengelyen
				original[i] = enemies[i].clone(); // Eredeti elt�rol�sa k�l�n
				LoadModel(id); // T�ltse be az ellens�g grafik�j�t
			}
		} catch (Exception e) { // Bet�lt�si hiba eset�n legyen lehet�s�g �j p�ly�t elkezdeni szerkeszteni
			enemies = new Enemy[0];
			original = new Enemy[0];
		}
	}

	/**
	 * P�lya ment�se a megadott n�ven.
	 * @param path El�r�si �tvonal
	 */
	private void SaveAs(String path) {
        Arrays.sort(enemies, new Comparator<Enemy>() { // Ellens�gek rendez�se poz�ci�, el�sz�r is sz�less�gi alapj�n
			public int compare(Enemy l, Enemy r) { // A p�ly�n el�r�bb l�v�k ker�ljenek el�r�bb a f�jlban
				return l.x != r.x ? new Integer(l.x).compareTo(new Integer(r.x)) : new Integer(l.y).compareTo(new Integer(r.y));
			}
        });
        FileOutputStream fos; // Ha ez nem itt k�v�l van, a precompiler �gy veszi, hogy nincs haszn�lva
		try {
			fos = new FileOutputStream(path); // F�jl megnyit�sa �r�sra
			fos.write((byte)enemies.length); // Ellens�gek sz�m�nak be�r�sa
	        for (int i = 0; i < enemies.length; ++i) { // Majd ellens�genk�nt
	        	fos.write((byte)(enemies[i].x / 256)); // Sz�less�gi poz�ci� (big endian), nagyobb helyi �rt�k
	        	fos.write((byte)(enemies[i].x % 256)); // Sz�less�gi poz�ci� (big endian), kisebb helyi �rt�k
	        	fos.write((byte)enemies[i].y); // Magass�gi poz�ci�
	        	fos.write((byte)enemies[i].enemyID); // Ellens�g azonos�t�ja
	        	fos.write((byte)(enemies[i].movement)); // Mozg�s ir�nya a magass�gtengelyen + 1
	        }
	        new BrowserPanelFolderClick(file.getParentFile().getParentFile().getParentFile()).actionPerformed(null); // F�jltall�z�k friss�t�se
	        JOptionPane.showMessageDialog(null, "Level saved successfully.", "Save result", JOptionPane.INFORMATION_MESSAGE); // Sikerjelz�s
		} catch (Exception e) { // Sikertelen ment�s eset�n err�l t�j�koztassa a felhaszn�l�t
			JOptionPane.showMessageDialog(null, "An error ocqured while saving the level.", "Save result", JOptionPane.ERROR_MESSAGE);
		}
    }

	/** Vissza�ll�t�s gomb akci�ja. */
	private ActionListener revertAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			enemies = EnemyArrayClone(original); // Eredeti ellens�glista visszam�sol�sa a jelenlegibe
			Redraw(); // P�lya �jrarajzol�sa
		}
	};

	/** Ment�s gomb akci�ja. */
	private ActionListener saveAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			SaveAs(file.getAbsolutePath());
		}
	};

	/** Akci�figyel� a ment�s m�sk�nt gombhoz. */
	private ActionListener saveAsAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int saveID = SwingHelpers.AskUserForID("Level"); // Szintazonos�t� bek�r�se a felhaszn�l�t�l
			if (saveID != -1) { // Ha a felhaszn�l� �rv�nyes azonos�t�t adott meg
				String fileName = file.getAbsolutePath(); // Vegy�k a megnyitott f�jl nev�t...
				fileName = fileName.substring(0, fileName.lastIndexOf("\\") + 1) + saveID + ".dat"; // ...�s cser�lj�k le benne az azonos�t�t
				SaveAs(fileName); // Ment�s az �j n�ven
				file = new File(fileName); // Innent�l a ment�s az �j f�jlt �rja fel�l, ne az eredetileg megnyitottat
				original = EnemyArrayClone(enemies); // Ha m�r a ment�s szerinti eredeti f�jl megv�ltozott, az eredeti szint is v�ltozzon a szerkesztettre
			}
		}
	};

	/** A szint m�ret�nek be�ll�t�sa, mivel az abszol�t layout nem teszi ezt meg. */
	private void ResizeLevel() {
		SwingHelpers.FixNullBounds(level); // Tartalom alapj�n aj�nlott m�ret k�r�se
		level.setPreferredSize(new Dimension(level.getPreferredSize().width, 48 * Obj.pixelSize)); // A magass�g lecser�l�se a p�lya magass�g�ra
		((JScrollPane)level.getParent() /* Ez m�g csak a viewport */ .getParent()).getHorizontalScrollBar().revalidate(); // Friss�lj�n a g�rget�s�v
	}

	/** Ellens�g elt�vol�t�sa a p�ly�r�l. */
	private ActionListener removeEnemy = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			JPanel panel = (JPanel)((JPanel)((JButton)e.getSource()).getParent()).getParent(); // Megjelen�tett ellens�g panelje
			int enemyCount = enemies.length; // Ellens�gek sz�ma
			for (int i = 0; i < enemyCount; ++i) { // Az ellens�geket vizsg�lva...
				if (enemies[i].display == panel) { // ...ha valamelyiknek ez a megjelen�t�se, az t�rlend�
					--enemyCount; // Ellens�gsz�m cs�kkent�se
					enemies[i] = enemies[enemyCount - 1]; // A t�mb utols� eleme ker�lj�n a t�rlend� helyre
					enemies = Arrays.copyOf(enemies, enemyCount); // Utols� ellens�g lev�g�sa
				}
			}
			level.remove(panel); // Panel lev�tele a p�ly�r�l
			level.repaint(); // P�lya �jrarajzol�sa, k�l�nben ott marad a panel
			ResizeLevel(); // P�lya �jram�retez�se (hogy ne lehessen t�lg�rgetni, a k�vetkez� g�rget�si pr�b�lkoz�s m�r a p�lya keretein bel�l lesz)
		}
	};

	/** Ir�nyokat jelz� ny�lkarakterek, a f�jlban a mozg�sir�ny azonos�t�j�nak megfelel� helyeken vannak az adott ir�nyba mutat� nyilak. */
	private static final String[] directions = new String[] {
		Character.toString((char)0x25B2), // Felfel� ny�l
		Character.toString((char)0x25C0), // Balra ny�l
		Character.toString((char)0x25BC)}; // Lefel� ny�l

	/** Mozg�sir�ny megv�ltoztat�sa. */
	private ActionListener changeDirection = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			JButton button = (JButton)e.getSource(); // Kattintott gomb
			JPanel panel = (JPanel)button.getParent().getParent(); // Megjelen�tett ellens�g panelje
			int enemyCount = enemies.length; // Ellens�gek sz�ma
			for (int i = 0; i < enemyCount; ++i) { // Az ellens�geket vizsg�lva...
				if (enemies[i].display == panel) { // ...ha valamelyiknek ez a megjelen�t�se, az m�dos�tand�
					enemies[i].movement = (enemies[i].movement + 1) % 3; // K�rbe halad�s a 3 lehets�ges ir�nyon
					button.setText(directions[enemies[i].movement]); // Aktu�lis mozg�sir�ny kijelz�se a gombon
				}
			}
		}
	};

	/** Ellens�g mozgat�si akci�ja. */
	private MouseMotionListener enemyMoved = new MouseMotionListener() {
		public void mouseMoved(MouseEvent e) {}
		public void mouseDragged(MouseEvent e) {
			JPanel panel = (JPanel)e.getSource(); // Megjelen�tett ellens�g panelje
			int enemyCount = enemies.length; // Ellens�gek sz�ma
			for (int i = 0; i < enemyCount; ++i) { // Az ellens�geket vizsg�lva...
				if (enemies[i].display == panel) { // ...ha valamelyiknek ez a megjelen�t�se, az m�dos�tand�
					enemies[i].x = panel.getX() / Obj.pixelSize; // Sz�less�gi poz�ci� friss�t�se
					enemies[i].y = panel.getY() / Obj.pixelSize; // Magass�gi poz�ci� friss�t�se
				}
			}
			ResizeLevel(); // P�lya m�ret�nek �jrasz�mol�sa, h�tha a legsz�ls� mozdult el
		}
	};

	/**
	 * Ellens�g kirajzol�sa a megjelen�tett szinten 
	 * @param enemy Ellens�g
	 */
	private void DisplayEnemy(Enemy enemy) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 0)); // Kont�nerpanel, ne legyen p�rn�z�s
		JPanel model = models.get(enemy.enemyID).Draw(); // Ellens�g kirajzol�sa
		panel.add(model); // Rajz hozz�ad�sa a kont�nerhez
		JPanel buttons = new JPanel(new BorderLayout()); // Gombok panelje
		JButton closeButton = new JButton("X"); // Bez�r�s gomb
		closeButton.addActionListener(removeEnemy); // Bez�r�s akci� hozz�rendel�se
		buttons.add(closeButton, BorderLayout.NORTH); // Bez�r�s gomb hozz�ad�sa a gombpanel tetej�re
		JButton directionButton = new JButton(directions[enemy.movement]); // Mozg�sir�ny gomb
		directionButton.addActionListener(changeDirection); // Mozg�sir�ny-v�ltoztat�s akci� hozz�rendel�se
		buttons.add(directionButton, BorderLayout.SOUTH); // Mozg�sir�ny gomb hozz�ad�sa a gombpanel alj�ra
		SwingHelpers.ForceSize(buttons, 50, model.getPreferredSize().height); // Gombok sz�less�ge legyen fix, �s a fels� legyen fel�l, az als� alul
		buttons.setBackground(Obj.inactiveColor); // A k�t gomb k�zti r�sz legyen h�tt�rsz�n�
		panel.add(buttons); // Ker�ljenek az ellens�gt�l jobbra a gombok
		MouseMover.enableFor(panel, Obj.pixelSize); // Panel mozgat�s�nak enged�lyez�se
		panel.addMouseMotionListener(enemyMoved); // Mozg�s figyel�se
		enemy.display = (JPanel)level.add(panel); // Megjelen�tett ellens�g szinthez ad�sa �s ellens�gben t�rol�sa
		panel.setBounds(enemy.x * Obj.pixelSize, enemy.y * Obj.pixelSize, panel.getPreferredSize().width, panel.getPreferredSize().height); // Elhelyez�s
	}

	/** Ellens�g hozz�ad�sa a p�ly�hoz */
	private ActionListener addEnemyAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int enemyID = SwingHelpers.AskUserForID("Enemy"); // Ellens�gazonos�t� bek�r�se a felhaszn�l�t�l
			if (enemyID != -1) { // Ha a felhaszn�l� �rv�nyes azonos�t�t adott meg
				JScrollPane levelBase = (JScrollPane)level.getParent().getParent();
				levelBase.getHorizontalScrollBar().getValue();
				int enemyCount = enemies.length; // Ellens�gek sz�ma
				enemies = Arrays.copyOf(enemies, enemyCount + 1); // �j hely hozz�ad�sa az ellens�gt�mb v�g�re
				enemies[enemyCount] = new Enemy( // �j ellens�g l�trehoz�sa
						levelBase.getHorizontalScrollBar().getValue() / Obj.pixelSize, // A jelenlegi k�perny� elej�re ker�lj�n 
						0, // A p�lya tetej�re ker�lj�n
						enemyID, // Ellens�g azonos�t�ja
						1); // Eredeti mozg�sir�ny: el�re
				LoadModel(enemyID); // Grafika bet�lt�se, ha m�g nem volt ilyen ellens�g
				DisplayEnemy(enemies[enemyCount]); // �j ellens�g megjelen�t�se
				level.revalidate(); // M�k�dj�n is
			}
		}
	};

	/**
	 * P�lya �s fel�let �jrarajzol�sa.
	 */
	private void Redraw() {
		removeAll(); // Ha esetleg volt valami kirajzolva, m�r ne legyen
		setLayout(new BorderLayout()); // K�t panel lesz, a p�lyaszerkeszt�, �s alatta a vez�rl�s�v
		JPanel controls = new JPanel(); // Vez�rl�s�v
		((JButton)controls.add(new JButton("AddEnemy"))).addActionListener(addEnemyAction); // Vissza�ll�t�s gomb
		((JButton)controls.add(new JButton("Revert"))).addActionListener(revertAction); // Vissza�ll�t�s gomb
		((JButton)controls.add(new JButton("Save"))).addActionListener(saveAction); // Ment�s gomb
		((JButton)controls.add(new JButton("Save as"))).addActionListener(saveAsAction); // Ment�s m�sk�nt gomb
		add(controls, BorderLayout.SOUTH); // Vez�rl�s�v elhelyez�se alul
		Obj.pixelSize = (Menu.browsers.getHeight() - controls.getPreferredSize().height - 15 /* G�rget�s�v m�rete */)
			/ 48; // A kirajzolt p�lya pont kit�ltse a helyet
		level.removeAll(); // Ha esetleg volt valami a szintre rajzolva, m�r ne legyen
		JScrollPane levelBase = new JScrollPane(level, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS); // A szint alapja a szroll, de prefer�ltan csak v�zszintesen
		level.setLayout(null); // A szint elrendez�se legyen szabad
		level.setBackground(Obj.inactiveColor); // A szint h�ttere a j�t�k grafik�j�nak h�tt�rsz�n�vel egyezzen
		for (int i = 0; i < enemies.length; ++i) // Ellens�gek v�gigiter�l�sa
			DisplayEnemy(enemies[i]); // �s felpakol�sa a p�ly�ra
		ResizeLevel(); // P�lya �jram�retez�se (hogy haszn�lhat� legyen a g�rget�s�v)
		add(levelBase); // Szint elhelyez�se a marad�k helyen
		revalidate(); // Legyen ism�t haszn�lhat� a panel
	}

	/**
	 * Szerkeszt�panel l�trehoz�sa a megadott f�jlhoz.
	 * @param file Egy szintet tartalmaz� f�jl
	 */
	public EditorLevel(File file) {
		this.file = file; // F�jl t�rol�sa
		String path = file.getAbsolutePath(); // F�jln�v
		enemyFolder = path.substring(0, path.lastIndexOf('\\')); // F�jln�v lev�g�sa
		enemyFolder = enemyFolder.substring(0, enemyFolder.lastIndexOf('\\')); // Gy�k�rmapp�ig visszav�g�s
		objectFolder = enemyFolder + "\\objects\\"; // Objektumok mapp�ba mozg�s
		enemyFolder += "\\enemies\\"; // Ellens�gek mapp�ba mozg�s
		ReloadData(); // P�lyaadatok beolvas�sa
		Redraw(); // Kirajzol�s
	}
}