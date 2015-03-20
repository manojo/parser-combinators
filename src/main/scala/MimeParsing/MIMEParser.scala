package MimeParsing

import java.io.Reader

import MimeParsing.JsonParser

import scala.util.parsing.combinator.RegexParsers


object MIMEParser extends RegexParsers with JsonParser with TextParser {

  override def skipWhitespace = false

  val MimeVersion = "MIME-Version:"
  val ContentType = "Content-Type:"

  override def CRLF = "\r\n" | "\n"

  def versionNumber = """\d+.\d+""".r

  override def parse(input: Reader) = parseAll(all, input) match {
    case Success(res, _) => res
    case e => throw new RuntimeException(e.toString);
  }

  def all: Parser[Mime] = header ~ content map (t => new Mime(t._1, t._2))

  def header: Parser[MimeHeader] = MimeVersion ~> versionNumber <~ CRLF map (v => new MimeHeader(v))

  def content: Parser[Any] = ContentType ~> ((contentType <~ "/") ~ subType) <~ ";" <~ CRLF flatMap (t => getParser((t._1, t._2)))

  def contentType = "application" | "audio" | "image" | "message" | "multipart" | "text" | "video"
  def subType = token | ""
  def token = "json" | "plain"

  def getParser(t: (String, String)): Parser[Any] = t match {
    case ("application", subtype) => throw new Exception ("Not implemented yet")

    case ("text", subtype) => getTextParser(subtype)

    case ("audio", _) => throw new Exception ("Not implemented yet")

    case ("image", _) => throw new Exception ("Not implemented yet")

    case ("message", _) => throw new Exception ("Not implemented yet")

    case ("multipart", _) => throw new Exception ("Not implemented yet")

    case ("video", _) => throw new Exception ("Not implemented yet")

    case _ => throw new Exception("Cannot parse such type: " + t)
  }

  def getTextParser(subtype: String): Parser[Any] = subtype match {
    case("json") => root
    case ("plain") => text
    case _ => throw new Exception("Cannot parse such text subtype: " + subtype)
  }


  /**
   * Classes to have a typed parser
   */
  final class Mime(header : MimeHeader, content: Any) {
    override def toString = List(header.toString, content).mkString("\n")
  }

  final class MimeHeader(versionNumber : String) {
    override def toString = MimeVersion + versionNumber
  }
}