package uk.ac.ebi.pride.toolsuite.px_validator.validators;

import de.mpc.pia.intermediate.compiler.PIASimpleCompiler;
import de.mpc.pia.intermediate.compiler.parser.InputFileParserFactory;
import org.apache.commons.cli.CommandLine;
import org.xml.sax.SAXException;
import uk.ac.ebi.pride.data.validation.ValidationMessage;
import uk.ac.ebi.pride.tools.ErrorHandlerIface;
import uk.ac.ebi.pride.tools.GenericSchemaValidator;
import uk.ac.ebi.pride.tools.ValidationErrorHandler;
import uk.ac.ebi.pride.toolsuite.px_validator.utils.IReport;
import uk.ac.ebi.pride.toolsuite.px_validator.utils.PeakReport;
import uk.ac.ebi.pride.toolsuite.px_validator.utils.ResultReport;
import uk.ac.ebi.pride.toolsuite.px_validator.utils.Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public class MzIdValidator implements Validator{

    final private File file;
    private List<File> peakFilesFromCmdLine;
    PeakValidator peakValidator;

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
        peakFilesFromCmdLine = uk.ac.ebi.pride.toolsuite.px_validator.Validator.getPeakFiles(cmd);
    }

    @Override
    public IReport validate() {

        IReport report = validateMzidSchema(file);
        ((ResultReport) report).setAssayFile(file.getName());
        ((ResultReport) report).setFileSize(file.length());
        if (report.getNumErrors() > 0)
            return report;

        PIASimpleCompiler piaCompiler = new PIASimpleCompiler();
        piaCompiler.getDataFromFile(file.getName(), file.getAbsolutePath(),
                null, InputFileParserFactory.InputFileTypes.MZIDENTML_INPUT.getFileTypeShort());
        piaCompiler.buildClusterList();
        piaCompiler.buildIntermediateStructure();

        peakValidator = new PeakValidator(piaCompiler,peakFilesFromCmdLine,report);
        List<PeakReport> peakReports = peakValidator.validate();

        int numProteins = piaCompiler.getNrAccessions();
        int numPeptides = piaCompiler.getNrPeptides();
        int numPSMs = piaCompiler.getNrPeptideSpectrumMatches();
        int numPeakFiles = peakReports.size();

        ((ResultReport) report).setAssayFile(file.getName());
        ((ResultReport) report).setFileSize(file.length());
        ((ResultReport) report).setNumberOfProteins(numProteins);
        ((ResultReport) report).setNumberOfPeptides(numPeptides);
        ((ResultReport) report).setNumberOfPSMs(numPSMs);
        ((ResultReport) report).setNumberOfPeakFiles(numPeakFiles);
        ((ResultReport) report).setPeakReports(peakReports);
        ((ResultReport) report).setValidSchema(true);
        return report;
    }

    private static IReport validateMzidSchema(File mzIdentML) {
        IReport report = new ResultReport();
        try (BufferedReader br = new BufferedReader(new FileReader(mzIdentML))) {
            GenericSchemaValidator genericValidator = new GenericSchemaValidator();
            URL url =  MzIdValidator.class.getClassLoader().getResource("mzIdentML1.1.0.xsd");
            if (url == null || url.getPath().length() == 0) {
                throw new IllegalStateException("MzIdentML1.1.0.xsd not found!");
            }
            genericValidator.setSchema(url.toURI());
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
