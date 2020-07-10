package simple.sso.example
package sso

import cats.implicits._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.scalatags._
import cats.effect.IO
import org.pac4j.http4s._
import org.http4s.server._
import org.pac4j.core.profile.CommonProfile
import org.http4s.headers.Location
import org.http4s.server.middleware.CORSConfig
import scala.concurrent.duration._
import org.http4s.server.middleware.CORS

final case class routes(
    cfg: config.Sso,
    getProfiles: Request[IO] => List[CommonProfile],
    callbackService: CallbackService,
    logoutService: LogoutService,
    profileRoutesFilter: Option[AuthClient] => HttpMiddleware[IO]
) {

  lazy val pages = sso.pages(cfg)

  def rootRoutes: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case GET -> Root / Pack4jConfig.localUserLoginPath =>
        Ok(pages.localLoginPage)
      case req @ GET -> Root / Pack4jConfig.authChoicePath =>
        Ok(pages.authChoicePage(getProfiles(req)))
      case req @ GET -> Root / "callback"  => callbackService.login(req)
      case req @ POST -> Root / "callback" => callbackService.login(req)
      case req @ GET -> Root / "logout"    => logoutService.logout(req)
      case GET -> Root / "favicon.ico"     => NotFound()
    }

  def callbackForAuthChoicePageClient: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case GET -> _ =>
        Found(
          "",
          Location(
            Pack4jConfig.clientCallbackUri(cfg, "AuthChoicePageClient")
          )
        )
    }

  def profilePageOrRedirect(req: Request[IO]): IO[Response[IO]] =
    req.params
      .get(cfg.returnUrlKey)
      .flatMap(Uri.fromString(_).toOption)
      .fold(
        Ok(pages.profilePage(getProfiles(req)))
      )(url => Found("", Location(url)))

  val corsConfig: CORSConfig =
    CORSConfig(
      anyOrigin = true,
      anyMethod = false,
      allowedHeaders =
        Set("Content-Type", "Authorization", "X-Requested-With", "*").some,
      allowedMethods = Some(Set("GET")),
      allowCredentials = true,
      maxAge = 1.day.toSeconds
    )

  def profileRoutes: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case req @ GET -> Root => profilePageOrRedirect(req)
      case req @ GET -> Root / "id" =>
        getProfiles(req).headOption.fold(NotFound())(p => Ok(p.getId))
    }

  def authMethodRoutes: HttpRoutes[IO] =
    HttpRoutes {
      case req @ GET -> Root / "google" =>
        profileRoutesFilter(Some("OidcClient"))
          .apply(callbackForAuthChoicePageClient)(req)
      case req @ GET -> Root / "form" =>
        profileRoutesFilter(Some("FormClient"))
          .apply(callbackForAuthChoicePageClient)(req)
    }

  def make: HttpRoutes[IO] =
    Router(
      "/" -> rootRoutes,
      "/auth" -> authMethodRoutes,
      "/profile" -> CORS(
        profileRoutesFilter(Some("authChoicePageClient"))(profileRoutes),
        corsConfig
      )
    )
}
