package browser;

import java.io.*;

/**
 * F�jlok sz�r�je tall�z�panelekhez.
 * @author Sg�netz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class BrowserPanelFilter implements FileFilter {
	/** A mappa tartalma (f�jlok) kell-e? */
	private boolean contents;

	/**
	 * Konstruktor, a f�jlszelekt�l�si preferencia �tv�tele.
	 * @param contents A mappa tartalma (f�jlok) kell-e
	 */
	public BrowserPanelFilter(boolean contents) {
		this.contents = contents;
	}

	/**
	 * Eld�nti, hogy egy f�jlrendszeri bejegyz�snek van-e helye a f�jltall�z�ban, ami a sz�r�t haszn�lja.
	 * @param file Vizsg�land� f�jl
	 */
	public boolean accept(File file) {
		// return contents ^ file.isDirectory(); // Az ig�nyt�l f�gg�en a mappa almapp�it vagy f�jlait adja vissza - j� lenne, ha nem lenne +1 txt a mapp�kban
		if (!contents) // Ha az almapp�k kellenek...
			return file.isDirectory(); // ...fogadja el azokat
		if (!file.isDirectory()) // Ha f�jlok kellenek, �s a vizsg�lt elem pont az...
			return file.getName().endsWith(".dat"); // ...fogadja el, amennyiben adatf�jl
		return false; // Ha se nem mappa, se nem adatf�jl, vagy sehogy nem kell, ne fogadja el
	}
}