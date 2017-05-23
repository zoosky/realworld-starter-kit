package com.realworld.shared.models

/**
  * Created by shubham on 22/5/17.
  */
/////////////////////////////////////////////////////
// Students
/////////////////////////////////////////////////////
case class Students(id: Students.Id, userId: Option[User.Id], created: Students.Created)

object Students {
  def create(id: java.util.UUID, userId: Option[java.util.UUID], created: java.time.LocalDateTime): Students = {
    Students(Id(id), userId.map(User.Id.apply), Created(created))
  }

  case class Id(value: java.util.UUID) extends AnyVal

  case class Created(value: java.time.LocalDateTime) extends AnyVal

}
