# WIER: Crawler implementation

## Brief description
This is an implementation of a RoadRunner-like algorithm written in [Scala](https://www.scala-lang.org/).

Its purpose is to generate a wrapper dynamically in order to be able to extract data with it.

## Requirements
In order to run the code you need to:

1. Make sure that `sbt` [installed](https://www.scala-sbt.org/0.13/docs/Setup.html) is installed on the machine where the code will be running
2. Make sure that a `outputs` directory exists within the working directory of the executable. (If you are running everything from the root directory you should be fine.)

## Running
To run the code please make sure you have the database set up (previous section) and then run the following command in the root directory: 
```bash
sbt assembly
java -jar ./target/scala-2.12/road_runner-assembly-0.1.jar <domain_name> <path_01.html> <path_02.html>
# example:
java -jar ./target/scala-2.12/road_runner-assembly-0.1.jar overstock.com ../../input/overstock.com/jewelry01.html ../../input/overstock.com/jewelry02.html
```
To stop the execution just send a `SIGTERM` signal in the console (<kbd>CTRL</kbd>/<kbd>CMD</kbd> + <kbd>C</kbd>)

You can lookup the arguments in [args](./args).

It is highly recommended that you set up and run the code through an IDE, such as IntelliJ as the arguments for rtvslo.si and autodiler.me contain whitespace and it is rather annoying to set it up in the terminal.
