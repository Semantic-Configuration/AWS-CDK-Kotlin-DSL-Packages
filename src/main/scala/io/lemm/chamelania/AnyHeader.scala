package io.lemm.chamelania

import cats.syntax.functor._
import cats.{Functor, Id}
import org.http4s.{Header, Message, headers}
import shapeless.HList
import shapeless.ops.product.ToHList

class AnyHeader[F[_], A](implicit private val F: Functor[F]) {
  final def unapply[G[_], B <: HList](message: Message[G])(
    implicit
    select: Header.Select.Aux[A, F],
    toHList: ToHList.Aux[A, B]
  ): Option[F[B]] =
    message.headers.get(select).map(_.map(toHList.apply))
}

object AnyHeader {
  object Host extends AnyHeader[Id, headers.Host]
  object `Content-Type` extends AnyHeader[Id, headers.`Content-Type`]
}
