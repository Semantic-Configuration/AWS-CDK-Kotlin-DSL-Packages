package io.lemm.chamelania

import cats.effect.{IO, IOApp}
import cats.syntax.compose._
import cats.syntax.flatMap._
import io.lemm.chamelania.HeaderOf.Host
import org.http4s.Request
import org.typelevel.ci._
import porterie.syntax.literal._
import porterie.syntax.uri._
import porterie.{Porterie, forwarded}
import shapeless.::

object Main extends IOApp.Simple {
  override def computeWorkerThreadCount = 8
  override def run: IO[Nothing] = environment[IO]("PORT", 8080) >>= (proxy >>> (_.start(runtime.compute)))

  private
  def legacyPackages = https"://raw.githubusercontent.com/semantic-configuration/aws-cdk-kotlin-dsl-packages/legacy"

  private
  def proxy = Porterie(
    _: Int,
    forwarded[IO](identity).map(_.removeHeader(ci"Host")).local[Request[IO]] {
      case r @ Request(_, _, _, Host(s"$subdirectory.$_.$_.$_" :: _), _, _) =>
        // Artifactory chokes on gzip decoding when 'Store Artifacts Locally' is disabled
        r.removeHeader(ci"Accept-Encoding").withUri(
          r.uri withPath r.uri.path.dropEndsWithSlash withBaseUri legacyPackages/subdirectory
        )

      case r =>
        // Prevent weird redirections that break previews on a browser
        r.removeHeader(ci"User-Agent").withUri(
          r.uri withBaseUri https"://chamelania.jfrog.io/artifactory/maven"
        )
    }
  )
}
