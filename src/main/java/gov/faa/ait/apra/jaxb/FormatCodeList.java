package gov.faa.ait.apra.jaxb;

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
