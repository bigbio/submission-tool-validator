package uk.ac.ebi.pride.toolsuite.px_validator.validators;

import de.mpc.pia.intermediate.compiler.PIASimpleCompiler;
import de.mpc.pia.intermediate.compiler.parser.InputFileParserFactory;
import org.apache.commons.cli.CommandLine;
import org.xml.sax.SAXException;
import uk.ac.ebi.pride.data.validation.ValidationMessage;
import uk.ac.ebi.pride.tools.ErrorHandlerIface;
import uk.ac.ebi.pride.tools.GenericSchemaValidator;
import uk.ac.ebi.pride.tools.ValidationErrorHandler;
import uk.ac.ebi.pride.toolsuite.px_validator.utils.*;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class MzIdValidator implements Validator{

    private File file;
    private static final String MZID_SCHEMA = "https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/psi-pi/mzIdentML1.1.0.xsd";

    public static Validator getInstance(CommandLine cmd) throws Exception {
        return new MzIdValidator(cmd);
    }

    private MzIdValidator(CommandLine cmd) throws Exception{

        if(cmd.hasOption(Utility.ARG_MZID)){
            file = new File(cmd.getOptionValue(Utility.ARG_MZID));
            if (!file.exists()){
                throw new IOException("The provided file name can't be found -- "
                        + cmd.getOptionValue(Utility.ARG_MZID));
            }
        }else{
            throw new IOException("In order to validate a mzid file the argument -mzid should be provided");
        }
    }

    @Override
    public IReport validate() {

        IReport report = validateMzidSchema(file);

        PIASimpleCompiler piaCompiler = new PIASimpleCompiler();
        piaCompiler.getDataFromFile(file.getName(), file.getAbsolutePath(), null, InputFileParserFactory.InputFileTypes.MZIDENTML_INPUT.getFileTypeShort());
        piaCompiler.buildClusterList();
        piaCompiler.buildIntermediateStructure();

        int numProteins = piaCompiler.getNrAccessions();
        int numPeptides = piaCompiler.getNrPeptides();
        int numPSMs = piaCompiler.getNrPeptideSpectrumMatches();

        ((ResultReport) report).setNumberOfPeptides(numPeptides);
        ((ResultReport) report).setNumberOfProteins(numProteins);
        ((ResultReport) report).setNumberOfPSMs(numPSMs);
        return report;
    }

    private static IReport validateMzidSchema(File mzIdentML) {
        IReport report = new ResultReport();
        try (BufferedReader br = new BufferedReader(new FileReader(mzIdentML))) {
            GenericSchemaValidator genericValidator = new GenericSchemaValidator();
            genericValidator.setSchema(new URI(MzIdValidator.MZID_SCHEMA));
            ErrorHandlerIface handler = new ValidationErrorHandler();
            genericValidator.setErrorHandler(handler);
            genericValidator.validate(br);
            List<String> errorMessages = handler.getErrorMessages();
            for(String error: errorMessages){
                report.addException(new IOException(error), ValidationMessage.Type.ERROR);
            }
        } catch (IOException | SAXException | URISyntaxException e ) {
            report.addException(e, ValidationMessage.Type.ERROR);
        }
        return report;
  }
}
