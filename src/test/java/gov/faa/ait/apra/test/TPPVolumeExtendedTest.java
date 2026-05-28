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

import java.util.List;

import org.junit.Test;

import gov.faa.ait.apra.bootstrap.TPPVolume;

public class TPPVolumeExtendedTest {

	@Test
	public void allNortheastVolumesValid() {
		assertTrue(TPPVolume.isValidVolumeName("NE-1"));
		assertTrue(TPPVolume.isValidVolumeName("NE-2"));
		assertTrue(TPPVolume.isValidVolumeName("NE-3"));
		assertTrue(TPPVolume.isValidVolumeName("NE-4"));
	}

	@Test
	public void allSoutheastVolumesValid() {
		assertTrue(TPPVolume.isValidVolumeName("SE-1"));
		assertTrue(TPPVolume.isValidVolumeName("SE-2"));
		assertTrue(TPPVolume.isValidVolumeName("SE-3"));
		assertTrue(TPPVolume.isValidVolumeName("SE-4"));
	}

	@Test
	public void allSouthCentralVolumesValid() {
		assertTrue(TPPVolume.isValidVolumeName("SC-1"));
		assertTrue(TPPVolume.isValidVolumeName("SC-2"));
		assertTrue(TPPVolume.isValidVolumeName("SC-3"));
		assertTrue(TPPVolume.isValidVolumeName("SC-4"));
		assertTrue(TPPVolume.isValidVolumeName("SC-5"));
	}

	@Test
	public void allEastCentralVolumesValid() {
		assertTrue(TPPVolume.isValidVolumeName("EC-1"));
		assertTrue(TPPVolume.isValidVolumeName("EC-2"));
		assertTrue(TPPVolume.isValidVolumeName("EC-3"));
	}

	@Test
	public void allNorthCentralVolumesValid() {
		assertTrue(TPPVolume.isValidVolumeName("NC-1"));
		assertTrue(TPPVolume.isValidVolumeName("NC-2"));
		assertTrue(TPPVolume.isValidVolumeName("NC-3"));
	}

	@Test
	public void allSouthwestVolumesValid() {
		assertTrue(TPPVolume.isValidVolumeName("SW-1"));
		assertTrue(TPPVolume.isValidVolumeName("SW-2"));
		assertTrue(TPPVolume.isValidVolumeName("SW-3"));
		assertTrue(TPPVolume.isValidVolumeName("SW-4"));
	}

	@Test
	public void northwestVolumeValid() {
		assertTrue(TPPVolume.isValidVolumeName("NW-1"));
	}

	@Test
	public void alaskaVolumeValid() {
		assertTrue(TPPVolume.isValidVolumeName("AK-1"));
	}

	@Test
	public void pacificVolumeValid() {
		assertTrue(TPPVolume.isValidVolumeName("PC-1"));
	}

	@Test
	public void invalidVolumeReturnsFalse() {
		assertFalse(TPPVolume.isValidVolumeName("XX-1"));
	}

	@Test
	public void emptyStringInvalid() {
		assertFalse(TPPVolume.isValidVolumeName(""));
	}

	@Test
	public void lowerCaseInvalid() {
		assertFalse(TPPVolume.isValidVolumeName("ne-1"));
	}

	@Test
	public void getVolumeListReturnsAllVolumes() {
		List<String> volumes = TPPVolume.getVolumeList();
		assertNotNull(volumes);
		assertEquals(26, volumes.size());
	}

	@Test
	public void getVolumeListReturnsNewList() {
		List<String> list1 = TPPVolume.getVolumeList();
		List<String> list2 = TPPVolume.getVolumeList();
		assertEquals(list1, list2);
		assertFalse(list1 == list2);
	}

	@Test
	public void volumeListIsNotModifiable() {
		List<String> list = TPPVolume.getVolumeList();
		int originalSize = list.size();
		list.add("FAKE-1");
		List<String> list2 = TPPVolume.getVolumeList();
		assertEquals(originalSize, list2.size());
	}
}
