package uk.ac.ebi.pride.toolsuite.px_validator;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.toolsuite.px_validator.utils.IReport;

import java.io.File;
import java.util.*;

import static uk.ac.ebi.pride.toolsuite.px_validator.utils.Utility.*;

/**
 * This is the main class for the tool, which parses cmmand line arguments and starts the approoriate operation of either convering to validating files.
 *
 * @author ypriverol
 */
public class SubmissionToolValidator {

  private static final Logger log = Logger.getLogger(SubmissionToolValidator.class);

  /**
   * Main class that gets run. Parses command line arguments, starts either the converter or validation operations.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    log.info("Starting application....");
    log.info("Program arguments: " + Arrays.toString(args));
    try {
      CommandLine cmd = SubmissionToolValidator.parseArgs(args);
      if (args.length > 0) {
        if (cmd.hasOption(ARG_VALIDATION)) {
          IReport report = Validator.startValidation(cmd);
          if (report != null) {
            if(cmd.hasOption(ARG_REPORTFILE)){
              File outputFile  = cmd.hasOption(ARG_REPORTFILE) ? new File(cmd.getOptionValue(ARG_REPORTFILE)) : null;
              outputReport(report, outputFile);
            }else {
              log.info(report.toString());
            }
          }
        }  else {
          log.error("Did not find validation command from arguments ");
          Arrays.stream(args).forEach(log::error);
        }
      }
    } catch (Exception e) {
      exitedUnexpectedly(e);
    }
  }

  /**
   * This method parses sets up and all the command line arguments to a CommandLine object.
   *
   * @param args the command line arguments.
   * @return a CommandLine object of the parsed command line arguments.
   * @throws ParseException if there are problems parsing the command line arguments.
   */
  private static CommandLine parseArgs(String[] args) throws ParseException{
    Options options = new Options();
    options.addOption(ARG_VALIDATION, false, "start to validate a file");
    options.addOption(ARG_CONVERSION, false, "start to convert a file");
    options.addOption(ARG_PXFILE, true, "submission.px file");
    options.addOption(ARG_MZID, true, "mzid file");
    options.addOption(ARG_PEAK, true, "peak file");
    options.addOption(ARG_PEAKS, true, "peak files");
    options.addOption(ARG_MZTAB, true, "mztab file");
    options.addOption(ARG_OUTPUTFILE, true, "exact output file");
    options.addOption(ARG_OUTPUTTFORMAT, true, "exact output file format");
    options.addOption(ARG_INPUTFILE, true, "exact input file");
    options.addOption(ARG_CHROMSIZES, true, "chrom sizes file");
    options.addOption(ARG_REPORTFILE, true, "report file");
    options.addOption(ARG_SCHEMA_VALIDATION, false, "XML Schema validation");
    options.addOption(ARG_SCHEMA_ONLY_VALIDATION, false, "XML Schema-only validation");
    options.addOption(ARG_FAST_VALIDATION, false, "Fast Validation of MzIdentML files");
    CommandLineParser parser = new DefaultParser();
    return parser.parse(options, args);
  }
}
