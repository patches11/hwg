package com.hwg.webgl

import org.scalajs.dom.raw.WebGLRenderingContext

import scala.collection.mutable

class TextureLoader(val gl: WebGLRenderingContext) {

  val textures: mutable.Map[String, TextureInfo] = mutable.Map()

  def get(url: String): TextureInfo = {
    this.textures.getOrElseUpdate(url, TextureInfo.createFromUrl(gl, url))
  }

  def unload(url: String): Unit = {
    this.textures.remove(url)
  }
}
