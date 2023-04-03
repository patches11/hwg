package com.hwg.gui

import com.hwg.Protocol.{ReceiveMessage, SendMessage}
import com.hwg.WebsocketClient
import com.hwg.util.LimitStack
import org.scalajs.dom
import org.scalajs.dom.{Event, Node, html}

import scala.scalajs.js.Date

class Chat(val client: WebsocketClient, val id: Int) {
  import monix.execution.Scheduler.Implicits.global
  import com.hwg.util.DateUtils._

  val limit = 100

  private val outerBox = dom.document.createElement("div").asInstanceOf[html.Div]
  private val chatBox = dom.document.createElement("div").asInstanceOf[html.Div]
  private val messageDiv = dom.document.createElement("div").asInstanceOf[html.Div]
  private val input = dom.document.createElement("input").asInstanceOf[html.Input]
  private val form = dom.document.createElement("form").asInstanceOf[html.Form]

  form.appendChild(input)

  form.style.marginBottom = "0"

  input.style.width = "100%"
  input.style.background = "rgba(0, 0, 0, 0.5)"
  input.style.color = "#AAA"
  input.style.border = "1px solid #222"

  messageDiv.className = "flex-column"
  messageDiv.style.overflowY = "scroll"
  messageDiv.style.padding = "5px"
  messageDiv.style.setProperty("flex-grow", "1")

  chatBox.appendChild(form)
  chatBox.appendChild(messageDiv)

  outerBox.appendChild(chatBox)

  outerBox.style.position = "fixed"
  outerBox.style.bottom = "0"
  outerBox.style.width = "100%"

  chatBox.style.border = "2px solid #222"
  chatBox.style.width = "50%"
  chatBox.style.margin = "auto"
  chatBox.style.height = "100px"
  chatBox.style.color = "#AAA"
  chatBox.className = "flex-column-reverse"

  dom.document.body.appendChild(outerBox)

  private val messages = new LimitStack[Node](limit)

  init()

  private def enterListener(e: Event): Unit = {
    val message = SendMessage(input.value)
    input.value = ""
    client.send(message)
  }

  def addMessage(who: String, time: Long, text: String): Unit = {
    val newMessage = dom.document.createElement("div")
    newMessage.innerHTML = s"${new Date(time).timeFormat()} $who - $text"
    val node = messageDiv.appendChild(newMessage)
    newMessage.scrollIntoView()
    messages.push(node).foreach { toRemove =>
      messageDiv.removeChild(toRemove)
    }
  }

  private def init(): Unit = {
    client.getObservable.collect {
      case ReceiveMessage(who, time, text) =>
        addMessage(who, time, text)
    }.subscribe()

    form.onsubmit = (e: Event) => {
      enterListener(e)
      false
    }
  }
}
