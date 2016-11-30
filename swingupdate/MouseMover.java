package swingupdate;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

/**
 * Komponens mozgatása egérrel.
 * @author Sgánetz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class MouseMover extends MouseInputAdapter {
	/** Ugrásmérték. A mozgott pixelek száma mindig ennyivel osztható. */
	private int snap = 1;
	/** A mozgás kezdõpontja. */
	private Point start;
	/** A komponens jelenlegi helye. */
	private Point location;

	/**
	 * Komponensmozgató létrehozása egy pixeles ugrásmértékkel.
	 */
	public MouseMover() {
		snap = 1;
	}

	/**
	 * Komponensmozgató létrehozása egyéni ugrásmértékkel.
	 * @param snap Ugrás mértéke
	 */
	public MouseMover(int snap) {
		this.snap = Math.max(snap, 1); // Minimum 1 legyen, <= 0 pixelt nem lehet mozogni
	}

	public void mousePressed(MouseEvent e) {
		start = e.getPoint(); // Kezdõpont tárolása az egér lenyomásakor
	}

	/**
	 * Elmozdulás korrigálása, hogy érvényesüljön az ugrásmérték.
	 * @param value Eredeti elmozdulás
	 * @return Korrigált elmozdulás
	 */
	private int snap(int value) {
		return (value / snap) * snap;
	}

	public void mouseDragged(MouseEvent e) {
		Component component = e.getComponent(); // Komponens tárolása
		location = component.getLocation(location); // Komponens pozíciója
		component.setLocation(location.x + snap(e.getX() - start.x), location.y + snap(e.getY() - start.y)); // Elmozdulás érvényesítése a komponensen
	}

	/**
	 * Komponens mozgatásának engedélyezése.
	 * @param component Célkomponens
	 */
	public static void enableFor(Component component) {
		enableFor(component, 1); // A bõvebb megvalósítás meghívása 1-es ugráshosszal
	}

	/**
	 * Komponens mozgatásának engedélyezése egyéni ugrásközzel.
	 * @param component Célkomponens
	 * @param snap Ugrásköz
	 */
	public static void enableFor(Component component, int snap) {
		MouseMover mover = new MouseMover(snap); // Új mozgató létrehozása
		component.addMouseListener(mover); // Kattintás figyelése
		component.addMouseMotionListener(mover); // Egérrel húzás figyelése
	}
}