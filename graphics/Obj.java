package graphics;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
import swingupdate.*;

/**
 * Egy k�tdimenzi�s grafikus objektum (n�piesen: k�p) t�rol�ja. A t�rol�si m�dszer le�r�s��rt l�sd: {@link #image}.
 * @author Sg�netz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class Obj {
	/** Akt�v pixelek sz�ne. */
	public static final Color activeColor = new Color(178, 189, 8);
	/** Inakt�v pixelek sz�ne. */
	public static final Color inactiveColor = new Color(0, 0, 0);
	/** Kirajzol�si szorz�, a k�p pixelei ennyiszer lesznek nagyobbak sz�lt�kben �s magass�gukban. */
	public static int pixelSize = 12;

	/** A k�p sz�less�ge. */
	private int x;
	/** A k�p magass�ga. */
	private int y;
	/**
	 * Az objektum pixelei sorfolytonosan. 0 vagy 1 lehet az �rt�k�k. 
	 * Az�rt b�jtok t�mbje, hogy a t�m�r�tett objektumokat helyben ki lehessen bontani.
	 */
	public byte[] image;

	/**
	 * Teljesen �res k�p konstruktora.
	 */
	public Obj() {
		this.x = this.y = 0;
	}

	/**
	 * Adott m�ret� �res k�p konstruktora.
	 * @param x Sz�less�g
	 * @param y Magass�g
	 */
	public Obj(int x, int y) {
		this.x = x;
		this.y = y;
		this.image = new byte[x * y];
	}

	/**
	 * Gener�lt adatokb�l k�pet l�trehoz� konstruktor.
	 * @param x Sz�less�g
	 * @param y Magass�g
	 * @param image Pixelt�rk�p
	 */
	public Obj(int x, int y, byte[] image) {
		this.x = x;
		this.y = y;
		this.image = image;
	}

	/**
	 * Lem�sol egy k�pet.
	 * @return M�solat
	 */
	public Obj clone() {
		return new Obj(x, y, image.clone()); // El�g a konstruktorh�v�s, de ne ugyanaz legyen a t�mb, mert akkor mindk�t p�ld�ny m�dosulna
	}

    /**
     * Egy objektum beolvas�sa f�jlb�l.
     * @param path El�r�si �tvonal
     * @return Beolvasott objektum
     */
	public static Obj Load(String path) {
		byte[] data; // A f�jlban t�rolt b�jtok
		try {
			data = Files.readAllBytes(new File(path).toPath()); // F�jl b�jtjainak beolvas�sa
		} catch (IOException e) { // Hiba eset�n...
			return error.clone(); // ...visszat�r�s a hib�t jelz� objektum m�solat�val
		}
		Obj loaded = new Obj(data[0], data[1]); // T�rol� l�trehoz�sa; az els� k�t b�jtban van a m�ret
		int pixels = data[0] * data[1]; // Pixelek sz�ma
		int bytes = pixels / 8 + (pixels % 8 != 0 ? 1 : 0); // T�m�r�tett t�mb m�rete
		int bits = pixels % 8; // A vizsg�lt b�jtb�l h�tral�v� bitek
		if (bits == 0) // Ha a pixelt�rk�p pontosan kit�lti a b�jtokat...
			bits = 8; // ...az eg�sz vizsg�lt b�jt h�tra van
		while (bytes-- != 0) { // Am�g van t�m�r�tett adat
			while (bits-- != 0) { // Am�g a vizsg�lt b�jtban van feldolgozatlan pixel
				loaded.image[bytes * 8 + bits] = (byte)(data[bytes + 2] % 2 == 0 ? 0 : 1); // Pixel elhelyez�se a hely�n + unsigned fix (el�jeles a byte)
				data[bytes + 2] >>= 1; // Ugr�s a k�vetkez� bitre a vizsg�lt b�jton
			}
			bits = 8; // A k�vetkez� b�jt feldolgozand� pixelei
		}
		return loaded; // Adja vissza a sikeresen bet�lt�tt objektumot
	}

	/**
	 * F�jlba menti az adott objektumot.
	 * @param path El�r�si �tvonal
	 * @return A ment�s sikeress�ge
	 */
	public boolean Save(String path) {
		int pixels = x * y; // Pixelek sz�ma
		int FileLength = pixels / 8 + (pixels % 8 != 0 ? 3 : 2); // T�m�r�tett pixelek sz�ma + b�jtok a m�retnek
		byte[] bytes = new byte[FileLength]; // F�jlba �rand� tartalom helye
		bytes[0] = (byte)x; // 0. b�jt: sz�less�g
		bytes[1] = (byte)y; // 1. b�jt: magass�g
		int writtenByte = 1; // Jelenleg �r�s alatt l�v� b�jt (a k�vetkez� ciklus els� l�p�sk�nt 2-re l�ki)
		for (int pixel = 0; pixel < pixels; ++pixel) { // Minden pixel t�m�r�t�se
			if (pixel % 8 == 0) // Ha 8 pixellel v�gzett...
				++writtenByte; // ...nyisson �j b�jtot
			bytes[writtenByte] <<= 1; // Hely szor�t�sa a jelenlegi b�jtban a k�vetkez� pixelnek
			if (image[pixel] == 1) // Ha akt�v a vizsg�lt pixel...
				++bytes[writtenByte]; // ...a jelenlegi b�jtba �rja bele
		}
		try {
			FileOutputStream fos = new FileOutputStream(path); // �rand� f�jl megnyit�sa, sz�nd�kosan ilyen v�ltoz�n�vvel
			fos.write(bytes); // B�jtt�mb ki�r�sa
			fos.close(); // F�jl bez�r�sa
			return true; // A f�jlba �r�s sikeresen megt�rt�nt, jelezze
		} catch (Exception e) { // Hiba eset�n...
			return false; // ...jelezze vissza a h�v�nak, hogy sikertelen a ment�s
		}
	}

	/**
	 * Getter a sz�less�gre.
	 * @return Sz�less�g
	 */
	public int GetWidth() {
		return x;
	}

	/**
	 * Getter a magass�gra.
	 * @return Magass�g
	 */
	public int GetHeight() {
		return y;
	}

	/**
	 * �tm�retez�s sz�lt�ben. Az �jonnan szerzett sz�less�gi pixelek �resek lesznek. Ha valahol ki lett rajzolva a k�p, aj�nlott �jrarajzolni.
	 * @param value �j sz�less�g
	 */
	public void SetWidth(int value) {
		int oldX = x; // El�z� sz�less�g t�rol�sa
		x = value; // �j sz�less�g alkalmaz�sa
		int oldPixel = 0; // Adott pixel r�gi helye
		int pixel = 0; // Adott pixel jelenlegi helye
		int widthDiff = oldX - x; // Sz�less�gk�l�nbs�g
		byte[] oldImage = image; // R�gi pixelt�rk�p
		image = new byte[x * y]; // �j, �res pixelt�rk�p
		for (int row = 0; row < y; ++row) { // A jelnlegi m�ret sorain...
			for (int column = 0; column < x; ++column) { // ...majd oszlopain (teh�t a pixelek sorrendj�ben) haladva...
				image[pixel++] = column >= oldX ? (byte)0 :
					oldImage[oldPixel]; // ...az odaill� pixel beilleszt�se, ami a r�gi sz�less�gen t�l biztos nem volt akt�v
				++oldPixel; // A r�gi pixelt�mb k�vetkez� elem�re ugr�s
			}
			oldPixel += widthDiff; // A r�gi pixelt�mbben is a k�vetkez� sor elej�re ker�lj�n a vizsg�lat
		}
	}

	/**
	 * �tm�retez�s magass�g�ban. Az �jonnan szerzett magass�gi pixelek �resek lesznek. Ha valahol ki lett rajzolva a k�p, aj�nlott �jrarajzolni.
	 * @param value �j magass�g
	 */
	public void SetHeight(int value) {
		y = value; // �j sz�less�g alkalmaz�sa
		image = Arrays.copyOf(image, x * y); // Pixelt�rk�p �tm�retez�se, vagyis egy m�solat a t�mbr�l a megtartand� pixelekkel
	}

	/**
	 * A k�p �r�t�se. Ha valahol ki lett rajzolva a k�p, aj�nlott �jrarajzolni.
	 */
	public void Clear() {
		int pixels = x * y; // Pixelek sz�ma
		for (int i = 0; i < pixels; ++i) // V�gighaladva a pixeleken...
			image[i] = 0; // ...kinull�zza azokat
	}

	/**
	 * �res sorok �s oszlopok lev�g�sa a sz�lekr�l. �res objektumot nem metsz. Ha valahol ki lett rajzolva a k�p, aj�nlott �jrarajzolni.
	 */
	public void Trim() {
		int cropTop = 48, cropLeft = 84, cropRight = 0, cropBottom = 0; // Lev�g�si hat�rok
		int pixels = x * y; // Pixelek sz�ma
		boolean work = false; // Legyen-e v�g�s
		for (int pixel = 0; pixel < pixels; ++pixel) { // Minden pixelt ellen�rizzen
			if (image[pixel] == 1) { // Ha egy pixel akt�v
				int row = pixel / x, column = pixel % x; // Sor �s oszlop meghat�roz�sa
				if (cropTop > row) // Ha feljebb van, mint eddig b�rmi...
					cropTop = row; // ...terjessze ki addig a lev�g�st
				if (cropLeft > column) // Ha balr�bb van, mint eddig b�rmi...
					cropLeft = column; // ...terjessze ki addig a lev�g�st
				if (cropRight < column) // Ha jobbr�bb van, mint eddig b�rmi...
					cropRight = column; // ...terjessze ki addig a lev�g�st
				if (cropBottom < row) // Ha lejjebb van, mint eddig b�rmi...
					cropBottom = row; // ...terjessze ki addig a lev�g�st
				work = true; // Legyen v�g�s
			}
		}
		if (work) { // Ha van akt�v pixel
			byte[] newMap = new byte[84 * 48]; // �j pixelt�rk�p
			int newWidth = cropRight - cropLeft + 1, newHeight = cropBottom - cropTop + 1; // �j sz�less�g �s magass�g
			int newPixel = 0; // Adott pixel �j helye
			for (int row = 0; row < newHeight; ++row) // El�bb soronk�nt...
				for (int Column = 0; Column < newWidth; ++Column) // ...majd oszloponk�nt bej�r�s (ez a b�jtsorrend)
					newMap[newPixel++] = image[(cropTop + row) * x + cropLeft + Column]; // Pixelek �j hely�kre mozgat�sa
			x = newWidth; // Sz�less�g friss�t�se
			y = newHeight; // Magass�g friss�t�se
			image = newMap; // Pixelt�rk�p friss�t�se
		}
	}

	/** Rajzoland� akt�v pixel, JLabel ikonnak */
	private static ImageIcon activePixel = null;
	/** Rajzoland� inakt�v pixel, JLabel ikonnak */
	private static ImageIcon inactivePixel = null;

	/**
	 * Getter az akt�v pixelre.
	 * @return Akt�v pixel
	 */
	public static ImageIcon ActivePixel() {
		return activePixel;
	}

	/**
	 * Getter az inakt�v pixelre.
	 * @return Inakt�v pixel
	 */
	public static ImageIcon InactivePixel() {
		return inactivePixel;
	}

	/**
	 * Gy�ri param�teres verzi� a {@link #Draw(boolean)}-hoz, ahol a szerkeszt�s lehet�s�ge hamis
	 * @return Kirajzolt panel
	 */
	public JPanel Draw() {
		return Draw(false); // Ha nincs megadva szerkeszthet�s�g, legyen kikapcsolva
	}

	/**
	 * Az objektum kirajzol�sa.
	 * @param canEdit Szerkeszthet� legyen-e a pixelekre kattint�ssal
	 * @return Kirajzolt panel
	 */
	public JPanel Draw(boolean canEdit) {
		if (activePixel == null || activePixel.getIconWidth() != pixelSize ||
			inactivePixel == null || inactivePixel.getIconWidth() != pixelSize) { // Ha nem k�sz�ltek el a pixelek, vagy rossz a m�ret�k
			int activeRGB = activeColor.getRGB(), inactiveRGB = inactiveColor.getRGB(); // ARGB int-k�nt a sz�nek
			BufferedImage
				active = new BufferedImage(pixelSize, pixelSize, BufferedImage.TYPE_INT_ARGB), // Ebbe rajzol�dik az akt�v pixel
				inactive = new BufferedImage(pixelSize, pixelSize, BufferedImage.TYPE_INT_ARGB); // Ebbe rajzol�dik az inakt�v pixel
			for (int i = 0; i < pixelSize; ++i) { // A k�t k�p minden pixele a megfelel� sz�n� legyen
				for (int j = 0; j < pixelSize; ++j) {
					active.setRGB(i, j, activeRGB);
					inactive.setRGB(i, j, inactiveRGB);
				}
			}
			activePixel = new ImageIcon(active); // Akt�v pixel ikonn� alak�t�sa
			inactivePixel = new ImageIcon(inactive); // Inakt�v pixel ikonn� alak�t�sa
		}
		JPanel container = new JPanel(); // Kell egy k�ls� panel (kont�ner), hogy a rajzot ne lehessen helysz�k�ben kisebbre m�retezni
		((FlowLayout)container.getLayout()).setHgap(0); // V�zszintes r�sek null�z�sa (ak�r marg� is lehet)
		((FlowLayout)container.getLayout()).setVgap(0); // F�gg�leges r�sek null�z�sa (ak�r marg� is lehet)
		JPanel drawn = new JPanel(); // A bels� panelre ker�lnek fel a pixelek, c�mkeikonk�nt, egyes�vel
		drawn.setLayout(new GridLayout(0, x)); // A sz�less�gnek megfelel� oszlopb�l �ll� r�cs legyen az alapja
		int pixels = x * y; // A k�p pixelsz�ma
		JLabel[] drawnPixels = new JLabel[pixels]; // Minden pixel egy JLabel, aminek az ikonk�pe az adott pixel
		for (int i = 0; i < pixels; ++i)
			drawnPixels[i] = (JLabel)drawn.add(new JLabel(image[i] == 0 ? inactivePixel : activePixel));
		if (canEdit) // Ha szerkeszthet�...
			drawn.addMouseListener(new ObjPixelListener(this, drawnPixels)); // ...az ezt kezel� Listener-t rakja a pixeleket tartalmaz� panelre
		SwingHelpers.ForceSize(drawn, x * pixelSize, y * pixelSize); // A rajz m�rete er�ltetetten be legyen tartva, az �tm�retez�st�l nem j� a szerkeszt�s
		SwingHelpers.ForceSize(container, x * pixelSize, y * pixelSize); // A kont�ner is a maga �tj�t j�rn� a m�retez�ssel, ha nincs er�ltetve
		container.add(drawn); // Csomagolja a kont�nerbe a k�pet
		return container; // Adja vissza a kont�nert
	}

	/** Bet�lt�si hib�t jelz� k�p */
	public static Obj error = new Obj(9, 11, new byte[] {
	0,1,1,1,1,1,1,1,0,
	1,0,0,0,0,0,0,0,1,
	1,0,1,1,1,1,0,0,1,
	1,0,0,0,0,1,1,0,1,
	1,0,0,0,1,1,0,0,1,
	1,0,0,1,1,0,0,0,1,
	1,0,0,1,1,0,0,0,1,
	1,0,0,0,0,0,0,0,1,
	1,0,0,1,1,0,0,0,1,
	1,0,0,0,0,0,0,0,1,
	0,1,1,1,1,1,1,1,0});
	/** A j�t�k log�j�nak Space r�sze */
	public static Obj space = new Obj(67, 12, new byte[] {
	0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,1,
	0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,1,
	0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,1,1,1,0,0,1,1,1,0,0,0,0,1,1,1,1,0,0,1,1,1,1,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,
	0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,1,1,1,0,0,1,1,1,0,0,0,0,1,1,1,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,
	0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,1,1,0,0,0,1,1,1,0,0,0,0,1,1,1,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,
	0,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,0,
	0,0,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,0,0,
	0,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,1,1,1,1,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,
	0,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,1,1,1,1,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,
	0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,1,1,1,1,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,
	0,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,1,0,
	1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,1,1,1,1,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,1,0,0});
	/** A j�t�k log�j�nak Impact r�sze */
    public static Obj impact = new Obj(76, 12, new byte[] {
    0,0,0,1,1,1,1,1,0,0,0,1,1,1,1,1,1,0,0,0,0,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,1,
    0,0,0,1,1,1,1,0,0,0,0,1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,1,
    0,0,0,1,1,1,1,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,0,0,0,0,0,1,1,1,1,0,1,1,1,0,0,0,0,1,1,1,1,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,
    0,0,1,1,1,1,1,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,0,0,0,0,0,1,1,1,0,0,1,1,1,0,0,0,0,1,1,1,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,
    0,0,1,1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,0,0,0,0,0,1,1,0,0,0,1,1,1,0,0,0,0,1,1,1,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,
    0,0,1,1,1,1,0,0,0,0,1,1,1,1,0,0,1,1,1,0,1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,
    0,0,1,1,1,1,0,0,0,0,1,1,1,1,0,0,1,1,0,0,1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,
    0,0,1,1,1,1,0,0,0,0,1,1,1,1,0,0,0,0,0,0,1,1,1,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,1,1,1,1,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,
    0,0,1,1,1,1,0,0,0,1,1,1,1,0,0,0,0,0,0,0,1,1,1,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,1,1,1,1,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,
    0,1,1,1,1,0,0,0,0,1,1,1,1,0,0,0,0,0,0,1,1,1,1,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,1,1,1,1,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,
    0,1,1,1,1,0,0,0,0,1,1,1,1,0,0,0,0,0,0,1,1,1,1,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,1,1,1,1,0,0,0,0,0,
    1,1,1,1,1,0,0,0,1,1,1,1,0,0,0,0,0,0,0,1,1,1,1,0,0,1,1,1,1,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,1,1,1,1,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0});
}