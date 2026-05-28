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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import gov.faa.ait.apra.bootstrap.Config;

public class ConfigTest {

	@Test
	public void getAeronavHostDefault() {
		String host = Config.getAeronavHost();
		assertNotNull(host);
		assertTrue(host.contains("aeronav"));
	}

	@Test
	public void getDDOFHostDefault() {
		String host = Config.getDDOFHost();
		assertNotNull(host);
		assertTrue(host.contains("tod"));
	}

	@Test
	public void getNFDCHostDefault() {
		String host = Config.getNFDCHost();
		assertNotNull(host);
		assertTrue(host.contains("nfdc"));
	}

	@Test
	public void getAeronavSectionalFolderDefault() {
		String folder = Config.getAeronavSectionalFolder();
		assertNotNull(folder);
		assertTrue(folder.contains("sectional"));
	}

	@Test
	public void getNfdcNasrPathDefault() {
		String path = Config.getNfdcNasrPath();
		assertNotNull(path);
		assertTrue(path.contains("DaySub"));
	}

	@Test
	public void getWallplanUploadFolderDefault() {
		String folder = Config.getWallplanUploadFolder();
		assertNotNull(folder);
	}

	@Test
	public void getDenodoHostDefault() {
		String host = Config.getDenodoHost();
		assertNotNull(host);
	}

	@Test
	public void getDenodoViewPathDefault() {
		String path = Config.getDenodoViewPath();
		assertNotNull(path);
		assertTrue(path.contains("views"));
	}

	@Test
	public void getDenodoCycleResourceDefault() {
		String resource = Config.getDenodoCycleResource();
		assertNotNull(resource);
		assertTrue(resource.contains("chart_cycle"));
	}

	@Test
	public void getDenodoVFRCycleResourceDefault() {
		String resource = Config.getDenodoVFRCycleResource();
		assertNotNull(resource);
		assertTrue(resource.contains("vfr"));
	}

	@Test
	public void getFAADMZProxyPortDefault() {
		String port = Config.getFAADMZProxyPort();
		assertEquals("8080", port);
	}

	@Test
	public void getCycleAgeLimitDefault() {
		int limit = Config.getCycleAgeLimit();
		assertEquals(1, limit);
	}

	@Test
	public void getTPPCheckFlagDefault() {
		assertFalse(Config.getTPPCheckFlag());
	}

	@Test
	public void getSectionalCheckFlagDefault() {
		assertFalse(Config.getSectioanlCheckFlag());
	}

	@Test
	public void getNASRDateFormatDefault() {
		String format = Config.getNASRDateFormat();
		assertNotNull(format);
		assertEquals("yyyy-MM-dd", format);
	}

	@Test
	public void getNASRFilePrefixDefault() {
		String prefix = Config.getNASRFilePrefix();
		assertNotNull(prefix);
		assertTrue(prefix.contains("DaySubscription"));
	}

	@Test
	public void getCIFPPathDefault() {
		String path = Config.getCIFPPath();
		assertNotNull(path);
		assertTrue(path.contains("cifp"));
	}

	@Test
	public void getCIFPFilePrefixDefault() {
		String prefix = Config.getCIFPFilePrefix();
		assertNotNull(prefix);
		assertEquals("cifp_", prefix);
	}

	@Test
	public void getDECPathDefault() {
		String path = Config.getDECPath();
		assertNotNull(path);
		assertTrue(path.contains("enroute"));
	}

	@Test
	public void getDECFilePrefixDefault() {
		String prefix = Config.getDECFilePrefix();
		assertNotNull(prefix);
		assertEquals("DDECUS", prefix);
	}

	@Test
	public void getDERSPathDefault() {
		String path = Config.getDERSPath();
		assertNotNull(path);
	}

	@Test
	public void getDERSFilePrefixDefault() {
		String prefix = Config.getDERSFilePrefix();
		assertNotNull(prefix);
		assertEquals("DERS_", prefix);
	}

	@Test
	public void getTACPathDefault() {
		String path = Config.getTACPath();
		assertNotNull(path);
		assertTrue(path.contains("tac"));
	}

	@Test
	public void getTACPdfPathDefault() {
		String path = Config.getTACPdfPath();
		assertNotNull(path);
		assertTrue(path.contains("PDFs"));
	}

	@Test
	public void getGOMPathDefault() {
		String path = Config.getGOMPath();
		assertNotNull(path);
		assertTrue(path.contains("GoM"));
	}

	@Test
	public void getEnrouteFolderDefault() {
		String folder = Config.getEnrouteFolder();
		assertNotNull(folder);
		assertEquals("enroute", folder);
	}

	@Test
	public void getHelicopterTIFFPathDefault() {
		String path = Config.getHelicopterTIFFPath();
		assertNotNull(path);
		assertTrue(path.contains("heli"));
	}

	@Test
	public void getHelicopterPDFPathDefault() {
		String path = Config.getHelicopterPDFPath();
		assertNotNull(path);
		assertTrue(path.contains("PDFs"));
	}

	@Test
	public void getVFRUploadFolderDefault() {
		String folder = Config.getVFRUploadFolder();
		assertNotNull(folder);
	}
}
