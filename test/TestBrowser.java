package test;

import java.io.*;
import java.util.*;
import org.junit.*;
import browser.*;

/**
 * Böngészõ osztályok és függvények tesztjei.
 * @author Sgánetz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class TestBrowser {
	/**
	 * Szûrés ({@link BrowserPanelFilter} osztály) tesztelése.
	 */
	@Test
	public void TestFiltering() {
		FileFilter datFilter = new BrowserPanelFilter(true); // Mappa adatfájlainak (.dat) szûrõje
		Assert.assertEquals(false, datFilter.accept(new File("."))); // Mappákat nem fogadhat el
		Assert.assertEquals(true, datFilter.accept(new File("test.dat"))); // Adatfájlokat el kell fogadnia
		Assert.assertEquals(false, datFilter.accept(new File("test.txt"))); // Más típusú fájlt viszont nem
		FileFilter folderFilter = new BrowserPanelFilter(false); // Mappák szûrõje
		Assert.assertEquals(true, folderFilter.accept(new File("."))); // Egy mappát el kell fogadnia
		Assert.assertEquals(false, folderFilter.accept(new File("test.txt"))); // Fájlokat nem fogadhat el
		Assert.assertEquals(false, folderFilter.accept(new File("test.dat"))); // Adatfájlokat sem
	}

	/**
	 * Fájlok numerikus fájlnév alapján rendezésének ({@link BrowserPanel#fixedNumericOrder}) tesztelése.
	 */
	@Test
	public void TestSorting() {
		File[] files = new File[] { new File("1.exe"), new File("100.dat"), new File("57.mkv") }; // Néhány fájl, aminek a sorrendje pont a gyári rendezés
		Arrays.sort(files, BrowserPanel.fixedNumericOrder); // Remélhetõleg a BrowserPanel komparátora ezen javít
		Assert.assertEquals("1.exe", files[0].getName()); // Az 1 nem csúszhat el
		Assert.assertEquals("57.mkv", files[1].getName()); // Az 57 viszont elõre kell kerüljön
		Assert.assertEquals("100.dat", files[2].getName()); // Akkor viszont a 100 van helyesen a végén
	}
}