package gov.faa.ait.apra.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ProductCodeList")
@XmlEnum
public enum ProductCodeList {
    CIFP,
    DDOF,
    DEC,
    ENROUTE,
    IFR_ENROUTE,
    IFR_OCEANIC,
    IFR_PLANNING,
    SECTIONAL,
    SUBSCRIBER,
    SUPPLEMENT,
    TAC,
    TPP,
    VFR,
    VFR_HELICOPTER,
    WALLPLANNING;

    public String value() {
        return name();
    }

    public static ProductCodeList fromValue(String v) {
        return valueOf(v);
    }
}
