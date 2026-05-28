package gov.faa.ait.apra.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "EditionCodeList")
@XmlEnum
public enum EditionCodeList {
    CURRENT,
    NEXT,
    DAILY;

    public String value() {
        return name();
    }

    public static EditionCodeList fromValue(String v) {
        return valueOf(v);
    }
}
