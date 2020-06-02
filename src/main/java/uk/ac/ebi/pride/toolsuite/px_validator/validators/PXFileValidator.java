package uk.ac.ebi.pride.toolsuite.px_validator.validators;

import org.apache.commons.cli.CommandLine;
import uk.ac.ebi.pride.data.exception.SubmissionFileException;
import uk.ac.ebi.pride.data.io.SubmissionFileParser;
import uk.ac.ebi.pride.data.model.Submission;
import uk.ac.ebi.pride.data.validation.SubmissionValidator;
import uk.ac.ebi.pride.data.validation.ValidationMessage;
import uk.ac.ebi.pride.data.validation.ValidationReport;
import uk.ac.ebi.pride.toolsuite.px_validator.utils.IReport;
import uk.ac.ebi.pride.toolsuite.px_validator.utils.PXReport;
import uk.ac.ebi.pride.toolsuite.px_validator.utils.Utility;

import java.io.File;
import java.io.IOException;

public class PXFileValidator implements Validator {

    File file;
    private static Validator validator;

    public static Validator getInstance(CommandLine cmd) throws Exception {
        validator = new PXFileValidator(cmd);
        return validator;
    }

    private PXFileValidator(CommandLine cmd) throws Exception{

        if(cmd.hasOption(Utility.ARG_PXFILE)){
            file = new File(cmd.getOptionValue(Utility.ARG_PXFILE));
            if (!file.exists()){
                throw new IOException("The provided file name can't be found -- "
                        + cmd.getOptionValue(Utility.ARG_PXFILE));
            }
        }else{
            throw new IOException("In order to validate a submission.px file the argument -px should be provided");
        }
    }

    @Override
    public IReport validate(){
        IReport report = new PXReport();
        try {
            Submission submission = SubmissionFileParser.parse(file);
            ValidationReport submissionValidator = SubmissionValidator.validateSubmission(submission);
            for(ValidationMessage message: submissionValidator.getMessages()){
                report.addException(new IOException(message.getMessage()), message.getType());
            }

        } catch (SubmissionFileException e) {
            report.addException(e, ValidationMessage.Type.ERROR);

        }
        return report;
    }
}
