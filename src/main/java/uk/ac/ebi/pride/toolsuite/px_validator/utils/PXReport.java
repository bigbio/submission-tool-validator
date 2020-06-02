package uk.ac.ebi.pride.toolsuite.px_validator.utils;

import uk.ac.ebi.pride.data.validation.ValidationMessage;

public class PXReport extends Report{

    public PXReport() {
    }

    @Override
    public void addException(Exception e, ValidationMessage.Type code) {
        super.addException(e, code);
    }
}
