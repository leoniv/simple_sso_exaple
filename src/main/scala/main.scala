package simple.sso.example

import cats.implicits._
import cats.effect._
import org.http4s._
import org.http4s.server._
import org.http4s.server.blaze._
import java.util.concurrent.Executors

object Main extends IOApp {

  def mkServer(
      httpApp: HttpApp[IO],
      host: String,
      port: Int
  ): Resource[IO, Server[IO]] =
    BlazeServerBuilder[IO]
      .bindHttp(port, host)
      .withHttpApp(httpApp)
      .resource

  val ssoConfig: config.Sso = config.Sso(
    host = "localhost",
    port = 8080,
    profilePath = "profile",
    logoutPath = "logout"
  )

  val serviceAConfig: config.Service = config.Service(
    host = "localhost",
    port = 8081,
    name = "A",
    ssoConfig
  )

  val serviceBConfig: config.Service = config.Service(
    host = "localhost",
    port = 8082,
    name = "B",
    ssoConfig
  )

  val publicConfig: config.PublicSite = config.PublicSite(
    host = "localhost",
    port = 8083,
    ssoConfig = ssoConfig,
    services = List(serviceAConfig, serviceBConfig)
  )

  val blocker: Blocker =
    Blocker.liftExecutorService(Executors.newCachedThreadPool())

  private def publicSite =
    mkServer(
      public.mkHttpApp(publicConfig, blocker),
      publicConfig.host,
      publicConfig.port
    )
  private def serviceA =
    mkServer(
      service.mkHttpApp(serviceAConfig, publicConfig, blocker),
      serviceAConfig.host,
      serviceAConfig.port
    )
  private def serviceB =
    mkServer(
      service.mkHttpApp(serviceBConfig, publicConfig, blocker),
      serviceBConfig.host,
      serviceBConfig.port
    )
  private def ssoS =
    mkServer(sso.mkHttpApp(ssoConfig), ssoConfig.host, ssoConfig.port)

  def run(args: List[String]): IO[ExitCode] =
    (publicSite *>
      serviceA *>
      serviceB *>
      ssoS).use(_ => IO.never).as(ExitCode.Success)
}
