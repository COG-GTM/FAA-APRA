package gov.faa.ait.apra.jaxb;

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
