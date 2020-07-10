package simple.sso.example

import org.http4s.HttpApp
import cats.effect.IO
import org.http4s.implicits._

package object service {
  def mkHttpApp(cfg: config.Service, public: config.PublicSite): HttpApp[IO] =
    routes(cfg, public).make.orNotFound
}

