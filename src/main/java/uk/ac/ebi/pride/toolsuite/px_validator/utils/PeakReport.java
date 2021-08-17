package uk.ac.ebi.pride.toolsuite.px_validator.utils;

import de.mpc.pia.intermediate.PeptideSpectrumMatch;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Suresh Hewapathirana
 */
public class PeakReport extends Report{

    private String peakFile;
    private long fileSize;
    private int numberOfPeaks;
    private List<PeptideSpectrumMatch> detectedPsms = new ArrayList<>();
    private List<PeptideSpectrumMatch> undetectedPsms = new ArrayList<>();

    public String getPeakFile() {
        return peakFile;
    }

    public void setPeakFile(String peakFile) {
        this.peakFile = peakFile;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getNumberOfPeaks() {
        return numberOfPeaks;
    }

    public void setNumberOfPeaks(int numberOfPeaks) {
        this.numberOfPeaks = numberOfPeaks;
    }

    public List<PeptideSpectrumMatch> getDetectedPsms() {
        return detectedPsms;
    }

    public void setDetectedPsms(List<PeptideSpectrumMatch> detectedPsms) {
        this.detectedPsms = detectedPsms;
    }

    public List<PeptideSpectrumMatch> getUndetectedPsms() {
        return undetectedPsms;
    }

    public void setUndetectedPsms(List<PeptideSpectrumMatch> undetectedPsms) {
        this.undetectedPsms = undetectedPsms;
    }

    public PeakReport() {
    }

    @Override
    public String toString() {
        return "Peak File : " + peakFile + '\t' +
                "File Size : " + fileSize + '\t' +
                "Number Of Peaks : " + numberOfPeaks + '\t' +
                "Number Of detected peaks : " + detectedPsms.size() + '\t' +
                "Number Of undetected peaks : " + undetectedPsms.size();
    }
}
