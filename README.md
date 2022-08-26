# ProteomeXchange Submission Validator

[![Java CI with Maven](https://github.com/bigbio/submission-tool-validator/actions/workflows/maven.yml/badge.svg)](https://github.com/bigbio/submission-tool-validator/actions/workflows/maven.yml)

This tool helps user to validate submissions in the client side before submitting to PRIDE. It is specially useful for Big submissions

## Minimum requirements
* Java 1.8, 64-bit
* A dual-core CPU
* 2+ GB RAM for complex mzIdentML files.

## Instructions
1. [Download](https://github.com/PRIDE-Archive/submission-tool-validator/releases/download/v1.0.0/submission-tool-validator-1.0.0-bin.jar) the tool as a zip archive file.
2. Extract the zip file to a directory.
3. From a terminal / command prompt, navigate to this new extracted directory and execute a command as described under the 'usage' section below.

## Usage

### PX validation.
You can create manually or with our own tool or with the submission tool the _submission.px_ file. This file contains the metadata around the file including title, description.

```
$ java -jar submission-tool-validator-{version}-bin.jar -v -px /path/to/data/submission.px
```

### Validation of mzIdentML and mzTab Files

Validation an mzTab

```
$ java -jar submission-tool-validator-{version}-bin.jar -v -mztab /path/to/data/file.mztab
```

Validation an mzIdentML

```
$ java -jar submission-tool-validator-{version}-bin.jar -v -mzid /path/to/data/file.mzid
$ java -jar submission-tool-validator-{version}-bin.jar -v -mzid /path/to/data/file.mzid -peak /path/to/data/file.mgf 
$ java -jar submission-tool-validator-{version}-bin.jar -v -mzid /path/to/data/file.mzid -peaks /path/to/data/file1.mgf##/path/to/data/file2.mgf
```



## Contact
To get in touch, please either email <pride-support@ebi.ac.uk> or raise an issue on the [issues page](https://github.com/PRIDE-Archive/submission-tool-validator/issues).
