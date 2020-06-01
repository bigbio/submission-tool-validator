# ProteomeXchange Submission Validator

This tool helps user to validate submissions in the client side before submitting to PRIDE. It is specially useful for Big submissions

## Minimum requirements
* Java 1.8, 64-bit
* A dual-core CPU
* 2+ GB RAM for complex mzIdentML files.

## Instructions
1. [Download](--soon--) the tool as a zip archive file.
2. Extract the zip file to a directory.
3. From a terminal / command prompt, navigate to this new extracted directory and execute a command as described under the 'usage' section below.

## Usage

### PX validation.
You can create manually or with our own tool or with the submission tool the _submission.px_ file. This file contains the metadata around the file including title, description.

```
$ java -jar submission-tool-validator-1.0.0-bin.jar -v -px /path/to/data/submission.px 
```

## Contact
To get in touch, please either email <pride-support@ebi.ac.uk> or raise an issue on the [issues page](https://github.com/PRIDE-Toolsuite/PGConverter/issues).
