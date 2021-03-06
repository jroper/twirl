/*
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package twirl.parser
package test

import org.specs2.mutable._
import scalax.io.Resource

object ParserSpec extends Specification {

  "The twirl parser" should {

    val parser = new TwirlParser(shouldParseInclusiveDot = false)

    def get(templateName: String): String = {
      Resource.fromClasspath(templateName, this.getClass).string
    }

    def parse(templateName: String) = {
      (new TwirlParser(shouldParseInclusiveDot = false)).parse(get(templateName))
    }

    def failAt(message: String, line: Int, column: Int): PartialFunction[parser.ParseResult, Boolean] = {
      case parser.Error(_, rest, msgs) => {
        message == msgs.head.toString && rest.pos.line == line && rest.pos.column == column
      }
    }

    "succeed for" in {

      "static.scala.html" in {
        parse("static.scala.html") must beLike({ case parser.Success(_, rest) => if (rest.atEnd) ok else ko })
      }

      "simple.scala.html" in {
        parse("simple.scala.html") must beLike({ case parser.Success(_, rest) => if (rest.atEnd) ok else ko })
      }

      "complicated.scala.html" in {
        parse("complicated.scala.html") must beLike({ case parser.Success(_, rest) => if (rest.atEnd) ok else ko })
      }

    }

    "fail for" in {

      "unclosedBracket.scala.html" in {
        parse("unclosedBracket.scala.html") must beLike({
          case parser.Error(_, rest, msgs) => {
            if (msgs.head.toString == "[ERROR] Expected '}' but found: 'EOF'.") ok else ko
          }
        })
      }

      "unclosedBracket2.scala.html" in {
        parse("unclosedBracket2.scala.html") must beLike({
          case parser.Error(_, rest, msgs) => {
            if (msgs.head.toString == "[ERROR] Expected '}' but found: 'EOF'.") ok else ko
          }
        })
      }

      "invalidAt.scala.html" in {
        parse("invalidAt.scala.html") must beLike({
          case parser.Error(_, rest, msgs) => {
            val (msg, pos) = (msgs.head.toString, msgs.head.pos)
            if (msg.contains("[ERROR] Invalid '@' symbol.") && pos.line == 5 && pos.column == 6) ok else ko
          }
        })
      }

    }

  }

}
