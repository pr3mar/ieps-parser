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
  case object LinkMismatch extends HtmlProperties
  case object ImageMismatch extends HtmlProperties
  case object DataMismatch extends HtmlProperties
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

  private def compareTag(reference: Element, document: Element): Boolean = reference.tagName().equals(document.tagName())

  private def compareText(reference: Element, document: Element): Option[HtmlProperties] = {
    val referenceText = reference.text()
    val documentText = document.text()
    if (!referenceText.equals(documentText)) {
      Some(TextMismatch)
    } else {
      None
    }
  }

  private def compareAttributes(reference: Element, rootDocument: Element, attribute: String, returnVal: HtmlProperties): Option[HtmlProperties] = {
    val referenceHref = reference.attr(attribute)
    val documentHref = rootDocument.attr(attribute)
    if (!referenceHref.equals(documentHref)) {
      Some(returnVal)
    } else {
      None
    }
  }

  private def compareImages(reference: Element, document: Element): Option[HtmlProperties] = {
    if (reference.tagName().equals("img")) {
      compareAttributes(reference, document, "alt", ImageMismatch)
    } else {
      None
    }
  }

  private def compareLinks(reference: Element, document: Element): Option[HtmlProperties] = {
    if (reference.tagName().equals("a")) {
      compareAttributes(reference, document, "href", LinkMismatch)
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
        if (referenceChildren.tail.forall(_.tagName().equals(head.tagName()))
              && documentChildren.forall(_.tagName().equals(head.tagName()))) {
          mismatch += Iterator
        }
      }

      compareText(reference, document).foreach {textMismatch =>
        if (referenceChildren.isEmpty && documentChildren.isEmpty) {
          logger.info(s"Wrapper: ${reference.text()}")
          logger.info(s"Document: ${document.text()}")
          mismatch += DataMismatch
        } else {
          mismatch += textMismatch
        }
      }

      compareLinks(reference, document).foreach { linkMismatch =>
        mismatch += linkMismatch
      }

      compareImages(reference, document).foreach { imageMismatch =>
        logger.info(s"image mismatch: $reference/$document")
        mismatch += imageMismatch
      }

      mismatch
    } else {
      mismatch += TagMismatch
    }
  }

  private def handleIterator(iteratorElements: List[(Option[Element], Option[Element])]): Element = {
    var generalItem = new Element(iteratorElements.head._1.get.tagName())
    generalItem.addClass("data-item")
//    iteratorElements.foreach {
//      case (Some(referencePage: Element), Some(document: Element)) =>
//        generalItem.appendChild(buildWrapper(referencePage, document))
//
//      case (None, Some(document: Element)) =>
//        generalItem = expandWrapper(generalItem, document.clone())
//
//      case (Some(referencePage: Element), None) =>
//        generalItem = expandWrapper(generalItem, referencePage.clone())
//
//      case rest => logger.error(s"wut? $rest")
//    }

    generalItem
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

  private def generalizeNode(element: Element): Element = {
    element.addClass("optional")
    element.tagName match {
      case "a" =>
        element.attr("href", "$link")
        element.text("$link-data")
      case "img" =>
        element.attr("src", "$img-link")
        element.attr("alt", "$img-alt")
      case _ if !element.children().isEmpty =>
        element.children().asScala.toList.foreach {
          element: Element =>
            generalizeNode(element)
        }
        element
      case _ if element.children().isEmpty =>
        element.addClass("data")
        element.text("$data")
        element
    }
  }

  private def expandWrapper(originalWrapper: Element, newWrapper: Element): Element = {
    // TODO: finish this, needs to generalize the wrapper
    if (!compareTag(originalWrapper, newWrapper)) {
      originalWrapper.appendChild(generalizeNode(newWrapper))
      originalWrapper
    } else if (compareText(originalWrapper, newWrapper).isEmpty) {
      originalWrapper
    } else {
      val wrapper = new Element(originalWrapper.tagName())
      zipChildren(originalWrapper.children().asScala.toList, newWrapper.children().asScala.toList).foreach {
        case (Some(origWrapper: Element), Some(newWrap: Element)) =>
          wrapper.appendChild(expandWrapper(origWrapper, newWrap))

        case (None, Some(newWrap: Element)) =>
          // TODO: mark child element as optional and add it to the wrapper
          newWrap.addClass("optional")
          wrapper.appendChild(newWrap)

        case (Some(origWrapper: Element), None) =>
          // TODO: mark child element as optional
          origWrapper.addClass("optional")
          wrapper.appendChild(origWrapper)

        case rest => logger.error(s"[expand] wut? $rest")
      }
      wrapper
    }
  }

  private def buildWrapper(rootReference: Element, rootDocument: Element): Element = {
    // TODO:
    //    1. iterate over tags
    //    2. solve mismatches:
    //      - text mismatches -> data fields OR items
    //      - tag mismatches -> optional items OR iterators (list of items)
    var wrapper = new Element(rootReference.tagName())
    val htmlProperties: Seq[HtmlProperties] = compareNodes(rootReference, rootDocument)

    if (htmlProperties.nonEmpty) logger.info(s"Mismatch types: $htmlProperties")

    if (htmlProperties.contains(LinkMismatch)) {
      wrapper.attr("href", "$link")
      wrapper.text("$link-data")
      return wrapper
    }

    if (htmlProperties.contains(ImageMismatch)) {
      wrapper.attr("src", "$img-link")
      wrapper.attr("alt", "$img-alt")
      return wrapper
    }

    if (htmlProperties.contains(DataMismatch)) {
      if(!wrapper.hasClass("data")) {
        wrapper.addClass("data")
        wrapper.text("$data")
      }
      return wrapper
    }

    if (!htmlProperties.contains(TextMismatch)) {
      // no need to compare the elements if they are identical, just add them to the wrapper
      wrapper.appendChild(rootReference.clone())
      return wrapper
    }

    val referenceChildren: List[Element] = rootReference.children().asScala.toList
    val documentChildren: List[Element] = rootDocument.children().asScala.toList

    if(referenceChildren.isEmpty) {
      logger.info(s"Children are empty: $rootReference")
      return wrapper
    }

    val zipped = zipChildren(referenceChildren, documentChildren)

    if (htmlProperties.contains(TextMismatch) && htmlProperties.contains(Iterator)) {
      logger.info(s"need to handle data-items") // based on this we now know that we need to generalize a single data item in the foreach below
      wrapper.appendChild(handleIterator(zipped))
    } else {

      zipped.foreach {
        case (Some(referencePage: Element), Some(document: Element)) =>
          wrapper.appendChild(buildWrapper(referencePage, document))

        case (None, Some(document: Element)) =>
          wrapper = expandWrapper(wrapper, document.clone())

        case (Some(referencePage: Element), None) =>
          wrapper = expandWrapper(wrapper, referencePage.clone())

        case rest => logger.error(s"wut? $rest")
      }
      wrapper
    }
  }
}
