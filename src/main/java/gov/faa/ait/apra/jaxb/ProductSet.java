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

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * JAXB ProductSet response object for APRA API responses.
 * This class was originally part of the apraresponse module.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"status", "edition"})
@XmlRootElement(name = "productSet")
public class ProductSet {

    @XmlElement(required = true)
    protected Status status;
    
    protected List<Edition> edition;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status value) {
        this.status = value;
    }

    public List<Edition> getEdition() {
        if (edition == null) {
            edition = new ArrayList<>();
        }
        return this.edition;
    }

    /**
     * Status nested class for API response status
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"code", "message"})
    public static class Status {
        protected Integer code;
        protected String message;

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer value) {
            this.code = value;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String value) {
            this.message = value;
        }
    }

    /**
     * Edition nested class for chart edition information
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"editionDate", "editionNumber", "editionName", "format", "geoname", "volume", "altitude", "product"})
    public static class Edition {
        protected String editionDate;
        protected Integer editionNumber;
        protected EditionCodeList editionName;
        protected FormatCodeList format;
        protected String geoname;
        protected String volume;
        protected AltitudeCategoryCodeList altitude;
        protected Product product;

        public String getEditionDate() {
            return editionDate;
        }

        public void setEditionDate(String value) {
            this.editionDate = value;
        }

        public Integer getEditionNumber() {
            return editionNumber;
        }

        public void setEditionNumber(Integer value) {
            this.editionNumber = value;
        }

        public EditionCodeList getEditionName() {
            return editionName;
        }

        public void setEditionName(EditionCodeList value) {
            this.editionName = value;
        }

        public FormatCodeList getFormat() {
            return format;
        }

        public void setFormat(FormatCodeList value) {
            this.format = value;
        }

        public String getGeoname() {
            return geoname;
        }

        public void setGeoname(String value) {
            this.geoname = value;
        }

        public String getVolume() {
            return volume;
        }

        public void setVolume(String value) {
            this.volume = value;
        }

        public AltitudeCategoryCodeList getAltitude() {
            return altitude;
        }

        public void setAltitude(AltitudeCategoryCodeList value) {
            this.altitude = value;
        }

        public Product getProduct() {
            return product;
        }

        public void setProduct(Product value) {
            this.product = value;
        }

        /**
         * Product nested class for chart product information
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {"productName", "chartName", "url", "change", "icao", "airportId", "cityName", "airportName"})
        public static class Product {
            protected ProductCodeList productName;
            protected String chartName;
            protected String url;
            protected ChangeCodeList change;
            protected String icao;
            protected String airportId;
            protected String cityName;
            protected String airportName;

            public ProductCodeList getProductName() {
                return productName;
            }

            public void setProductName(ProductCodeList value) {
                this.productName = value;
            }

            public String getChartName() {
                return chartName;
            }

            public void setChartName(String value) {
                this.chartName = value;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String value) {
                this.url = value;
            }

            public ChangeCodeList getChange() {
                return change;
            }

            public void setChange(ChangeCodeList value) {
                this.change = value;
            }

            public String getIcao() {
                return icao;
            }

            public void setIcao(String value) {
                this.icao = value;
            }

            public String getAirportId() {
                return airportId;
            }

            public void setAirportId(String value) {
                this.airportId = value;
            }

            public String getCityName() {
                return cityName;
            }

            public void setCityName(String value) {
                this.cityName = value;
            }

            public String getAirportName() {
                return airportName;
            }

            public void setAirportName(String value) {
                this.airportName = value;
            }
        }
    }
}
