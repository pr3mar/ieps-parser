# XPath

## Brief description

This is an implementation of extracting structured data from three different pairs of web pages using Regular expressions written in Python3.  

## Requirements
In order to run the code you need to have `re`, `json` and `sys` modules installed. All three of them should come bundled in the Python installation and no prior installation should be made. 

## Running the code
In order to run the scripts for three different pairs of web pages the following command should be executed with the programs found in `source/regex/` directory and input html files found in `WebPages/regex/`.
```bash
python3 program_name input_html output_file.json
```
Example command to run the program on a `jewelry01.html` from  `overstock.com` web page :
 ```bash
python jewelry.py ../../WebPages/overstock.com/jewelry01.html ../../outputs/regex/overstock.com/jewelry1.json
```