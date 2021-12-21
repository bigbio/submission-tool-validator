package uk.ac.ebi.pride.toolsuite.px_validator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import uk.ac.ebi.pride.toolsuite.px_validator.utils.IReport;

import java.io.File;
import java.util.*;

import static uk.ac.ebi.pride.toolsuite.px_validator.utils.Utility.*;

/**
 * This is the main class for the tool, which parses command line arguments and starts the appropriate operation of either converting to validating files.
 *
 * @author ypriverol
 */
@Slf4j
public class SubmissionToolValidator {


  /**
   * Main class that gets run. Parses command line arguments, starts either the converter or validation operations.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    log.info("Starting application....");
    log.info("Program arguments: " + Arrays.toString(args));
    log.info("\nFree Memory: " + (Runtime.getRuntime().freeMemory()/1024/1024) + "MB" +
            "\nTotal Memory: " + (Runtime.getRuntime().totalMemory()/1024/1024) + "MB" +
            "\nMax Heap Memory: " + (Runtime.getRuntime().maxMemory()/1024/1024) + "MB");

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
    options.addOption(ARG_SKIP_PEAK_VAL, false, "skip peak file validations");
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
