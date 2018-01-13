package com.hwg.util

import com.thoughtworks._

object If {
  /**
    * Enable members in `Jvm` if no Scala.js plugin is found (i.e. Normal JVM target)
    */
  @enableMembersIf(c => !c.compilerSettings.exists(_.matches("""^-Xplugin:.*scalajs-compiler_[0-9\.\-]*\.jar$""")))
  object Jvm {

    type Buffer[A] = collection.mutable.ArrayBuffer[A]
    def newBuffer[A](len: Int) = {
      val a = collection.mutable.ArrayBuffer[A]()
      a.sizeHint(len)
      a
    }

  }


  /**
    * Enable members in `Js` if a Scala.js plugin is found (i.e. Scala.js target)
    */
  @enableMembersIf(c => c.compilerSettings.exists(_.matches("""^-Xplugin:.*scalajs-compiler_[0-9\.\-]*\.jar$""")))
  object Js {

    type Buffer[A] = scalajs.js.Array[A]
    @inline def newBuffer[A](len: Int) = new scalajs.js.Array[A](len)

  }
}