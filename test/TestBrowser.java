package test;

import java.io.*;
import java.util.*;
import org.junit.*;
import browser.*;

/**
 * B�ng�sz� oszt�lyok �s f�ggv�nyek tesztjei.
 * @author Sg�netz Bence
 * @version 1.0J
 * @since 1.0J
 */
public class TestBrowser {
	/**
	 * Sz�r�s ({@link BrowserPanelFilter} oszt�ly) tesztel�se.
	 */
	@Test
	public void TestFiltering() {
		FileFilter datFilter = new BrowserPanelFilter(true); // Mappa adatf�jlainak (.dat) sz�r�je
		Assert.assertEquals(false, datFilter.accept(new File("."))); // Mapp�kat nem fogadhat el
		Assert.assertEquals(true, datFilter.accept(new File("test.dat"))); // Adatf�jlokat el kell fogadnia
		Assert.assertEquals(false, datFilter.accept(new File("test.txt"))); // M�s t�pus� f�jlt viszont nem
		FileFilter folderFilter = new BrowserPanelFilter(false); // Mapp�k sz�r�je
		Assert.assertEquals(true, folderFilter.accept(new File("."))); // Egy mapp�t el kell fogadnia
		Assert.assertEquals(false, folderFilter.accept(new File("test.txt"))); // F�jlokat nem fogadhat el
		Assert.assertEquals(false, folderFilter.accept(new File("test.dat"))); // Adatf�jlokat sem
	}

	/**
	 * F�jlok numerikus f�jln�v alapj�n rendez�s�nek ({@link BrowserPanel#fixedNumericOrder}) tesztel�se.
	 */
	@Test
	public void TestSorting() {
		File[] files = new File[] { new File("1.exe"), new File("100.dat"), new File("57.mkv") }; // N�h�ny f�jl, aminek a sorrendje pont a gy�ri rendez�s
		Arrays.sort(files, BrowserPanel.fixedNumericOrder); // Rem�lhet�leg a BrowserPanel kompar�tora ezen jav�t
		Assert.assertEquals("1.exe", files[0].getName()); // Az 1 nem cs�szhat el
		Assert.assertEquals("57.mkv", files[1].getName()); // Az 57 viszont el�re kell ker�lj�n
		Assert.assertEquals("100.dat", files[2].getName()); // Akkor viszont a 100 van helyesen a v�g�n
	}
}