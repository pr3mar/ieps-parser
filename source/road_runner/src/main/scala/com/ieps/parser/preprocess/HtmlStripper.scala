package com.ieps.parser.preprocess

import org.jsoup.nodes.Document
import org.jsoup.safety.{Cleaner, Whitelist}

object HtmlStripper {
  def strip(document: Document): Document = {
    document.select("head,script,input,map").remove()
    val whitelist: Whitelist = Whitelist.relaxed()
    new Cleaner(whitelist).clean(document)
  }
}