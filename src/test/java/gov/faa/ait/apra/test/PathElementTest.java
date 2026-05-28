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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import gov.faa.ait.apra.path.PathElement;

public class PathElementTest {

	@Test
	public void defaultConstructorSetsEmptyPath() {
		PathElement pe = new PathElement();
		assertEquals("", pe.getPathElement());
	}

	@Test
	public void defaultConstructorSetsDirectory() {
		PathElement pe = new PathElement();
		assertTrue(pe.isDirectory());
		assertFalse(pe.isFile());
	}

	@Test
	public void stringConstructorSetsName() {
		PathElement pe = new PathElement("charts");
		assertEquals("charts", pe.getPathElement());
	}

	@Test
	public void stringConstructorSetsDirectory() {
		PathElement pe = new PathElement("charts");
		assertTrue(pe.isDirectory());
		assertFalse(pe.isFile());
	}

	@Test
	public void setPathElementReturnsThis() {
		PathElement pe = new PathElement();
		PathElement result = pe.setPathElement("newpath");
		assertEquals(pe, result);
		assertEquals("newpath", pe.getPathElement());
	}

	@Test
	public void setFileSetsFileFlag() {
		PathElement pe = new PathElement("chart.pdf");
		pe.setFile();
		assertTrue(pe.isFile());
		assertFalse(pe.isDirectory());
	}

	@Test
	public void setDirectoryResetsFileFlag() {
		PathElement pe = new PathElement("chart.pdf");
		pe.setFile();
		assertTrue(pe.isFile());
		pe.setDirectory();
		assertFalse(pe.isFile());
		assertTrue(pe.isDirectory());
	}

	@Test
	public void chainingSetPathElement() {
		PathElement pe = new PathElement();
		pe.setPathElement("first").setPathElement("second");
		assertEquals("second", pe.getPathElement());
	}
}
