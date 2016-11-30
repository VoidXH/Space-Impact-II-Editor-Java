package test;

import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import org.junit.*;
import graphics.*;

/**
 * Objektumt�rol� ({@link Obj}) �s pixelfigyel� ({@link ObjPixelListener}) tesztel�se.
 * @author Sg�netz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class TestGraphics {
	/**
	 * Az {@link Obj} konstruktorainak tesztel�se.
	 */
	@Test
	public void TestConstruction() {
		Obj empty = new Obj(); // Teljesen �res objektum l�trehoz�sa
		Assert.assertEquals(0, empty.GetWidth()); // A sz�less�ge 0 kell legyen
		Assert.assertEquals(0, empty.GetHeight()); // A magass�ga is 0 kell legyen
		Obj customSize = new Obj(5, 7); // Egy�ni m�ret� �res objektum l�trehoz�sa
		Assert.assertEquals(5, customSize.GetWidth()); // A sz�less�ge 5 kell legyen
		Assert.assertEquals(7, customSize.GetHeight()); // A magass�ga 7 kell legyen
		for (int i = 0; i < 5 * 7; ++i) // Minden pixelnek...
			Assert.assertEquals(0, customSize.image[i]); // ...�resnek kell lennie
		Obj customObj = new Obj(Obj.space.GetWidth(), Obj.space.GetHeight(), Obj.space.image); // Egy gy�ri objektumra �p�tett egyedi objektum
		Assert.assertEquals(Obj.space.GetWidth(), customObj.GetWidth()); // A sz�less�geknek egyezni�k kell
		Assert.assertEquals(Obj.space.GetHeight(), customObj.GetHeight()); // A magass�goknak is
		Assert.assertEquals(Obj.space.image, customObj.image); // A t�mb referenci�val van �tadva, el�g megn�zni, hogy ott van-e
	}

	/**
	 * Objektum �r�t�s�nek tesztel�se, null�zza-e a k�pet a {@link Obj#Clear()}.
	 */
	@Test
	public void TestClearing() {
		Obj testObj = Obj.error.clone(); // Egy hardcode-olt objektum kl�noz�sa
		testObj.Clear(); // Tesztobjektum �r�t�se
		Assert.assertArrayEquals(new byte[testObj.GetWidth() * testObj.GetHeight()], testObj.image); // A pixelt�rk�p egy �res t�mb kell legyen
	}

	/**
	 * Objektum kl�noz�s�nak tesztel�se, az eredetivel megegyez� m�ret� �s tartalm� k�pet hoz-e l�tre a {@link Obj#clone()}.
	 */
	@Test
	public void TestCloning() {
		Obj testObj = Obj.space.clone(); // Egy hardcode-olt objektum kl�noz�sa
		Assert.assertNotEquals(testObj, Obj.space); // Val�di kl�nr�l van-e sz�, nem csak referenciam�sol�sr�l
		Assert.assertEquals(Obj.space.GetWidth(), testObj.GetWidth()); // Helyes sz�less�g�-e a kl�n
		Assert.assertEquals(Obj.space.GetHeight(), testObj.GetHeight()); // Helyes magass�g�-e a kl�n
		Assert.assertArrayEquals(Obj.space.image, testObj.image); // Helyesek-e a kl�n pixelei
	}

	/**
	 * Pixel l�trehoz�s�nak tesztel�se, helyes m�ret� �s sz�n� nagy�tott pixelek j�nnek-e l�tre, mint a {@link Obj#Draw()} egyik feladata.
	 */
	@Test
	public void TestPixelCreation() {
		Obj.pixelSize = 15; // Pixelm�ret be�ll�t�sa
		new Obj(1, 1).Draw(); // Kirajzol�s futtat�sa ak�rmire; pixelikonok l�trehoz�sa
		Assert.assertNotNull(Obj.ActivePixel()); // L�trej�tt-e az akt�v pixel
		Assert.assertNotNull(Obj.InactivePixel()); // L�trej�tt-e az inakt�v pixel
		Assert.assertEquals(Obj.pixelSize, Obj.ActivePixel().getIconWidth()); // Helyes sz�less�g�-e az akt�v pixel
		Assert.assertEquals(Obj.pixelSize, Obj.ActivePixel().getIconHeight()); // Helyes magass�g�-e az akt�v pixel
		Assert.assertEquals(Obj.pixelSize, Obj.InactivePixel().getIconWidth()); // Helyes sz�less�g�-e az inakt�v pixel
		Assert.assertEquals(Obj.pixelSize, Obj.InactivePixel().getIconHeight()); // Helyes magass�g�-e az inakt�v pixel
		Assert.assertEquals(Obj.activeColor.getRGB(), ((BufferedImage)Obj.ActivePixel().getImage()).getRGB(0, 0)); // Helyes sz�n� az akt�v pixel
		Assert.assertEquals(Obj.inactiveColor.getRGB(), ((BufferedImage)Obj.InactivePixel().getImage()).getRGB(0, 0)); // Helyes sz�n�-e az inakt�v pixel
	}

	/**
	 * �trajzolhat�s�g tesztel�se, a {@link Obj#Draw()} l�trehoz-e egy erre alkalmas panelt, valamint az {@link ObjPixelListener} m�k�dik-e.
	 */
	@Test
	public void TestRedrawing() {
		Obj.pixelSize = 10; // Pixelm�ret be�ll�t�sa
		Obj image = new Obj(2, 1); // �res 2x1-es rajz
		JPanel panel = image.Draw(true); // Szerkeszthet� kirajzol�s
		panel = (JPanel)panel.getComponent(0); // A k�p egy kont�neren bel�l van
		panel.getMouseListeners()[0].mousePressed(new MouseEvent(panel, 0, 0, 0, 0, 0, 0, false)); // Eg�r megnyom�sa az els� pixelen, a bal fels� sark�ban
		Assert.assertEquals(1, image.image[0]); // Kisz�nez�d�tt-e az els� pixel a kattint�sra
		Assert.assertEquals(0, image.image[1]); // Megmaradt-e �resen a m�sik pixel a kattint�s ut�n is
	}

	/**
	 * �tm�retez�s tesztel�se, a {@link Obj#SetWidth(int)} �s {@link Obj#SetHeight(int)} defin�ci�j�ban le�rt m�k�d�s zajlik-e.
	 */
	@Test
	public void TestResizing() {
		Obj testObj = Obj.impact.clone(); // Egy hardcode-olt objektum kl�noz�sa
		int originalWidth = testObj.GetWidth(); // Eredeti sz�less�g
		int originalHeight = testObj.GetHeight(); // Eredeti magass�g
		testObj.SetWidth(originalWidth / 2); // Sz�less�g megfelez�se
		testObj.SetHeight(originalHeight / 2); // Sz�less�g megfelez�se
		int cutWidth = testObj.GetWidth(); // V�gott sz�less�g
		int cutHeight = testObj.GetHeight(); // V�gott magass�g
		Assert.assertEquals(originalWidth / 2, cutWidth); // Az eredeti sz�less�g fele lett-e a sz�less�ge
		Assert.assertEquals(originalHeight / 2, cutHeight); // Az eredeti magass�g fele lett-e a magass�ga
		for (int row = 0; row < cutHeight; ++row) // A tesztobjektum magass�g�ban...
			for (int column = 0; column < cutWidth; ++ column) // ...�s sz�less�g�ben tesztelje, hogy megmaradtak-e a pixelek
				Assert.assertEquals(Obj.impact.image[row * originalWidth + column], testObj.image[row * cutWidth + column]);
		testObj.SetWidth(originalWidth); // Eredeti sz�less�g vissza�ll�t�sa
		testObj.SetHeight(originalHeight); // Eredeti magass�g vissza�ll�t�sa
		for (int row = 0; row < originalHeight; ++row) { // A tesztobjektum magass�g�ban...
			for (int column = 0; column < originalWidth; ++column) { // ...�s sz�less�g�ben tesztelje, hogy az elv�rt m�k�d�s zajlott-e
				int pixel = row * originalWidth + column; // A pixel helye a pixelt�rk�pen
				if (row < cutHeight && column < cutWidth) // A nem vagdosott r�szen az eredeti tartalom az elv�rt
					Assert.assertEquals(Obj.impact.image[pixel], testObj.image[pixel]);
				else // A v�g�s ut�n visszam�retezett r�sznek �resnek kell lennie
					Assert.assertEquals(0, testObj.image[pixel]);
			}
		}
	}

	/**
	 * V�g�s tesztel�se, a {@link Obj#Trim()} defin�ci�j�ban le�rt m�k�d�s zajlik-e.
	 */
	@Test
	public void TestTrimming() {
		Obj testObj = Obj.error.clone(); // A hibaikont vehetj�k tesztalanynak, de ak�rmi j�, aminek a fels� �s als� k�t sor�ban van pixel
		for (int i = 0; i < testObj.GetWidth(); ++i) // Els� sor ki�r�t�se
			testObj.image[i] = 0;
		for (int i = testObj.GetWidth() * (testObj.GetHeight() - 1); i < testObj.GetWidth() * testObj.GetHeight(); ++i) // Utols� sor ki�r�t�se
			testObj.image[i] = 0;
		int untrimmedHeight = testObj.GetHeight(); // V�gatlan magass�g t�rol�sa
		testObj.Trim(); // Lev�g�s
		Assert.assertEquals(untrimmedHeight - 2, testObj.GetHeight()); // Lev�g�dott-e a fels� �s als� �res sor
		testObj = new Obj(8, 8); // �j, �res objektum, amit nem b�nthat a metsz�s
		testObj.Trim(); // Tesztobjektum metsz�se
		Assert.assertEquals(8, testObj.GetHeight()); // A magass�gnak a kezdeti �rt�ken kell maradni
	}
}