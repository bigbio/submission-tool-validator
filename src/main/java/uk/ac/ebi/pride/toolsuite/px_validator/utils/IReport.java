package uk.ac.ebi.pride.toolsuite.px_validator.utils;

import uk.ac.ebi.pride.data.validation.ValidationMessage;

import java.io.Serializable;

public interface IReport extends Serializable {

    String toString();

    void addException(Exception e, ValidationMessage.Type code);

    int getNumErrors();

}
