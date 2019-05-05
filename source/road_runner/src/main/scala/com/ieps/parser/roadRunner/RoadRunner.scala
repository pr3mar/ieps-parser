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
  var outputWrapper = new Element("html")

  def outputStrippedHtml(): Unit = {
    documents.zipWithIndex.foreach {
      case (document, id) =>
      Files.write(Paths.get(s"outputs/cleaned_$baseUrl-${id + 1}.html"), document.outerHtml().getBytes(StandardCharsets.UTF_8))
    }
    Files.write(Paths.get(s"outputs/wrapper_$baseUrl.html"), outputWrapper.html().getBytes(StandardCharsets.UTF_8))
  }

  def run(): Unit = {
//    val shuffled = Random.shuffle(documents) // TODO: uncomment this line when roadRunner is done
    val shuffled = documents
    val referencePage = shuffled.head.body()
    val restDocuments = shuffled.tail
    var pageWrapper = new Element("html")
    restDocuments.foreach { document =>
      pageWrapper = buildWrapper(referencePage, document.body()) // TODO: compare global wrapper with the returned one
    }
    outputWrapper.appendChild(pageWrapper)
  }

  private def compareTag(reference: Element, document: Element): Boolean = reference.tagName().equals(document.tagName())

  private def compareText(reference: Element, document: Element): Option[HtmlProperties] = {
    val referenceText = reference.text()
    val documentText = document.text()
    if (!referenceText.equals(documentText) && (!referenceText.equals("$data") || !documentText.equals("$data"))) {
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

      referenceChildren.headOption.foreach { head =>
        if (referenceChildren.tail.forall(_.tagName().equals(head.tagName()))
              && documentChildren.forall(_.tagName().equals(head.tagName()))) {
          mismatch += Iterator
        }
      }

      compareText(reference, document).foreach {textMismatch =>
        if (referenceChildren.isEmpty && documentChildren.isEmpty) {
//          logger.info(s"Wrapper/Document: `${reference.text()}`/`${document.text()}`")
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
    if (!compareTag(originalWrapper, newWrapper)) {
      originalWrapper.appendChild(newWrapper)
      originalWrapper
    } else if (compareText(originalWrapper, newWrapper).isEmpty) {
      originalWrapper
    } else {
      val zippedItems = zipChildren(originalWrapper.children().asScala.toList, newWrapper.children().asScala.toList)
      logger.info(s"[expand] original: $originalWrapper")
      logger.info(s"[expand] new: $newWrapper")
      logger.info(s"[expand zipped] $zippedItems")//${zippedItems.map(el => s"(${el._1.map(_.tagName())}, ${el._2.map(_.tagName())})")}")
      zippedItems match {
        case List() =>
          newWrapper

        case zipped =>
          var wrapperItem = new Element(originalWrapper.tagName())
          zipped.foreach {
          case (Some(origWrapper: Element), Some(newWrap: Element)) =>
            logger.info(s"[expand both] item left: `$origWrapper`")
            logger.info(s"[expand both] item right: `$newWrap`")
            val expandedWrappers = expandWrapper(origWrapper, newWrap)
            logger.info(s"[expand both] expanded: $expandedWrappers")
            wrapperItem = expandWrapper(wrapperItem, expandedWrappers)
            logger.info(s"[expand both] generalised $wrapperItem")
            wrapperItem

          case (None, Some(newWrap: Element)) =>
            logger.info(s"[expand right] item right: `$newWrap`")
            newWrap.addClass("optional")
            wrapperItem = expandWrapper(wrapperItem, newWrap)
            logger.info(s"[expand right] generalised $wrapperItem")

            wrapperItem

          case (Some(origWrapper: Element), None) =>
            logger.info(s"[expand left] item left: `$origWrapper`")
            origWrapper.addClass("optional")
            wrapperItem = expandWrapper(wrapperItem, origWrapper)
            logger.info(s"[expand left] generalised $wrapperItem")
            wrapperItem

          case rest => logger.error(s"[expand] wut? $rest")
        }
        wrapperItem
      }
    }
  }

  private def handleIterator(iteratorElements: List[(Option[Element], Option[Element])]): Element = {
    var generalItem = new Element(iteratorElements.head._1.get.tagName())
    iteratorElements.foreach {
      case (Some(referencePage: Element), Some(page: Element)) =>
        val builtWrapper = buildWrapper(referencePage, page)
        logger.info(s"[iterator both] expanding $generalItem")
        generalItem = expandWrapper(generalItem, builtWrapper)
        logger.info(s"[iterator both] expanded general item: $generalItem")

      case (None, Some(page: Element)) =>
        logger.info(s"[iterator right] items: `None` `$page`")
        logger.info(s"[iterator right] expanding $generalItem")
        val builtWrapper = buildWrapper(generalItem, generalizeNode(page.clone()))
        generalItem = expandWrapper(generalItem, builtWrapper)
        logger.info(s"[iterator right] expanded general item: $generalItem")

      case (Some(referencePage: Element), None) =>
        logger.info(s"[iterator left] items: `None` `$referencePage`")
        logger.info(s"[iterator left] expanding $generalItem")
        val builtWrapper = buildWrapper(generalItem, generalizeNode(referencePage.clone()))
        generalItem = expandWrapper(generalItem, builtWrapper)
        logger.info(s"[iterator left] expanded general item: $generalItem")

      case rest => logger.error(s"wut? $rest")
    }
    generalItem.addClass("multiple")
    generalItem.addClass("data-item")
    generalItem
  }

  private def buildWrapper(rootReference: Element, rootPage: Element): Element = {
    // TODO:
    //    1. iterate over tags
    //    2. solve mismatches:
    //      - text mismatches -> data fields OR items
    //      - tag mismatches -> optional items OR iterators (list of items)
    var currentWrapper = new Element(rootReference.tagName())
    val htmlProperties: Seq[HtmlProperties] = compareNodes(rootReference, rootPage)

//    if (htmlProperties.nonEmpty) logger.info(s"Mismatch types: $htmlProperties")

    if (htmlProperties.contains(LinkMismatch)) {
      currentWrapper.attr("href", "$link")
      currentWrapper.text("$link-data")
      logger.info(s"[build] Built a link item: $currentWrapper")
      return currentWrapper
    }

    if (htmlProperties.contains(ImageMismatch)) {
      currentWrapper.attr("src", "$img-link")
      currentWrapper.attr("alt", "$img-alt")
      logger.info(s"[build] Built an image item: $currentWrapper")
      return currentWrapper
    }

    if (htmlProperties.contains(DataMismatch)) {
      if (!currentWrapper.hasClass("data")) {
        currentWrapper.addClass("data")
        currentWrapper.text("$data")
      }
      logger.info(s"[build] Built a data item: $currentWrapper")
      return currentWrapper
    }

    if (!htmlProperties.contains(TextMismatch)) {
      // no need to compare the elements if they are identical, just add them to the wrapper
      return rootReference.clone()
    }

    val referenceChildren: List[Element] = rootReference.children().asScala.toList
    val pageChildren: List[Element] = rootPage.children().asScala.toList

    if(referenceChildren.isEmpty) {
      logger.info(s"Children are empty: $rootReference")
      return currentWrapper
    }

    val zipped = zipChildren(referenceChildren, pageChildren)

    if (htmlProperties.contains(TextMismatch) && htmlProperties.contains(Iterator)) {
      // based on this we now know that we need to generalize a single data item in the foreach below
      logger.info(s"[build] building iterator with: $zipped")
      currentWrapper.appendChild(handleIterator(zipped))
      logger.info(s"[build] built iterator: $currentWrapper")
      return currentWrapper
    } else {
      zipped.foreach {
        case (Some(referencePage: Element), Some(page: Element)) =>
          currentWrapper.appendChild(buildWrapper(referencePage, page))

        case (None, Some(page: Element)) =>
          currentWrapper = buildWrapper(currentWrapper, page.clone())

        case (Some(referencePage: Element), None) =>
          currentWrapper = expandWrapper(currentWrapper, referencePage.clone())

        case rest => logger.error(s"wut? $rest")
      }
      logger.info(s"[build] built item: $currentWrapper")
      return currentWrapper
    }
  }
}
