/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.commons.lang

import java.util.Scanner
import scala.collection.mutable

/** Console input utilities (confirm, prompt, shell, colored output). */
object Consoles {

  /** Prompts for yes/no; returns true if user enters yes.
   *
   * @param msg the prompt message
   * @param yes strings that mean yes
   * @param no  strings that mean no
   * @return true for yes
   */
  def confirm(msg: String, yes: Set[String] = Set("Y", "yes"), no: Set[String] = Set("n", "no")): Boolean = {
    val scanner = new Scanner(System.in)
    var content: String = null
    var anwser = false
    while ( {
      printImmediate(msg)
      content = Strings.trim(scanner.nextLine())
      anwser = yes.contains(content)
      !(yes.contains(content) || no.contains(content))
    })
      ()
    anwser
  }

  /** Runs a read-eval loop until user enters an exit string.
   *
   * @param prompt the prompt to display
   * @param exits  strings to exit the loop
   * @param p      the handler for each input line
   */
  def shell(prompt: => String, exits: Set[String], p: String => Unit): Unit = {
    val scanner = new Scanner(System.in)
    var content: String = null
    var exit = false
    while ( {
      printImmediate(prompt)
      if scanner.hasNextLine then
        content = Strings.trim(scanner.nextLine())
        exit = exits.contains(content)
      else exit = true
      if (!exit) p(content)
      !exit
    })
      ()
  }

  /** Prompts until validator f returns true.
   *
   * @param msg        the prompt message
   * @param defaultStr default value when empty
   * @param f          validator; loop until true
   * @return the validated input
   */
  def prompt(msg: String, defaultStr: String, f: String => Boolean): String = {
    val scanner = new Scanner(System.in)
    var content: String = null
    val promptMsg = msg + (if (null != defaultStr) "(default " + defaultStr + ")" else "")
    var exit = false
    while ( {
      printImmediate(promptMsg)
      if (scanner.hasNextLine) {
        content = scanner.nextLine()
        if (Strings.isEmpty(content)) content = defaultStr
        exit = Strings.isNotEmpty(content) && f(content)
      } else
        exit = true
      !exit
    })
      ()
    content
  }

  /** Prompts for input; returns default when empty.
   *
   * @param msg        the prompt message
   * @param defaultStr default when empty (null = no default)
   * @return the input or default
   */
  def prompt(msg: String, defaultStr: String = null): String = {
    val scanner = new Scanner(System.in)
    var content: String = null
    val promptMsg = msg + (if (null != defaultStr) "(default " + defaultStr + ")" else "")
    var exit = false
    while ( {
      printImmediate(promptMsg)
      if scanner.hasNextLine then
        content = scanner.nextLine()
        if (Strings.isEmpty(content)) content = defaultStr
        exit = (null != content)
      else exit = true
      !exit
    }) ()
    content
  }

  /** Reads a password from console (echo disabled). */
  def readPassword(): String = new String(System.console().readPassword())

  /** Reads a password with printf-style prompt. */
  def readPassword(fmt: String, args: Any*): String = io.StdIn.readLine(fmt, args: _*)

  private def printImmediate(msg: String): Unit = {
    Console.print(msg)
    if (!msg.endsWith("\n")) Console.flush()
  }

  enum Color(val id: Int) {
    case Black extends Color(30)
    case Red extends Color(31)
    case Green extends Color(32)
    case Yellow extends Color(33)
    case Blue extends Color(34)
    case Purple extends Color(35)
    case Cyan extends Color(36)
    case Gray extends Color(37)
  }

  object ColorText {

    /** Renders text in red. */
    def red(text: String): String = render(text, Color.Red)

    /** Renders text in green. */
    def green(text: String): String = render(text, Color.Green)

    /** Renders text in yellow. */
    def yellow(text: String): String = render(text, Color.Yellow)

    /** Renders text in blue. */
    def blue(text: String): String = render(text, Color.Blue)

    /** Renders text in purple. */
    def purple(text: String): String = render(text, Color.Purple)

    /** Renders text in cyan. */
    def cyan(text: String): String = render(text, Color.Cyan)

    /** Renders text in gray. */
    def gray(text: String): String = render(text, Color.Gray)

    private def render(text: String, color: Color): String = {
      s"\u001B[${color.id}m${text}\u001B[0m"
    }
  }

  class ColorText(val s: String) {
    private val ansiSeq = new mutable.ArrayBuffer[Int]

    /** Applies foreground color. */
    def color(color: Color): ColorText = {
      ansiSeq += color.id
      this
    }

    /** Applies bold style. */
    def bold(): ColorText = {
      ansiSeq += 1 // 1 Bold
      this
    }

    /** Applies underlined style. */
    def underlined(): ColorText = {
      ansiSeq += 4 // underlined
      this
    }

    /** Applies blinking style. */
    def blinking(): ColorText = {
      ansiSeq += 5
      this
    }

    /** Applies background color. */
    def background(color: Color): ColorText = {
      ansiSeq += (color.id + 10)
      this
    }

    /** Renders text with accumulated ANSI codes. */
    def render(text: String): String = {
      if ansiSeq.isEmpty then text
      else s"\u001B[${ansiSeq.mkString(";")}m${text}\u001B[0m"
    }

    override def toString: String = render(s)
  }

  given Conversion[String, ColorText] = new ColorText(_)
}
