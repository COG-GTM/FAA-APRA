package gov.faa.ait.apra.jaxb;

import javax.xml.bind.annotation.XmlRegistry;

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
