package uk.ac.ebi.pride.toolsuite.px_validator.utils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * This class describes the summary information of an assay file.
 */
@Getter
@Setter
@NoArgsConstructor
public class ResultReport extends Report {

    private String assayFile;
    private long fileSize;
    private int numberOfProteins;
    private int numberOfPeptides;
    private int numberOfPSMs;
    private int numberOfPeakFiles;
    private boolean isValidSchema = false;
    private List<PeakReport> peakReports = new ArrayList<>();

    @Override
    public String toString() {
        String peakReportSection = "";
        for (int i = 0; i < peakReports.size(); i++) {
            PeakReport peakReport = peakReports.get(i);
            peakReportSection += "PEAK" + i +'\t' + peakReport.toString() + "\n";
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
