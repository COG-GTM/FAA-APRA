package gov.faa.ait.apra.jaxb;

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
