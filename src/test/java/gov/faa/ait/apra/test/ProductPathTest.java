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

import org.junit.Test;

import gov.faa.ait.apra.path.PathElement;
import gov.faa.ait.apra.path.ProductPath;

public class ProductPathTest {

	@Test
	public void emptyPathReturnsSlash() {
		ProductPath path = new ProductPath();
		assertEquals("/", path.getPathAsString());
	}

	@Test
	public void singleDirectoryElement() {
		ProductPath path = new ProductPath();
		path.addPathElement(new PathElement("charts"));
		assertEquals("/charts/", path.getPathAsString());
	}

	@Test
	public void multipleDirectoryElements() {
		ProductPath path = new ProductPath();
		path.addPathElement(new PathElement("content"));
		path.addPathElement(new PathElement("aeronav"));
		path.addPathElement(new PathElement("charts"));
		assertEquals("/content/aeronav/charts/", path.getPathAsString());
	}

	@Test
	public void pathWithFileElement() {
		ProductPath path = new ProductPath();
		path.addPathElement(new PathElement("content"));
		PathElement file = new PathElement("chart.pdf");
		file.setFile();
		path.addPathElement(file);
		assertEquals("/content/chart.pdf", path.getPathAsString());
	}

	@Test
	public void addLastElementCompletesPath() {
		ProductPath path = new ProductPath();
		path.addPathElement(new PathElement("content"));
		path.addLastElement(new PathElement("final"));
		path.addPathElement(new PathElement("shouldNotAppear"));
		assertEquals("/content/final/", path.getPathAsString());
	}

	@Test
	public void fileElementStopsFurtherAdditions() {
		ProductPath path = new ProductPath();
		PathElement file = new PathElement("chart.pdf");
		file.setFile();
		path.addPathElement(file);
		path.addPathElement(new PathElement("shouldNotAppear"));
		assertEquals("/chart.pdf", path.getPathAsString());
	}

	@Test
	public void addLastElementAfterAlreadyCompleteIsNoOp() {
		ProductPath path = new ProductPath();
		PathElement file = new PathElement("chart.pdf");
		file.setFile();
		path.addPathElement(file);
		path.addLastElement(new PathElement("extra"));
		assertEquals("/chart.pdf", path.getPathAsString());
	}
}
