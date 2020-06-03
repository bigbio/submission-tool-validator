package uk.ac.ebi.pride.toolsuite.px_validator.utils;

/**
 * This class describes the summary information of an assay file.
 */
public class ResultReport extends Report {

    private int numberOfProteins;
    private int numberOfPeptides;
    private int numberOfPSMs;

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

    @Override
    public String toString() {
        return super.toString() + "\nIdentification results Report: \nNumber of reported proteins  -- " + numberOfProteins + "\n" +
                "Number of reported peptides  -- " + numberOfPeptides + "\n" +
                "Number of reported PSMs  -- " + numberOfPSMs + "\n";
    }
}
