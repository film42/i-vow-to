package models

import play.api.Play.current
import java.util.{Date}
import com.novus.salat._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import salatcontext._

/**
 * Created by: film42 on: 3/10/14.
 */
case class Vow(
  id: ObjectId = new ObjectId,
  text: String,
  slug: String,
  userId: String,
  createdAt: Date = new Date()
) {
  def user: User = {
    val us = User.findOneByUsername(userId)
    us match {
      case Some(u) => u
      case None => null
    }
  }
}

object Vow extends ModelCompanion[Vow, ObjectId] {
  def collection = mongoCollection("vows")

  val dao = new SalatDAO[Vow, ObjectId](collection) {}
  def all: List[Vow] = dao.find(ref = MongoDBObject()).toList

  def findAllByUserID(id :String) = dao.find(ref = MongoDBObject("userId" -> id)).toList

  collection.ensureIndex(DBObject("userId" -> 1), "vow_user_id", unique = false)

  def findUser(v: Vow) = User.findOneByUsername(v.userId)

  def findOneBySlug(slug: String): Option[Vow] = dao.findOne(MongoDBObject("slug" -> slug))

}

