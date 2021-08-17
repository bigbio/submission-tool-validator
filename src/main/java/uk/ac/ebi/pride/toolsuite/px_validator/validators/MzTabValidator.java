package uk.ac.ebi.pride.toolsuite.px_validator.validators;


import de.mpc.pia.intermediate.compiler.PIASimpleCompiler;
import de.mpc.pia.intermediate.compiler.parser.InputFileParserFactory;
import org.apache.commons.cli.CommandLine;
import uk.ac.ebi.pride.data.validation.ValidationMessage;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;
import uk.ac.ebi.pride.jmztab.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab.utils.errors.MZTabErrorType;
import uk.ac.ebi.pride.toolsuite.px_validator.utils.IReport;
import uk.ac.ebi.pride.toolsuite.px_validator.utils.PeakReport;
import uk.ac.ebi.pride.toolsuite.px_validator.utils.ResultReport;
import uk.ac.ebi.pride.toolsuite.px_validator.utils.Utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MzTabValidator implements Validator{

    private File file;
    private List<File> peakFilesFromCmdLine;
//    PeakValidator peakValidator;

    public static Validator getInstance(CommandLine cmd) throws Exception {
        return new MzTabValidator(cmd);
    }

    private MzTabValidator(CommandLine cmd) throws Exception{

        if(cmd.hasOption(Utility.ARG_MZTAB)){
            file = new File(cmd.getOptionValue(Utility.ARG_MZTAB));
            if (!file.exists()){
                throw new IOException("The provided file name can't be found -- "
                        + cmd.getOptionValue(Utility.ARG_MZTAB));
            }
        }else{
            throw new IOException("In order to validate a mztab file the argument -mztab should be provided");
        }
        peakFilesFromCmdLine = uk.ac.ebi.pride.toolsuite.px_validator.Validator.getPeakFiles(cmd);
    }

    @Override
    public IReport validate() {
        ResultReport report = new ResultReport();
        try {
            MZTabFileParser mzTab = new MZTabFileParser(file, new FileOutputStream(file.getAbsolutePath() + "-mztab-errors.out"));

            for(MZTabError message: mzTab.getErrorList().getErrorList()){
                ValidationMessage.Type errType;

                if(message.getType().getLevel() == MZTabErrorType.Level.Error)
                    errType = ValidationMessage.Type.ERROR;
                else if(message.getType().getLevel() == MZTabErrorType.Level.Warn)
                    errType = ValidationMessage.Type.WARNING;
                else
                    errType = ValidationMessage.Type.INFO;
                report.addException(new IOException(message.getMessage()), errType);
            }

            if(report.getNumErrors() > 0)
                return report;

            PIASimpleCompiler piaCompiler = new PIASimpleCompiler();
            piaCompiler.getDataFromFile(file.getName(), file.getAbsolutePath(), null, InputFileParserFactory.InputFileTypes.MZTAB_INPUT.getFileTypeShort());
            piaCompiler.buildClusterList();
            piaCompiler.buildIntermediateStructure();

//            peakValidator = new PeakValidator(piaCompiler,peakFilesFromCmdLine,report);
//            List<PeakReport> peakReports = peakValidator.validate();

            int numProteins = piaCompiler.getNrAccessions();
            int numPeptides = piaCompiler.getNrPeptides();
            int numPSMs = piaCompiler.getNrPeptideSpectrumMatches();
//            int numPeakFiles = peakReports.size();

            ((ResultReport) report).setAssayFile(file.getName());
            ((ResultReport) report).setFileSize(file.length());
            ((ResultReport) report).setNumberOfPeptides(numPeptides);
            ((ResultReport) report).setNumberOfProteins(numProteins);
            ((ResultReport) report).setNumberOfPSMs(numPSMs);
//            ((ResultReport) report).setNumberOfPeakFiles(numPeakFiles);
//            ((ResultReport) report).setPeakReports(peakReports);
            ((ResultReport) report).setValidSchema(true);

        } catch (IOException e) {
            report.addException(e, ValidationMessage.Type.ERROR);
        }
        return report;
    }
}
