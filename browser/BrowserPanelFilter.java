package browser;

import java.io.*;

/**
 * Fájlok szûrõje tallózópanelekhez.
 * @author Sgánetz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class BrowserPanelFilter implements FileFilter {
	/** A mappa tartalma (fájlok) kell-e? */
	private boolean contents;

	/**
	 * Konstruktor, a fájlszelektálási preferencia átvétele.
	 * @param contents A mappa tartalma (fájlok) kell-e
	 */
	public BrowserPanelFilter(boolean contents) {
		this.contents = contents;
	}

	/**
	 * Eldönti, hogy egy fájlrendszeri bejegyzésnek van-e helye a fájltallózóban, ami a szûrõt használja.
	 * @param file Vizsgálandó fájl
	 */
	public boolean accept(File file) {
		// return contents ^ file.isDirectory(); // Az igénytõl függõen a mappa almappáit vagy fájlait adja vissza - jó lenne, ha nem lenne +1 txt a mappákban
		if (!contents) // Ha az almappák kellenek...
			return file.isDirectory(); // ...fogadja el azokat
		if (!file.isDirectory()) // Ha fájlok kellenek, és a vizsgált elem pont az...
			return file.getName().endsWith(".dat"); // ...fogadja el, amennyiben adatfájl
		return false; // Ha se nem mappa, se nem adatfájl, vagy sehogy nem kell, ne fogadja el
	}
}