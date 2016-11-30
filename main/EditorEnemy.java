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
 * Ellens�geket szerkeszt� panel.
 * @author Sg�netz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class EditorEnemy extends JPanel {
	private static final long serialVersionUID = 1L;

	/** Az aktu�lisan szerkesztett ellens�get tartalmaz�, eredetileg megnyitott f�jl. */
	private File file;

	/** Ment�s, mint ez az azonos�t�. A t�pus az�rt StringBuilder, hogy referenciak�nt �t lehessen adni. */
	private StringBuilder saveAsID;
	/** Els� anim�ci�s f�zis modellj�nek azonos�t�ja sz�vegk�nt. A t�pus az�rt StringBuilder, hogy referenciak�nt �t lehessen adni. */
	private StringBuilder modelID_str;
	/** Els� anim�ci�s f�zis modellj�nek azonos�t�ja. */
	private int modelID;
	/** Sz�less�g. */
	private int x;
	/** Magass�g. */
	private int y;
	/** Anim�ci�s. */
	private int anims;
	/** �letek sz�ma. */
	private int lives;
	/** Be�szva a p�ly�ra v�zszintesen helyben marad-e? */
	private int floats;
	/** L�v�sek k�zti id�. */
	private int shotTime;
	/** Mozog-e felfel�? */
	private int moveUp;
	/** Mozog-e lefel�? */
	private int moveDown;
	/** Mozog-e, am�g nem �rt be a p�ly�ra? */
	private int moveAnyway;
	/** Fels� mozg�shat�r. */
	private int moveMin;
	/** Als� mozg�shat�r. */
	private int moveMax;

	/** Kezel�panel, az ellens�g �ll�that� tulajdons�gaival. */
	private JPanel controls = new JPanel();
	/** M�retet kijelz� c�mke */
	private JLabel size = new JLabel();
	/** Kirajzolt anim�ci�s f�zis. */
	private JPanel drawn = new JPanel();
	/** Lehets�ges megjelen�sek, anim�ci�s f�zisok. */
	private Obj[] animPhases;
	/** Jelenleg megjelen�tett anim�ci�s f�zis. */
	private int animPhase;

	/** Anim�ci�v�lt� id�z�t�. */
	private static Timer animChanger = null;

	/**
	 * Az anim�ci�v�lt� lel�v�se, ha fut.
	 */
	public static void Cleanup() {
		if (animChanger != null) // Ha egy el�z� p�ld�nynak m�g futna id�z�t�je...
			animChanger.cancel(); // ...ne fusson tov�bb, mert az Obj kirajzol�sa nem thread-safe, nem kullogna el a h�tt�rben GC-re v�rva ez az id�z�t�,
								  // �s m�g a pixelm�retet is fel�l�rja a h�tt�rben, ami rosszul hat ki egy esetlegesen elkezdett rajzol�sra
	}

	/**
	 * Anim�ci�s f�zis kirajzol�sa.
	 */
	private void DrawAnimPhase() {
		this.drawn.removeAll(); // Rajzpanel �r�t�se
		int drawSpaceX = (Menu.frame.getWidth() - Menu.browsers.getWidth()) / 2, drawSpaceY = Menu.browsers.getHeight(); // A megjelen�tett elens�g m�rete
		Obj.pixelSize = Math.min( // �gy m�retezze a rajzot, hogy a helynek, ahova menni fog, �rintse minimum az egyik tengelyen a sz�leit
			drawSpaceX / animPhases[animPhase].GetWidth(), // Sz�less�g, az ablak �s a tall�z�s�v k�l�nbs�ge adja meg
			drawSpaceY / animPhases[animPhase].GetHeight()); // Magass�g, a keret ablakmagass�got adna, az�rt a tall�z�s�v
		JPanel drawn = SwingHelpers.CenterVertically(animPhases[animPhase].Draw()); // F�gg�legesen k�z�pen legyen a kirajzolt ellens�g
		SwingHelpers.ForceSize(drawn, drawSpaceX, drawSpaceY); // A megjelen�t� panel sz�less�ge ne v�ltozzon az anim�ci�s f�zissal
		this.drawn.add(drawn); // �j rajz hozz�ad�sa a rajzpanelhez
		this.drawn.revalidate(); // Rajzpanel �jrahiteles�t�se, k�l�nben nem rajzol�dik ki
	}

	/**
	 * Az �sszes anim�ci�s f�zis �jb�li bet�lt�se.
	 */
	private void ReloadAnimations() {
		modelID = Integer.parseInt(modelID_str.toString()); // Els� anim�ci�s f�zis objektum�nak azonos�t�ja
        animPhases = new Obj[anims]; // Az �sszes anim�ci�s f�zis grafik�inak helye
        String objectFolder = file.getAbsolutePath(); // Objektummappa kezdete (jelenlegi f�jln�v)
        objectFolder = objectFolder.substring(0, objectFolder.lastIndexOf('\\')); // Lev�g�s a mapp�ig
        objectFolder = objectFolder.substring(0, objectFolder.lastIndexOf('\\')) /* Lev�gott jelenlegi mappan�v */ + "\\objects\\"; // C�lmappa
        for (int phase = 0; phase < anims; ++phase) // Minden f�zisra...
        	animPhases[phase] = Obj.Load(objectFolder + new Integer(modelID + phase).toString() + ".dat"); // ...t�ltse be a modellt
        animPhase = x = y = 0; // Anim�ci�s f�zis �s m�retek null�z�sa
        for (int anim = 0; anim < anims; ++anim) { // Ellens�g m�ret�nek meghat�roz�sa: a legnagyobb az anim�ci�k k�zt
            if (x < animPhases[anim].GetWidth())
                x = animPhases[anim].GetWidth();
            if (y < animPhases[anim].GetHeight())
                y = animPhases[anim].GetHeight();
        }
        size.setText(x + "x" + y + " (automatically calculated)"); // M�ret kijelz�se
	}

	/**
	 * Az ellens�g minden adat�nak �jb�li bet�lt�se.
	 */
	private void ReloadData() {
		String fileName = file.getAbsolutePath(); // A f�jl neve
		int AfterBackslash = fileName.lastIndexOf('\\') + 1; // F�jln�v kezdet�nek helye a f�jl el�r�si �tvonal�ban
        saveAsID = new StringBuilder(fileName.substring(AfterBackslash, fileName.lastIndexOf('.'))); // F�jln�v, azaz ment�si azonos�t�
        byte[] data; // A f�jl tartalma
		try {
			data = Files.readAllBytes(file.toPath()); // F�jl beolvas�sa
		} catch (IOException e) {
			add(new JLabel("Error while reading the file.")); // A felhaszn�l� �rtes�t�se, hogy a f�jlt nem lehet beolvasni
			return; // Visszat�r�s, nem allok�lt t�mbb�l olvas�ssal csak gy�lne a hiba
		}
        modelID = data[0]; // Els� anim�ci�s f�zis objektum�nak azonos�t�ja
        modelID_str = new StringBuilder(new Integer(modelID).toString()); // Els� anim�ci�s f�zis objektum�nak azonos�t�ja sz�vegk�nt
        anims = data[1]; // Anim�ci�s f�zisok sz�ma
        lives = data[2]; // �letek
        floats = data[3]; // Be�szva a p�ly�ra helyben marad-e
        shotTime = data[4]; // L�v�sek k�zti id�
        moveUp = data[5]; // Mozog-e felfel�
        moveDown = data[6]; // Mozog-e lefel�
        moveAnyway = data[7]; // Mozog-e p�ly�n k�v�l
        moveMin = data[8]; // Fels� mozg�shat�r
        moveMax = data[9]; // Als� mozg�shat�r
        ReloadAnimations(); // Anim�ci�s f�zisok bet�lt�se
        removeAll(); // Ha volt b�rmi a panelen, t�nj�n el
		setLayout(new BorderLayout()); // A szerkeszt�fel�lethez ennyi kell: vez�rl�k balra, ellens�g megjelen�t�se jobbra
		DrawAnimPhase(); // Anim�ci�s f�zis megrajzol�sa
		add(drawn, BorderLayout.EAST); // Megrajzolt anim�ci�s f�zis hozz�ad�sa jobb oldalra
		add(controls); // Vez�rl�fel�let hozz�ad�sa a m�retezhet� ter�letre
		revalidate();
	}

	/**
	 * Vez�rl� hozz�ad�sa a vez�rl�panelhez.
	 * @param title Vez�rl� neve
	 * @param component Vez�rl� komponens
	 * @return Vez�rl� komponens
	 */
	private Component AddControl(String title, Component component) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 5)); // �j, sorfolytonos, balra igaz�tott panel l�trehoz�sa
		panel.setAlignmentX(.048f); // K�r�lbel�l marg�ba h�zza a jel�l�n�gyzeteket is
		panel.add(new JLabel(title + ":")); // Vez�rl� megjel�l�se
		panel.add(component); // Vez�rl� komponens elhelyez�se
		SwingHelpers.ForceSize(panel, Menu.frame.getWidth() - Menu.browsers.getWidth() - drawn.getWidth(), panel.getPreferredSize().height); // Teljes sz�less�g
		controls.add(panel); // Hozz�ad�s a vez�rl�panelhez
		return component; // Vez�rl� komponens visszaad�sa, hogy lehessen tov�bb kezelni ugyanabban a sorban
	}

	/** Anim�ci�v�lt� id�z�t�. */
	private class AnimationChanger extends TimerTask {
		public void run() {
			animPhase = (animPhase + 1) % anims; // K�rk�r�sen a k�vetkez� f�zisba l�p�s
			String modelID_string = modelID_str.toString(); // A kezd�anim�ci� mez� tartalma
			if (modelID_string.matches("\\d*")) { // Ha sz�m van a kezd�anim�ci� mez�ben (pl. egy billenty�t nyomva tartva nincs az)
				int newModel = Integer.parseInt(modelID_string); // A mez�be �rt modellazonos�t�
				if (modelID != newModel) { // Ha megv�ltozott a kezd�anim�ci�
					modelID = newModel; // Az ellens�g adatai k�z� is ker�lj�n be
					ReloadAnimations(); // Anim�ci�k �jrat�lt�se
				}
			}
			DrawAnimPhase(); // Az �j f�zis kirajzol�sa
		}
	}

	/** Anim�ci�k sz�m�nak megv�ltoztat�sa cs�szk�val */
	private ChangeListener animsChanged = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			anims = ((JSlider)e.getSource()).getValue(); // �rt�k t�rol�sa
			ReloadAnimations(); // Anim�l�s �jrakezd�se a megv�ltozott anim�ci�sz�mmal
		}
	};

	/** �letek megv�ltoztat�sa cs�szk�val */
	private ChangeListener livesChanged = new ChangeListener() { public void stateChanged(ChangeEvent e) {
			lives = ((JSlider)e.getSource()).getValue(); } };
	/** Lebeg�s megv�ltoztat�sa jel�l�n�gyzettel */
	private ChangeListener floatsChanged = new ChangeListener() { public void stateChanged(ChangeEvent e) {
			floats = ((JCheckBox)e.getSource()).isSelected() ? 1 : 0; } };
	/** L�v�sek id�k�z�nek megv�ltoztat�sa cs�szk�val */
	private ChangeListener shotTimeChanged = new ChangeListener() { public void stateChanged(ChangeEvent e) {
			shotTime = ((JSlider)e.getSource()).getValue(); } };
	/** Felfel� mozg�s megv�ltoztat�sa jel�l�n�gyzettel */
	private ChangeListener moveUpChanged = new ChangeListener() { public void stateChanged(ChangeEvent e) {
			moveUp = ((JCheckBox)e.getSource()).isSelected() ? 1 : 0; } };
	/** Lefel� mozg�s megv�ltoztat�sa jel�l�n�gyzettel */
	private ChangeListener moveDownChanged = new ChangeListener() { public void stateChanged(ChangeEvent e) {
			moveDown = ((JCheckBox)e.getSource()).isSelected() ? 1 : 0; } };
	/** Csak a k�perny�n mozg�s megv�ltoztat�sa jel�l�n�gyzettel */
	private ChangeListener moveAnywayChanged = new ChangeListener() { public void stateChanged(ChangeEvent e) {
			moveAnyway = ((JCheckBox)e.getSource()).isSelected() ? 0 : 1; } };
	/** Fels� mozg�shat�r megv�ltoztat�sa cs�szk�val */
	private ChangeListener moveMinChanged = new ChangeListener() { public void stateChanged(ChangeEvent e) {
			moveMin = ((JSlider)e.getSource()).getValue(); } };
	/** Als� mozg�shat�r megv�ltoztat�sa cs�szk�val */
	private ChangeListener moveMaxChanged = new ChangeListener() { public void stateChanged(ChangeEvent e) {
			moveMax = ((JSlider)e.getSource()).getValue(); } };

	/**
	 * Vez�rl�s�v l�trehoz�sa.
	 */
	private void CreateControls() {
		controls.removeAll(); // Ha m�r egyszer l�tre lett hozva a vez�rl�s�v, �rja fel�l
		controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS)); // Fel�lr�l lefel�, FlowLayoutk�nt ker�lnek kirajzol�sra a vez�rl�k
		controls.add(Box.createGlue()); // Automata marg� hozz�ad�sa fel�lre (hogy f�gg�legesen k�z�pen legyen a tartalom)
		AddControl("Enemy ID", new JTextField(saveAsID.toString(), 3)).addKeyListener(new NumericInputListener(saveAsID)); // Ment�si ID
		AddControl("Model ID", new JTextField(modelID_str.toString(), 3)).addKeyListener(new NumericInputListener(modelID_str)); // Kezd�modell
		AddControl("Size", size); // M�retkijelz�
		((SliderWithDisplay)AddControl("Animation phases", new SliderWithDisplay(1, 15, anims, 50))).slider.addChangeListener(animsChanged); // Anim�ci�k
		((SliderWithDisplay)AddControl("Lives", new SliderWithDisplay(1, 127, lives, 150))).slider.addChangeListener(livesChanged); // �letek
		((JCheckBox)controls.add(new JCheckBox("Floats", floats == 1))).addChangeListener(floatsChanged); // Lebeg�s
		((SliderWithDisplay)AddControl("Shot cooldown", new SliderWithDisplay(0, 80, shotTime, 100))).slider.addChangeListener(shotTimeChanged); // L�v�sk�z
		((JCheckBox)controls.add(new JCheckBox("Move upwards", moveUp == 1))).addChangeListener(moveUpChanged); // Felfel� mozg�s
		((JCheckBox)controls.add(new JCheckBox("Move downwards", moveDown == 1))).addChangeListener(moveDownChanged); // Lefel� mozg�s
		((JCheckBox)controls.add(new JCheckBox("Only move on screen", moveAnyway == 0))).addChangeListener(moveAnywayChanged); // Csak a k�perny�n mozg�s
		((SliderWithDisplay)AddControl("Top position", new SliderWithDisplay(0, 47, moveMin, 100))).slider.addChangeListener(moveMinChanged); // Fels� hat�r
		((SliderWithDisplay)AddControl("Bottom position", new SliderWithDisplay(0, 47, moveMax, 100))).slider.addChangeListener(moveMaxChanged); // Als� hat�r
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 5)); // Panel a k�t gombnak, hogy sorban legyenek
		((JButton)buttons.add(new JButton("Save"))).addActionListener(new ActionListener() { // Ment�s gomb
			public void actionPerformed(ActionEvent e) {
				if (Integer.parseInt(saveAsID.toString()) > 255) // Ha t�l nagy sz�mot adott meg, tudjon r�la
					JOptionPane.showMessageDialog(null, "Error: the ID can't be larger than 255.", "Save result", JOptionPane.ERROR_MESSAGE);
				else { // Ha 0-255 k�zti eg�sz sz�mot adott meg
					String fileName = file.getAbsolutePath(); // Vegy�k a megnyitott f�jl nev�t...
					fileName = fileName.substring(0, fileName.lastIndexOf("\\") + 1) + saveAsID + ".dat"; // ...�s cser�lj�k le benne az azonos�t�t
					try { // Ment�s megpr�b�l�sa az �j n�ven
						FileOutputStream fos = new FileOutputStream(fileName); // �rand� f�jl megnyit�sa, sz�nd�kosan ilyen v�ltoz�n�vvel
						fos.write(new byte[] {Byte.parseByte(modelID_str.toString()), (byte)anims, (byte)lives, (byte)floats, (byte)shotTime, (byte)moveUp,
					            (byte)moveDown, (byte)moveAnyway, (byte)moveMin, (byte)moveMax}); // B�jtt�mb ki�r�sa
						fos.close(); // F�jl bez�r�sa
						new BrowserPanelFolderClick(file.getParentFile().getParentFile().getParentFile()).actionPerformed(null); // F�jltall�z�k friss�t�se
						JOptionPane.showMessageDialog(null, "Enemy saved successfully.", "Save result", JOptionPane.INFORMATION_MESSAGE); // Sikerjelz�s
					} catch (Exception expecto_patronum) { // Sikertelen ment�s eset�n err�l t�j�koztassa a felhaszn�l�t
						JOptionPane.showMessageDialog(null, "An error ocqured while saving the enemy.", "Save result", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		((JButton)buttons.add(new JButton("Revert"))).addActionListener(new ActionListener() { // Vissza�ll�t�s gomb
			public void actionPerformed(ActionEvent e) {
				ReloadData(); // Ellens�g teljes �jrat�lt�se
				CreateControls(); // Vez�rl�k �jra l�trehoz�sa (egyszer�bb, mint �rt�keket friss�teni)
			}
		});
		controls.add(buttons); // Gombok megjelen�t�se
		controls.add(Box.createGlue()); // Automata marg� hozz�ad�sa alulra
	}
	/**
	 * Szerkeszt�panel l�trehoz�sa a megadott f�jlhoz.
	 * @param file Egy ellens�get tartalmaz� f�jl
	 */
	public EditorEnemy(File file) {
		this.file = file;
		ReloadData(); // F�jl bet�lt�se �s a panel l�trehoz�sa
		SwingHelpers.ForceSize(size, size.getPreferredSize().width + 10, size.getPreferredSize().height); // L�trej�tt a m�retpanel, f�rjen el benne m�g sz�m
		CreateControls(); // Vez�rl�k l�trehoz�sa
		Cleanup(); // Az anim�ci�v�lt� lel�v�se, ha fut
		animChanger = new Timer(); // �j id�z�t� l�trehoz�sa...
		animChanger.schedule(new AnimationChanger(), 500, 500); // ...hogy f�l m�sodpercenk�nt a k�vetkez� anim�ci�s f�zist mutassa
	}
}