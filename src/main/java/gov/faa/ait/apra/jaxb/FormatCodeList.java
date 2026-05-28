package gov.faa.ait.apra.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "FormatCodeList")
@XmlEnum
public enum FormatCodeList {
    PDF,
    ZIP,
    TIFF;

    public String value() {
        return name();
    }

    public static FormatCodeList fromValue(String v) {
        return valueOf(v);
    }
}
