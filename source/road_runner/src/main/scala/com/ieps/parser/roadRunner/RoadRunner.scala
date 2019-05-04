package com.ieps.parser.roadRunner

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.UUID

import com.ieps.parser.preprocess.HtmlStripper
import com.typesafe.scalalogging.StrictLogging
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Random

object RoadRunner {
  def apply(inputDocuments: List[Document]): RoadRunner = new RoadRunner(inputDocuments)

  trait Mismatch
  case object TagMismatch extends Mismatch
  case object LengthMismatch extends Mismatch
  case object OptionMismatch extends Mismatch
  case object TextMismatch extends Mismatch
}

class RoadRunner(inputDocuments: List[Document]) extends StrictLogging {
  import RoadRunner._

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

  private def compareTag(wrapper: Element, document: Element): Boolean = wrapper.tagName().equals(document.tagName())

  private def compareText(rootWrapper: Element, rootDocument: Element): Option[Mismatch] = {
    val wrapperText = rootWrapper.text()
    val documentText = rootDocument.text()
    if (!wrapperText.equals(documentText)) {
      Some(TextMismatch)
    } else {
      None
    }
  }

  private def compareNodes(reference: Element, document: Element): List[Mismatch] = {
    var mismatch: List[Mismatch] = Nil
    if (compareTag(reference, document)) {
      val referenceChildren = reference.children().asScala
      val documentChildren = document.children().asScala
      if (referenceChildren.length != documentChildren.length) {
        mismatch = LengthMismatch :: mismatch
      }

      compareText(reference, document) match {
        case Some(textMismatch) =>
          if (referenceChildren.isEmpty && documentChildren.isEmpty) {
            logger.info(s"Wrapper: ${reference.text()}")
            logger.info(s"Document: ${document.text()}")
          }
          mismatch = textMismatch :: mismatch
        case None =>
      }

      mismatch
    } else {
      TagMismatch :: mismatch
    }
  }

  def zipChildren(referenceChildren: List[Element], documentChildren: List[Element]): List[(Option[Element], Option[Element])] = {
    (referenceChildren, documentChildren) match {
      case (List(), List()) => Nil
      case (List(), documentHead::documentTail) => (None, Some(documentHead)) :: zipChildren(Nil, documentTail)
      case (wrapperHead::wrapperTail, List()) => (Some(wrapperHead), None) :: zipChildren(Nil, wrapperTail)
      case (wrapperHead::wrapperTail, documentHead::documentTail) =>
        if (compareTag(wrapperHead, documentHead)) {
          (Some(wrapperHead), Some(documentHead)) :: zipChildren(wrapperTail, documentTail)
        } else if(referenceChildren.length >= documentChildren.length) {
          (Some(wrapperHead), None) :: zipChildren(wrapperTail, documentHead::documentTail)
        } else {
          (None, Some(documentHead)) :: zipChildren(wrapperHead::wrapperTail, documentTail)
        }
    }
  }

  private def expandWrapper(rootReference: Element, rootDocument: Element): Element = {
    // TODO:
    //    1. iterate over tags
    //    2. solve mismatches:
    //      - text mismatches -> data fields OR items
    //      - tag mismatches -> optional items OR iterators (list of items)
    val htmlMismatches: List[Mismatch] = compareNodes(rootReference, rootDocument)

    val referenceChildren: List[Element] = rootReference.children().asScala.toList
    val documentChildren: List[Element] = rootDocument.children().asScala.toList

//    if (htmlMismatches.nonEmpty) logger.info(s"Mismatch types: $htmlMismatches")

    val zipped = zipChildren(referenceChildren, documentChildren)
//    logger.info(s"Zipped: ${zipped.map(el => s"(${el._1.map(_.tagName())}, ${el._2.map(_.tagName())})")}")
    zipped.foreach {
      case (Some(wrapper: Element), Some(document: Element)) =>
        expandWrapper(wrapper, document)
      case (None, Some(document: Element)) =>
        // TODO: mark element as optional and add it to the wrapper
        logger.info(s"Missing wrapper, htmlMismatches: $htmlMismatches")
//        logger.info(s"$document") // is optional
      case (Some(wrapper: Element), None) =>
        // TODO: mark element as optional
        logger.info(s"Missing document, htmlMismatches: $htmlMismatches")
//        logger.info(s"$wrapper") // is optional
      case rest => logger.error(s"wut? $rest")
    }

    rootReference
  }
}
