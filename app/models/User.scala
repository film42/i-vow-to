package models

import play.api.Play.current
import java.util.{Date}
import com.novus.salat._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import salatcontext._

case class User(
  id: ObjectId = new ObjectId,
  username: String,
  password: String,
  added: Date = new Date()
) {
  def profileUrl: String = s"/u/$username"
  def vows = Vow.findAllByUserID(username)
  def confirmPassword(check: String) = password == check
}

object User extends ModelCompanion[User, ObjectId] {
  def collection = mongoCollection("users")
  val dao = new SalatDAO[User, ObjectId](collection) {}

  def all: List[User] = dao.find(ref = MongoDBObject()).toList

  collection.ensureIndex(DBObject("username" -> 1), "user_username", unique = true)

  def findOneByUsername(username: String): Option[User] = dao.findOne(MongoDBObject("username" -> username))
}
