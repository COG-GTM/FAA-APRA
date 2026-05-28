package gov.faa.ait.apra.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "AltitudeCategoryCodeList")
@XmlEnum
public enum AltitudeCategoryCodeList {
    HIGH,
    LOW;

    public String value() {
        return name();
    }

    public static AltitudeCategoryCodeList fromValue(String v) {
        return valueOf(v);
    }
}
