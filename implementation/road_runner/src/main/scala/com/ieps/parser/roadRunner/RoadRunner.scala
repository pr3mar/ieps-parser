package com.ieps.parser.roadRunner

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import com.ieps.parser.preprocess.HtmlStripper
import com.typesafe.scalalogging.StrictLogging
import org.jsoup.nodes.{Document, Element}

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
    val shuffled = Random.shuffle(documents)
    val referencePage = shuffled.head.body()
    val restDocuments = shuffled.tail
    var pageWrapper = new Element("html")
    restDocuments.foreach { document =>
      pageWrapper = buildWrapper(referencePage, document.body())
    }
    outputWrapper.appendChild(pageWrapper)
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

      referenceChildren.headOption.foreach { head =>
        if (referenceChildren.tail.forall(_.tagName().equals(head.tagName()))
              && documentChildren.forall(_.tagName().equals(head.tagName()))) {
          mismatch += Iterator
        }
      }

      compareText(reference, document).foreach {textMismatch =>
        if (referenceChildren.isEmpty && documentChildren.isEmpty) {
          logger.debug(s"Wrapper/Document: `${reference.text()}`/`${document.text()}`")
          mismatch += DataMismatch
        } else {
          mismatch += textMismatch
        }
      }

      compareLinks(reference, document).foreach { linkMismatch =>
        mismatch += linkMismatch
      }

      compareImages(reference, document).foreach { imageMismatch =>
        logger.debug(s"image mismatch: $reference/$document")
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
      val allClasses = (originalWrapper.attr("class") + " " + newWrapper.attr("class")).split(" ").toSet.mkString(" ")
      originalWrapper.attr("class", allClasses)
      originalWrapper
    } else {
      val zippedItems = zipChildren(originalWrapper.children().asScala.toList, newWrapper.children().asScala.toList)
      logger.debug(s"[expand] original: $originalWrapper")
      logger.debug(s"[expand] new: $newWrapper")
      logger.debug(s"[expand zipped] $zippedItems")//${zippedItems.map(el => s"(${el._1.map(_.tagName())}, ${el._2.map(_.tagName())})")}")
      zippedItems match {
        case List() =>
          newWrapper

        case zipped =>
          var wrapperItem = new Element(originalWrapper.tagName())
          zipped.foreach {
          case (Some(origWrapper: Element), Some(newWrap: Element)) =>
            logger.debug(s"[expand both] item left: `$origWrapper`")
            logger.debug(s"[expand both] item right: `$newWrap`")
            val allClasses: String = (origWrapper.attr("class") + " " + newWrap.attr("class")).split(" ").toSet.mkString(" ")
            logger.debug(s"[expand both] classes: $allClasses")
            val expandedWrappers = expandWrapper(origWrapper, newWrap)
            expandedWrappers.attr("class", allClasses)
            logger.debug(s"[expand both] expanded: $expandedWrappers")
            wrapperItem = expandWrapper(wrapperItem, expandedWrappers)
            logger.debug(s"[expand both] generalised $wrapperItem")
            wrapperItem

          case (None, Some(newWrap: Element)) =>
            newWrap.addClass("optional")
            logger.debug(s"[expand right] item right: `$newWrap`")
            wrapperItem = expandWrapper(wrapperItem, newWrap)
            logger.debug(s"[expand right] generalised $wrapperItem")
            wrapperItem

          case (Some(origWrapper: Element), None) =>
            origWrapper.addClass("optional")
            logger.debug(s"[expand left] item left: `$origWrapper`")
            wrapperItem = expandWrapper(wrapperItem, origWrapper)
            logger.debug(s"[expand left] generalised $wrapperItem")
            wrapperItem

          case rest => logger.error(s"[expand] wut? $rest")
        }
        wrapperItem
      }
    }
  }

  private def handleIterator(iteratorElements: List[(Option[Element], Option[Element])]): Element = {
    var generalItem = new Element(iteratorElements.head._1.get.tagName())
    logger.debug(s"[iterator] Items: $iteratorElements")
    var step = 0
    iteratorElements.foreach {
      case (Some(referencePage: Element), Some(page: Element)) =>
        step += 1
        logger.debug(s"[iterator both-$step/${iteratorElements.size}] expanding $generalItem")
        val builtWrapper = buildWrapper(referencePage, page)
        logger.debug(s"[iterator both-$step/${iteratorElements.size}] built $builtWrapper")
        generalItem = expandWrapper(generalItem, builtWrapper)
        logger.debug(s"[iterator both-$step/${iteratorElements.size}] expanded general item: $generalItem")

      case (None, Some(page: Element)) =>
        step += 1
        logger.debug(s"[iterator right-$step/${iteratorElements.size}] items: `None`, `$page`")
        logger.debug(s"[iterator right-$step/${iteratorElements.size}] expanding $generalItem")
        val builtWrapper = buildWrapper(generalItem, generalizeNode(page.clone()))
        logger.debug(s"[iterator right-$step/${iteratorElements.size}] built $builtWrapper")
        generalItem = expandWrapper(generalItem, builtWrapper)
        logger.debug(s"[iterator right-$step/${iteratorElements.size}] expanded general item: $generalItem")

      case (Some(referencePage: Element), None) =>
        step += 1
        logger.debug(s"[iterator left-$step/${iteratorElements.size}] items: `None` `$referencePage`")
        logger.debug(s"[iterator left-$step/${iteratorElements.size}] expanding $generalItem")
        val builtWrapper = buildWrapper(generalItem, generalizeNode(referencePage.clone()))
        generalItem = expandWrapper(generalItem, builtWrapper)
        logger.debug(s"[iterator left-$step/${iteratorElements.size}] expanded general item: $generalItem")

      case rest => logger.error(s"$step wut? $rest")
    }
    generalItem.addClass("data-item")
    logger.debug(s"[iterator] step: $step")
    generalItem
  }

  private def buildWrapper(rootReference: Element, rootPage: Element): Element = {
    var currentWrapper = new Element(rootReference.tagName())
    val htmlProperties: Seq[HtmlProperties] = compareNodes(rootReference, rootPage)

//    if (htmlProperties.nonEmpty) logger.debug(s"Mismatch types: $htmlProperties")

    if (htmlProperties.contains(LinkMismatch)) {
      currentWrapper.attr("href", "$link")
      currentWrapper.text("$link-data")
      logger.trace(s"[build] Built a link item: $currentWrapper")
      return currentWrapper
    }

    if (htmlProperties.contains(ImageMismatch)) {
      currentWrapper.attr("src", "$img-link")
      currentWrapper.attr("alt", "$img-alt")
      logger.trace(s"[build] Built an image item: $currentWrapper")
      return currentWrapper
    }

    if (htmlProperties.contains(DataMismatch)) {
      if (!currentWrapper.hasClass("data")) {
        currentWrapper.addClass("data")
        currentWrapper.text("$data")
      }
      logger.trace(s"[build] Built a data item: $currentWrapper")
      return currentWrapper
    }

    if (!htmlProperties.contains(TextMismatch)) {
      // no need to compare the elements if they are identical, just add them to the wrapper
      return rootReference.clone()
    }

    val referenceChildren: List[Element] = rootReference.children().asScala.toList
    val pageChildren: List[Element] = rootPage.children().asScala.toList

    if(referenceChildren.isEmpty) {
      logger.debug(s"Children are empty: $rootReference")
      return rootReference
    }
    logger.trace(s"Reference children: $referenceChildren")
    logger.trace(s"Page children: $pageChildren")
    val zipped = zipChildren(referenceChildren, pageChildren)
    logger.trace(s"Zipped children: $zipped")
    if (htmlProperties.contains(TextMismatch) && htmlProperties.contains(Iterator)) {
      // based on this we now know that we need to generalize a single data item in the foreach below
      logger.debug(s"[build] building iterator with: $zipped")
      currentWrapper.appendChild(handleIterator(zipped))
      logger.debug(s"[build] built iterator: $currentWrapper")
      currentWrapper
    } else {
      zipped.foreach {
        case (Some(referencePage: Element), Some(page: Element)) =>
          logger.debug(s"[build both] left $referencePage")
          logger.debug(s"[build both] left $page")
          val built = buildWrapper(referencePage, page)
          currentWrapper.appendChild(built)
          logger.debug(s"[build both] built: $built")

        case (None, Some(page: Element)) =>
          logger.debug(s"[build right] right $page")
          val right = page.clone().addClass("optional")
          currentWrapper = expandWrapper(currentWrapper, right)
          logger.debug(s"[build right] built $right")

        case (Some(referencePage: Element), None) =>
          logger.debug(s"[build left] left $referencePage")
          val left = referencePage.clone().addClass("optional")
          currentWrapper = expandWrapper(currentWrapper, left)
          logger.debug(s"[build left] built $currentWrapper")

        case rest => logger.error(s"wut? $rest")
      }
      logger.debug(s"[build] built item: $currentWrapper")
      currentWrapper
    }
  }
}
