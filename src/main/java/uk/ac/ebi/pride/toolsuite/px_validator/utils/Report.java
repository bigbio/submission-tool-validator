package uk.ac.ebi.pride.toolsuite.px_validator.utils;

import lombok.NoArgsConstructor;
import uk.ac.ebi.pride.data.validation.ValidationMessage;

import java.util.*;

/**
 * This class provides details of assay file(s) as a validation report for key information,
 * e.g. peptide and protein numbers.
 *
 * @author ypriverol
 */
@NoArgsConstructor
public class Report implements IReport{

    // This map store the errors for each Report
    private final List<Map.Entry<Exception, ValidationMessage.Type>> errors = new ArrayList<>();

    public void addException(Exception exception, ValidationMessage.Type code){
        errors.add(new AbstractMap.SimpleEntry<>(exception, code));
    }

    @Override
    public int getNumErrors() {
        return errors.size();
    }

    @Override
    public String toString() {
        StringBuilder error = new StringBuilder();
        for(Map.Entry er: errors)
            error.append(er.getKey().toString()).append(" == Level Error: ").append(er.getValue()).append(" ==\n");
        if(errors.isEmpty()){
            error.append("Status : Valid").append("\n");
        }else{
            error.append("Status : Invalid").append("\n");
        }
        return error.toString();
    }
}
