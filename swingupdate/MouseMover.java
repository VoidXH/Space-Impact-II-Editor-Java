package swingupdate;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

/**
 * Komponens mozgat�sa eg�rrel.
 * @author Sg�netz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class MouseMover extends MouseInputAdapter {
	/** Ugr�sm�rt�k. A mozgott pixelek sz�ma mindig ennyivel oszthat�. */
	private int snap = 1;
	/** A mozg�s kezd�pontja. */
	private Point start;
	/** A komponens jelenlegi helye. */
	private Point location;

	/**
	 * Komponensmozgat� l�trehoz�sa egy pixeles ugr�sm�rt�kkel.
	 */
	public MouseMover() {
		snap = 1;
	}

	/**
	 * Komponensmozgat� l�trehoz�sa egy�ni ugr�sm�rt�kkel.
	 * @param snap Ugr�s m�rt�ke
	 */
	public MouseMover(int snap) {
		this.snap = Math.max(snap, 1); // Minimum 1 legyen, <= 0 pixelt nem lehet mozogni
	}

	public void mousePressed(MouseEvent e) {
		start = e.getPoint(); // Kezd�pont t�rol�sa az eg�r lenyom�sakor
	}

	/**
	 * Elmozdul�s korrig�l�sa, hogy �rv�nyes�lj�n az ugr�sm�rt�k.
	 * @param value Eredeti elmozdul�s
	 * @return Korrig�lt elmozdul�s
	 */
	private int snap(int value) {
		return (value / snap) * snap;
	}

	public void mouseDragged(MouseEvent e) {
		Component component = e.getComponent(); // Komponens t�rol�sa
		location = component.getLocation(location); // Komponens poz�ci�ja
		component.setLocation(location.x + snap(e.getX() - start.x), location.y + snap(e.getY() - start.y)); // Elmozdul�s �rv�nyes�t�se a komponensen
	}

	/**
	 * Komponens mozgat�s�nak enged�lyez�se.
	 * @param component C�lkomponens
	 */
	public static void enableFor(Component component) {
		enableFor(component, 1); // A b�vebb megval�s�t�s megh�v�sa 1-es ugr�shosszal
	}

	/**
	 * Komponens mozgat�s�nak enged�lyez�se egy�ni ugr�sk�zzel.
	 * @param component C�lkomponens
	 * @param snap Ugr�sk�z
	 */
	public static void enableFor(Component component, int snap) {
		MouseMover mover = new MouseMover(snap); // �j mozgat� l�trehoz�sa
		component.addMouseListener(mover); // Kattint�s figyel�se
		component.addMouseMotionListener(mover); // Eg�rrel h�z�s figyel�se
	}
}