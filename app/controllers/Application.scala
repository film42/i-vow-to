package controllers

import play.api._
import play.api.mvc._
import models.{Vow, User}
import play.api.data._
import play.api.data.Forms._
import java.text.Normalizer
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class WebString(s: String) {
  def isReserveWord: Boolean = s match {
    case "new" => true
    case "user" => true
    case "vows" => true
    case "i-vow-to" => true
    case _ => false
  }

  def toSlug: String = {
    val s1 = s.trim
    val s2 = Normalizer.normalize(s1, Normalizer.Form.NFD)
      .replaceAll("[^\\p{ASCII}]", "")
      .replaceAll("[^\\w+]", "-")
      .replaceAll("\\s+", "-")
      .replaceAll("[-]+", "-")
      .replaceAll("^-", "")
      .replaceAll("-$", "")
    val replace = s2.toLowerCase
    replace
  }
}

object Application extends Controller with Secured {
  implicit def webStringTools(s: String) = new WebString(s)

  val vowForm = Form (
    "text" -> nonEmptyText
  )

  def index = Action { implicit request =>
    Ok(views.html.index(User.all, Vow.all, vowForm))
  }

  def newVow = withUser { user => implicit request =>

    val form = vowForm.bindFromRequest

    val msg = form.get.trim
    val slug = msg.toSlug
    if (!slug.isReserveWord) {
      val cleanSlug = {
        if (slug startsWith "i-vow-to") slug.substring(9)
        else slug
      }

      val cleanText = {
        if (msg.toLowerCase startsWith "i vow to") msg.substring(9)
        else msg
      }

      val v = Vow(text = cleanText, slug = cleanSlug, userId = user.username)
      Vow.insert(v)
      Redirect("/")
    } else {
      BadRequest("Improper vow name: used reserve word.")
    }

  }

  def show(username: String) = Action.async { implicit request =>
    val user = User.findOneByUsername(username)
    user match {
      case Some(u) =>
        Future(Ok(views.html.show(u)))
      case None =>
        Future(NotFound("No such user"))
    }
  }

  def showVow(slug: String) = Action { implicit request =>
    val vow = Vow.findOneBySlug(slug)
    vow match {
      case Some(v) => Ok(views.html.showVow(v))
      case None => NotFound
    }
  }
}

