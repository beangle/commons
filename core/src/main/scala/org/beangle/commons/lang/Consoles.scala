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

object Consoles {

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

  def readPassword(): String = new String(System.console().readPassword())

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
    case White extends Color(37)
  }

  def red(text: String): String = render(text, Color.Red)

  def green(text: String): String = render(text, Color.Green)

  def yellow(text: String): String = render(text, Color.Yellow)

  def blue(text: String): String = render(text, Color.Blue)

  def purple(text: String): String = render(text, Color.Purple)

  def cyan(text: String): String = render(text, Color.Cyan)

  def render(text: String, color: Color): String = {
    s"\u001B[${color.id}m${text}\u001B[0m"
  }

}
