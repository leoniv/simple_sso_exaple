package simple.sso.example
package sso

import org.pac4j.core.{config => pac4jCfg}
import org.pac4j.core.context._
import org.pac4j.core.client.Clients
import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.http4s
import org.pac4j.oidc.client.OidcClient
import org.pac4j.oidc.config.OidcConfiguration
import org.pac4j.oidc.profile.OidcProfile
import org.pac4j.core.context.session.SessionStore
import org.pac4j.http.client.indirect.FormClient
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator
import org.http4s.Query
import org.http4s.Uri

object Pack4jConfig extends {
  val authChoicePath = "authchoice"
  val localUserLoginPath = "localUser"

  def apply(cfg: config.Sso): pac4jCfg.Config = {
    val config = new pac4jCfg.Config(clients(cfg))
    config.setHttpActionAdapter(http4s.DefaultHttpActionAdapter)
    config
  }

  private def clients(cfg: config.Sso): Clients =
    new Clients(
      cfg.getOrigin.copy(path = "callback").toString,
      authChoiceClient(cfg),
      googleClient,
      formClient(cfg)
    )

  // Uses google app profile from project http4s-pac4j-demo
  private def googleClient: OidcClient[OidcProfile, OidcConfiguration] = {
    val oidcConfiguration = new OidcConfiguration()
    oidcConfiguration.setClientId(
      "343992089165-sp0l1km383i8cbm2j5nn20kbk5dk8hor.apps.googleusercontent.com"
    )
    oidcConfiguration.setSecret("uR3D8ej1kIRPbqAFaxIE3HWh")
    oidcConfiguration.setDiscoveryURI(
      "https://accounts.google.com/.well-known/openid-configuration"
    )
    oidcConfiguration.setUseNonce(true)
    oidcConfiguration.addCustomParam("prompt", "consent")
    val oidcClient =
      new OidcClient[OidcProfile, OidcConfiguration](oidcConfiguration)

    val authorizationGenerator = new AuthorizationGenerator[OidcProfile] {
      override def generate(
          context: WebContext,
          profile: OidcProfile
      ): OidcProfile = {
        profile.addRole("ROLE_ANY")
        profile
      }
    }
    oidcClient.setAuthorizationGenerator(authorizationGenerator)
    oidcClient
  }

  def formClient(cfg: config.Sso): FormClient = {
    new FormClient(
      cfg.getOrigin.copy(path = localUserLoginPath).toString,
      new SimpleTestUsernamePasswordAuthenticator()
    )
  }

  private def authChoiceClient(cfg: config.Sso): pac4j.AuthChoicePageClient = {
    val loginPage = new pac4j.AuthChoiceUrlResolver {
      def get(cl: pac4j.AuthChoicePageClient, ctx: WebContext): String = {
        cfg.getOrigin.copy(path = authChoicePath).toString
      }
    }

    val returnUrl = new pac4j.ReturnUrlResolver {
      val storedReturnUrlKey =
        "E147EE6D-C5F2-40FD-A714-B1830A7E7C6B:REQUESTED_URL"

      def get(cl: pac4j.AuthChoicePageClient, ctx: WebContext): String = {

        @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
        val storedReturnUrl = ctx
          .getSessionStore()
          .asInstanceOf[SessionStore[WebContext]]
          .get(ctx, storedReturnUrlKey)
          .asInstanceOf[String]

        Option(storedReturnUrl).getOrElse(cfg.profilePath)
      }

      @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
      def set(cl: pac4j.AuthChoicePageClient, ctx: WebContext): Unit = {
        ctx
          .getSessionStore()
          .asInstanceOf[SessionStore[WebContext]]
          .set(ctx, storedReturnUrlKey, resolveReturnUrl(ctx))
      }

      def resolveReturnUrl(ctx: WebContext): String = ctx.getFullRequestURL
    }
    pac4j.AuthChoicePageClient(loginPage, returnUrl)
  }

  def clientCallbackUri(cfg: config.Sso, clientName: String): Uri =
    cfg.getOrigin
      .copy(
        path = "callback",
        query = Query.fromPairs("client_name" -> clientName)
      )
}
