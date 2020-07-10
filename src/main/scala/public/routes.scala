package simple.sso.example
package public

import cats.effect._
import scalatags.Text.all._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.server.Router
import org.http4s.scalatags._
import org.http4s.server.staticcontent._

final case class routes(cfg: config.PublicSite)(implicit cs: ContextShift[IO]) {
  private def indexpage =
    html(
      head(
        script(attr("src") := "/public/frontend.js")
      ),
      body(
        h1("Public site"),
        div(attr("id") := "current_profile"),
        button(
          attr("type") := "button",
          attr("onclick") := s"getId('${cfg.ssoConfig.getOrigin.copy(path = "profile/id")}')"
        )("Fetch user ID"),
        br,
        for (srv <- cfg.services)
          yield div(
            a(attr("href") := srv.entryPoint.toString)(
              h3("Service ", srv.name)
            )
          ),
        br,
        a(
          attr("href") := cfg.ssoConfig
            .loginUrl(cfg.getOrigin.copy(path = "/"))
            .toString
        )(
          p("Login")
        ),
        a(
          attr("href") := cfg.ssoConfig.logoutUrl.toString
        )(
          p("Logout")
        )
      )
    )

  def make(blocker: Blocker): HttpRoutes[IO] =
    Router(
      "/" -> HttpRoutes.of[IO] {
        case _ @GET -> Root                => Ok(indexpage)
        case _ @GET -> Root / "index.html" => Ok(indexpage)
      },
      "public" -> resourceService[IO](
        ResourceService.Config("/public", blocker)
      )
    )
}
