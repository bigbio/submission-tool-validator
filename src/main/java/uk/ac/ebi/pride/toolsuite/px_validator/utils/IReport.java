package uk.ac.ebi.pride.toolsuite.px_validator.utils;

import uk.ac.ebi.pride.data.validation.ValidationMessage;

import java.io.Serializable;

public interface IReport extends Serializable {

    public String toString();

    public void addException(Exception e, ValidationMessage.Type code);

}
