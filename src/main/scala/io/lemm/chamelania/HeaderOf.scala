package io.lemm.chamelania

import cats.syntax.functor._
import cats.{Functor, Id}
import io.lemm.chamelania.HeaderOf.HeaderSelectAux
import org.http4s.{Header, Headers, headers}
import shapeless.HList
import shapeless.ops.product.ToHList

class HeaderOf[F[_], A](implicit private val F: Functor[F]) {
  final def unapply[B <: HList](headers: Headers)(
    implicit
    select: HeaderSelectAux[A, F],
    toHList: ToHList.Aux[A, B]
  ): Option[F[B]] =
    headers.get(select).map(_.map(toHList.apply))
}

object HeaderOf {
  private
  type HeaderSelectAux[A, G[_]] = Header.Select[A] {
    type F[x] = G[x]
  }

  object Host extends HeaderOf[Id, headers.Host]
  object `Content-Type` extends HeaderOf[Id, headers.`Content-Type`]
}
