package com.ieps.parser.roadRunner

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.UUID

import com.ieps.parser.preprocess.HtmlStripper
import com.typesafe.scalalogging.StrictLogging
import org.jsoup.nodes.{Document, Element, Node}
import org.jsoup.parser.Tag

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Random

object RoadRunner {
  def apply(baseUrl: String, inputDocuments: List[Document]): RoadRunner = new RoadRunner(baseUrl, inputDocuments)

  trait HtmlProperties
  case object TagMismatch extends HtmlProperties
  case object LengthMismatch extends HtmlProperties
  case object OptionMismatch extends HtmlProperties
  case object TextMismatch extends HtmlProperties
  case object Iterator extends HtmlProperties
}

class RoadRunner(baseUrl: String, inputDocuments: List[Document]) extends StrictLogging {
  import RoadRunner._

  if (inputDocuments.isEmpty) {
    throw new Exception("Document list cannot be empty.")
  }
  val documents: List[Document] = inputDocuments.map(HtmlStripper.strip)
  var wrapper = new Element("html")

  def outputStrippedHtml(): Unit = {
    documents.zipWithIndex.foreach {
      case (document, id) =>
      Files.write(Paths.get(s"outputs/cleaned_$baseUrl-${id + 1}.html"), document.outerHtml().getBytes(StandardCharsets.UTF_8))
    }
    Files.write(Paths.get(s"outputs/wrapper_$baseUrl.html"), wrapper.outerHtml().getBytes(StandardCharsets.UTF_8))
  }

  def run(): Unit = {
//    val shuffled = Random.shuffle(documents) // TODO: uncomment this line when roadRunner is done
    val shuffled = documents
    val referencePage = shuffled.head.body()
    val restDocuments = shuffled.tail
    restDocuments.foreach { document =>
      wrapper = buildWrapper(referencePage, document.body()) // TODO: compare global wrapper with the returned one
    }
  }

  private def compareTag(wrapper: Element, document: Element): Boolean = wrapper.tagName().equals(document.tagName())

  private def compareText(rootWrapper: Element, rootDocument: Element): Option[HtmlProperties] = {
    val wrapperText = rootWrapper.text()
    val documentText = rootDocument.text()
    if (!wrapperText.equals(documentText)) {
      Some(TextMismatch)
    } else {
      None
    }
  }

  private def compareNodes(reference: Element, document: Element): Seq[HtmlProperties] = {
    val mismatch: mutable.MutableList[HtmlProperties] = mutable.MutableList.empty
    if (compareTag(reference, document)) {
      val referenceChildren = reference.children().asScala
      val documentChildren = document.children().asScala
      if (referenceChildren.length != documentChildren.length) {
        mismatch += LengthMismatch
      }

      referenceChildren.headOption.foreach{ head =>
//          logger.info(s"reference children: ${referenceChildren.map(_.tagName())}")
//          logger.info(s"document children: ${documentChildren.map(_.tagName())}")
//          logger.info(s"reference all equal: ${referenceChildren.forall(_.tagName().equals(head.tagName()))}")
//          logger.info(s"document all equal: ${documentChildren.forall(_.tagName().equals(head.tagName()))}")
        if (referenceChildren.tail.forall(_.tagName().equals(head.tagName()))
              && documentChildren.forall(_.tagName().equals(head.tagName()))) {
          mismatch += Iterator
        }
      }

      compareText(reference, document).foreach {textMismatch =>
        if (referenceChildren.isEmpty && documentChildren.isEmpty) {
          logger.info(s"Wrapper: ${reference.text()}")
          logger.info(s"Document: ${document.text()}")
          mismatch += textMismatch
        }
      }

      mismatch
    } else {
      mismatch += TagMismatch
    }
  }

  private def zipChildren(referenceChildren: List[Element], documentChildren: List[Element]): List[(Option[Element], Option[Element])] = {
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

  private def expandWrapper(originalWrapper: Element, newWrapper: Element): Element = {
    originalWrapper
  }

  private def buildWrapper(rootReference: Element, rootDocument: Element): Element = {
    // TODO:
    //    1. iterate over tags
    //    2. solve mismatches:
    //      - text mismatches -> data fields OR items
    //      - tag mismatches -> optional items OR iterators (list of items)
    var wrapper = new Element(rootReference.tagName())
    val htmlProperties: Seq[HtmlProperties] = compareNodes(rootReference, rootDocument)

    if (htmlProperties.intersect(List(LengthMismatch, Iterator)).equals(List(LengthMismatch, Iterator))) wrapper.addClass("data-items") // based on this we now know that we need to generalize a single data item in the foreach below

    if (htmlProperties.contains(TextMismatch)) wrapper.addClass("data")

    val referenceChildren: List[Element] = rootReference.children().asScala.toList
    val documentChildren: List[Element] = rootDocument.children().asScala.toList

    if (htmlProperties.nonEmpty) logger.info(s"Mismatch types: $htmlProperties")

    val zipped = zipChildren(referenceChildren, documentChildren)
    zipped.foreach {
      case (Some(referencePage: Element), Some(document: Element)) =>
//        if (htmlProperties.contains(Iterator)) {
//          wrapper = expandWrapper(wrapper, buildWrapper(referencePage, document))
//        } else {
          wrapper.appendChild(buildWrapper(referencePage, document))
//        }
      case (None, Some(document: Element)) =>
        // TODO: mark child element as optional and add it to the wrapper
        wrapper.addClass("optional")
        logger.info(s"Missing wrapper, htmlPropertieses: $htmlProperties")
//        logger.info(s"$document") // is optional
      case (Some(wrapper: Element), None) =>
        // TODO: mark child element as optional
        wrapper.addClass("optional")
        logger.info(s"Missing document, htmlPropertieses: $htmlProperties")
//        logger.info(s"$wrapper") // is optional
      case rest => logger.error(s"wut? $rest")
    }

    wrapper
  }
}
