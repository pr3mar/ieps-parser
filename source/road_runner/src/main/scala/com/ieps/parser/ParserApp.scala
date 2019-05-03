package com.ieps.parser

import java.io.File

import com.ieps.parser.roadRunner.RoadRunner
import com.typesafe.scalalogging.StrictLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object ParserApp extends App with StrictLogging {

  if (args.isEmpty || args.length < 3) {
    logger.error(
      """
        | Not enough arguments provided.
        | Please provide:
        | - base url for both HTML pages
        | - absolute paths to 2 HTML pages from the same base URL
        |
        | Example call:
        | ./roadRunner <baseUrl> <path 1> <path 2> ... <path N>
      """.stripMargin)
    System.exit(-1)
  }

  val baseUrl: String = args.head
  val inputFiles: List[Document] = args.tail.map { arg =>
    Jsoup.parse(new File(arg), "UTF-8", baseUrl)
  }.toList

  Utils.time("RoadRunner"){
    val roadRunner = RoadRunner(inputFiles)
    roadRunner.run()
//    roadRunner.outputStrippedHtml()
  }
}
