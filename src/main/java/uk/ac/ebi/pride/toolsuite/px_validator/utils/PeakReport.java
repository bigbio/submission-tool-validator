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
@Getter
@Setter
@NoArgsConstructor
public class PeakReport extends Report{

    private String peakFile;
    private long fileSize;
    private int numberOfPeaks;
    private List<PeptideSpectrumMatch> detectedPsms = new ArrayList<>();
    private List<PeptideSpectrumMatch> undetectedPsms = new ArrayList<>();

    @Override
    public String toString() {
        return "Peak File : " + peakFile + '\t' +
                "File Size : " + fileSize + '\t' +
                "Number Of Peaks : " + numberOfPeaks + '\t' +
                "Number Of detected peaks : " + detectedPsms.size() + '\t' +
                "Number Of undetected peaks : " + undetectedPsms.size();
    }
}
