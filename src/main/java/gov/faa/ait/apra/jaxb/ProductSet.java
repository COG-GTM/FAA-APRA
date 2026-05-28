package gov.faa.ait.apra.jaxb;

import java.util.ArrayList;
import java.util.List;

public class ProductSet {

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

    public static class Edition {

        protected EditionCodeList editionName;
        protected Integer editionNumber;
        protected String editionDate;
        protected FormatCodeList format;
        protected String geoname;
        protected String volume;
        protected AltitudeCategoryCodeList altitude;
        protected Product product;

        public EditionCodeList getEditionName() { return editionName; }
        public void setEditionName(EditionCodeList value) { this.editionName = value; }

        public Integer getEditionNumber() { return editionNumber; }
        public void setEditionNumber(Integer value) { this.editionNumber = value; }

        public String getEditionDate() { return editionDate; }
        public void setEditionDate(String value) { this.editionDate = value; }

        public FormatCodeList getFormat() { return format; }
        public void setFormat(FormatCodeList value) { this.format = value; }

        public String getGeoname() { return geoname; }
        public void setGeoname(String value) { this.geoname = value; }

        public String getVolume() { return volume; }
        public void setVolume(String value) { this.volume = value; }

        public AltitudeCategoryCodeList getAltitude() { return altitude; }
        public void setAltitude(AltitudeCategoryCodeList value) { this.altitude = value; }

        public Product getProduct() { return product; }
        public void setProduct(Product value) { this.product = value; }

        public static class Product {

            protected ProductCodeList productName;
            protected String chartName;
            protected String url;
            protected ChangeCodeList change;
            protected String icao;
            protected String airportId;
            protected String cityName;
            protected String airportName;

            public ProductCodeList getProductName() { return productName; }
            public void setProductName(ProductCodeList value) { this.productName = value; }

            public String getChartName() { return chartName; }
            public void setChartName(String value) { this.chartName = value; }

            public String getUrl() { return url; }
            public void setUrl(String value) { this.url = value; }

            public ChangeCodeList getChange() { return change; }
            public void setChange(ChangeCodeList value) { this.change = value; }

            public String getIcao() { return icao; }
            public void setIcao(String value) { this.icao = value; }

            public String getAirportId() { return airportId; }
            public void setAirportId(String value) { this.airportId = value; }

            public String getCityName() { return cityName; }
            public void setCityName(String value) { this.cityName = value; }

            public String getAirportName() { return airportName; }
            public void setAirportName(String value) { this.airportName = value; }
        }
    }

    public static class Status {

        protected Integer code;
        protected String message;

        public Integer getCode() { return code; }
        public void setCode(int value) { this.code = value; }

        public String getMessage() { return message; }
        public void setMessage(String value) { this.message = value; }
    }
}
