package uk.ac.ebi.pride.toolsuite.px_validator;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.toolsuite.px_validator.utils.*;
import uk.ac.ebi.pride.toolsuite.px_validator.validators.PXFileValidator;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import static uk.ac.ebi.pride.toolsuite.px_validator.utils.Utility.*;

/**
 * This class validates an input file and produces a plain text report file,
 * and potentially a serialized version of AssayFileSummary as well.
 *
 * @author ypriverol
 */
public class Validator {

  private static final Logger log = LoggerFactory.getLogger(Validator.class);
  private static final String PRIDE_XML_SCHEMA = "http://ftp.pride.ebi.ac.uk/pride/resources/schema/pride/pride.xsd";
  private static final String MZID_SCHEMA = "https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/psi-pi/mzIdentML1.1.0.xsd";
  public static final String SCHEMA_OK_MESSAGE = "XML schema validation OK on: ";
  public static final String MISSING_SPECTRA_ERROR_MESSAGE = "Missing spectra Found. Hint: Please check your results file correctly referenced their peak files!";

  /**
   * This class parses the command line arguments and beings the file validation.
   *
   * @param cmd command line arguments.
   */
  public static IReport startValidation(CommandLine cmd) {
    IReport report = null;
    if (cmd.hasOption(ARG_PXFILE)) {
      try {
        uk.ac.ebi.pride.toolsuite.px_validator.validators.Validator validator = PXFileValidator.getInstance(cmd);
        report = validator.validate();
      } catch (Exception e) {
        e.printStackTrace();
      }

    } else {
      log.error("Unable to validate unknown input file type");
      return null;
    }
    return report;
  }

  /**
   * This method identifies a file's format extension type.
   *
   * @param file the input file.
   * @return the corresponding FileType.
   */
//  private static FileType getFileType(File file) {
//    FileType result;
//    log.info("Checking file type for : " + file);
//    if (PrideXmlControllerImpl.isValidFormat(file)) {
//      result = FileType.PRIDEXML;
//    } else if (MzIdentMLControllerImpl.isValidFormat(file)) {
//      result = FileType.MZID;
//    } else if (MzTabControllerImpl.isValidFormat(file)) {
//      result = FileType.MZTAB;
//    } else {
//      log.error("Unrecognised file type: " + file);
//      result = FileType.UNKNOWN;
//    }
//    return result;
//  }

  /**
   * This method validates an an mzIdentML file.
   *
   * @param cmd the command line arguments.
   */
//  private static Report validateMzIdentML(CommandLine cmd) {
//    File file = new File(cmd.getOptionValue(ARG_MZID));
//    List<File> filesToValidate = getFilesToValidate(file);
//    File mzid = filesToValidate.get(0);
//    List<File> peakFiles = getPeakFiles(cmd);
//    AssayFileSummary assayFileSummary = new AssayFileSummary();
//    Report report = new Report();
//    FileType fileType = getFileType(filesToValidate.get(0));
//    File outputFile  = cmd.hasOption(ARG_REPORTFILE) ? new File(cmd.getOptionValue(ARG_REPORTFILE)) : null;
//    if (fileType.equals(FileType.MZID)) {
//      boolean valid = true; // assume true if not validating schema
//      SchemaCheckResult schemaResult;
//      List<String> schemaErrors = null;
//      if (cmd.hasOption(ARG_SCHEMA_VALIDATION) || cmd.hasOption(ARG_SCHEMA_ONLY_VALIDATION)) {
//        schemaResult = validateMzidSchema(MZID_SCHEMA, mzid);
//        valid = schemaResult.isValidAgainstSchema();
//        schemaErrors = schemaResult.getErrorMessages();
//      }
//      if (valid) {
//        if (cmd.hasOption(ARG_SCHEMA_ONLY_VALIDATION)) {
//          report.setStatusOK();
//        } else {
//          ValidationResult validationResult;
//          if (cmd.hasOption(ARG_FAST_VALIDATION)) {
//            validationResult = validateAssayFile(mzid, FileType.MZID, peakFiles, true);
//          } else {
//            validationResult = validateAssayFile(mzid, FileType.MZID, peakFiles);
//          }
//          report = validationResult.getReport();
//          assayFileSummary = validationResult.getAssayFileSummary();
//        }
//      } else {
//        String message = "ERROR: Supplied -mzid file failed XML schema validation: " + filesToValidate.get(0) +
//            (schemaErrors==null ? "" : String.join(",", schemaErrors));
//        log.error(message);
//        report.setStatus(message);
//      }
//    } else {
//      String message = "ERROR: Supplied -mzid file is not a valid mzIdentML file: " + filesToValidate.get(0);
//      log.error(message);
//      report.setStatus(message);
//    }
//    outputReport(assayFileSummary, report, outputFile, cmd.hasOption(ARG_SKIP_SERIALIZATION));
//    return report;
//  }

  /**
   * This method validates a PRIDE XML file.
   *
   * @param cmd the command line arguments.
   */
//  private static Report validatePrideXML(CommandLine cmd) {
//    List<File> filesToValidate = new ArrayList<>();
//    File file = new File(cmd.getOptionValue(ARG_PRIDEXML));
//    if (file.isDirectory()) {
//      log.error("Unable to validate against directory");
//    } else {
//      filesToValidate.add(file);
//    }
//    filesToValidate = extractZipFiles(filesToValidate);
//    File pridexxml = filesToValidate.get(0);
//    FileType fileType = getFileType(pridexxml);
//    AssayFileSummary assayFileSummary = new AssayFileSummary();
//    Report report = new Report();
//    File outputFile  = cmd.hasOption(ARG_REPORTFILE) ? new File(cmd.getOptionValue(ARG_REPORTFILE)) : null;
//    if (fileType.equals(FileType.PRIDEXML)) {
//      boolean valid = true; // assume true if not validating schema
//      List<String> schemaErrors = null;
//      if (cmd.hasOption(ARG_SCHEMA_VALIDATION) || cmd.hasOption(ARG_SCHEMA_ONLY_VALIDATION)) {
//        SchemaCheckResult schemaCheckResult = validatePridexmlSchema(PRIDE_XML_SCHEMA, pridexxml);
//        valid = schemaCheckResult .isValidAgainstSchema();
//        schemaErrors = schemaCheckResult .getErrorMessages();
//        log.debug("Schema errors: " + String.join(",", schemaErrors));
//      }
//      if (valid ) {
//        if(cmd.hasOption(ARG_SCHEMA_ONLY_VALIDATION)) {
//          report.setStatusOK();
//        } else {
//          ValidationResult validationResult= validateAssayFile(pridexxml, FileType.PRIDEXML, null);
//          report = validationResult.getReport();
//          assayFileSummary = validationResult.getAssayFileSummary();
//        }
//      } else {
//        String message = "ERROR: Supplied -pridexml file failed XML schema validation: " + filesToValidate.get(0) + String.join(",", schemaErrors);
//        log.error(message);
//        report.setStatus(message);
//      }
//    } else {
//      String message = "Supplied -pridexml file is not a valid PRIDE XML file: " + pridexxml.getAbsolutePath();
//      log.error(message);
//      report.setStatus(message);
//    }
//    outputReport(assayFileSummary, report, outputFile, cmd.hasOption(ARG_SKIP_SERIALIZATION));
//    return report;
//  }


  /**
   * This method validates an mzTab file.
   *
   * @param cmd the command line arguments.
   */
//  private static Report validateMzTab(CommandLine cmd) {
//    File file = new File(cmd.getOptionValue(ARG_MZTAB));
//    List<File> filesToValidate = getFilesToValidate(file);
//    List<File> peakFiles = getPeakFiles(cmd);
//    AssayFileSummary assayFileSummary = new AssayFileSummary();
//    Report report = new Report();
//    FileType fileType = getFileType(filesToValidate.get(0));
//    if (fileType.equals(FileType.MZTAB)) {
//      ValidationResult validationResult = validateAssayFile(filesToValidate.get(0), FileType.MZTAB, peakFiles);
//      report = validationResult.getReport();
//      assayFileSummary = validationResult.getAssayFileSummary();
//    } else {
//      String message = "ERROR: Supplied -mztab file is not a valid mzTab file: " + filesToValidate.get(0);
//      log.error(message);
//      report.setStatus(message);
//    }
//    File outputFile  = cmd.hasOption(ARG_REPORTFILE) ? new File(cmd.getOptionValue(ARG_REPORTFILE)) : null;
//    outputReport(assayFileSummary, report, outputFile, cmd.hasOption(ARG_SKIP_SERIALIZATION));
//    return report;
//  }

  /**
   * This method gets all the input file ready for validation, if it is extracted.
   *
   * @param file the input file for validation.
   * @return List of extracted files for validation.
   */
  private static List<File> getFilesToValidate(File file) {
    List<File> filesToValidate = new ArrayList<>();
    if (file.isDirectory()) {
      log.error("Unable to validate against directory of mzid files.");
    } else {
      filesToValidate.add(file);
    }
    filesToValidate = extractZipFiles(filesToValidate);
    return filesToValidate;
  }

  /**
   * This method gets a list of providede peak files.
   *
   * @param cmd the command line arguments.
   * @return List of peak files.
   */
  private static List<File> getPeakFiles(CommandLine cmd) {
    List<File> peakFiles = new ArrayList<>();
    if (cmd.hasOption(ARG_PEAK) || cmd.hasOption(ARG_PEAKS)) {
      String[] peakFilesString = cmd.hasOption(ARG_PEAK) ? cmd.getOptionValues(ARG_PEAK)
          : cmd.hasOption(ARG_PEAKS) ?  cmd.getOptionValue(ARG_PEAKS).split(STRING_SEPARATOR) : new String[0];
      for (String aPeakFilesString : peakFilesString) {
        File peakFile = new File(aPeakFilesString);
        if (peakFile.isDirectory()) {
          File[] listFiles = peakFile.listFiles(File::isFile);
          if (listFiles!=null) {
            peakFiles.addAll(Arrays.asList(listFiles));
          }
        } else {
          peakFiles.add(peakFile);
          log.info("Added peak file: " + peakFile.getPath());
        }
      }
      peakFiles = extractZipFiles(peakFiles);
    } else {
      log.error("Peak file not supplied with mzIdentML file.");
    }
    return peakFiles;
  }

  /**
   * This method extracts an input list of files.
   *
   * @param files a list of input zip files to extract.
   * @return a list of extracted files.
   */
  public static List<File> extractZipFiles(List<File> files) {
    List<File> zippedFiles = findZippedFiles(files);
    if (zippedFiles.size()>0) {
      files.removeAll(zippedFiles);
      files.addAll(unzipFiles(zippedFiles, zippedFiles.get(0).getParentFile().getAbsoluteFile()));
    }
    return files.stream().distinct().collect(Collectors.toList());
  }

  /**
   * ~This method identifies any gzipped files.
   * @param files a list if input files.
   * @return a list of files that are gzipped.
   */
  private static List<File> findZippedFiles(List<File> files) {
    return files.stream().filter(file -> file.getName().endsWith(".gz")).collect(Collectors.toList());
  }

  /**
   * This method extracts a list of iniput gzipped files to an output directory.
   * @param zippedFiles a list of input files to extract.
   * @param outputFolder the output directory.
   * @return a list of files that have been extracted.
   */
  private static List<File> unzipFiles(List<File> zippedFiles, File outputFolder) {
    List<File> unzippedFiles = new ArrayList<>();
    zippedFiles.parallelStream().forEach(inputFile -> {
      try {
        log.info("Unzipping file: " + inputFile.getAbsolutePath());
        FileInputStream fis = null;
        GZIPInputStream gs = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
          fis = new FileInputStream(inputFile);
          gs = new GZIPInputStream(fis);
          String outputFile = outputFolder + File.separator + inputFile.getName().replace(".gz", "");
          fos = new FileOutputStream(outputFile);
          bos = new BufferedOutputStream(fos, 2048);
          byte data[] = new byte[2048];
          int count;
          while ((count = gs.read(data, 0, 2048)) != -1) {
            bos.write(data, 0, count);
          }
          bos.flush();
          bos.close();
          unzippedFiles.add(new File(outputFile));
          log.info("Unzipped file: " + outputFile);
        } finally {
          if (fis != null) {
            fis.close();
          }
          if (gs != null) {
            gs.close();
          }
          if (fos != null) {
            fos.close();
          }
          if (bos != null) {
            bos.close();
          }
        }
      } catch (IOException ioe) {
        log.error("IOException when unzipping files.", ioe);
      }
    });
    return unzippedFiles;
  }

  /**
   * This method writes the report to a specified file, and may also write this as a serialized object.
   *
   * @param assayFileSummary the validation summary of the file.
   * @param report the validation report.
   * @param reportFile the report file to output to.
   * @param skipSerialization true to skip serialized output.
   */
//  private static void outputReport(AssayFileSummary assayFileSummary, Report report, File reportFile, boolean skipSerialization) {
//    log.info(report.toString(assayFileSummary));
//    if (reportFile!=null) {
//      try {
//        log.info("Writing report to: " + reportFile.getAbsolutePath());
//        Files.write(reportFile.toPath(), report.toString(assayFileSummary).getBytes());
//        if (!skipSerialization) {
//          ObjectOutputStream oos = null;
//          FileOutputStream fout;
//          try{
//            String serialFileName = reportFile.getAbsolutePath() + ".ser";
//            log.info("Writing serial summary object to: " + serialFileName);
//            fout = new FileOutputStream(serialFileName);
//            oos = new ObjectOutputStream(fout);
//            oos.writeObject(assayFileSummary);
//          } catch (Exception ex) {
//            log.error("Error while writing assayFileSummary object: " + reportFile.getAbsolutePath() + ".ser", ex);
//          } finally {
//            if(oos  != null){
//              oos.close();
//            }
//          }
//        } else {
//          log.info("Skipping report serialization.");
//        }
//      } catch (IOException ioe) {
//        log.error("Problem when writing report file: ", ioe);
//      }
//    }
//  }

//  /**
//   * This method checks to see if the fragment ions match the spectrum.
//   *
//   * @param fragmentIons the fragment ions.
//   * @param spectrum the spectrum.
//   * @return true if they match, false otherwise.
//   */
//  private static boolean matchingFragmentIons(List<FragmentIon> fragmentIons, Spectrum spectrum) {
//    double[][] massIntensityMap = spectrum.getMassIntensityMap();
//    for (FragmentIon fragmentIon : fragmentIons) {
//      double intensity = fragmentIon.getIntensity();
//      double mz = fragmentIon.getMz();
//      boolean matched = false;
//      for (double[] massIntensity : massIntensityMap) {
//        if (massIntensity[0] == mz && massIntensity[1] == intensity) {
//          matched = true;
//          break;
//        }
//      }
//      if (!matched) {
//        return false;
//      }
//    }
//    return true;
//  }
//
//  /**
//   * This method scans for general metadata.
//   *
//   * @param dataAccessController the input controller to read over.
//   * @param assayFileSummary the assay file summary to output results to.
//   */
//  private static void scanForGeneralMetadata(DataAccessController dataAccessController, AssayFileSummary assayFileSummary) {
//    log.info("Started scanning for general metadata.");
//    String title = dataAccessController.getExperimentMetaData().getName();
//    assayFileSummary.setName(StringUtils.isEmpty(title) || title.contains("no assay title provided") ?
//        dataAccessController.getName() :
//        title);
//    assayFileSummary.setShortLabel(StringUtils.isEmpty(dataAccessController.getExperimentMetaData().getShortLabel()) ?
//        "" :
//        dataAccessController.getExperimentMetaData().getShortLabel());
//    assayFileSummary.addContacts(DataConversionUtil.convertContact(dataAccessController.getExperimentMetaData().getPersons()));
//    ParamGroup additional = dataAccessController.getExperimentMetaData().getAdditional();
//    assayFileSummary.addCvParams(DataConversionUtil.convertAssayGroupCvParams(additional));
//    assayFileSummary.addUserParams(DataConversionUtil.convertAssayGroupUserParams(additional));
//    log.info("Finished scanning for general metadata.");
//  }
//
//  /**
//   * This method scans for instruments metadata.
//   *
//   * @param dataAccessController the input controller to read over.
//   * @param assayFileSummary the assay file summary to output results to.
//   */
//  private static void scanForInstrument(DataAccessController dataAccessController, AssayFileSummary assayFileSummary) {
//    log.info("Started scanning for instruments");
//    Set<Instrument> instruments = new HashSet<>();
//    // check to see if we have instrument configurations in the result file to scan, this isn't always present
//    MzGraphMetaData mzGraphMetaData = null;
//    try {
//      mzGraphMetaData = dataAccessController.getMzGraphMetaData();
//    } catch (Exception e) {
//      log.error("Exception while getting mzgraph instrument data." + e);
//    }
//    if (mzGraphMetaData != null) {
//      Collection<InstrumentConfiguration> instrumentConfigurations = dataAccessController.getMzGraphMetaData().getInstrumentConfigurations();
//      for (InstrumentConfiguration instrumentConfiguration : instrumentConfigurations) {
//        Instrument instrument = new Instrument();
//        // set instrument cv param
//        uk.ac.ebi.pride.archive.repo.param.CvParam cvParam = new uk.ac.ebi.pride.archive.repo.param.CvParam();
//        cvParam.setCvLabel(Constant.MS);
//        cvParam.setName(Utility.MS_INSTRUMENT_MODEL_NAME);
//        cvParam.setAccession(Utility.MS_INSTRUMENT_MODEL_AC);
//        instrument.setCvParam(cvParam);
//        instrument.setValue(instrumentConfiguration.getId());
//        // build instrument components
//        instrument.setSources(new ArrayList<>());
//        instrument.setAnalyzers(new ArrayList<>());
//        instrument.setDetectors(new ArrayList<>());
//        int orderIndex = 1;
//        // source
//        for (InstrumentComponent source : instrumentConfiguration.getSource()) {
//          if (source!=null) {
//            SourceInstrumentComponent sourceInstrumentComponent = new SourceInstrumentComponent();
//            sourceInstrumentComponent.setInstrument(instrument);
//            sourceInstrumentComponent.setOrder(orderIndex++);
//            sourceInstrumentComponent.setInstrumentComponentCvParams(DataConversionUtil.convertInstrumentComponentCvParam(sourceInstrumentComponent, source.getCvParams()));
//            sourceInstrumentComponent.setInstrumentComponentUserParams(DataConversionUtil.convertInstrumentComponentUserParam(sourceInstrumentComponent, source.getUserParams()));
//            instrument.getSources().add(sourceInstrumentComponent);
//          }
//        }
//        // analyzer
//        for (InstrumentComponent  analyzer: instrumentConfiguration.getAnalyzer()) {
//          if (analyzer!=null) {
//            AnalyzerInstrumentComponent analyzerInstrumentComponent = new AnalyzerInstrumentComponent();
//            analyzerInstrumentComponent.setInstrument(instrument);
//            analyzerInstrumentComponent.setOrder(orderIndex++);
//            analyzerInstrumentComponent.setInstrumentComponentCvParams(DataConversionUtil.convertInstrumentComponentCvParam(analyzerInstrumentComponent, analyzer.getCvParams()));
//            analyzerInstrumentComponent.setInstrumentComponentUserParams(DataConversionUtil.convertInstrumentComponentUserParam(analyzerInstrumentComponent, analyzer.getUserParams()));
//            instrument.getAnalyzers().add(analyzerInstrumentComponent);
//          }
//        }
//        // detector
//        for (InstrumentComponent detector : instrumentConfiguration.getDetector()) {
//          if (detector!=null) {
//            DetectorInstrumentComponent detectorInstrumentComponent = new DetectorInstrumentComponent();
//            detectorInstrumentComponent.setInstrument(instrument);
//            detectorInstrumentComponent.setOrder(orderIndex++);
//            detectorInstrumentComponent.setInstrumentComponentCvParams(DataConversionUtil.convertInstrumentComponentCvParam(detectorInstrumentComponent, detector.getCvParams()));
//            detectorInstrumentComponent.setInstrumentComponentUserParams(DataConversionUtil.convertInstrumentComponentUserParam(detectorInstrumentComponent, detector.getUserParams()));
//            instrument.getDetectors().add(detectorInstrumentComponent);
//          }
//        }
//        instruments.add(instrument); //store instrument
//      }
//    } // else do nothing
//    assayFileSummary.addInstruments(instruments);
//    log.info("Finished scanning for instruments");
//  }
//
//  /**
//   * This method scans for software metadata.
//   *
//   * @param dataAccessController the input controller to read over.
//   * @param assayFileSummary the assay file summary to output results to.
//   */
//  private static void scanForSoftware(DataAccessController dataAccessController, AssayFileSummary assayFileSummary) {
//    log.info("Started scanning for software");
//    ExperimentMetaData experimentMetaData = dataAccessController.getExperimentMetaData();
//    Set<Software> softwares = new HashSet<>(experimentMetaData.getSoftwares());
//    Set<uk.ac.ebi.pride.archive.repo.assay.software.Software> softwareSet = new HashSet<>(DataConversionUtil.convertSoftware(softwares));
//    assayFileSummary.addSoftwares(softwareSet);
//    log.info("Finished scanning for software");
//  }
//
//  /**
//   * This method scans for search details metadata.
//   *
//   * @param dataAccessController the input controller to read over.
//   * @param assayFileSummary the assay file summary to output results to.
//   */
//  private static void scanForSearchDetails(DataAccessController dataAccessController, AssayFileSummary assayFileSummary) {
//    log.info("Started scanning for search details");
//    // protein group
//    boolean proteinGroupPresent = dataAccessController.hasProteinAmbiguityGroup();
//    assayFileSummary.setProteinGroupPresent(proteinGroupPresent);
//    Collection<Comparable> proteinIds = dataAccessController.getProteinIds();
//    if (proteinIds != null && !proteinIds.isEmpty()) {
//      Comparable firstProteinId = proteinIds.iterator().next();
//      // protein accession
//      String accession = dataAccessController.getProteinAccession(firstProteinId);
//      assayFileSummary.setExampleProteinAccession(accession);
//      // search database
//      SearchDataBase searchDatabase = dataAccessController.getSearchDatabase(firstProteinId);
//      if (searchDatabase != null) {
//        assayFileSummary.setSearchDatabase(searchDatabase.getName());
//      }
//    }
//    log.info("Finished scanning for search details");
//  }
//
//  /**
//   * This method scans for ReferencedIdentificationController-specific metadata.
//   *
//   * @param referencedIdentificationController the input controller to read over.
//   * @param peakFiles the input related peak files.
//   * @param assayFileSummary the assay file summary to output results to.
//   */
//  private static void scanRefIdControllerpecificDetails(ReferencedIdentificationController referencedIdentificationController, List<File> peakFiles, AssayFileSummary assayFileSummary) {
//    log.info("Started scanning for mzid- or mztab-specific details");
//    Set<PeakFileSummary> peakFileSummaries = new HashSet<>();
//    List<String> peakFileNames = new ArrayList<>();
//    for (File peakFile : peakFiles) {
//      peakFileNames.add(peakFile.getName());
//      String extension = FilenameUtils.getExtension(peakFile.getAbsolutePath());
//      if (MassSpecFileFormat.MZML.toString().equalsIgnoreCase(extension)) {
//        log.info("MzML summary: " + getMzMLSummary(peakFile, assayFileSummary));
//        break;
//      }
//    }
//    List<SpectraData> spectraDataFiles = referencedIdentificationController.getSpectraDataFiles();
//    for (SpectraData spectraDataFile : spectraDataFiles) {
//      String location = spectraDataFile.getLocation();
//      String realFileName = FileUtil.getRealFileName(location);
//      Integer numberOfSpectrabySpectraData = referencedIdentificationController.getNumberOfSpectrabySpectraData(spectraDataFile);
//      peakFileSummaries.add(new PeakFileSummary(realFileName, !peakFileNames.contains(realFileName), numberOfSpectrabySpectraData));
//    }
//    assayFileSummary.addPeakFileSummaries(peakFileSummaries);
//    log.info("Finished scanning for ReferencedIdentificationController-specific details");
//  }
//
//
//  /**
//   * This method checks if a mapped mzML file has chromatograms or not.
//   * @param mappedFile the input mzML file.
//   * @param assayFileSummary the assay file summary to output the result to.
//   * @return true if a mzML has chromatograms, false otherwise.
//   */
//  private static boolean getMzMLSummary(File mappedFile, AssayFileSummary assayFileSummary) {
//    log.info("Getting mzml summary.");
//    MzMLControllerImpl mzMLController = null;
//    boolean result = false;
//    try {
//      mzMLController = new MzMLControllerImpl(mappedFile);
//      if (mzMLController.hasChromatogram()) {
//        assayFileSummary.setChromatogram(true);
//        mzMLController.close();
//        result = true;
//      }
//    } finally {
//      if (mzMLController != null) {
//        log.info("Finished getting mzml summary.");
//        mzMLController.close();
//      }
//    }
//    return result;
//  }
//
//  /**
//   * This method validates an input assay file.
//   *
//   * @param assayFile the input assay file.
//   * @return an array of objects[2]: a Report object and an AssayFileSummary, respectively.
//   */
//  private static ValidationResult validateAssayFile(File assayFile, FileType type, List<File> dataAccessControllerFiles) {
//    File tempAssayFile = createNewTempFile(assayFile);
//    List<File> tempDataAccessControllerFiles = new ArrayList<>();
//    boolean badtempDataAccessControllerFiles = createTempDataAccessControllerFiles(dataAccessControllerFiles, tempDataAccessControllerFiles);
//    log.info("Validating assay file: " + assayFile.getAbsolutePath());
//    log.info("From temp file: " + tempAssayFile.getAbsolutePath());
//    AssayFileSummary assayFileSummary = new AssayFileSummary();
//    Report report = new Report();
//    try {
//      final ResultFileController assayFileController;
//      switch(type) {
//        case MZID :
//          assayFileController = new MzIdentMLControllerImpl(tempAssayFile);
//          assayFileController.addMSController(badtempDataAccessControllerFiles ? dataAccessControllerFiles : tempDataAccessControllerFiles);
//          break;
//        case PRIDEXML :
//          assayFileController = new PrideXmlControllerImpl(tempAssayFile);
//          break;
//        case MZTAB : assayFileController = new MzTabControllerImpl(tempAssayFile);
//          assayFileController.addMSController(badtempDataAccessControllerFiles ? dataAccessControllerFiles : tempDataAccessControllerFiles);
//          break;
//        default : log.error("Unrecognized assay fle type: " + type);
//          assayFileController = new MzIdentMLControllerImpl(tempAssayFile);
//          break;
//      }
//      checkSampleDeltaMzErrorRate(assayFileSummary, assayFileController);
//      report.setFileName(assayFile.getAbsolutePath());
//      assayFileSummary.setNumberOfIdentifiedSpectra(assayFileController.getNumberOfIdentifiedSpectra());
//      assayFileSummary.setNumberOfPeptides(assayFileController.getNumberOfPeptides());
//      assayFileSummary.setNumberOfProteins(assayFileController.getNumberOfProteins());
//      assayFileSummary.setNumberofMissingSpectra(assayFileController.getNumberOfMissingSpectra());
//      assayFileSummary.setNumberOfSpectra(assayFileController.getNumberOfSpectra());
//      if (assayFileSummary.getNumberofMissingSpectra()<1) {
//        validateProteinsAndPeptides(assayFile, assayFileSummary, assayFileController);
//      } else {
//        log.error(MISSING_SPECTRA_ERROR_MESSAGE);
//        report.setStatusError(MISSING_SPECTRA_ERROR_MESSAGE);
//      }
//      scanExtraMetadataDetails(type, dataAccessControllerFiles, assayFileSummary, assayFileController);
//      if (StringUtils.isEmpty(report.getStatus())) {
//        report.setStatusOK();
//      }
//    } catch (NullPointerException e) {
//      log.error("Null pointer Exception when scanning assay file", e);
//      report.setStatusError(e.getMessage());
//    } finally {
//      deleteAllTempFiles(tempAssayFile, tempDataAccessControllerFiles);
//    }
//    return new ValidationResult(assayFileSummary, report);
//  }
//
//
//  /**
//   * This method validates an input assay file. Based on isFastValidation flag, input files will get validated by one of the two approaches.
//   *
//   * @param assayFile the input assay file.
//   * @return an array of objects[2]: a Report object and an AssayFileSummary, respectively.
//   */
//  private static ValidationResult validateAssayFile(File assayFile, FileType type, List<File> dataAccessControllerFiles, boolean isFastValidation) {
//    final int NUMBER_OF_CHECKS = 100;
//    final double DELTA_THRESHOLD = 4.0;
//
//    if (isFastValidation) {
//      File tempAssayFile = createNewTempFile(assayFile);
//      List<File> tempDataAccessControllerFiles = new ArrayList<>();
//      boolean badtempDataAccessControllerFiles =
//              createTempDataAccessControllerFiles(
//                      dataAccessControllerFiles, tempDataAccessControllerFiles);
//      AssayFileSummary assayFileSummary = new AssayFileSummary();
//      Report report = new Report();
//      final FastMzIdentMLController assayFileController;
//      log.info("Validating assay file: " + assayFile.getAbsolutePath());
//      log.info("From temp file: " + tempAssayFile.getAbsolutePath());
//
//      try {
//        if (type.equals(FileType.MZID)) {
//          assayFileController = new FastMzIdentMLController(tempAssayFile);
//          assayFileController.addMSController(badtempDataAccessControllerFiles ? dataAccessControllerFiles : tempDataAccessControllerFiles);
//          assayFileController.doSpectraValidation();
//        } else {
//          throw new NotImplementedException(
//                  "No fast validation implementation for PRIDE XML or MzTAB");
//        }
//        report.setFileName(assayFile.getAbsolutePath());
//        assayFileSummary.setNumberOfIdentifiedSpectra(assayFileController.getNumberOfIdentifiedSpectra());
//        assayFileSummary.setNumberOfPeptides(assayFileController.getNumberOfPeptides());
//        assayFileSummary.setNumberOfProteins(assayFileController.getNumberOfProteins());
//        assayFileSummary.setNumberofMissingSpectra(assayFileController.getNumberOfMissingSpectra());
//        assayFileSummary.setNumberOfSpectra(assayFileController.getNumberOfSpectra());
//        assayFileSummary.setNumberOfUniquePeptides((assayFileController).getNumberOfUniquePeptides());
//        assayFileSummary.setDeltaMzErrorRate((assayFileController).getSampleDeltaMzErrorRate(NUMBER_OF_CHECKS, DELTA_THRESHOLD));
//        assayFileSummary.addPtms(DataConversionUtil.convertAssayPTMs(LightModelsTransformer.transformToCvParam(assayFileController.getIdentifiedUniquePTMs())));
//        assayFileSummary.setSearchDatabase(assayFileController.getSearchDataBases().get(0).getName());
//        assayFileSummary.setExampleProteinAccession("Not Applicable");
//        assayFileSummary.setProteinGroupPresent(assayFileController.hasProteinAmbiguityGroup());
//        if (assayFileSummary.getNumberofMissingSpectra() > 0) {
//          log.error(MISSING_SPECTRA_ERROR_MESSAGE);
//          report.setStatusError(MISSING_SPECTRA_ERROR_MESSAGE);
//        }
//        scanForGeneralMetadata(assayFileController, assayFileSummary);
//        scanForInstrument(assayFileController, assayFileSummary);
//        scanForSoftware(assayFileController, assayFileSummary);
//        if (StringUtils.isEmpty(report.getStatus())) {
//          report.setStatusOK();
//        }
//      } catch (NullPointerException e) {
//        log.error("Null pointer Exception when scanning assay file", e);
//        report.setStatusError(e.getMessage());
//      } finally {
//        deleteAllTempFiles(tempAssayFile, tempDataAccessControllerFiles);
//      }
//      return new ValidationResult(assayFileSummary, report);
//    } else {
//      return validateAssayFile(assayFile, type, dataAccessControllerFiles);
//    }
//  }
//
//
//  /**
//   * Creates temp data access controller files.
//   * @param dataAccessControllerFiles the input data access controller files
//   * @param tempDataAccessControllerFiles the temp data acceess controller files that get created
//   * @return true if all the temp files were created OK, false otherwise
//   */
//  private static boolean createTempDataAccessControllerFiles(List<File> dataAccessControllerFiles, List<File> tempDataAccessControllerFiles) {
//    boolean badtempDataAccessControllerFiles = true;
//    if (CollectionUtils.isNotEmpty(dataAccessControllerFiles)) {
//      for (File dataAccessControllerFile : dataAccessControllerFiles) {
//        File tempDataAccessControllerFile = createNewTempFile(dataAccessControllerFile);
//        if (tempDataAccessControllerFile!=null && 0<tempDataAccessControllerFile.length()) {
//          tempDataAccessControllerFiles.add(tempDataAccessControllerFile);
//        }
//      }
//      badtempDataAccessControllerFiles = CollectionUtils.isEmpty(tempDataAccessControllerFiles) ||
//          tempDataAccessControllerFiles.size()!=dataAccessControllerFiles.size();
//    }
//    return badtempDataAccessControllerFiles;
//  }
//
//  /**
//   * Deletes all the temporary files (assay file, data access controller files).
//   * @param tempAssayFile the temp assay file to be deleted
//   * @param tempDataAccessControllerFiles the temp data access controller files to be deleted
//   */
//  private static void deleteAllTempFiles(File tempAssayFile, List<File> tempDataAccessControllerFiles) {
//    deleteTempFile(tempAssayFile);
//    if (CollectionUtils.isNotEmpty(tempDataAccessControllerFiles)) {
//      for (File dataAccessControllerFile : tempDataAccessControllerFiles) {
//        if (dataAccessControllerFile != null) {
//          deleteTempFile(dataAccessControllerFile);
//        }
//      }
//    }
//  }
//
//  /**
//   * Checks a sampling of the delta m/z error rates.
//   * @param assayFileSummary the assay file summary
//   * @param assayFileController the assay file controller
//   */
//  private static void checkSampleDeltaMzErrorRate(AssayFileSummary assayFileSummary, ResultFileController assayFileController) {
//    final int NUMBER_OF_CHECKS = 10;
//    List<Boolean> randomChecks = new ArrayList<>();
//    IntStream.range(1, NUMBER_OF_CHECKS).sequential().forEach(i -> randomChecks.add(assayFileController.checkRandomSpectraByDeltaMassThreshold(NUMBER_OF_CHECKS, 4.0)));
//    int checkFalseCounts = 0;
//    for (Boolean check : randomChecks) {
//      if (!check) {
//        checkFalseCounts++;
//      }
//    }
//    assayFileSummary.setDeltaMzErrorRate(new BigDecimal(((double) checkFalseCounts / (NUMBER_OF_CHECKS*NUMBER_OF_CHECKS))).setScale(2, RoundingMode.HALF_UP).doubleValue());
//  }
//
//  /**
//   * Scans for extra metadata details.
//   * @param type the filetype
//   * @param dataAccessControllerFiles the data access controller files
//   * @param assayFileSummary the assay file summary
//   * @param assayFileController the assay file controller
//   */
//  private static void scanExtraMetadataDetails(FileType type, List<File> dataAccessControllerFiles, AssayFileSummary assayFileSummary, ResultFileController assayFileController) {
//    scanForGeneralMetadata(assayFileController, assayFileSummary);
//    scanForInstrument(assayFileController, assayFileSummary);
//    scanForSoftware(assayFileController, assayFileSummary);
//    scanForSearchDetails(assayFileController, assayFileSummary);
//    switch(type) {
//      case MZID :
//      case MZTAB :
//        scanRefIdControllerpecificDetails((ReferencedIdentificationController) assayFileController, dataAccessControllerFiles, assayFileSummary);
//        break;
//      default : // do nothing
//        break;
//    }
//  }
//
////  /**
////   * Validates across proteins and peptides for a given assay file
////   * @param assayFile the assay file (e.g. .mzid file)
////   * @param assayFileSummary the assay file summary
////   * @param assayFileController the assay file controller (e.g. for mzIdentML etc).
////   */
////  private static void validateProteinsAndPeptides(File assayFile, AssayFileSummary assayFileSummary, ResultFileController assayFileController) throws NullPointerException {
////    Set<String> uniquePeptides = new HashSet<>();
////    Set<CvParam> ptms = new HashSet<>();
////    for (Comparable proteinId : assayFileController.getProteinIds()) {
////      for (Peptide peptide : assayFileController.getProteinById(proteinId).getPeptides()) {
////        uniquePeptides.add(peptide.getSequence());
////        for (Modification modification : peptide.getModifications()) {
////          for (CvParam cvParam : modification.getCvParams()) {
////            if (StringUtils.isEmpty(cvParam.getCvLookupID())|| StringUtils.isEmpty(cvParam.getAccession()) || StringUtils.isEmpty(cvParam.getName())) {
////              String message = "A PTM CV Param's ontology, accession, or name is not defined properly: " + cvParam.toString() + " in file: " +  assayFile.getPath();
////              log.error(message);
////              throw new NullPointerException(message);
////            }
////            if (cvParam.getCvLookupID().equalsIgnoreCase(Constant.PSI_MOD) || cvParam.getCvLookupID().equalsIgnoreCase(Constant.UNIMOD)) {
////              ptms.add(cvParam);
////            }
////          }
////        }
////      }
////    }
////    List<Boolean> matches = new ArrayList<>();
////    matches.add(true);
////    IntStream.range(
////            1,
////            (assayFileController.getNumberOfPeptides() < 100
////                ? assayFileController.getNumberOfPeptides()
////                : 100))
////        .sequential()
////        .forEach(
////            i -> {
////              Protein protein =
////                  assayFileController.getProteinById(
////                      assayFileController.getProteinIds().stream().findAny().orElse(null));
////              Peptide peptide = null;
////              if (protein != null) {
////                peptide = protein.getPeptides().stream().findAny().orElse(null);
////              } else {
////                log.error("Unable to read a random protein.");
////              }
////              if (peptide != null) {
////                if (peptide.getFragmentation() != null && peptide.getFragmentation().size() > 0 && (peptide.getSpectrum() != null) ) {
////                  if (!matchingFragmentIons(peptide.getFragmentation(), peptide.getSpectrum())) {
////                    matches.add(false);
////                  }
////                }
////              } else {
////                log.error("Unable to read peptide form protein: " + protein.toString());
////              }
////            });
////    assayFileSummary.addPtms(DataConversionUtil.convertAssayPTMs(ptms));
////    assayFileSummary.setSpectrumMatchFragmentIons(matches.size() <= 1);
////    assayFileSummary.setNumberOfUniquePeptides(uniquePeptides.size());
////  }
//
//  /**
//   * Deletes a temporary file
//   * @param tempFile the temp file to be deleted.
//   */
//  private static void deleteTempFile(File tempFile) {
//    log.info("Deleting temp file " + tempFile.getName() + ": " + tempFile.delete());
//  }
//
//  /**
//   * This method validates an input mzIdentML file according to the supplied schema, and writes the output to a file.
//   *
//   * @param schemaLocation the location of the schema
//   * @param mzIdentML the input mzIdentML file.
//   * @param outputFile the output log file with an OK message if there were no errors
//   */
//  private static void validateMzidSchema(String schemaLocation, File mzIdentML, File outputFile) {
//    log.info("Validating mzIdentML XML schema for: " + mzIdentML.getPath() + " using schema: " + schemaLocation);
//    ErrorHandlerIface handler = new ValidationErrorHandler();
//    try (BufferedReader br = new BufferedReader(new FileReader(mzIdentML))) {
//      GenericSchemaValidator genericValidator = new GenericSchemaValidator();
//      genericValidator.setSchema(new URI(schemaLocation));
//      genericValidator.setErrorHandler(handler);
//      genericValidator.validate(br);
//      log.info(SCHEMA_OK_MESSAGE + mzIdentML.getName());
//      if (outputFile!=null) {
//        Files.write(outputFile.toPath(), SCHEMA_OK_MESSAGE.getBytes());
//      }
//    } catch (IOException | SAXException e) {
//      log.error("File Not Found or SAX Exception: ", e);
//    } catch (URISyntaxException usi) {
//      log.error("URI syntax exxception: ", usi);
//    }
//  }
//
//  /**
//   * This method validates an input mzIdentML file according to the supplied schema, and returns the outcome.
//   *
//   * @param schemaLocation the location of the schema
//   * @param mzIdentML the input mzIdentML file.
//   * @return a SchemaCheckResult - if the mzIdentML passed validation, validAgainstSchema will be true. False otherwise, and contains a list of the error messages.
//   */
//  private static SchemaCheckResult validateMzidSchema(String schemaLocation, File mzIdentML) {
//    log.info("Validating mzIdentML XML schema for: " + mzIdentML.getPath() + " using schema: " + schemaLocation);
//    SchemaCheckResult result = new SchemaCheckResult(false, new ArrayList<>());
//    ValidationErrorHandler handler = new ValidationErrorHandler();
//    try (BufferedReader br = new BufferedReader(new FileReader(mzIdentML))) {
//      GenericSchemaValidator genericValidator = new GenericSchemaValidator();
//      genericValidator.setSchema(new URI(schemaLocation));
//      genericValidator.setErrorHandler(handler);
//      genericValidator.validate(br);
//      log.info(SCHEMA_OK_MESSAGE + mzIdentML.getName());
//      List<String> errorMessages = handler.getErrorMessages();
//      result.setValidAgainstSchema(errorMessages.size()<1);
//      result.setErrorMessages(errorMessages);
//    } catch (IOException | SAXException e) {
//      log.error("Problem reading or parsing the file: ", e);
//    } catch (URISyntaxException usi) {
//      log.error("Unable to parse URI syntax: ", usi);
//    }
//    return result;
//  }
//
//  /**
//   *
//   * This method validates an input PRIDE XML file according to the supplied schema, and writes the output to a file.
//   *
//   * @param schemaLocation the location of the schema
//   * @param pridexml the input PRIDE XML file.
//   * @param outputFile the output log file with an OK message if there were no errors
//   */
//  private static void validatePridexmlSchema(String schemaLocation, File pridexml, File outputFile) {
//    log.info("Validating PRIDE XML schema for: " + pridexml.getPath() + " using schema: " + schemaLocation);
//    try {
//      PrideXmlClValidator validator = new PrideXmlClValidator();
//      validator.setSchema(new URL(schemaLocation));
//      BufferedReader br = new BufferedReader(new FileReader(pridexml));
//      XMLValidationErrorHandler xveh = validator.validate(br);
//      final String ERROR_MESSAGES = xveh.getErrorsFormattedAsPlainText();
//      if (StringUtils.isEmpty(ERROR_MESSAGES)) {
//        log.info(SCHEMA_OK_MESSAGE + pridexml.getName());
//        if (outputFile!=null) {
//          Files.write(outputFile.toPath(), SCHEMA_OK_MESSAGE.getBytes());
//        }
//      } else {
//        log.error(ERROR_MESSAGES);
//        if (outputFile!=null) {
//          Files.write(outputFile.toPath(), ERROR_MESSAGES.getBytes());
//        }
//      }
//    } catch (IOException | SAXException e) {
//      log.error("File Not Found or SAX Exception: ", e);
//    } catch (Exception e) {
//      log.error("Exception while validating PRIDE XML schema:", e);
//    }
//  }
//
//  /**
//   * This method validates an input mzIdentML file according to the supplied schema, and returns the outcome.
//   *
//   * @param schemaLocation the location of the schema
//   * @param pridexml the input PRIDE XML file.
//   * @return a list of two elements: the first element is a boolean (true or false) if the file passed validation. If false, the 2nd element in the list of the error messages.
//   */
//  private static SchemaCheckResult validatePridexmlSchema(String schemaLocation, File pridexml) {
//    log.info("Validating PRIDE XML schema for: " + pridexml.getPath() + " using schema: " + schemaLocation);
//    SchemaCheckResult result = new SchemaCheckResult(false, new ArrayList<>());
//    try {
//      PrideXmlClValidator validator = new PrideXmlClValidator();
//      validator.setSchema(new URL(schemaLocation));
//      BufferedReader br = new BufferedReader(new FileReader(pridexml));
//      XMLValidationErrorHandler xveh = validator.validate(br);
//      final String ERROR_MESSAGES = xveh.getErrorsFormattedAsPlainText();
//      result.setValidAgainstSchema(StringUtils.isEmpty(ERROR_MESSAGES));
//      if (StringUtils.isEmpty(ERROR_MESSAGES)) {
//        log.info(SCHEMA_OK_MESSAGE + pridexml.getName());
//      } else {
//        log.error(ERROR_MESSAGES);
//        result.setErrorMessages(xveh.getErrorsAsList());
//      }
//    } catch (Exception e) {
//      log.error("Exception while validating PRIDE XML schema:", e);
//    }
//    return result;
//  }
}





