package simple.sso.example

import org.http4s.HttpApp
import cats.effect._
import org.http4s.implicits._

package object public {
  def mkHttpApp(cfg: config.PublicSite, blocker: Blocker)(implicit
      cs: ContextShift[IO]
  ): HttpApp[IO] =
    routes(cfg).make(blocker: Blocker).orNotFound
}
