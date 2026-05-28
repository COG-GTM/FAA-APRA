package gov.faa.ait.apra.jaxb;

public enum FormatCodeList {
    PDF("PDF"),
    TIFF("TIFF"),
    ZIP("ZIP");

    private final String value;

    FormatCodeList(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static FormatCodeList fromValue(String v) {
        for (FormatCodeList c : FormatCodeList.values()) {
            if (c.value.equalsIgnoreCase(v) || c.name().equalsIgnoreCase(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
