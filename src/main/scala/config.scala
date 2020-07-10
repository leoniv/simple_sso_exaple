package simple.sso.example
package config

import cats.implicits._
import org.http4s._
import org.http4s.implicits._

sealed trait NetworkServiceConfig {
  def getOrigin: Uri = NetworkServiceConfig.getOrigin(this)
}
object NetworkServiceConfig {
  private def uri(host: String, port: Int) =
    Uri(
      scheme"http".some,
      Uri.Authority(None, Uri.RegName(host), port.some).some
    )
  val getOrigin: NetworkServiceConfig => Uri = {
    case s: Sso        => uri(s.host, s.port)
    case s: PublicSite => uri(s.host, s.port)
    case s: Service    => uri(s.host, s.port)
  }
}

final case class Sso(
    host: String,
    port: Int,
    profilePath: String,
    logoutPath: String
) extends NetworkServiceConfig {
  val returnUrlKey = "returnTo"
  val idParamName = "id"

  def loginUrl(callBackUrl: Uri): Uri =
    getOrigin.copy(
      path = profilePath,
      query = getOrigin.query :+ (returnUrlKey -> callBackUrl.toString.some)
    )

  def logoutUrl: Uri =
    getOrigin.copy(
      path = logoutPath
    )
}

final case class PublicSite(
    host: String,
    port: Int,
    ssoConfig: Sso,
    services: List[Service]
) extends NetworkServiceConfig
final case class Service(host: String, port: Int, name: String, ssoConfig: Sso)
    extends NetworkServiceConfig {
  def entryPoint: Uri = getOrigin
}
