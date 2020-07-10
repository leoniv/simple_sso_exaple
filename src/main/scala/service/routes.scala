package simple.sso.example
package service

import org.http4s.HttpRoutes
import cats.effect.IO

final case class routes(cfg: config.Service, public: config.PublicSite) {
  def make: HttpRoutes[IO] = HttpRoutes.empty
}

