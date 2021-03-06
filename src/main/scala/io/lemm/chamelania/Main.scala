package io.lemm.chamelania

import cats.data.Kleisli
import cats.effect.{IO, IOApp}
import cats.syntax.compose._
import cats.syntax.flatMap._
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
      case r @ AnyHeader.Host(s"$subdirectory.$_.$_.$_" :: _) =>
        // Artifactory chokes on gzip decoding when 'Store Artifacts Locally' is disabled
        r.removeHeader[`Accept-Encoding`].withUri(
          r.uri withPath r.uri.path.dropEndsWithSlash withBaseUri legacyPackages/subdirectory
        )

      case r @ AnyHeader.Host(s"$site.$_.$_" :: _) =>
        // Prevent weird redirections that break previews on a browser
        r.removeHeader[`User-Agent`].withUri(
          r.uri withBaseUri (site match {
            case "cdk" => https"://chamelania.jfrog.io/artifactory"
            case _ => https"://chamelania.jfrog.io/artifactory/maven"
          })
        )

      case r =>
        r
    },
    Kleisli.fromFunction[IO, Response[IO]] {
      case r @ AnyHeader.`Content-Type`((`xml` | `pom`) :: _) if r.status == Ok =>
        // Prefer previewing XML files directly from a browser
        r.removeHeader[`Content-Disposition`].withContentTypeOption(
          r.contentType.map(_ withMediaType xml)
        )

      case r =>
        r
    }
  )
}
