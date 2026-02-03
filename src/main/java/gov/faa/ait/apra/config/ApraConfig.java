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
package gov.faa.ait.apra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "apra")
public class ApraConfig {

    private Aeronav aeronav = new Aeronav();
    private Ddof ddof = new Ddof();
    private Nfdc nfdc = new Nfdc();
    private Denodo denodo = new Denodo();
    private Proxy proxy = new Proxy();
    private int cycleAgeLimit = 1;
    private CheckFlags checkFlags = new CheckFlags();
    private Paths paths = new Paths();
    private Files files = new Files();

    public static class Aeronav {
        private String host = "http://aeronav.faa.gov";

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
    }

    public static class Ddof {
        private String host = "http://tod.faa.gov";

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
    }

    public static class Nfdc {
        private String host = "http://nfdc.faa.gov";

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
    }

    public static class Denodo {
        private String host = "https://soadev.smext.faa.gov";
        private String cycleResource = "/denodo/apra/server/ifpa/edai/views/chart_cycle";
        private String vfrCycleResource = "/denodo/apra/server/ifpa/edai/views/vfr_chart_cycle";
        private String viewPath = "/denodo/apra/server/ifpa/edai/views";

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public String getCycleResource() { return cycleResource; }
        public void setCycleResource(String cycleResource) { this.cycleResource = cycleResource; }
        public String getVfrCycleResource() { return vfrCycleResource; }
        public void setVfrCycleResource(String vfrCycleResource) { this.vfrCycleResource = vfrCycleResource; }
        public String getViewPath() { return viewPath; }
        public void setViewPath(String viewPath) { this.viewPath = viewPath; }
    }

    public static class Proxy {
        private String host;
        private String port = "8080";

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public String getPort() { return port; }
        public void setPort(String port) { this.port = port; }
    }

    public static class CheckFlags {
        private boolean tpp = false;
        private boolean supplement = false;
        private boolean sectional = false;
        private boolean nasr = true;

        public boolean isTpp() { return tpp; }
        public void setTpp(boolean tpp) { this.tpp = tpp; }
        public boolean isSupplement() { return supplement; }
        public void setSupplement(boolean supplement) { this.supplement = supplement; }
        public boolean isSectional() { return sectional; }
        public void setSectional(boolean sectional) { this.sectional = sectional; }
        public boolean isNasr() { return nasr; }
        public void setNasr(boolean nasr) { this.nasr = nasr; }
    }

    public static class Paths {
        private String cifp = "/Upload_313-d/cifp";
        private String dec = "/enroute";
        private String ders = "/enroute";
        private String gom = "/enroute/GoM";
        private String tac = "/content/aeronav/tac_files";
        private String tacPdf = "/content/aeronav/tac_files/PDFs";
        private String tppUs = "/upload_313-d/terminal";
        private String tppChart = "/d-tpp";
        private String supPdf = "/afd";
        private String supUs = "/upload_313-d/supplements";
        private String ddof = "/tod";
        private String nasr = "/webContent/28DaySub";
        private String sectional = "/content/aeronav/sectional_files";
        private String wallplan = "/content/aeronav/grand_canyon_files";
        private String vfr = "/content/aeronav/grand_canyon_files";
        private String helicopterTiff = "/content/aeronav/heli_files";
        private String helicopterPdf = "/content/aeronav/heli_files/PDFs";
        private String enroute = "enroute";

        public String getCifp() { return cifp; }
        public void setCifp(String cifp) { this.cifp = cifp; }
        public String getDec() { return dec; }
        public void setDec(String dec) { this.dec = dec; }
        public String getDers() { return ders; }
        public void setDers(String ders) { this.ders = ders; }
        public String getGom() { return gom; }
        public void setGom(String gom) { this.gom = gom; }
        public String getTac() { return tac; }
        public void setTac(String tac) { this.tac = tac; }
        public String getTacPdf() { return tacPdf; }
        public void setTacPdf(String tacPdf) { this.tacPdf = tacPdf; }
        public String getTppUs() { return tppUs; }
        public void setTppUs(String tppUs) { this.tppUs = tppUs; }
        public String getTppChart() { return tppChart; }
        public void setTppChart(String tppChart) { this.tppChart = tppChart; }
        public String getSupPdf() { return supPdf; }
        public void setSupPdf(String supPdf) { this.supPdf = supPdf; }
        public String getSupUs() { return supUs; }
        public void setSupUs(String supUs) { this.supUs = supUs; }
        public String getDdof() { return ddof; }
        public void setDdof(String ddof) { this.ddof = ddof; }
        public String getNasr() { return nasr; }
        public void setNasr(String nasr) { this.nasr = nasr; }
        public String getSectional() { return sectional; }
        public void setSectional(String sectional) { this.sectional = sectional; }
        public String getWallplan() { return wallplan; }
        public void setWallplan(String wallplan) { this.wallplan = wallplan; }
        public String getVfr() { return vfr; }
        public void setVfr(String vfr) { this.vfr = vfr; }
        public String getHelicopterTiff() { return helicopterTiff; }
        public void setHelicopterTiff(String helicopterTiff) { this.helicopterTiff = helicopterTiff; }
        public String getHelicopterPdf() { return helicopterPdf; }
        public void setHelicopterPdf(String helicopterPdf) { this.helicopterPdf = helicopterPdf; }
        public String getEnroute() { return enroute; }
        public void setEnroute(String enroute) { this.enroute = enroute; }
    }

    public static class Files {
        private String cifpPrefix = "cifp_";
        private String nasrPrefix = "28DaySubscription_Effective_";
        private String nasrDateFormat = "yyyy-MM-dd";
        private String tppUsPrefix = "DDTPP";
        private String decPrefix = "DDECUS";
        private String dersPrefix = "DERS_";
        private String ddof = "DAILY_DOF.ZIP";
        private String ddofDailyChange = "DOF_DAILY_CHANGE_UPDATE.ZIP";
        private String gomWestPdf = "gom_west_pdf.zip";
        private String gomCentralPdf = "gom_central_pdf.zip";
        private String gomWestTiff = "gom_west_tif.zip";
        private String gomCentralTiff = "gom_central_tif.zip";

        public String getCifpPrefix() { return cifpPrefix; }
        public void setCifpPrefix(String cifpPrefix) { this.cifpPrefix = cifpPrefix; }
        public String getNasrPrefix() { return nasrPrefix; }
        public void setNasrPrefix(String nasrPrefix) { this.nasrPrefix = nasrPrefix; }
        public String getNasrDateFormat() { return nasrDateFormat; }
        public void setNasrDateFormat(String nasrDateFormat) { this.nasrDateFormat = nasrDateFormat; }
        public String getTppUsPrefix() { return tppUsPrefix; }
        public void setTppUsPrefix(String tppUsPrefix) { this.tppUsPrefix = tppUsPrefix; }
        public String getDecPrefix() { return decPrefix; }
        public void setDecPrefix(String decPrefix) { this.decPrefix = decPrefix; }
        public String getDersPrefix() { return dersPrefix; }
        public void setDersPrefix(String dersPrefix) { this.dersPrefix = dersPrefix; }
        public String getDdof() { return ddof; }
        public void setDdof(String ddof) { this.ddof = ddof; }
        public String getDdofDailyChange() { return ddofDailyChange; }
        public void setDdofDailyChange(String ddofDailyChange) { this.ddofDailyChange = ddofDailyChange; }
        public String getGomWestPdf() { return gomWestPdf; }
        public void setGomWestPdf(String gomWestPdf) { this.gomWestPdf = gomWestPdf; }
        public String getGomCentralPdf() { return gomCentralPdf; }
        public void setGomCentralPdf(String gomCentralPdf) { this.gomCentralPdf = gomCentralPdf; }
        public String getGomWestTiff() { return gomWestTiff; }
        public void setGomWestTiff(String gomWestTiff) { this.gomWestTiff = gomWestTiff; }
        public String getGomCentralTiff() { return gomCentralTiff; }
        public void setGomCentralTiff(String gomCentralTiff) { this.gomCentralTiff = gomCentralTiff; }
    }

    public Aeronav getAeronav() { return aeronav; }
    public void setAeronav(Aeronav aeronav) { this.aeronav = aeronav; }
    public Ddof getDdof() { return ddof; }
    public void setDdof(Ddof ddof) { this.ddof = ddof; }
    public Nfdc getNfdc() { return nfdc; }
    public void setNfdc(Nfdc nfdc) { this.nfdc = nfdc; }
    public Denodo getDenodo() { return denodo; }
    public void setDenodo(Denodo denodo) { this.denodo = denodo; }
    public Proxy getProxy() { return proxy; }
    public void setProxy(Proxy proxy) { this.proxy = proxy; }
    public int getCycleAgeLimit() { return cycleAgeLimit; }
    public void setCycleAgeLimit(int cycleAgeLimit) { this.cycleAgeLimit = cycleAgeLimit; }
    public CheckFlags getCheckFlags() { return checkFlags; }
    public void setCheckFlags(CheckFlags checkFlags) { this.checkFlags = checkFlags; }
    public Paths getPaths() { return paths; }
    public void setPaths(Paths paths) { this.paths = paths; }
    public Files getFiles() { return files; }
    public void setFiles(Files files) { this.files = files; }
}
