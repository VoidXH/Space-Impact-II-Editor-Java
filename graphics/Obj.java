package graphics;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
import swingupdate.*;

/**
 * Egy kétdimenziós grafikus objektum (népiesen: kép) tárolója. A tárolási módszer leírásáért lásd: {@link #image}.
 * @author Sgánetz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class Obj {
	/** Aktív pixelek színe. */
	public static final Color activeColor = new Color(178, 189, 8);
	/** Inaktív pixelek színe. */
	public static final Color inactiveColor = new Color(0, 0, 0);
	/** Kirajzolási szorzó, a kép pixelei ennyiszer lesznek nagyobbak széltükben és magasságukban. */
	public static int pixelSize = 12;

	/** A kép szélessége. */
	private int x;
	/** A kép magassága. */
	private int y;
	/**
	 * Az objektum pixelei sorfolytonosan. 0 vagy 1 lehet az értékük. 
	 * Azért bájtok tömbje, hogy a tömörített objektumokat helyben ki lehessen bontani.
	 */
	public byte[] image;

	/**
	 * Teljesen üres kép konstruktora.
	 */
	public Obj() {
		this.x = this.y = 0;
	}

	/**
	 * Adott méretû üres kép konstruktora.
	 * @param x Szélesség
	 * @param y Magasság
	 */
	public Obj(int x, int y) {
		this.x = x;
		this.y = y;
		this.image = new byte[x * y];
	}

	/**
	 * Generált adatokból képet létrehozó konstruktor.
	 * @param x Szélesség
	 * @param y Magasság
	 * @param image Pixeltérkép
	 */
	public Obj(int x, int y, byte[] image) {
		this.x = x;
		this.y = y;
		this.image = image;
	}

	/**
	 * Lemásol egy képet.
	 * @return Másolat
	 */
	public Obj clone() {
		return new Obj(x, y, image.clone()); // Elég a konstruktorhívás, de ne ugyanaz legyen a tömb, mert akkor mindkét példány módosulna
	}

    /**
     * Egy objektum beolvasása fájlból.
     * @param path Elérési útvonal
     * @return Beolvasott objektum
     */
	public static Obj Load(String path) {
		byte[] data; // A fájlban tárolt bájtok
		try {
			data = Files.readAllBytes(new File(path).toPath()); // Fájl bájtjainak beolvasása
		} catch (IOException e) { // Hiba esetén...
			return error.clone(); // ...visszatérés a hibát jelzõ objektum másolatával
		}
		Obj loaded = new Obj(data[0], data[1]); // Tároló létrehozása; az elsõ két bájtban van a méret
		int pixels = data[0] * data[1]; // Pixelek száma
		int bytes = pixels / 8 + (pixels % 8 != 0 ? 1 : 0); // Tömörített tömb mérete
		int bits = pixels % 8; // A vizsgált bájtból hátralévõ bitek
		if (bits == 0) // Ha a pixeltérkép pontosan kitölti a bájtokat...
			bits = 8; // ...az egész vizsgált bájt hátra van
		while (bytes-- != 0) { // Amíg van tömörített adat
			while (bits-- != 0) { // Amíg a vizsgált bájtban van feldolgozatlan pixel
				loaded.image[bytes * 8 + bits] = (byte)(data[bytes + 2] % 2 == 0 ? 0 : 1); // Pixel elhelyezése a helyén + unsigned fix (elõjeles a byte)
				data[bytes + 2] >>= 1; // Ugrás a következõ bitre a vizsgált bájton
			}
			bits = 8; // A következõ bájt feldolgozandó pixelei
		}
		return loaded; // Adja vissza a sikeresen betöltött objektumot
	}

	/**
	 * Fájlba menti az adott objektumot.
	 * @param path Elérési útvonal
	 * @return A mentés sikeressége
	 */
	public boolean Save(String path) {
		int pixels = x * y; // Pixelek száma
		int FileLength = pixels / 8 + (pixels % 8 != 0 ? 3 : 2); // Tömörített pixelek száma + bájtok a méretnek
		byte[] bytes = new byte[FileLength]; // Fájlba írandó tartalom helye
		bytes[0] = (byte)x; // 0. bájt: szélesség
		bytes[1] = (byte)y; // 1. bájt: magasság
		int writtenByte = 1; // Jelenleg írás alatt lévõ bájt (a következõ ciklus elsõ lépésként 2-re löki)
		for (int pixel = 0; pixel < pixels; ++pixel) { // Minden pixel tömörítése
			if (pixel % 8 == 0) // Ha 8 pixellel végzett...
				++writtenByte; // ...nyisson új bájtot
			bytes[writtenByte] <<= 1; // Hely szorítása a jelenlegi bájtban a következõ pixelnek
			if (image[pixel] == 1) // Ha aktív a vizsgált pixel...
				++bytes[writtenByte]; // ...a jelenlegi bájtba írja bele
		}
		try {
			FileOutputStream fos = new FileOutputStream(path); // Írandó fájl megnyitása, szándékosan ilyen változónévvel
			fos.write(bytes); // Bájttömb kiírása
			fos.close(); // Fájl bezárása
			return true; // A fájlba írás sikeresen megtörtént, jelezze
		} catch (Exception e) { // Hiba esetén...
			return false; // ...jelezze vissza a hívónak, hogy sikertelen a mentés
		}
	}

	/**
	 * Getter a szélességre.
	 * @return Szélesség
	 */
	public int GetWidth() {
		return x;
	}

	/**
	 * Getter a magasságra.
	 * @return Magasság
	 */
	public int GetHeight() {
		return y;
	}

	/**
	 * Átméretezés széltében. Az újonnan szerzett szélességi pixelek üresek lesznek. Ha valahol ki lett rajzolva a kép, ajánlott újrarajzolni.
	 * @param value Új szélesség
	 */
	public void SetWidth(int value) {
		int oldX = x; // Elõzõ szélesség tárolása
		x = value; // Új szélesség alkalmazása
		int oldPixel = 0; // Adott pixel régi helye
		int pixel = 0; // Adott pixel jelenlegi helye
		int widthDiff = oldX - x; // Szélességkülönbség
		byte[] oldImage = image; // Régi pixeltérkép
		image = new byte[x * y]; // Új, üres pixeltérkép
		for (int row = 0; row < y; ++row) { // A jelnlegi méret sorain...
			for (int column = 0; column < x; ++column) { // ...majd oszlopain (tehát a pixelek sorrendjében) haladva...
				image[pixel++] = column >= oldX ? (byte)0 :
					oldImage[oldPixel]; // ...az odaillõ pixel beillesztése, ami a régi szélességen túl biztos nem volt aktív
				++oldPixel; // A régi pixeltömb következõ elemére ugrás
			}
			oldPixel += widthDiff; // A régi pixeltömbben is a következõ sor elejére kerüljön a vizsgálat
		}
	}

	/**
	 * Átméretezés magasságában. Az újonnan szerzett magassági pixelek üresek lesznek. Ha valahol ki lett rajzolva a kép, ajánlott újrarajzolni.
	 * @param value Új magasság
	 */
	public void SetHeight(int value) {
		y = value; // Új szélesség alkalmazása
		image = Arrays.copyOf(image, x * y); // Pixeltérkép átméretezése, vagyis egy másolat a tömbrõl a megtartandó pixelekkel
	}

	/**
	 * A kép ürítése. Ha valahol ki lett rajzolva a kép, ajánlott újrarajzolni.
	 */
	public void Clear() {
		int pixels = x * y; // Pixelek száma
		for (int i = 0; i < pixels; ++i) // Végighaladva a pixeleken...
			image[i] = 0; // ...kinullázza azokat
	}

	/**
	 * Üres sorok és oszlopok levágása a szélekrõl. Üres objektumot nem metsz. Ha valahol ki lett rajzolva a kép, ajánlott újrarajzolni.
	 */
	public void Trim() {
		int cropTop = 48, cropLeft = 84, cropRight = 0, cropBottom = 0; // Levágási határok
		int pixels = x * y; // Pixelek száma
		boolean work = false; // Legyen-e vágás
		for (int pixel = 0; pixel < pixels; ++pixel) { // Minden pixelt ellenõrizzen
			if (image[pixel] == 1) { // Ha egy pixel aktív
				int row = pixel / x, column = pixel % x; // Sor és oszlop meghatározása
				if (cropTop > row) // Ha feljebb van, mint eddig bármi...
					cropTop = row; // ...terjessze ki addig a levágást
				if (cropLeft > column) // Ha balrább van, mint eddig bármi...
					cropLeft = column; // ...terjessze ki addig a levágást
				if (cropRight < column) // Ha jobbrább van, mint eddig bármi...
					cropRight = column; // ...terjessze ki addig a levágást
				if (cropBottom < row) // Ha lejjebb van, mint eddig bármi...
					cropBottom = row; // ...terjessze ki addig a levágást
				work = true; // Legyen vágás
			}
		}
		if (work) { // Ha van aktív pixel
			byte[] newMap = new byte[84 * 48]; // Új pixeltérkép
			int newWidth = cropRight - cropLeft + 1, newHeight = cropBottom - cropTop + 1; // Új szélesség és magasság
			int newPixel = 0; // Adott pixel új helye
			for (int row = 0; row < newHeight; ++row) // Elõbb soronként...
				for (int Column = 0; Column < newWidth; ++Column) // ...majd oszloponként bejárás (ez a bájtsorrend)
					newMap[newPixel++] = image[(cropTop + row) * x + cropLeft + Column]; // Pixelek új helyükre mozgatása
			x = newWidth; // Szélesség frissítése
			y = newHeight; // Magasság frissítése
			image = newMap; // Pixeltérkép frissítése
		}
	}

	/** Rajzolandó aktív pixel, JLabel ikonnak */
	private static ImageIcon activePixel = null;
	/** Rajzolandó inaktív pixel, JLabel ikonnak */
	private static ImageIcon inactivePixel = null;

	/**
	 * Getter az aktív pixelre.
	 * @return Aktív pixel
	 */
	public static ImageIcon ActivePixel() {
		return activePixel;
	}

	/**
	 * Getter az inaktív pixelre.
	 * @return Inaktív pixel
	 */
	public static ImageIcon InactivePixel() {
		return inactivePixel;
	}

	/**
	 * Gyári paraméteres verzió a {@link #Draw(boolean)}-hoz, ahol a szerkesztés lehetõsége hamis
	 * @return Kirajzolt panel
	 */
	public JPanel Draw() {
		return Draw(false); // Ha nincs megadva szerkeszthetõség, legyen kikapcsolva
	}

	/**
	 * Az objektum kirajzolása.
	 * @param canEdit Szerkeszthetõ legyen-e a pixelekre kattintással
	 * @return Kirajzolt panel
	 */
	public JPanel Draw(boolean canEdit) {
		if (activePixel == null || activePixel.getIconWidth() != pixelSize ||
			inactivePixel == null || inactivePixel.getIconWidth() != pixelSize) { // Ha nem készültek el a pixelek, vagy rossz a méretük
			int activeRGB = activeColor.getRGB(), inactiveRGB = inactiveColor.getRGB(); // ARGB int-ként a színek
			BufferedImage
				active = new BufferedImage(pixelSize, pixelSize, BufferedImage.TYPE_INT_ARGB), // Ebbe rajzolódik az aktív pixel
				inactive = new BufferedImage(pixelSize, pixelSize, BufferedImage.TYPE_INT_ARGB); // Ebbe rajzolódik az inaktív pixel
			for (int i = 0; i < pixelSize; ++i) { // A két kép minden pixele a megfelelõ színû legyen
				for (int j = 0; j < pixelSize; ++j) {
					active.setRGB(i, j, activeRGB);
					inactive.setRGB(i, j, inactiveRGB);
				}
			}
			activePixel = new ImageIcon(active); // Aktív pixel ikonná alakítása
			inactivePixel = new ImageIcon(inactive); // Inaktív pixel ikonná alakítása
		}
		JPanel container = new JPanel(); // Kell egy külsõ panel (konténer), hogy a rajzot ne lehessen helyszûkében kisebbre méretezni
		((FlowLayout)container.getLayout()).setHgap(0); // Vízszintes rések nullázása (akár margó is lehet)
		((FlowLayout)container.getLayout()).setVgap(0); // Függõleges rések nullázása (akár margó is lehet)
		JPanel drawn = new JPanel(); // A belsõ panelre kerülnek fel a pixelek, címkeikonként, egyesével
		drawn.setLayout(new GridLayout(0, x)); // A szélességnek megfelelõ oszlopból álló rács legyen az alapja
		int pixels = x * y; // A kép pixelszáma
		JLabel[] drawnPixels = new JLabel[pixels]; // Minden pixel egy JLabel, aminek az ikonképe az adott pixel
		for (int i = 0; i < pixels; ++i)
			drawnPixels[i] = (JLabel)drawn.add(new JLabel(image[i] == 0 ? inactivePixel : activePixel));
		if (canEdit) // Ha szerkeszthetõ...
			drawn.addMouseListener(new ObjPixelListener(this, drawnPixels)); // ...az ezt kezelõ Listener-t rakja a pixeleket tartalmazó panelre
		SwingHelpers.ForceSize(drawn, x * pixelSize, y * pixelSize); // A rajz mérete erõltetetten be legyen tartva, az átméretezéstõl nem jó a szerkesztés
		SwingHelpers.ForceSize(container, x * pixelSize, y * pixelSize); // A konténer is a maga útját járná a méretezéssel, ha nincs erõltetve
		container.add(drawn); // Csomagolja a konténerbe a képet
		return container; // Adja vissza a konténert
	}

	/** Betöltési hibát jelzõ kép */
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
	/** A játék logójának Space része */
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
	/** A játék logójának Impact része */
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