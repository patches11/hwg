package com.hwg

import scala.scalajs.js
import org.scalajs.dom
import org.scalajs.dom.{html, raw}

object Boot extends js.JSApp {

  private val canvas = dom.document.getElementById("canvas").asInstanceOf[html.Canvas]

  def main(): Unit = {
    val gl: raw.WebGLRenderingContext = canvas.getContext("webgl", {}).asInstanceOf[raw.WebGLRenderingContext]

    //val keyboardEvents = dom.document.onkeydown

    new HwgApplication(gl)
  }
}
