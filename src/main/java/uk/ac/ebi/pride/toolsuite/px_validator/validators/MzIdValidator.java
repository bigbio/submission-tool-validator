package uk.ac.ebi.pride.toolsuite.px_validator.validators;

import de.mpc.pia.intermediate.compiler.PIASimpleCompiler;
import de.mpc.pia.intermediate.compiler.parser.InputFileParserFactory;
import de.mpc.pia.modeller.PIAModeller;
import org.apache.commons.cli.CommandLine;
import org.xml.sax.SAXException;
import uk.ac.ebi.jmzidml.model.mzidml.SpectraData;
import uk.ac.ebi.pride.data.validation.ValidationMessage;
import uk.ac.ebi.pride.tools.ErrorHandlerIface;
import uk.ac.ebi.pride.tools.GenericSchemaValidator;
import uk.ac.ebi.pride.tools.ValidationErrorHandler;
import uk.ac.ebi.pride.toolsuite.px_validator.utils.*;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MzIdValidator implements Validator{

    private File file;
    private List<File> peakFiles;
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
        peakFiles = uk.ac.ebi.pride.toolsuite.px_validator.Validator.getPeakFiles(cmd);
    }

    @Override
    public IReport validate() {

        IReport report = validateMzidSchema(file);
        if (report.getNumErrors() > 0)
            return report;

        PIASimpleCompiler piaCompiler = new PIASimpleCompiler();
        piaCompiler.getDataFromFile(file.getName(), file.getAbsolutePath(),
                null, InputFileParserFactory.InputFileTypes.MZIDENTML_INPUT.getFileTypeShort());
        piaCompiler.buildClusterList();
        piaCompiler.buildIntermediateStructure();

        // check peak files are correctly referenced and tally with in SpectraData MzIdentML and the Submission.px
        List<SpectraData> spectrumFiles = getSpectraDataMap(piaCompiler).entrySet().stream().map(Map.Entry::getValue)
                .collect(Collectors.toList());
        List<String> peakFilesError = validatePeakFiles(spectrumFiles);
        if (peakFilesError.size() > 0) {
            for (String error : peakFilesError) {
                report.addException(new IOException(error), ValidationMessage.Type.ERROR);
            }
        }

        int numProteins = piaCompiler.getNrAccessions();
        int numPeptides = piaCompiler.getNrPeptides();
        int numPSMs = piaCompiler.getNrPeptideSpectrumMatches();
        int numPeakFiles = spectrumFiles.size();

        ((ResultReport) report).setAssayFile(file.getName());
        ((ResultReport) report).setFileSize(file.length());
        ((ResultReport) report).setNumberOfProteins(numProteins);
        ((ResultReport) report).setNumberOfPeptides(numPeptides);
        ((ResultReport) report).setNumberOfPSMs(numPSMs);
        ((ResultReport) report).setNumberOfPeakFiles(numPeakFiles);
        ((ResultReport) report).setValidSchema(true);
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



    private Map<String, SpectraData> getSpectraDataMap(PIASimpleCompiler piaCompiler){
        PIAModeller piaModeller = null;
        try {
            if (piaCompiler.getAllPeptideSpectrumMatcheIDs() != null
                    && !piaCompiler.getAllPeptideSpectrumMatcheIDs().isEmpty()) {
                File inferenceTempFile = File.createTempFile("assay", ".tmp");
                piaCompiler.writeOutXML(inferenceTempFile);
                piaCompiler.finish();
                piaModeller = new PIAModeller(inferenceTempFile.getAbsolutePath());

                if (inferenceTempFile.exists()) {
                    inferenceTempFile.deleteOnExit();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return piaModeller.getSpectraData();

    }


    private List<String> validatePeakFiles(List<SpectraData> spectraDataFiles){
      List<String> peakFileErrors = new ArrayList<>();

      for(File peakFile: this.peakFiles){
          boolean isFound = false;
          for (SpectraData spectraData : spectraDataFiles) {
              String filename = Utility.getRealFileName(spectraData.getLocation());
              if(filename.toLowerCase().equals(peakFile.getName().toLowerCase())){
                  isFound = true;
                  break;
              }
          }
          if(!isFound){
              peakFileErrors.add(peakFile.getName() + " does not found as a reference in MzIdentML");
          }
      }
      return peakFileErrors;
  }
}
