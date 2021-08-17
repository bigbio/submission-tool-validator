package uk.ac.ebi.pride.toolsuite.px_validator.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * This class describes the summary information of an assay file.
 */
public class ResultReport extends Report {

    private String assayFile;
    private long fileSize;
    private int numberOfProteins;
    private int numberOfPeptides;
    private int numberOfPSMs;
    private int numberOfPeakFiles;
    private boolean isValidSchema = false;
    private List<PeakReport> peakReports = new ArrayList<>();

    public ResultReport() {

    }

    public void setNumberOfProteins(int numberOfProteins) {
        this.numberOfProteins = numberOfProteins;
    }

    public void setNumberOfPeptides(int numberOfPeptides) {
        this.numberOfPeptides = numberOfPeptides;
    }

    public void setNumberOfPSMs(int numberOfPSMs) {
        this.numberOfPSMs = numberOfPSMs;
    }

    public void setNumberOfPeakFiles(int numberOfPeakFiles) {
        this.numberOfPeakFiles = numberOfPeakFiles;
    }

    public void setValidSchema(boolean validSchema) {
        isValidSchema = validSchema;
    }

    public void setAssayFile(String assayFile) {
        this.assayFile = assayFile;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setPeakReports(List<PeakReport> peakReports) {
        this.peakReports = peakReports;
    }

    @Override
    public String toString() {
        String peakReportSection = "";
        for (int i = 0; i < peakReports.size(); i++) {
            PeakReport peakReport = peakReports.get(i);
            peakReportSection = "PEAK" + i +'\t' + peakReport.toString() + "\n";
        }
        return super.toString() +
                "Assay file : " + assayFile + "\n" +
                "Assay file size: " + fileSize + "\n" +
                "Valid Schema : " + isValidSchema + "\n" +
                "Number of reported proteins : " + numberOfProteins + "\n" +
                "Number of reported peptides : " + numberOfPeptides + "\n" +
                "Number of reported PSMs : " + numberOfPSMs + "\n" +
                "Number of peak files : " + numberOfPeakFiles + "\n" +
                peakReportSection;
    }
}
