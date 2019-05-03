package com.ieps.parser.roadRunner

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.UUID

import com.ieps.parser.preprocess.HtmlStripper
import com.typesafe.scalalogging.StrictLogging
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConverters._
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
//    val shuffled = Random.shuffle(documents) // TODO: uncomment this line when roadRunner is done
    val shuffled = documents
    var wrapper = shuffled.head.body()
    val restDocuments = shuffled.tail
    restDocuments.foreach { document =>
      wrapper = expandWrapper(wrapper, document.body())
    }
  }

  def handleEmptyChildren(rootWrapper: Element, rootDocument: Element): Boolean = {
    val wrapperText = rootWrapper.text()
    val documentText = rootDocument.text()
    if (!wrapperText.equals(documentText)) {
      logger.info(s"Content mismatch: `$wrapperText` : `$documentText`")
      false
    } else {
      true
    }
  }

  private def expandWrapper(rootWrapper: Element, rootDocument: Element): Element = {
    // TODO:
    //    1. iterate over tags
    //    2. solve mismatches:
    //      - text mismatches -> data fields OR items
    //      - tag mismatches -> optional items OR iterators (list of items)
    var mismatch = false
    val wrapperChildren = rootWrapper.children().asScala
    val documentChildren = rootDocument.children().asScala
    if (wrapperChildren.length != documentChildren.length) {
//      logger.info(s"Children length mismatch")
      mismatch = true
    }

    if (wrapperChildren.isEmpty && documentChildren.isEmpty) {
      handleEmptyChildren(rootWrapper, rootDocument)
    }

    val zipped = wrapperChildren.zipAll(documentChildren, None, None)
    zipped.foreach {
      case (wrapper: Element, document: Element) =>
        expandWrapper(wrapper, document)
      case (None, document: Element) =>
//        logger.info(s"Missing wrapper, mismatch: $mismatch")
//        logger.info(s"$document") // is optional
      case (wrapper: Element, None) =>
//        logger.info(s"Missing document, mismatch: $mismatch")
//        logger.info(s"$wrapper") // is optional
      case rest => logger.error(s"wut? $rest")
    }

    rootWrapper
  }
}
