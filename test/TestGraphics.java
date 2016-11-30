package test;

import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import org.junit.*;
import graphics.*;

/**
 * Objektumtároló ({@link Obj}) és pixelfigyelõ ({@link ObjPixelListener}) tesztelése.
 * @author Sgánetz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class TestGraphics {
	/**
	 * Az {@link Obj} konstruktorainak tesztelése.
	 */
	@Test
	public void TestConstruction() {
		Obj empty = new Obj(); // Teljesen üres objektum létrehozása
		Assert.assertEquals(0, empty.GetWidth()); // A szélessége 0 kell legyen
		Assert.assertEquals(0, empty.GetHeight()); // A magassága is 0 kell legyen
		Obj customSize = new Obj(5, 7); // Egyéni méretû üres objektum létrehozása
		Assert.assertEquals(5, customSize.GetWidth()); // A szélessége 5 kell legyen
		Assert.assertEquals(7, customSize.GetHeight()); // A magassága 7 kell legyen
		for (int i = 0; i < 5 * 7; ++i) // Minden pixelnek...
			Assert.assertEquals(0, customSize.image[i]); // ...üresnek kell lennie
		Obj customObj = new Obj(Obj.space.GetWidth(), Obj.space.GetHeight(), Obj.space.image); // Egy gyári objektumra épített egyedi objektum
		Assert.assertEquals(Obj.space.GetWidth(), customObj.GetWidth()); // A szélességeknek egyezniük kell
		Assert.assertEquals(Obj.space.GetHeight(), customObj.GetHeight()); // A magasságoknak is
		Assert.assertEquals(Obj.space.image, customObj.image); // A tömb referenciával van átadva, elég megnézni, hogy ott van-e
	}

	/**
	 * Objektum ürítésének tesztelése, nullázza-e a képet a {@link Obj#Clear()}.
	 */
	@Test
	public void TestClearing() {
		Obj testObj = Obj.error.clone(); // Egy hardcode-olt objektum klónozása
		testObj.Clear(); // Tesztobjektum ürítése
		Assert.assertArrayEquals(new byte[testObj.GetWidth() * testObj.GetHeight()], testObj.image); // A pixeltérkép egy üres tömb kell legyen
	}

	/**
	 * Objektum klónozásának tesztelése, az eredetivel megegyezõ méretû és tartalmú képet hoz-e létre a {@link Obj#clone()}.
	 */
	@Test
	public void TestCloning() {
		Obj testObj = Obj.space.clone(); // Egy hardcode-olt objektum klónozása
		Assert.assertNotEquals(testObj, Obj.space); // Valódi klónról van-e szó, nem csak referenciamásolásról
		Assert.assertEquals(Obj.space.GetWidth(), testObj.GetWidth()); // Helyes szélességû-e a klón
		Assert.assertEquals(Obj.space.GetHeight(), testObj.GetHeight()); // Helyes magasságú-e a klón
		Assert.assertArrayEquals(Obj.space.image, testObj.image); // Helyesek-e a klón pixelei
	}

	/**
	 * Pixel létrehozásának tesztelése, helyes méretû és színû nagyított pixelek jönnek-e létre, mint a {@link Obj#Draw()} egyik feladata.
	 */
	@Test
	public void TestPixelCreation() {
		Obj.pixelSize = 15; // Pixelméret beállítása
		new Obj(1, 1).Draw(); // Kirajzolás futtatása akármire; pixelikonok létrehozása
		Assert.assertNotNull(Obj.ActivePixel()); // Létrejött-e az aktív pixel
		Assert.assertNotNull(Obj.InactivePixel()); // Létrejött-e az inaktív pixel
		Assert.assertEquals(Obj.pixelSize, Obj.ActivePixel().getIconWidth()); // Helyes szélességû-e az aktív pixel
		Assert.assertEquals(Obj.pixelSize, Obj.ActivePixel().getIconHeight()); // Helyes magasságú-e az aktív pixel
		Assert.assertEquals(Obj.pixelSize, Obj.InactivePixel().getIconWidth()); // Helyes szélességû-e az inaktív pixel
		Assert.assertEquals(Obj.pixelSize, Obj.InactivePixel().getIconHeight()); // Helyes magasságú-e az inaktív pixel
		Assert.assertEquals(Obj.activeColor.getRGB(), ((BufferedImage)Obj.ActivePixel().getImage()).getRGB(0, 0)); // Helyes színû az aktív pixel
		Assert.assertEquals(Obj.inactiveColor.getRGB(), ((BufferedImage)Obj.InactivePixel().getImage()).getRGB(0, 0)); // Helyes színû-e az inaktív pixel
	}

	/**
	 * Átrajzolhatóság tesztelése, a {@link Obj#Draw()} létrehoz-e egy erre alkalmas panelt, valamint az {@link ObjPixelListener} mûködik-e.
	 */
	@Test
	public void TestRedrawing() {
		Obj.pixelSize = 10; // Pixelméret beállítása
		Obj image = new Obj(2, 1); // Üres 2x1-es rajz
		JPanel panel = image.Draw(true); // Szerkeszthetõ kirajzolás
		panel = (JPanel)panel.getComponent(0); // A kép egy konténeren belül van
		panel.getMouseListeners()[0].mousePressed(new MouseEvent(panel, 0, 0, 0, 0, 0, 0, false)); // Egér megnyomása az elsõ pixelen, a bal felsõ sarkában
		Assert.assertEquals(1, image.image[0]); // Kiszínezõdött-e az elsõ pixel a kattintásra
		Assert.assertEquals(0, image.image[1]); // Megmaradt-e üresen a másik pixel a kattintás után is
	}

	/**
	 * Átméretezés tesztelése, a {@link Obj#SetWidth(int)} és {@link Obj#SetHeight(int)} definíciójában leírt mûködés zajlik-e.
	 */
	@Test
	public void TestResizing() {
		Obj testObj = Obj.impact.clone(); // Egy hardcode-olt objektum klónozása
		int originalWidth = testObj.GetWidth(); // Eredeti szélesség
		int originalHeight = testObj.GetHeight(); // Eredeti magasság
		testObj.SetWidth(originalWidth / 2); // Szélesség megfelezése
		testObj.SetHeight(originalHeight / 2); // Szélesség megfelezése
		int cutWidth = testObj.GetWidth(); // Vágott szélesség
		int cutHeight = testObj.GetHeight(); // Vágott magasság
		Assert.assertEquals(originalWidth / 2, cutWidth); // Az eredeti szélesség fele lett-e a szélessége
		Assert.assertEquals(originalHeight / 2, cutHeight); // Az eredeti magasság fele lett-e a magassága
		for (int row = 0; row < cutHeight; ++row) // A tesztobjektum magasságában...
			for (int column = 0; column < cutWidth; ++ column) // ...és szélességében tesztelje, hogy megmaradtak-e a pixelek
				Assert.assertEquals(Obj.impact.image[row * originalWidth + column], testObj.image[row * cutWidth + column]);
		testObj.SetWidth(originalWidth); // Eredeti szélesség visszaállítása
		testObj.SetHeight(originalHeight); // Eredeti magasság visszaállítása
		for (int row = 0; row < originalHeight; ++row) { // A tesztobjektum magasságában...
			for (int column = 0; column < originalWidth; ++column) { // ...és szélességében tesztelje, hogy az elvárt mûködés zajlott-e
				int pixel = row * originalWidth + column; // A pixel helye a pixeltérképen
				if (row < cutHeight && column < cutWidth) // A nem vagdosott részen az eredeti tartalom az elvárt
					Assert.assertEquals(Obj.impact.image[pixel], testObj.image[pixel]);
				else // A vágás után visszaméretezett résznek üresnek kell lennie
					Assert.assertEquals(0, testObj.image[pixel]);
			}
		}
	}

	/**
	 * Vágás tesztelése, a {@link Obj#Trim()} definíciójában leírt mûködés zajlik-e.
	 */
	@Test
	public void TestTrimming() {
		Obj testObj = Obj.error.clone(); // A hibaikont vehetjük tesztalanynak, de akármi jó, aminek a felsõ és alsó két sorában van pixel
		for (int i = 0; i < testObj.GetWidth(); ++i) // Elsõ sor kiürítése
			testObj.image[i] = 0;
		for (int i = testObj.GetWidth() * (testObj.GetHeight() - 1); i < testObj.GetWidth() * testObj.GetHeight(); ++i) // Utolsó sor kiürítése
			testObj.image[i] = 0;
		int untrimmedHeight = testObj.GetHeight(); // Vágatlan magasság tárolása
		testObj.Trim(); // Levágás
		Assert.assertEquals(untrimmedHeight - 2, testObj.GetHeight()); // Levágódott-e a felsõ és alsó üres sor
		testObj = new Obj(8, 8); // Új, üres objektum, amit nem bánthat a metszés
		testObj.Trim(); // Tesztobjektum metszése
		Assert.assertEquals(8, testObj.GetHeight()); // A magasságnak a kezdeti értéken kell maradni
	}
}