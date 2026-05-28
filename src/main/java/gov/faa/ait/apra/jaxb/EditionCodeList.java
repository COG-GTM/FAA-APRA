package gov.faa.ait.apra.jaxb;

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
