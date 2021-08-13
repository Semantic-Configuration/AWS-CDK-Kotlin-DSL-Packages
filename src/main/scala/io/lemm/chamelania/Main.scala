package io.lemm.chamelania

import cats.data.Kleisli
import cats.effect.{IO, IOApp}
import cats.syntax.compose._
import cats.syntax.flatMap._
import io.lemm.chamelania.HeaderOf._
import org.http4s.Status.Ok
import org.http4s.headers.{Host, `Accept-Encoding`, `Content-Disposition`, `User-Agent`}
import org.http4s.syntax.literals._
import org.http4s.{Request, Response}
import porterie.syntax.literal._
import porterie.syntax.uri._
import porterie.{Porterie, forwarded}
import shapeless.::

object Main extends IOApp.Simple {
  override def computeWorkerThreadCount = 8
  override def run: IO[Nothing] = environment[IO]("PORT", 8080) >>= (proxy >>> (_.start(runtime.compute)))

  val legacyPackages = https"://raw.githubusercontent.com/semantic-configuration/aws-cdk-kotlin-dsl-packages/legacy"

  val xml = mediaType"application/xml"
  val pom = mediaType"application/x-maven-pom+xml"

  private
  def proxy = Porterie(
    _: Int,
    forwarded[IO](identity).map(_.removeHeader[Host]).local[Request[IO]] {
      case r @ Request(_, _, _, HeaderOf.Host(s"$subdirectory.$_.$_.$_" :: _), _, _) =>
        // Artifactory chokes on gzip decoding when 'Store Artifacts Locally' is disabled
        r.removeHeader[`Accept-Encoding`].withUri(
          r.uri withPath r.uri.path.dropEndsWithSlash withBaseUri legacyPackages/subdirectory
        )

      case r =>
        // Prevent weird redirections that break previews on a browser
        r.removeHeader[`User-Agent`].withUri(
          r.uri withBaseUri https"://chamelania.jfrog.io/artifactory/maven"
        )
    },
    Kleisli.fromFunction[IO, Response[IO]] {
      case r @ Response(Ok, _, `Content-Type`((`xml` | `pom`) :: _), _, _) =>
        // Prefer previewing XML files directly from a browser
        r.removeHeader[`Content-Disposition`].withContentTypeOption(
          r.contentType.map(_ withMediaType xml)
        )

      case r =>
        r
    }
  )
}
