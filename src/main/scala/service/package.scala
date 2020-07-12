package simple.sso.example

import org.http4s.HttpApp
import cats.effect.IO
import org.http4s.implicits._
import cats.effect.ContextShift
import cats.effect.Blocker

package object service {
  def mkHttpApp(
      cfg: config.Service,
      public: config.PublicSite,
      blocker: Blocker
  )(implicit
      cs: ContextShift[IO]
  ): HttpApp[IO] =
    routes(cfg, public).make(blocker).orNotFound
}
