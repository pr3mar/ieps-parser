# XPath
## Brief description


This is an implementation of extracting structured data from three different pairs of web pages using XML path language - 'XPATH' written in Python3.  
It is using 'LXML' library that offers support for 'XPATH'.

## Requirenments
In order to run the code you need to have 'lxml' library installed which can be installed via :
1. On linux platforms lxml cane be installed using system package manager, e.g. apt-get on Debian/Ubuntu: 
```bash
sudo apt-get install python3-lxml
```
2.On MacOS-X, a macport of lxml is available and can be installed via:
```bash
sudo port install py27-lxml 
```

## Running the code
In order to run the scripts for three different pairs of web pages the following command should be executed with the programs found in 'source/xpath' directory and input html files found in 'WebPages/xpath/name_of_selected_page'. 
```bash
python program_name input_html output_file.json
```

