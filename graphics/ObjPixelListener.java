package graphics;

import java.awt.event.*;
import javax.swing.*;

/**
 * Egy szerkeszthetõ kirajzolt kép paneljét figyeli, hogy kattintással módosíthatóak legyenek a pixelei.
 * @author Sgánetz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class ObjPixelListener implements MouseListener {
	/** Referencia a kirajzolt képre. */
	private Obj obj;
	/** A pixeleket kirajzoló ikonnal ellátott JLabel-ek */
	private JLabel[] pixels;

	/**
	 * A kép referenciáját és a pixelek JLabel-jeit konstruktorból kapja meg
	 * @param obj Referencia a kirajzolt képre
	 * @param pixels A pixeleket kirajzoló ikonnal ellátott JLabel-ek
	 */
	public ObjPixelListener(Obj obj, JLabel[] pixels) {
		this.obj = obj;
		this.pixels = pixels;
	}

	// A MouseListener kihasználatlan függvényei
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

	/**
	 * Az egérgomb panel fölött lenyomásakor hívódik meg, és az egér alatti pixelt invertálja.
	 * @param e Kattintási esemény
	 */
	public void mousePressed(MouseEvent e) {
		int x = e.getPoint().x / Obj.pixelSize, y = e.getPoint().y / Obj.pixelSize; // A kattintás helye a nagyított képre vetítve
		int pixel = y * obj.GetWidth() + x; // A kattintott pixel helye a pixeltérképen
		obj.image[pixel] = (byte)(1 - obj.image[pixel]); // A kattintott pixel invertálása
		pixels[pixel].setIcon(obj.image[pixel] == 0 ? Obj.InactivePixel() : Obj.ActivePixel()); // A pixelt reprezentáló JLabel ikonjának módosítása
	}
}