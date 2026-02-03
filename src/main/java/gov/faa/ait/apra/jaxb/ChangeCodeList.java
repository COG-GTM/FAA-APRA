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
 * Change code list enumeration for APRA API.
 * This class was originally part of the apraresponse module.
 */
@XmlType(name = "changeCodeList")
@XmlEnum
public enum ChangeCodeList {

    @XmlEnumValue("CHANGED")
    CHANGED("CHANGED"),
    
    @XmlEnumValue("UNCHANGED")
    UNCHANGED("UNCHANGED"),
    
    @XmlEnumValue("NEW")
    NEW("NEW"),
    
    @XmlEnumValue("DELETED")
    DELETED("DELETED");

    private final String value;

    ChangeCodeList(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ChangeCodeList fromValue(String v) {
        for (ChangeCodeList c : ChangeCodeList.values()) {
            if (c.value.equalsIgnoreCase(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
