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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public class MzIdValidator implements Validator {

    public static final String VERSION = "version";
    private static final String VERSION_1_2_0 = "1.2.0";
    final private File file;
    private List<File> peakFilesFromCmdLine;
    boolean isPeakValidationSkipped = false;

    public static Validator getInstance(CommandLine cmd) throws Exception {
        return new MzIdValidator(cmd);
    }

    private MzIdValidator(CommandLine cmd) throws Exception {

        if (cmd.hasOption(Utility.ARG_MZID)) {
            file = new File(cmd.getOptionValue(Utility.ARG_MZID));
            if (!file.exists()) {
                throw new IOException("The provided file name can't be found -- "
                        + cmd.getOptionValue(Utility.ARG_MZID));
            }
            if (cmd.hasOption(Utility.ARG_SKIP_PEAK_VAL)) {
                isPeakValidationSkipped = true;
            }
        } else {
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

        int numProteins = piaCompiler.getNrAccessions();
        int numPeptides = piaCompiler.getNrPeptides();
        int numPSMs = piaCompiler.getNrPeptideSpectrumMatches();

        ((ResultReport) report).setAssayFile(file.getName());
        ((ResultReport) report).setFileSize(file.length());
        ((ResultReport) report).setNumberOfProteins(numProteins);
        ((ResultReport) report).setNumberOfPeptides(numPeptides);
        ((ResultReport) report).setNumberOfPSMs(numPSMs);
        ((ResultReport) report).setValidSchema(true);

        if (!isPeakValidationSkipped) {
            PeakValidator peakValidator = new PeakValidator(piaCompiler, peakFilesFromCmdLine, report);
            List<PeakReport> peakReports = peakValidator.validate();
            int numPeakFiles = peakReports.size();
            ((ResultReport) report).setNumberOfPeakFiles(numPeakFiles);
            ((ResultReport) report).setPeakReports(peakReports);
        }
        return report;
    }

    private static IReport validateMzidSchema(File mzIdentML) {
        IReport report = new ResultReport();
        try (BufferedReader br = new BufferedReader(new FileReader(mzIdentML))) {
            GenericSchemaValidator genericValidator = new GenericSchemaValidator();
            URL url = getMzidSchemaUrl(mzIdentML);
            if (url == null || url.getPath().length() == 0) {
                throw new IllegalStateException("MzIdentML xsd not found!");
            }
            genericValidator.setSchema(url.toURI());
            ErrorHandlerIface handler = new ValidationErrorHandler();
            genericValidator.setErrorHandler(handler);
            genericValidator.validate(br);
            List<String> errorMessages = handler.getErrorMessages();
            for (String error : errorMessages) {
                report.addException(new IOException(error), ValidationMessage.Type.ERROR);
            }
        } catch (IOException | SAXException | URISyntaxException e) {
            report.addException(e, ValidationMessage.Type.ERROR);
        }
        return report;
    }

    private static URL getMzidSchemaUrl(File mzIdentML) {
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        xmlif.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        URL url = null;
        try (FileReader fileReader = new FileReader(mzIdentML)) {
            XMLStreamReader xmlr = xmlif.createXMLStreamReader(fileReader);
            // move to the root element and check its name.
            xmlr.nextTag();
            int attributeCount = xmlr.getAttributeCount();
            for (int i = 0; i < attributeCount; i++) {
                if (xmlr.getAttributeName(i).toString().equals(VERSION)) {
                    if (xmlr.getAttributeValue(i).equals(VERSION_1_2_0)) {
                        url = MzIdValidator.class.getClassLoader().getResource("mzIdentML1.2.0.xsd");
                    } else {
                        MzIdValidator.class.getClassLoader().getResource("mzIdentML1.1.0.xsd");
                    }
                    break;
                }
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return url;
    }
}