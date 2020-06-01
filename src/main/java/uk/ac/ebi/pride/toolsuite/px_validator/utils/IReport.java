package uk.ac.ebi.pride.toolsuite.px_validator.utils;

import java.io.Serializable;

public interface IReport extends Serializable {

    public String toString();

    public void addException(Exception e, Utility.ErrorCode code);

}
