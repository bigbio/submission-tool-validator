package uk.ac.ebi.pride.toolsuite.px_validator.validators;

import org.apache.commons.cli.CommandLine;
import uk.ac.ebi.pride.toolsuite.px_validator.utils.IReport;
import uk.ac.ebi.pride.toolsuite.px_validator.utils.PXReport;

public interface Validator {

    public IReport validate();
}
