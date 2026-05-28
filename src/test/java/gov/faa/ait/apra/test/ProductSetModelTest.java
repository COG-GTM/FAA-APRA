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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import gov.faa.ait.apra.jaxb.AltitudeCategoryCodeList;
import gov.faa.ait.apra.jaxb.ChangeCodeList;
import gov.faa.ait.apra.jaxb.EditionCodeList;
import gov.faa.ait.apra.jaxb.FormatCodeList;
import gov.faa.ait.apra.jaxb.ObjectFactory;
import gov.faa.ait.apra.jaxb.ProductCodeList;
import gov.faa.ait.apra.jaxb.ProductSet;

public class ProductSetModelTest {

	@Test
	public void objectFactoryCreatesProductSet() {
		ObjectFactory of = new ObjectFactory();
		ProductSet ps = of.createProductSet();
		assertNotNull(ps);
		assertNotNull(ps.getEdition());
		assertTrue(ps.getEdition().isEmpty());
	}

	@Test
	public void objectFactoryCreatesStatus() {
		ObjectFactory of = new ObjectFactory();
		ProductSet.Status status = of.createProductSetStatus();
		assertNotNull(status);
	}

	@Test
	public void objectFactoryCreatesEdition() {
		ObjectFactory of = new ObjectFactory();
		ProductSet.Edition edition = of.createProductSetEdition();
		assertNotNull(edition);
	}

	@Test
	public void objectFactoryCreatesProduct() {
		ObjectFactory of = new ObjectFactory();
		ProductSet.Edition.Product product = of.createProductSetEditionProduct();
		assertNotNull(product);
	}

	@Test
	public void productSetStatusFields() {
		ProductSet.Status status = new ProductSet.Status();
		status.setCode(200);
		status.setMessage("OK");
		assertEquals(Integer.valueOf(200), status.getCode());
		assertEquals("OK", status.getMessage());
	}

	@Test
	public void productSetEditionFields() {
		ProductSet.Edition ed = new ProductSet.Edition();
		ed.setEditionDate("2024-01-15");
		ed.setEditionNumber(42);
		ed.setEditionName(EditionCodeList.CURRENT);
		ed.setFormat(FormatCodeList.PDF);
		ed.setGeoname("ALBUQUERQUE");
		ed.setVolume("NE-1");
		ed.setAltitude(AltitudeCategoryCodeList.HIGH);

		assertEquals("2024-01-15", ed.getEditionDate());
		assertEquals(42, ed.getEditionNumber());
		assertEquals(EditionCodeList.CURRENT, ed.getEditionName());
		assertEquals(FormatCodeList.PDF, ed.getFormat());
		assertEquals("ALBUQUERQUE", ed.getGeoname());
		assertEquals("NE-1", ed.getVolume());
		assertEquals(AltitudeCategoryCodeList.HIGH, ed.getAltitude());
	}

	@Test
	public void productSetProductFields() {
		ProductSet.Edition.Product product = new ProductSet.Edition.Product();
		product.setUrl("http://example.com/chart.pdf");
		product.setProductName(ProductCodeList.SECTIONAL);
		product.setChange(ChangeCodeList.CHANGED);
		product.setChart("Albuquerque Sectional");
		product.setVolume("NE-1");
		product.setChartName("ILS RWY 14R");
		product.setCityName("Omaha");
		product.setAirportName("Eppley Airfield");
		product.setAirportId("OMA");
		product.setIcao("KOMA");

		assertEquals("http://example.com/chart.pdf", product.getUrl());
		assertEquals(ProductCodeList.SECTIONAL, product.getProductName());
		assertEquals(ChangeCodeList.CHANGED, product.getChange());
		assertEquals("Albuquerque Sectional", product.getChart());
		assertEquals("NE-1", product.getVolume());
		assertEquals("ILS RWY 14R", product.getChartName());
		assertEquals("Omaha", product.getCityName());
		assertEquals("Eppley Airfield", product.getAirportName());
		assertEquals("OMA", product.getAirportId());
		assertEquals("KOMA", product.getIcao());
	}

	@Test
	public void editionSetAndGetProduct() {
		ProductSet.Edition ed = new ProductSet.Edition();
		ProductSet.Edition.Product product = new ProductSet.Edition.Product();
		product.setUrl("http://example.com/chart.pdf");
		ed.setProduct(product);
		assertNotNull(ed.getProduct());
		assertEquals("http://example.com/chart.pdf", ed.getProduct().getUrl());
	}

	@Test
	public void productSetWithEditions() {
		ProductSet ps = new ProductSet();
		ProductSet.Edition ed = new ProductSet.Edition();
		ed.setGeoname("DENVER");
		ps.getEdition().add(ed);
		assertEquals(1, ps.getEdition().size());
		assertEquals("DENVER", ps.getEdition().get(0).getGeoname());
	}

	@Test
	public void editionCodeListFromValue() {
		assertEquals(EditionCodeList.CURRENT, EditionCodeList.fromValue("Current"));
		assertEquals(EditionCodeList.NEXT, EditionCodeList.fromValue("Next"));
		assertEquals(EditionCodeList.DAILY, EditionCodeList.fromValue("Daily"));
	}

	@Test
	public void editionCodeListFromNameCaseInsensitive() {
		assertEquals(EditionCodeList.CURRENT, EditionCodeList.fromValue("CURRENT"));
		assertEquals(EditionCodeList.CURRENT, EditionCodeList.fromValue("current"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void editionCodeListFromValueInvalid() {
		EditionCodeList.fromValue("INVALID");
	}

	@Test
	public void formatCodeListFromValue() {
		assertEquals(FormatCodeList.PDF, FormatCodeList.fromValue("PDF"));
		assertEquals(FormatCodeList.TIFF, FormatCodeList.fromValue("TIFF"));
		assertEquals(FormatCodeList.ZIP, FormatCodeList.fromValue("ZIP"));
	}

	@Test
	public void formatCodeListCaseInsensitive() {
		assertEquals(FormatCodeList.PDF, FormatCodeList.fromValue("pdf"));
		assertEquals(FormatCodeList.TIFF, FormatCodeList.fromValue("tiff"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void formatCodeListFromValueInvalid() {
		FormatCodeList.fromValue("INVALID");
	}

	@Test
	public void productCodeListValues() {
		assertNotNull(ProductCodeList.SECTIONAL);
		assertNotNull(ProductCodeList.TAC);
		assertNotNull(ProductCodeList.VFR);
		assertNotNull(ProductCodeList.IFR_ENROUTE);
		assertNotNull(ProductCodeList.CIFP);
		assertNotNull(ProductCodeList.TPP);
	}

	@Test
	public void changeCodeListValues() {
		assertNotNull(ChangeCodeList.CHANGED);
		assertNotNull(ChangeCodeList.UNCHANGED);
		assertNotNull(ChangeCodeList.NEW);
		assertNotNull(ChangeCodeList.DELETED);
		assertNotNull(ChangeCodeList.ADDED);
	}

	@Test
	public void altitudeCategoryValues() {
		assertNotNull(AltitudeCategoryCodeList.HIGH);
		assertNotNull(AltitudeCategoryCodeList.LOW);
	}

	@Test
	public void editionCodeListValueMethod() {
		assertEquals("Current", EditionCodeList.CURRENT.value());
		assertEquals("Next", EditionCodeList.NEXT.value());
		assertEquals("Daily", EditionCodeList.DAILY.value());
	}

	@Test
	public void formatCodeListValueMethod() {
		assertEquals("PDF", FormatCodeList.PDF.value());
		assertEquals("TIFF", FormatCodeList.TIFF.value());
		assertEquals("ZIP", FormatCodeList.ZIP.value());
	}

	@Test
	public void productSetStatusDefaultNull() {
		ProductSet ps = new ProductSet();
		assertNull(ps.getStatus());
	}

	@Test
	public void productSetSetAndGetStatus() {
		ProductSet ps = new ProductSet();
		ProductSet.Status status = new ProductSet.Status();
		status.setCode(200);
		status.setMessage("OK");
		ps.setStatus(status);
		assertNotNull(ps.getStatus());
		assertEquals(Integer.valueOf(200), ps.getStatus().getCode());
		assertEquals("OK", ps.getStatus().getMessage());
	}
}
