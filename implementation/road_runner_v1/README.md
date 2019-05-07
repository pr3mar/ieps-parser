# RoadRunner v1

## Brief description

This is an implementation of extracting structured data from three different pairs of web pages using RoadRunner algorithm written in Python3.  

## Requirements
In order to run the code you need to have `re`, `BeautifulSoup` and `sys` modules installed. `Sys` and `re` should come bundled in the Python installation and no prior installation for them should be made.
While the `BeautifulSoup` you can install in the following way on linux platforms using system package manager, e.g. apt-get on Debian/Ubuntu: 
```bash
sudo apt-get install python3-bs4
```
## Running the code
In order to run the scripts for three different pairs of web pages the following command should be executed with the program.
```bash
python3	program_name input_html1 input_html2 output_file
```
Example command to run the program on a `jewelry01.html` and `jewelry02.html` from  `overstock.com` web page:
 ```bash
python roadrunner1.py ../../WebPages/overstock.com/jewelry01.html ../../WebPages/overstock.com/jewelry02.html jewelryOutput.txt
```