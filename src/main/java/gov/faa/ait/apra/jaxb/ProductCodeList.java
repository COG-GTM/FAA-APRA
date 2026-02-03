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
 * Product code list enumeration for APRA API.
 * This class was originally part of the apraresponse module.
 */
@XmlType(name = "productCodeList")
@XmlEnum
public enum ProductCodeList {

    @XmlEnumValue("SECTIONAL")
    SECTIONAL("SECTIONAL"),
    
    @XmlEnumValue("TAC")
    TAC("TAC"),
    
    @XmlEnumValue("HELICOPTER")
    HELICOPTER("HELICOPTER"),
    
    @XmlEnumValue("IFR_ENROUTE")
    IFR_ENROUTE("IFR_ENROUTE"),
    
    @XmlEnumValue("IFR_AREA")
    IFR_AREA("IFR_AREA"),
    
    @XmlEnumValue("CIFP")
    CIFP("CIFP"),
    
    @XmlEnumValue("DDOF")
    DDOF("DDOF"),
    
    @XmlEnumValue("SUBSCRIBER")
    SUBSCRIBER("SUBSCRIBER"),
    
    @XmlEnumValue("DEC")
    DEC("DEC"),
    
    @XmlEnumValue("SUPPLEMENT")
    SUPPLEMENT("SUPPLEMENT"),
    
    @XmlEnumValue("TPP")
    TPP("TPP"),
    
    @XmlEnumValue("WALL_PLANNING")
    WALL_PLANNING("WALL_PLANNING"),
    
    @XmlEnumValue("OCEANIC")
    OCEANIC("OCEANIC"),
    
    @XmlEnumValue("GOM")
    GOM("GOM"),
    
    @XmlEnumValue("VFR")
    VFR("VFR"),
    
    @XmlEnumValue("NASR")
    NASR("NASR"),
    
    @XmlEnumValue("VFR_HELICOPTER")
    VFR_HELICOPTER("VFR_HELICOPTER"),
    
    @XmlEnumValue("WALLPLANNING")
    WALLPLANNING("WALLPLANNING"),
    
    @XmlEnumValue("IFR_OCEANIC")
    IFR_OCEANIC("IFR_OCEANIC"),
    
    @XmlEnumValue("ENROUTE")
    ENROUTE("ENROUTE"),
    
    @XmlEnumValue("IFR_PLANNING")
    IFR_PLANNING("IFR_PLANNING"),
    
    @XmlEnumValue("VFR_PLANNING")
    VFR_PLANNING("VFR_PLANNING"),
    
    @XmlEnumValue("DERS")
    DERS("DERS");

    private final String value;

    ProductCodeList(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ProductCodeList fromValue(String v) {
        for (ProductCodeList c : ProductCodeList.values()) {
            if (c.value.equalsIgnoreCase(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
