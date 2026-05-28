package gov.faa.ait.apra.jaxb;

public enum EditionCodeList {
    CURRENT("Current"),
    NEXT("Next"),
    DAILY("Daily");

    private final String value;

    EditionCodeList(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static EditionCodeList fromValue(String v) {
        for (EditionCodeList c : EditionCodeList.values()) {
            if (c.value.equalsIgnoreCase(v) || c.name().equalsIgnoreCase(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
