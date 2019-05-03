package com.ieps.parser.roadRunner

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.UUID

import com.ieps.parser.preprocess.HtmlStripper
import com.typesafe.scalalogging.StrictLogging
import org.jsoup.nodes.{Document, Element}

import scala.util.Random

object RoadRunner {
  def apply(inputDocuments: List[Document]): RoadRunner = new RoadRunner(inputDocuments)
}

class RoadRunner(inputDocuments: List[Document]) extends StrictLogging {
  if (inputDocuments.isEmpty) {
    throw new Exception("Document list cannot be empty.")
  }
  val documents: List[Document] = inputDocuments.map(HtmlStripper.strip)

  def outputStrippedHtml(): Unit = {
    documents.foreach { document =>
      Files.write(Paths.get(s"outputs/${UUID.randomUUID()}.html"), document.outerHtml().getBytes(StandardCharsets.UTF_8))
    }
  }

  def run(): Unit = {
    val shuffled = Random.shuffle(documents)
    var wrapper = shuffled.head.body()
    val restDocuments = shuffled.tail
    restDocuments.foreach { document =>
      wrapper = expandWrapper(wrapper, document.body())
    }
  }

  private def expandWrapper(wrapper: Element, document: Element): Element = {
    // TODO:
    //    1. iterate over tags
    //    2. solve mismatches:
    //      - text mismatches -> data fields OR items
    //      - tag mismatches -> optional items OR iterators (list of items)
    wrapper
  }
}
