package com.ieps.parser.preprocess

import org.jsoup.nodes.Document
import org.jsoup.safety.{Cleaner, Whitelist}
import scala.collection.JavaConverters._

object HtmlStripper {
  def strip(document: Document): Document = {
    // remove iframe, head, script tags
    document.select("head,script,input,map").remove()
    val whitelist: Whitelist = Whitelist.relaxed()
    // remove tag attributes
    val cleanedDocument: Document = new Cleaner(whitelist).clean(document)
    // remove empty tags such as `<span></span>` and nested tags with no text `<div><div></div></div>`
    // adapted from here: https://stackoverflow.com/questions/8711032/remove-empty-tag-pairs-from-html-fragment#8803252
    cleanedDocument.select("*").asScala.foreach { element =>
      if ((!element.hasText && element.isBlock) || (!element.hasText && element.childNodes().isEmpty)) element.remove()
    }
    cleanedDocument
  }
}