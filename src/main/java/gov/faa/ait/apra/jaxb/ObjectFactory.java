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
package gov.faa.ait.apra.jaxb;

import jakarta.xml.bind.annotation.XmlRegistry;

/**
 * JAXB ObjectFactory for creating APRA response objects.
 * This class was originally part of the apraresponse module.
 */
@XmlRegistry
public class ObjectFactory {

    public ObjectFactory() {
    }

    public ProductSet createProductSet() {
        return new ProductSet();
    }

    public ProductSet.Status createProductSetStatus() {
        return new ProductSet.Status();
    }

    public ProductSet.Edition createProductSetEdition() {
        return new ProductSet.Edition();
    }

    public ProductSet.Edition.Product createProductSetEditionProduct() {
        return new ProductSet.Edition.Product();
    }
}
