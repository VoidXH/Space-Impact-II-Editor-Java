package graphics;

import java.awt.event.*;
import javax.swing.*;

/**
 * Egy szerkeszthet� kirajzolt k�p panelj�t figyeli, hogy kattint�ssal m�dos�that�ak legyenek a pixelei.
 * @author Sg�netz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class ObjPixelListener implements MouseListener {
	/** Referencia a kirajzolt k�pre. */
	private Obj obj;
	/** A pixeleket kirajzol� ikonnal ell�tott JLabel-ek */
	private JLabel[] pixels;

	/**
	 * A k�p referenci�j�t �s a pixelek JLabel-jeit konstruktorb�l kapja meg
	 * @param obj Referencia a kirajzolt k�pre
	 * @param pixels A pixeleket kirajzol� ikonnal ell�tott JLabel-ek
	 */
	public ObjPixelListener(Obj obj, JLabel[] pixels) {
		this.obj = obj;
		this.pixels = pixels;
	}

	// A MouseListener kihaszn�latlan f�ggv�nyei
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

	/**
	 * Az eg�rgomb panel f�l�tt lenyom�sakor h�v�dik meg, �s az eg�r alatti pixelt invert�lja.
	 * @param e Kattint�si esem�ny
	 */
	public void mousePressed(MouseEvent e) {
		int x = e.getPoint().x / Obj.pixelSize, y = e.getPoint().y / Obj.pixelSize; // A kattint�s helye a nagy�tott k�pre vet�tve
		int pixel = y * obj.GetWidth() + x; // A kattintott pixel helye a pixelt�rk�pen
		obj.image[pixel] = (byte)(1 - obj.image[pixel]); // A kattintott pixel invert�l�sa
		pixels[pixel].setIcon(obj.image[pixel] == 0 ? Obj.InactivePixel() : Obj.ActivePixel()); // A pixelt reprezent�l� JLabel ikonj�nak m�dos�t�sa
	}
}