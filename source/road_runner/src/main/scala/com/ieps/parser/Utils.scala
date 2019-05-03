package com.ieps.parser

import com.typesafe.scalalogging.StrictLogging

object Utils extends StrictLogging {

  def time[R](funName: String)(block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    logger.info(s"Elapsed time for $funName: ${(t1 - t0)/ 10e9} s")
    result
  }

}
