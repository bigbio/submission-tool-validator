package uk.ac.ebi.pride.toolsuite.px_validator.utils;

import com.google.common.io.Files;
import de.mpc.pia.intermediate.PeptideSpectrumMatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import uk.ac.ebi.jmzidml.model.mzidml.FileFormat;
import uk.ac.ebi.jmzidml.model.mzidml.SpectraData;
import uk.ac.ebi.pride.utilities.util.Triple;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class Utility {

  public static final String ARG_VALIDATION = "v";
  public static final String ARG_CONVERSION = "c";
  public static final String ARG_MZID = "mzid";
  public static final String ARG_PXFILE = "px";
  public static final String ARG_PEAK = "peak";
  public static final String ARG_PEAKS = "peaks";
  public static final String ARG_SKIP_PEAK_VAL = "skippeakval";
  public static final String ARG_MZTAB = "mztab";
  public static final String ARG_OUTPUTFILE = "outputfile";
  public static final String ARG_INPUTFILE = "inputfile";
  public static final String ARG_OUTPUTTFORMAT = "outputformat";
  public static final String ARG_CHROMSIZES = "chromsizes";
  public static final String ARG_REPORTFILE = "reportfile";
  public static final String ARG_SCHEMA_VALIDATION = "schema";
  public static final String ARG_SCHEMA_ONLY_VALIDATION = "schemaonly";
  public static final String ARG_FAST_VALIDATION = "fastvalidation";
  public static final String STRING_SEPARATOR = "##";

  // peak files
  public static final String MGF_EXT = ".mgf";
  public static final String DTA_EXT = ".dta";
  public static final String MS2_EXT = ".ms2";
  public static final String PKL_EXT = ".pkl";
  public static final String MZXML_EXT = ".mzxml";
  public static final String APL_EXT = ".apl";

  public static final String NOT_AVAILABLE = "N/A";
  public static final String XML_EXT = ".xml";
  public static final String MZML_EXT = ".mzML";
  public static final String MZTAB_EXT = ".mztab";
  public static final String WIFF_EXT = ".wiff";
  public static final String NETCDF_EXT = ".cdf";

  private static final String SIGN = "[+-]";
  public static final String INTEGER = SIGN + "?\\d+";

  /** An enum of the supported spectra file types */
  public static enum SpecFileFormat {
    MZML,
    PKL,
    DTA,
    MGF,
    MZXML,
    MZDATA,
    MS2,
    APL,
    NONE
  }

  public static enum SpecIdFormat {
    MASCOT_QUERY_NUM,
    MULTI_PEAK_LIST_NATIVE_ID,
    SINGLE_PEAK_LIST_NATIVE_ID,
    SCAN_NUMBER_NATIVE_ID,
    MZML_ID,
    MZDATA_ID,
    WIFF_NATIVE_ID,
    SPECTRUM_NATIVE_ID,
    WIFF_MGF_TITLE,
    NONE
  }

  public enum FileType {
    PRIDE,
    MZTAB,
    MZID,
    MGF,
    MS2,
    MZML,
    MZXML,
    DTA,
    PKL,
    APL;

    public static FileType getFileTypeFromPRIDEFileName(String filename) {
      filename = returnUnCompressPath(filename.toLowerCase());
      if (filename.toLowerCase().endsWith("mzid") || filename.toLowerCase().endsWith("mzidentml")) {
        return MZID;
      } else if (filename.toLowerCase().endsWith("mzml")) {
        return MZML;
      } else if (filename.toLowerCase().endsWith("mgf")) {
        return MGF;
      } else if (filename.toLowerCase().endsWith("mzxml")) {
        return MZXML;
      } else if (filename.toLowerCase().endsWith("mztab")) {
        return MZTAB;
      } else if (filename.toLowerCase().endsWith("apl")) {
        return APL;
      } else if (filename.toLowerCase().endsWith(".xml"))
        return PRIDE;

      return null;
    }

    public static FileType getFileTypeFromSpectraData(SpectraData spectraData) {
      FileFormat specFileFormat = spectraData.getFileFormat();
      if (specFileFormat != null) {
        if (specFileFormat.getCvParam().getAccession().equals("MS:1000613")) return DTA;
        if (specFileFormat.getCvParam().getAccession().equals("MS:1001062")) return MGF;
        if (specFileFormat.getCvParam().getAccession().equals("MS:1000565")) return PKL;
        if (specFileFormat.getCvParam().getAccession().equals("MS:1002996")) return APL;
        if (specFileFormat.getCvParam().getAccession().equals("MS:1000584") || specFileFormat.getCvParam().getAccession().equals("MS:1000562"))
          return MZML;
        if (specFileFormat.getCvParam().getAccession().equals("MS:1000566")) return MZXML;
        if (specFileFormat.getCvParam().getAccession().equals("MS:1001466")) return MS2;
        if (specFileFormat.getCvParam().getAccession().equals("MS:1002600")) return PRIDE;
      }
      return null;
    }
  }

  public enum Compress_Type {
    GZIP("gz"),
    ZIP("zip");

    String extension;

    Compress_Type(String extension) {
      this.extension = extension;
    }

    public String getExtension() {
      return extension;
    }
  }

  public static String returnUnCompressPath(String originalPath) {
    if (originalPath.endsWith(Compress_Type.GZIP.extension) || originalPath.endsWith(Compress_Type.ZIP.extension)) {
      return originalPath.substring(0, originalPath.length() - 3);
    }
    return originalPath;
  }

  public static List<Triple<String, SpectraData, Utility.FileType>> combineSpectraControllers(List<File> msIdentMLFiles, List<SpectraData> spectraDataList) {

    String buildPath = "";
    List<Triple<String, SpectraData, Utility.FileType>> spectraFileMap = new ArrayList<>();

    for (File file : msIdentMLFiles) {
      Iterator iterator = spectraDataList.iterator();
      while (iterator.hasNext()) {
        SpectraData spectraData = (SpectraData) iterator.next();
        if (spectraData.getLocation() != null && spectraData.getLocation().toLowerCase().contains(file.getName().toLowerCase())) {
          spectraFileMap.add(new Triple<>(buildPath + file, spectraData,
                  Utility.FileType.getFileTypeFromSpectraData(spectraData)));
        }
      }
    }
    return spectraFileMap;
  }

  public enum ErrorCode {
      ERROR("Major structure error"),
      WARNING("Small error in the structure of the file");

      final String message;

      ErrorCode(String message ) {
        this.message = message;
      }

      public String toString() {
          return message;
      }
  }

  /**
   * Handles exiting unexpectedly from the tool.
   * @param e Caught exception during processing
   */
  public static void exitedUnexpectedly(Exception e) {
    log.error("Exception while processing files: ", e);
    System.exit(-1);
  }

  /**
   * Creates a new temporary file, as a copy of an input file. DeleteOnExit() is set.
   * @param file the source input file to copy from.
   * @return the new temporary file. This may be null if it was not created successfully.
   */
  public static File createNewTempFile(File file) {
    if(file != null) {
      log.info("Trying to creating File: " +  file.getAbsolutePath());
    }else{
      log.info("Creating File: NULL");
    }
    File tempFile = null;
    try {
      assert file != null;
      tempFile = new File (Files.createTempDir(), file.getName());
      File tempParentFile = tempFile.getParentFile();
      tempFile.deleteOnExit();
      tempParentFile.deleteOnExit();
      FileUtils.copyFile(file, tempFile);
    } catch (IOException e) {
      log.error("Problem creating temp fle for: " + file.getPath());
      log.error("Deleting temp file " + tempFile.getName() + ": " + tempFile.delete());
      tempFile = null;
    }
    return tempFile;
  }

  /**
   * Write the output report to a file
   * @param report report
   * @param reportFile  report file
   */
  public static void outputReport(IReport report, File reportFile){
    log.info(report.toString());
    if (reportFile!=null) {
      try {
        log.info("Writing report to: " + reportFile.getAbsolutePath());
        java.nio.file.Files.write(reportFile.toPath(), report.toString().getBytes());
      } catch (IOException ioe) {
        log.error("Problem when writing report file: ", ioe);
      }
    }
  }

  /**
   * Get the real name of the file
   * @param fileName filename
   * @return  real file name
   */
  public static String getRealFileName(String fileName) {
    String name = fileName;

    if (name.contains("/") || name.contains("\\")) {
      String[] parts = name.split("/");
      name = parts[parts.length - 1];
      parts = name.split("\\\\");
      name = parts[parts.length - 1];
    }

    return name;
  }
  /**
   * Spectrum Id format for an specific CVterm accession
   *
   * @param accession CvTerm Accession
   * @return Specific Spectrum Id Format
   */
  public static SpecIdFormat getSpectraDataIdFormat(String accession) {
    if (accession.equals("MS:1001528")) return SpecIdFormat.MASCOT_QUERY_NUM;
    if (accession.equals("MS:1000774")) return SpecIdFormat.MULTI_PEAK_LIST_NATIVE_ID;
    if (accession.equals("MS:1000775")) return SpecIdFormat.SINGLE_PEAK_LIST_NATIVE_ID;
    if (accession.equals("MS:1001530")) return SpecIdFormat.MZML_ID;
    if (accession.equals("MS:1000776")) return SpecIdFormat.SCAN_NUMBER_NATIVE_ID;
    if (accession.equals("MS:1000770")) return SpecIdFormat.WIFF_NATIVE_ID;
    if (accession.equals("MS:1000777")) return SpecIdFormat.MZDATA_ID;
    if (accession.equals(("MS:1000768"))) return SpecIdFormat.SPECTRUM_NATIVE_ID;
    if (accession.equals("MS:1000796")) return SpecIdFormat.WIFF_MGF_TITLE;
    return SpecIdFormat.NONE;
  }

  /**
   * extract or construct the Spectrum Id  from source ID/Spectrum title
   * @param spectraData SpectraData
   * @param psm Peptide Spectrum Match
   * @return Spectrum Id
   */
  public static String getSpectrumId(uk.ac.ebi.jmzidml.model.mzidml.SpectraData spectraData, PeptideSpectrumMatch psm) {
    SpecIdFormat fileIdFormat = getSpectraDataIdFormat(spectraData.getSpectrumIDFormat().getCvParam().getAccession());

    if (fileIdFormat == SpecIdFormat.MASCOT_QUERY_NUM) {
      String rValueStr = psm.getSourceID().replaceAll("query=", "");
      String id = null;
      if (rValueStr.matches(INTEGER)) {
        id = Integer.toString(Integer.parseInt(rValueStr) + 1);
      }
      return id;
    } else if (fileIdFormat == SpecIdFormat.MULTI_PEAK_LIST_NATIVE_ID) {
      String rValueStr = psm.getSourceID().replaceAll("index=", "");
      String id;
      if (rValueStr.matches(INTEGER)) {
        id = Integer.toString(Integer.parseInt(rValueStr) + 1);
        return id;
      }
      return psm.getSourceID();
    } else if (fileIdFormat == SpecIdFormat.SINGLE_PEAK_LIST_NATIVE_ID) {
      return psm.getSourceID().replaceAll("file=", "");
    } else if (fileIdFormat == SpecIdFormat.MZML_ID) {
      return psm.getSourceID().replaceAll("mzMLid=", "");
    } else if (fileIdFormat == SpecIdFormat.SCAN_NUMBER_NATIVE_ID) {
      return psm.getSourceID().replaceAll("scan=", "");
    } else {
      return psm.getSpectrumTitle();
    }
  }

}
