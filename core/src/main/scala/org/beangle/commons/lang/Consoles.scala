/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.lang

import java.util.Scanner

object Consoles {

  def confirm(msg: String, yes: Set[String] = Set("Y", "yes"), no: Set[String] = Set("n", "no")): Boolean = {
    val scanner = new Scanner(System.in)
    var content: String = null
    var anwser = false
    do {
      print(msg)
      content = Strings.trim(scanner.nextLine())
      anwser = yes.contains(content)
    } while (!(yes.contains(content) || no.contains(content)))
    anwser
  }

  def shell(prompt: => String, exits: Set[String], p: String => Unit) {
    val scanner = new Scanner(System.in)
    var content: String = null
    var exit = false
    do {
      print(prompt)
      if (scanner.hasNextLine) {
        content = Strings.trim(scanner.nextLine())
        exit = exits.contains(content)
      } else {
        exit = true
      }
      if (!exit) p(content)
    } while (!exit)
  }

  def prompt(msg: String, defaultStr: String, f: String => Boolean): String = {
    val scanner = new Scanner(System.in)
    var content: String = null
    val promptMsg = msg + (if (null != defaultStr) "(default " + defaultStr + ")" else "")
    var exit = false
    do {
      print(promptMsg)
      if (scanner.hasNextLine) {
        content = scanner.nextLine()
        if (Strings.isEmpty(content)) content = defaultStr
        exit = Strings.isNotEmpty(content) && f(content)
      } else {
        exit = true
      }
    } while (!exit)
    content
  }

  def prompt(msg: String, defaultStr: String = null): String = {
    val scanner = new Scanner(System.in)
    var content: String = null
    val promptMsg = msg + (if (null != defaultStr) "(default " + defaultStr + ")" else "")
    var exit = false
    do {
      print(promptMsg)
      if (scanner.hasNextLine) {
        content = scanner.nextLine()
        if (Strings.isEmpty(content)) content = defaultStr
        exit = (null != content)
      } else {
        exit = true
      }
    } while (!exit)
    content
  }

  def readPassword(): String = new String(System.console().readPassword())

  def readPassword(fmt: String, args: Any*): String = io.StdIn.readLine(fmt, args: _*)

}