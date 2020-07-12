package simple.sso.example
package service

import cats.effect._
import scalatags.Text.all._
import org.http4s._
import org.http4s.headers.Authorization
import org.http4s.dsl.io._
import org.http4s.server.Router
import org.http4s.scalatags._
import org.http4s.server.staticcontent._

final case class routes(cfg: config.Service, public: config.PublicSite)(implicit
    cs: ContextShift[IO]
) {
  private def indexpage =
    html(
      head(
        script(attr("src") := "/public/frontend.js")
      ),
      body(
        h1(s"Service ${cfg.name}"),
        div(attr("id") := "current_profile"),
        button(
          attr("type") := "button",
          attr("onclick") := s"callApi('${cfg.getOrigin .copy(path = "api/ping")}',"
            ++ s" '${cfg.ssoConfig.getOrigin.copy(path = "profile/id")}',"
            ++ s" '${cfg.ssoConfig.loginUrl(cfg.getOrigin)}')"
        )("Call API"),
        br,
        a(attr("href") := public.getOrigin.toString)(h3("Public site")),
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

  def apiRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ GET -> Root / "ping" => req.headers.get(Authorization)
      .fold(IO.pure(Response[IO](Status.Unauthorized)))(id => Ok(s"Ping: $id"))
  }

  def make(blocker: Blocker): HttpRoutes[IO] =
    Router(
      "/" -> HttpRoutes.of[IO] {
        case _ @GET -> Root                => Ok(indexpage)
        case _ @GET -> Root / "index.html" => Ok(indexpage)
      },
      "public" -> resourceService[IO](
        ResourceService.Config("/public", blocker)
      ),
      "api" -> apiRoutes
    )
}
