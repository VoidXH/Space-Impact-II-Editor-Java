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
 * Grafikai objektumokat szerkeszt� panel.
 * @author Sg�netz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class EditorObject extends JPanel {
	private static final long serialVersionUID = 1L;

	/** Az aktu�lisan szerkesztett k�pet tartalmaz�, eredetileg megnyitott f�jl. */
	private File file;
	/** Az aktu�lisan szerkesztett k�p. */
	private Obj image;
	/** Az eredetileg megnyitott k�p. */
	private Obj originalImage;
	/** Vez�rl�s�v, amire a m�dos�t� cs�szk�k �s gombok ker�lnek, a ment�si lehet�s�gek mell�. Ez egy v�zszintes s�v, ez�rt j� a gy�ri FlowLayout. */
	private JPanel controllers = new JPanel();
	/** A szerkesztett k�p m�reteit kijelz� c�mke. */
	private JLabel size = new JLabel();
	/** A sz�less�get szab�lyz� cs�szka. */
	private JSlider widthSlider = new JSlider(1, 84);
	/** A magass�got szab�lyz� cs�szka. */
	private JSlider heightSlider = new JSlider(1, 48);

	/**
	 * Csak a k�p �jrarajzol�sa, a vez�rl�s�v megtart�s�val.
	 */
	public void Redraw() {
		removeAll(); // Minden komponens elt�vol�t�sa a panelr�l - semmi gond, a vez�rl�s�v el van t�rolva
		int controllersHeight = controllers.getPreferredSize().height; // Kezel�s�v magass�ga - els� rajzol�s el�tt csak ez ad �rtelmes adatot
		Obj.pixelSize = Math.min( // �gy m�retezze a rajzot, hogy a helynek, ahova menni fog, �rintse minimum az egyik tengelyen a sz�leit
			(Menu.frame.getWidth() - Menu.browsers.getWidth()) / image.GetWidth(), // Sz�less�g, az ablak �s a tall�z�s�v k�l�nbs�ge adja meg
			(Menu.browsers.getHeight() - controllersHeight) / image.GetHeight()); // Magass�g, a keret ablakmagass�got adna, az�rt a tall�z�s�v
		add(SwingHelpers.CenterVertically(image.Draw(true))); // A k�p �jrarajzol�sa a panel k�zep�re
		add(controllers, BorderLayout.SOUTH); // A vez�rl�s�v m�dos�tatlanul ker�lj�n vissza ugyanoda, ahol volt
		revalidate(); // N�h�ny m�k�d�s (pl. rajzra kattint�s) miatt �jra kell hiteles�teni a panelt
	}

	/**
	 * Ment�s a megadott helyre.
	 * @param path El�r�si �tvonal
	 */
	private void SaveAs(String path) {
		if (image.Save(path)) { // Ha sikeresen el lehet menteni
			new BrowserPanelFolderClick(file.getParentFile().getParentFile().getParentFile()).actionPerformed(null); // F�jltall�z�k friss�t�se
			JOptionPane.showMessageDialog(null, "Object saved successfully.", "Save result", JOptionPane.INFORMATION_MESSAGE); // Siker jelz�se
		} else // Sikertelen ment�s eset�n err�l t�j�koztassa a felhaszn�l�t
			JOptionPane.showMessageDialog(null, "An error ocqured while saving the object.", "Save result", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * A k�p m�ret�nek kijelz�se az erre szolg�l� c�mk�n.
	 */
	private void DisplaySize() {
		size.setText("Size: " + image.GetWidth() + "x" + image.GetHeight());
	}

	/**
	 * Ha nem a felhaszn�l� m�retezte �t a k�pet, friss�tse a m�retet, az �tm�retez� cs�szk�k �rt�k�t, �s rajzolja �jra
	 */
	private void OnResized() {
		widthSlider.setValue(image.GetWidth()); // Sz�less�g�ll�t� cs�szka �rt�k�nek be�ll�t�sa
		heightSlider.setValue(image.GetHeight()); // Magass�g�ll�t� cs�szka �rt�k�nek be�ll�t�sa
		DisplaySize(); // M�dosult m�ret kijelz�se
	}

	/** V�ltoz�sfigyel� a sz�less�get �ll�t� cs�szk�hoz. */
	private ChangeListener widthChangeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			image.SetWidth(((JSlider)e.getSource()).getValue()); // K�p sz�less�g�nek m�dos�t�sa
			DisplaySize(); // Megv�ltozott m�ret kijelz�se
			Redraw(); // K�p �jrarajzol�sa az �j m�retben
		}
	};

	/** V�ltoz�sfigyel� a magass�got �ll�t� cs�szk�hoz. */
	private ChangeListener heightChangeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			image.SetHeight(((JSlider)e.getSource()).getValue()); // K�p magass�g�nak m�dos�t�sa
			DisplaySize(); // Megv�ltozott m�ret kijelz�se
			Redraw(); // K�p �jrarajzol�sa az �j m�retben
		}
	};

	/** Akci�figyel� az �r�t�s gombhoz. */
	private ActionListener clearAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			image.Clear(); // Az objektum �r�t�se
			Redraw(); // A k�p fel�l�r�sa a semmivel
		}
	};

	/** Akci�figyel� a vissza�ll�t�s gombhoz. */
	private ActionListener revertAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			image = originalImage.clone(); // Az eredeti objektum visszam�sol�sa
			OnResized(); // Ha a szerkeszt�sben v�ltoztak a m�retek, �lljanak vissza az eredetire
			Redraw(); // Eredeti k�p visszarajzol�sa
		}
	};

	/** Akci�figyel� a metsz�s gombhoz. */
	private ActionListener trimAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			image.Trim(); // K�p metsz�se
			OnResized(); // A metszett m�ret kijelz�se
			Redraw(); // A metszett k�p kirajzol�sa
		}
	};

	/** Akci�figyel� a ment�s gombhoz. */
	private ActionListener saveAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			SaveAs(file.getAbsolutePath()); // Az eredeti f�jl fel�l�r�sa az �j rajzzal.
		}
	};

	/** Akci�figyel� a ment�s m�sk�nt gombhoz. */
	private ActionListener saveAsAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int saveID = SwingHelpers.AskUserForID("Object"); // Szintazonos�t� bek�r�se a felhaszn�l�t�l
			if (saveID != -1) { // Ha a felhaszn�l� �rv�nyes azonos�t�t adott meg
				String fileName = file.getAbsolutePath(); // Vegy�k a megnyitott f�jl nev�t...
				fileName = fileName.substring(0, fileName.lastIndexOf("\\") + 1) + saveID + ".dat"; // ...�s cser�lj�k le benne az azonos�t�t
				SaveAs(fileName); // Ment�s az �j n�ven
				file = new File(fileName); // Innent�l a ment�s az �j f�jlt �rja fel�l, ne az eredetileg megnyitottat
				originalImage = image.clone(); // Ha m�r a ment�s szerinti eredeti f�jl megv�ltozott, az eredeti k�p is v�ltozzon a szerkesztettre
			}
		}
	};

	/** Akci�figyel� a hardcode gombhoz */
	private ActionListener hardcodeAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int size = image.GetWidth() * image.GetHeight(); // K�pm�ret
			StringBuilder outString = new StringBuilder("[").append(size).append("] = {"); // Kimenet, t�mbform�tumban
			for (int pixel = 0; pixel < size; ++pixel) // Minden pixelt...
				outString.append(image.image[pixel] == 0 ? '0' : '1').append(','); // ...adjon hozz�, vessz�vel elv�lasztva
			outString.setCharAt(outString.length() - 1, '}'); // Utols� vessz� lev�g�sa, t�mb lez�r�sa
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(outString.toString()), null); // V�g�lapra m�sol�s
			JOptionPane.showMessageDialog(null, "Hardcode copied to clipboard.", "Hardcode result", JOptionPane.INFORMATION_MESSAGE); // Inf� r�la
		}
	};

	/**
	 * Szerkeszt�panel l�trehoz�sa a megadott f�jlhoz.
	 * @param file Egy grafikus objektumot tartalmaz� f�jl
	 */
	public EditorObject(File file) {
		this.file = file; // F�jl t�rol�sa
		image = Obj.Load(file.getAbsolutePath()); // Objektum bet�lt�se
		originalImage = image.clone(); // Objektum lem�sol�sa, ha a felhaszn�l� vissza szeretn� �ll�tani a kezdeti �llapotot
		setLayout(new BorderLayout()); // A szerkeszt�fel�lethez ennyi kell: vez�rl�s�v alulra, illeszked� tartalom a marad�k helyre
		OnResized(); // Kezd��rt�k ad�sa h�rom komponensnek, k�z�l�k az egyiknek a k�vetkez� sorban kellenek a m�retei
		SwingHelpers.ForceSize(size, (int)size.getPreferredSize().width + 15, (int)size.getPreferredSize().height); // Legyen hely plusz sz�mjegyeknek
		controllers.add(size); // A m�ret kijelz�je ker�lj�n bal oldalra
		SwingHelpers.ForceSize(widthSlider, 100, widthSlider.getPreferredSize().height); // 100 sz�lesek legyenek a m�retcs�szk�k
		SwingHelpers.ForceSize(heightSlider, 100, heightSlider.getPreferredSize().height); // Alapb�l t�l sz�les egy cs�szka, hogy idef�rjen
		controllers.add(new JLabel("Width:")); // Sz�less�g cs�szk�t jelz� c�mke
		((JSlider)controllers.add(widthSlider)).addChangeListener(widthChangeListener); // Sz�less�g cs�szka
		controllers.add(new JLabel("Height:")); // Magass�g cs�szk�t jelz� c�mke
		((JSlider)controllers.add(heightSlider)).addChangeListener(heightChangeListener); // Magass�g cs�szka
		((JButton)controllers.add(new JButton("Clear"))).addActionListener(clearAction); // �r�t�s gomb
		((JButton)controllers.add(new JButton("Revert"))).addActionListener(revertAction); // Vissza�ll�t�s gomb
		((JButton)controllers.add(new JButton("Trim"))).addActionListener(trimAction); // Metsz�s gomb
		((JButton)controllers.add(new JButton("Save"))).addActionListener(saveAction); // Ment�s gomb
		((JButton)controllers.add(new JButton("Save as"))).addActionListener(saveAsAction); // Ment�s m�sk�nt gomb
		((JButton)controllers.add(new JButton("Hardcode"))).addActionListener(hardcodeAction); // Hardcode gomb
		Redraw(); // Fel�let kirajzol�sa
	}
}