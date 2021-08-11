package io.lemm

import cats.effect.kernel.Async
import ciris.ConfigDecoder

package object chamelania {
  def environment[F[_]] = new EnvironmentPartiallyApplied[F](())

  final class EnvironmentPartiallyApplied[F[_]](private val dummy: Unit) extends AnyVal {
    def apply[A](name: String, defaultValue: => A)(implicit F: Async[F], decoder: ConfigDecoder[String, A]): F[A] =
      ciris.env(name).as[A].default(defaultValue).load[F]
  }
}
