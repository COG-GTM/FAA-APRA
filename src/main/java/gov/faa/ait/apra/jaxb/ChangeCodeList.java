package gov.faa.ait.apra.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ChangeCodeList")
@XmlEnum
public enum ChangeCodeList {
    ADDED,
    CHANGED,
    DELETED;

    public String value() {
        return name();
    }

    public static ChangeCodeList fromValue(String v) {
        return valueOf(v);
    }
}
