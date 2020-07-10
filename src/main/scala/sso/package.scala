package simple.sso.example

import org.http4s._
import cats.effect.IO
import org.http4s.server._
import org.http4s.implicits._
import org.pac4j.core.{config => pac4jCfg}
import org.pac4j.http4s
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import scala.collection.JavaConverters._

package object sso {
  type AuthClient = String

  def mkHttpApp(cfg: config.Sso): HttpApp[IO] = {
    val pacCfg = Pack4jConfig(cfg)
    pacCfg.setSessionStore(new http4s.Http4sCacheSessionStore())
    val callbackService = new http4s.CallbackService(pacCfg)
    routes(
       cfg,
       getProfiles(pacCfg),
       callbackService,
       logoutService(pacCfg),
       protectedPageFilter(pacCfg)
      ).make.orNotFound
  }

  def logoutService(
      pacCfg: pac4jCfg.Config
  ): http4s.LogoutService =
    new http4s.LogoutService(
      pacCfg,
      destroySession = true,
      localLogout = true,
      centralLogout = false
    )

  def protectedPageFilter(
      pacCfg: pac4jCfg.Config
  ): Option[AuthClient] => HttpMiddleware[IO] =
    clients =>
      http4s.SecurityFilterMiddleware.securityFilter(
        pacCfg,
        clients = clients
      )

  def getProfiles(config: pac4jCfg.Config): Request[IO] => List[CommonProfile] = request => {
    val context = http4s.Http4sWebContext(request, config)
    val manager = new ProfileManager[CommonProfile](context)
    manager.getAll(true).asScala.toList
  }
}
