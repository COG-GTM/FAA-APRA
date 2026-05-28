/*
 * Federal Aviation Administration (FAA) public work 
 * 
 * As a work of the United States Government, this project is in the 
 * public domain within the United States. Additionally, we waive copyright 
 * and related rights in the work worldwide 
 * through the Creative Commons 0 (CC0) 1.0 Universal public domain dedication
 * 
 * APRA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package gov.faa.ait.apra.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import gov.faa.ait.apra.util.TACSpecialCase;

public class TACSpecialCaseTest {

	@Test
	public void getSpecialCasePuertoRico() {
		assertEquals(0, TACSpecialCase.getSpecialCase("Puerto Rico-VI"));
	}

	@Test
	public void getSpecialCasePuertoRicoLowerCase() {
		assertEquals(0, TACSpecialCase.getSpecialCase("puerto rico"));
	}

	@Test
	public void getSpecialCaseDenver() {
		assertEquals(1, TACSpecialCase.getSpecialCase("Denver-Colorado Springs"));
	}

	@Test
	public void getSpecialCaseDenverLowerCase() {
		assertEquals(1, TACSpecialCase.getSpecialCase("denver"));
	}

	@Test
	public void getSpecialCaseAnchorage() {
		assertEquals(3, TACSpecialCase.getSpecialCase("Anchorage-Fairbanks"));
	}

	@Test
	public void getSpecialCaseAnchorageLowerCase() {
		assertEquals(3, TACSpecialCase.getSpecialCase("anchorage"));
	}

	@Test
	public void getSpecialCaseNonSpecial() {
		assertEquals(-1, TACSpecialCase.getSpecialCase("Seattle"));
	}

	@Test
	public void getSpecialCaseNonSpecialCity() {
		assertEquals(-1, TACSpecialCase.getSpecialCase("Los Angeles"));
	}

	@Test
	public void getTACFileNameDenverPdf() {
		String fileName = TACSpecialCase.getTACFileName(1, "100", "pdf");
		assertEquals("Denver_TAC_100_P.pdf", fileName);
	}

	@Test
	public void getTACFileNameDenverTiff() {
		String fileName = TACSpecialCase.getTACFileName(1, "100", "tiff");
		assertEquals("Denver_TAC_100.zip", fileName);
	}

	@Test
	public void getTACFileNameAnchoragePdf() {
		String fileName = TACSpecialCase.getTACFileName(3, "50", "pdf");
		assertEquals("Anchorage-Fairbanks_TAC_50_P.pdf", fileName);
	}

	@Test
	public void getTACFileNameAnchorageTiff() {
		String fileName = TACSpecialCase.getTACFileName(3, "50", "tiff");
		assertEquals("Anchorage-Fairbanks_TAC_50.zip", fileName);
	}

	@Test
	public void getTACFileNamePuertoRicoPdf() {
		String fileName = TACSpecialCase.getTACFileName(0, "75", "pdf");
		assertEquals("Puerto_Rico-VI_TAC_75_P.pdf", fileName);
	}

	@Test
	public void getTACFileNamePuertoRicoTiff() {
		String fileName = TACSpecialCase.getTACFileName(0, "75", "tiff");
		assertEquals("Puerto_Rico-VI_TAC_75.zip", fileName);
	}

	@Test
	public void getTACFileNameInvalidSpecialCasePdf() {
		String fileName = TACSpecialCase.getTACFileName(99, "100", "pdf");
		assertEquals("", fileName);
	}

	@Test
	public void getTACFileNameInvalidSpecialCaseTiff() {
		String fileName = TACSpecialCase.getTACFileName(99, "100", "tiff");
		assertEquals("", fileName);
	}

	@Test
	public void getTACFileNameInvalidFormat() {
		String fileName = TACSpecialCase.getTACFileName(1, "100", "png");
		assertEquals("", fileName);
	}

	@Test
	public void getTACFileNameCaseInsensitiveFormat() {
		String fileName = TACSpecialCase.getTACFileName(1, "100", "PDF");
		assertEquals("Denver_TAC_100_P.pdf", fileName);
	}

	@Test
	public void getTACFileNameTiffUpperCase() {
		String fileName = TACSpecialCase.getTACFileName(1, "100", "TIFF");
		assertEquals("Denver_TAC_100.zip", fileName);
	}
}
