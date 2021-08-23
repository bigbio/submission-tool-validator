package uk.ac.ebi.pride.toolsuite.px_validator.validators;


import de.mpc.pia.intermediate.PeptideSpectrumMatch;
import de.mpc.pia.intermediate.compiler.PIASimpleCompiler;
import de.mpc.pia.modeller.PIAModeller;
import uk.ac.ebi.jmzidml.model.mzidml.CvParam;
import uk.ac.ebi.jmzidml.model.mzidml.SpectraData;
import uk.ac.ebi.pride.data.validation.ValidationMessage;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.toolsuite.px_validator.utils.*;
import uk.ac.ebi.pride.utilities.util.Triple;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class PeakValidator {

    final int NUMBER_OF_CHECKS = 10;
    PIASimpleCompiler piaCompiler;
    private List<File> peakFilesFromCmdLine;
    IReport report;

    public PeakValidator(PIASimpleCompiler piaCompiler, List<File> peakFilesFromCmdLine, IReport report) {
        this.piaCompiler = piaCompiler;
        this.peakFilesFromCmdLine = peakFilesFromCmdLine;
        this.report = report;
    }

    /**
     * Validate the Peak files by checking the reference from assay to peak files
     * and check the spectra are matching from the original peak file
     * @return
     */
    public  List<PeakReport> validate(){

        List<PeakReport> reportList =  new ArrayList<>();

        // check peak files are correctly referenced and match with in SpectraData in MzIdentML and the Submission.px
            Map<String, SpectraData> spectrumFilesMap = getSpectraDataMap(piaCompiler);
        List<SpectraData> spectrumFiles = spectrumFilesMap.entrySet().stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        List<String> peakFilesRefError = peakFileReferenceCheck(this.peakFilesFromCmdLine, spectrumFiles);
        if (peakFilesRefError.size() > 0) {
            for (String error : peakFilesRefError) {
                report.addException(new IOException(error), ValidationMessage.Type.ERROR);
            }
        }

        for (Map.Entry<String, SpectraData> entry : spectrumFilesMap.entrySet()){
            System.out.println(getSpectraDataFormat(entry.getValue()));
        }

        Map<String, String> spectraDataIDToOriginalIdMap = piaCompiler.getSpectraDataIDToOriginalIdMap();
        // validate peak files
        Map<Long, PeptideSpectrumMatch> psms = piaCompiler.getAllPeptideSpectrumMatches();
        // group psm by Spectra File
        Map<String, List<PeptideSpectrumMatch>> psmBySpectaDataRef = getPsmBySpectraGroup(psms);
        // select few psms from each spectra file
        Map<String, List<PeptideSpectrumMatch>> selectedPsmBySpectaDataRef = getSelectedPsmsBySpectaDataRef(psmBySpectaDataRef);

        // prepare to read Spectra from the original Spectra File(eg: mgf)
        JmzReaderSpectrumService service = null;
        List<Triple<String, SpectraData, Utility.FileType>> peakRelatedFiles = null;
        try {
            peakRelatedFiles  = Utility.combineSpectraControllers(peakFilesFromCmdLine, spectrumFiles);
            service = JmzReaderSpectrumService.getInstance(peakRelatedFiles);
        } catch (JMzReaderException e) {
            e.printStackTrace();
        } catch (MzXMLParsingException e) {
            e.printStackTrace();
        }

        Spectrum fileSpectrum = null;
        String spectrumFile;

        // check if the selected PSMs are exits in the Spectra File
        if(service != null){
            for (Map.Entry<String, List<PeptideSpectrumMatch>> psmEntry : selectedPsmBySpectaDataRef.entrySet()) {

                PeakReport peakReport = new PeakReport();
                List<Triple<String, SpectraData, Utility.FileType>> currentSpectra = new ArrayList<>();

                // check which spectra file has this psm
                for (Triple<String, SpectraData, Utility.FileType> peakEntry:peakRelatedFiles) {
                    String origSpectraDataId = spectraDataIDToOriginalIdMap.get(peakEntry.getSecond().getId());
                    if(psmEntry.getKey().equals(peakEntry.getSecond().getName()) ||
                            psmEntry.getKey().equals(origSpectraDataId)){
                        currentSpectra.add(peakEntry);
                    }
                }
                if(currentSpectra.size() == 1){
                    spectrumFile = currentSpectra.get(0).getFirst();
                    Utility.SpecIdFormat fileIdFormat = Utility.getSpectraDataIdFormat(currentSpectra.get(0).getSecond().getSpectrumIDFormat().getCvParam().getAccession());

                    File file =  new File(spectrumFile);
                    peakReport.setPeakFile(file.getName());
                    peakReport.setFileSize(file.length());
                    peakReport.setNumberOfPeaks(psms.size());

                    for (PeptideSpectrumMatch psm : psmEntry.getValue()) {
                        System.out.println(psm.getSpectraDataRef() + " --> " + psm.getID());

                        String spectrumId = Utility.getSpectrumId(currentSpectra.get(0).getSecond(), psm);
                        try {
                            if (fileIdFormat != Utility.SpecIdFormat.MULTI_PEAK_LIST_NATIVE_ID) {
                                fileSpectrum = service.getSpectrumById(spectrumFile, spectrumId);
                            } else {
                                fileSpectrum = service.getSpectrumByIndex(spectrumFile, Integer.parseInt(spectrumId));
                            }
                        } catch (JMzReaderException e) {
                            e.printStackTrace();
                        }

                        DecimalFormat df = new DecimalFormat("#.##");
                        if(df.format(fileSpectrum.getPrecursorMZ().floatValue()).equals(df.format(psm.getMassToCharge()))){
                            List<PeptideSpectrumMatch> detectedPsms = peakReport.getDetectedPsms();
                            detectedPsms.add(psm);
                            peakReport.setDetectedPsms(detectedPsms);
                        }else{
                            List<PeptideSpectrumMatch> undetectedPsms = peakReport.getUndetectedPsms();
                            undetectedPsms.add(psm);
                            peakReport.setUndetectedPsms(undetectedPsms);
                        }
                    }
                }else{
                    for (PeptideSpectrumMatch psm:psmEntry.getValue()) {
                        System.out.println("Spectra File cannot be mapped for " + psm.getID() + " -> " + psm.getSourceID());
                        List<PeptideSpectrumMatch> undetectedPsms = peakReport.getUndetectedPsms();
                        undetectedPsms.add(psm);
                        peakReport.setUndetectedPsms(undetectedPsms);
                    }
                }
                reportList.add(peakReport);
            }
        }
        return reportList;
    }

    /**
     * Group PSMs identified from the assay by the Spectra reference
     * @param psms
     * @return
     */
    private Map<String, List<PeptideSpectrumMatch>> getPsmBySpectraGroup(Map<Long, PeptideSpectrumMatch> psms){

        Map<String, List<PeptideSpectrumMatch>> psmBySpectaDataRef = new HashMap<>();

        for (Map.Entry<Long, PeptideSpectrumMatch> entry : psms.entrySet()){
            PeptideSpectrumMatch psm = entry.getValue();
            if(psmBySpectaDataRef.containsKey(psm.getSpectraDataRef())){
                List<PeptideSpectrumMatch> psmList = psmBySpectaDataRef.get(psm.getSpectraDataRef());
                psmList.add(psm);
                psmBySpectaDataRef.put(psm.getSpectraDataRef(), psmList);
            }else{
                List<PeptideSpectrumMatch> newPsmList = new ArrayList<>();
                newPsmList.add(psm);
                psmBySpectaDataRef.put(psm.getSpectraDataRef(), newPsmList);
            }
        }
        return psmBySpectaDataRef;
    }

    /**
     * Selects few PSMs that are referred  from each peak file(s)
     * @param psmBySpectaDataRef all the PSMs from each peak file(s)
     * @return Map of selected PSMs from each peak file(s)
     */
    private Map<String, List<PeptideSpectrumMatch>> getSelectedPsmsBySpectaDataRef(Map<String, List<PeptideSpectrumMatch>> psmBySpectaDataRef){

        Map<String, List<PeptideSpectrumMatch>> selectedPsmBySpectaDataRef = new HashMap<>();
        Random rand = new Random();

        for (Map.Entry<String, List<PeptideSpectrumMatch>> entry : psmBySpectaDataRef.entrySet()){
            System.out.println(entry.getKey() + " has " + entry.getValue().size() + " identified spectra");
            List<PeptideSpectrumMatch> psmList = entry.getValue();

            int numberOfChecks = (entry.getValue().size()>NUMBER_OF_CHECKS)? NUMBER_OF_CHECKS:entry.getValue().size();
            List<PeptideSpectrumMatch> psmsListSelected = new ArrayList<>();
            for(int i=0; i<psmList.size(); i+=(psmList.size()/numberOfChecks)) {
                psmsListSelected.add(psmList.get(i));
            }
            selectedPsmBySpectaDataRef.put(entry.getKey(), psmsListSelected);
        }
        return selectedPsmBySpectaDataRef;
    }

    /**
     * Get the Spectra Data along with their ID
     * @param piaCompiler PIA compiler
     * @return Map of Spectra Data
     */
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


    /**
     * Check the Spectra reference is match with the MzIdentML SpectraData and the Peak files
     * given from the command-line arguments(-peak or -peaks)
     * @param peakFilesFromCmdLine Peak file(s) given from the command-line
     * @param spectraDataInMzIdentML SpectraData from MzIdentML file
     * @return
     */
    private List<String> peakFileReferenceCheck(List<File> peakFilesFromCmdLine, List<SpectraData> spectraDataInMzIdentML){
        List<String> peakFileRefErrors = new ArrayList<>();

        for(File peakFileFromCmdLine: peakFilesFromCmdLine){ // peak files submitted in the command line
            boolean isFound = false;
            for (SpectraData spectraData : spectraDataInMzIdentML) { // spectra data in mzIdentML file
                String filename = Utility.getRealFileName(spectraData.getLocation());
                if(filename.equalsIgnoreCase(peakFileFromCmdLine.getName())){
                    isFound = true;
                    break;
                }
            }
            if(!isFound){
                peakFileRefErrors.add(peakFileFromCmdLine.getName() + " does not found as a reference in MzIdentML");
            }
        }
        return peakFileRefErrors;
    }

    /**
     * This function returns the Spectrum File format for an specific SpectraData ob object
     *
     * @param spectraData The SpectraData object
     * @return the Spectrum File format
     */
    public static Utility.SpecFileFormat getSpectraDataFormat(SpectraData spectraData) {
        CvParam specFileFormat = spectraData.getFileFormat().getCvParam();
        if (specFileFormat != null) {
            if (specFileFormat.getAccession().equals("MS:1000613")) return Utility.SpecFileFormat.DTA;
            if (specFileFormat.getAccession().equals("MS:1001062")) return Utility.SpecFileFormat.MGF;
            if (specFileFormat.getAccession().equals("MS:1000565")) return Utility.SpecFileFormat.PKL;
            if (specFileFormat.getAccession().equals("MS:1002996")) return Utility.SpecFileFormat.APL;
            if (specFileFormat.getAccession().equals("MS:1000584")
                    || specFileFormat.getAccession().equals("MS:1000562"))
                return Utility.SpecFileFormat.MZML;
            if (specFileFormat.getAccession().equals("MS:1000566")) return Utility.SpecFileFormat.MZXML;
            if (specFileFormat.getAccession().equals("MS:1001466")) return Utility.SpecFileFormat.MS2;
        }
        return getDataFormatFromFileExtension(spectraData);
    }

    /**
     * Return the Spectrum File format based on the SpectraData object name
     *
     * @param spectradata SpectraData Object
     * @return Spectrum File Format
     */
    public static Utility.SpecFileFormat getDataFormatFromFileExtension(SpectraData spectradata) {
        Utility.SpecFileFormat fileFormat = Utility.SpecFileFormat.NONE;
        if (spectradata.getLocation() != null) {
            fileFormat = getSpecFileFormatFromLocation(spectradata.getLocation());
        } else if (spectradata.getName() != null) {
            fileFormat = getSpecFileFormatFromLocation(spectradata.getName());
        }
        return fileFormat;
    }

    /**
     * Return the SpectrumFile format for an specific path such as: /myppath/spectrum_file.mgf
     *
     * @param path the specific path
     * @return the SpectrumFile format such as MZXML or PKL
     */
    public static Utility.SpecFileFormat getSpecFileFormatFromLocation(String path) {
        if (path != null && path.length() > 0) {

            if (path.toUpperCase().contains(Utility.MZXML_EXT.toUpperCase())) return Utility.SpecFileFormat.MZXML;
            if (path.toUpperCase().contains(Utility.DTA_EXT.toUpperCase())) return Utility.SpecFileFormat.DTA;
            if (path.toUpperCase().contains(Utility.MGF_EXT.toUpperCase())) return Utility.SpecFileFormat.MGF;
            if (path.toUpperCase().contains(Utility.XML_EXT.toUpperCase())) return Utility.SpecFileFormat.MZDATA;
            if (path.toUpperCase().contains(Utility.MZML_EXT.toUpperCase())) return Utility.SpecFileFormat.MZML;
            if (path.toUpperCase().contains(Utility.APL_EXT.toUpperCase())) return Utility.SpecFileFormat.APL;
            if (path.toUpperCase().contains(Utility.PKL_EXT.toUpperCase())) return Utility.SpecFileFormat.PKL;
            if (path.toUpperCase().contains(Utility.MS2_EXT.toUpperCase())) return Utility.SpecFileFormat.MS2;
        }
        return Utility.SpecFileFormat.NONE;
    }
}
