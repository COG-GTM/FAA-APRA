/*
 * Federal Aviation Administration (FAA) public work 
 * 
 * As a work of the United States Government, this project is in the 
 * public domain within the United States. Additionally, we waive copyright 
 * and related rights in the work worldwide 
 * through the Creative Commons 0 (CC0) 1.0 Universal public domain dedication
 * 
 * APRA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package gov.faa.ait.apra.jaxb;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Altitude category code list enumeration for APRA API.
 * This class was originally part of the apraresponse module.
 */
@XmlType(name = "altitudeCategoryCodeList")
@XmlEnum
public enum AltitudeCategoryCodeList {

    @XmlEnumValue("LOW")
    LOW("LOW"),
    
    @XmlEnumValue("HIGH")
    HIGH("HIGH"),
    
    @XmlEnumValue("AREA")
    AREA("AREA");

    private final String value;

    AltitudeCategoryCodeList(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AltitudeCategoryCodeList fromValue(String v) {
        for (AltitudeCategoryCodeList c : AltitudeCategoryCodeList.values()) {
            if (c.value.equalsIgnoreCase(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
