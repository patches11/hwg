package com.hwg.webgl

import org.scalajs.dom
import org.scalajs.dom.raw.{Event, HTMLImageElement, WebGLRenderingContext, WebGLTexture}

import scala.scalajs.js.typedarray.Uint8Array

class TextureInfo(var width: Int, var height: Int, var texture: WebGLTexture) {

}

object TextureInfo {
  val GL = WebGLRenderingContext

  val black = scalajs.js.Array[Short]()
  black.push(0, 0, 0, 255)
  val oneBlack = new Uint8Array(black)

  private def isPowerOf2(value: Int): Boolean = {
    (value & (value - 1)) == 0
  }

  // creates a texture info { width: w, height: h, texture: tex }
  // The texture will start with 1x1 pixels and be updated
  // when the image has loaded
  def createFromUrl(gl: WebGLRenderingContext, url: String): TextureInfo = {
    val texture = gl.createTexture()

    gl.bindTexture(GL.TEXTURE_2D, texture)

    // Fill the texture with a 1x1 black pixel.
    gl.texImage2D(GL.TEXTURE_2D, 0, GL.RGBA, 1, 1, 0, GL.RGBA, GL.UNSIGNED_BYTE, oneBlack)

    // let's assume all images are not a power of 2
    gl.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_WRAP_S, GL.CLAMP_TO_EDGE)
    gl.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_WRAP_T, GL.CLAMP_TO_EDGE)
    gl.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_MIN_FILTER, GL.LINEAR)

    val textureInfo = new TextureInfo(1, 1, texture)

    //val img = new Image()

    val img = dom.document.createElement("img").asInstanceOf[HTMLImageElement]

    img.addEventListener("load", (_: Event) => {
      textureInfo.width = img.width
      textureInfo.height = img.height

      gl.bindTexture(GL.TEXTURE_2D, textureInfo.texture)
      gl.texImage2D(GL.TEXTURE_2D, 0, GL.RGBA, GL.RGBA, GL.UNSIGNED_BYTE, img)
      if (TextureInfo.isPowerOf2(img.width) && TextureInfo.isPowerOf2(img.height)) {
        // Yes, it's a power of 2. Generate mips.
        gl.generateMipmap(GL.TEXTURE_2D)
      } else {
        // No, it's not a power of 2. Turn of mips and set
        // wrapping to clamp to edge
        gl.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_WRAP_S, GL.CLAMP_TO_EDGE)
        gl.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_WRAP_T, GL.CLAMP_TO_EDGE)
        gl.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_MIN_FILTER, GL.LINEAR)
      }
    })

    gl.bindTexture(GL.TEXTURE_2D, null)

    img.src = url

    textureInfo
  }

  def createFromTex(gl: WebGLRenderingContext, width: Int, height: Int, textureData: Uint8Array): TextureInfo = {
    val texture = gl.createTexture()

    gl.bindTexture(GL.TEXTURE_2D, texture)
      // Fill the texture with a 1x1 blue pixel.
      gl.texImage2D(GL.TEXTURE_2D, 0, GL.RGBA, width, height, 0, GL.RGBA, GL.UNSIGNED_BYTE, textureData)

      if (TextureInfo.isPowerOf2(width) && TextureInfo.isPowerOf2(height)) {
      // Yes, it's a power of 2. Generate mips.
      gl.generateMipmap(GL.TEXTURE_2D)
    } else {
      // No, it's not a power of 2. Turn of mips and set
      // wrapping to clamp to edge
      gl.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_WRAP_S, GL.CLAMP_TO_EDGE)
      gl.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_WRAP_T, GL.CLAMP_TO_EDGE)
      gl.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_MIN_FILTER, GL.LINEAR)
    }

    val textureInfo = new TextureInfo(width, height, texture)

    textureInfo
  }
}