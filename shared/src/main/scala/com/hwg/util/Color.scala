package com.hwg.util


case class Color(r: Short, g: Short, b: Short) {
  def brighten(factor: Double): Color = {
    val vfac = 1 + factor

    Color(
      Math.min(r * vfac, 255).toShort,
      Math.min(g * vfac, 255).toShort,
      Math.min(b * vfac, 255).toShort
    )
  }

  def darken(factor: Double): Color = {
    val vfac = 1 - factor

    Color(
      Math.max(r * vfac, 0).toShort,
      Math.max(g * vfac, 0).toShort,
      Math.max(b * vfac, 0).toShort
    )
  }

  def adjust(factor: Double): Color = {
    val vfac = 1 + factor

    Color(
      clamp(r * vfac),
      clamp(g * vfac),
      clamp(b * vfac)
    )
  }

  def reverseAdjust(factor: Double): Color = {
    val vfac = 1 - factor

    Color(
      clamp(r * vfac),
      clamp(g * vfac),
      clamp(b * vfac)
    )
  }

  def blend(color: Color, alpha: Double): Color = {
    val iAlpha = 1 - alpha

    Color(
      clampHigh(r * iAlpha + color.r * alpha),
      clampHigh(g * iAlpha + color.g * alpha),
      clampHigh(b * iAlpha + color.b * alpha)
    )
  }

  def toJavaColor: java.awt.Color = {
    new java.awt.Color(r, g, b)
  }

  def rgb: Int = {
    r << 16 | g << 8 | b
  }

  @inline
  private def clampHigh(v: Double): Short = {
    Math.min(v, 255).toShort
  }

  private def clamp(v: Double): Short = {
    if (v < 0) 0 else if (v > 255) 255 else v.toShort
  }
 }

object Color {
  def white = Color(255, 255, 255)
}