package gov.faa.ait.apra.jaxb;

import java.util.ArrayList;
import java.util.List;

public class ProductSet {
    private Status status;
    private List<Edition> edition;

    public ProductSet() {
        this.edition = new ArrayList<>();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Edition> getEdition() {
        return edition;
    }

    public static class Status {
        private Integer code;
        private String message;

        public Integer getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class Edition {
        private String editionDate;
        private int editionNumber;
        private EditionCodeList editionName;
        private FormatCodeList format;
        private String geoname;
        private String volume;
        private AltitudeCategoryCodeList altitude;
        private Product product;

        public Edition() {
        }

        public String getEditionDate() {
            return editionDate;
        }

        public void setEditionDate(String editionDate) {
            this.editionDate = editionDate;
        }

        public int getEditionNumber() {
            return editionNumber;
        }

        public void setEditionNumber(int editionNumber) {
            this.editionNumber = editionNumber;
        }

        public EditionCodeList getEditionName() {
            return editionName;
        }

        public void setEditionName(EditionCodeList editionName) {
            this.editionName = editionName;
        }

        public FormatCodeList getFormat() {
            return format;
        }

        public void setFormat(FormatCodeList format) {
            this.format = format;
        }

        public String getGeoname() {
            return geoname;
        }

        public void setGeoname(String geoname) {
            this.geoname = geoname;
        }

        public String getVolume() {
            return volume;
        }

        public void setVolume(String volume) {
            this.volume = volume;
        }

        public AltitudeCategoryCodeList getAltitude() {
            return altitude;
        }

        public void setAltitude(AltitudeCategoryCodeList altitude) {
            this.altitude = altitude;
        }

        public Product getProduct() {
            return product;
        }

        public void setProduct(Product product) {
            this.product = product;
        }

        public static class Product {
            private String url;
            private ProductCodeList productName;
            private ChangeCodeList change;
            private String chart;
            private String volume;
            private String chartName;
            private String cityName;
            private String airportName;
            private String airportId;
            private String icao;

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public ProductCodeList getProductName() {
                return productName;
            }

            public void setProductName(ProductCodeList productName) {
                this.productName = productName;
            }

            public ChangeCodeList getChange() {
                return change;
            }

            public void setChange(ChangeCodeList change) {
                this.change = change;
            }

            public String getChart() {
                return chart;
            }

            public void setChart(String chart) {
                this.chart = chart;
            }

            public String getVolume() {
                return volume;
            }

            public void setVolume(String volume) {
                this.volume = volume;
            }

            public String getChartName() {
                return chartName;
            }

            public void setChartName(String chartName) {
                this.chartName = chartName;
            }

            public String getCityName() {
                return cityName;
            }

            public void setCityName(String cityName) {
                this.cityName = cityName;
            }

            public String getAirportName() {
                return airportName;
            }

            public void setAirportName(String airportName) {
                this.airportName = airportName;
            }

            public String getAirportId() {
                return airportId;
            }

            public void setAirportId(String airportId) {
                this.airportId = airportId;
            }

            public String getIcao() {
                return icao;
            }

            public void setIcao(String icao) {
                this.icao = icao;
            }
        }
    }
}
