package simple.sso.example
package sso
package pac4j

import org.pac4j.core.client._
import org.pac4j.core.context._
import org.pac4j.core.profile._
import org.pac4j.core.credentials._
import org.pac4j.core.credentials.extractor._
import org.pac4j.core.credentials.authenticator._
import org.pac4j.core.exception._
import org.pac4j.core.redirect._

final case class NeverCredentials() extends Credentials
final case class NeverExtractor() extends CredentialsExtractor[NeverCredentials] {
  def extract(x: WebContext): NeverCredentials = NeverCredentials()
}
final case class NeverAuthenticator() extends Authenticator[NeverCredentials] {
  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def validate(x: NeverCredentials, ctx: WebContext): Unit = {
    throw new CredentialsException("Never authenticate!")
  }
}

trait ReturnUrlResolver {
  // Url can be relative or absolute
  def get(cl: AuthChoicePageClient, ctx: WebContext): String
  def set(cl: AuthChoicePageClient, ctx: WebContext): Unit
}

trait AuthChoiceUrlResolver {
  // Url can be relative or absolute
  def get(cl: AuthChoicePageClient, ctx: WebContext): String
}

final case class AuthChoicePageClient(
  authChoiceUrlResolver: AuthChoiceUrlResolver,
  returnUrlResolver: ReturnUrlResolver
) extends IndirectClient[NeverCredentials, CommonProfile] {
  protected def clientInit(): Unit = {
    defaultAuthenticator(NeverAuthenticator())

    defaultRedirectActionBuilder(ctx => {
      returnUrlResolver.set(this, ctx)
      RedirectAction.redirect(
        getUrlResolver().compute(authChoiceUrlResolver.get(this, ctx), ctx)
      )
    });

    defaultCredentialsExtractor(NeverExtractor())
  }

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  override protected def retrieveCredentials(
      ctx: WebContext
  ): NeverCredentials = {
    val finalRequstedUrl =
      getUrlResolver().compute(returnUrlResolver.get(this, ctx), ctx);
    throw HttpAction.redirect(ctx, finalRequstedUrl)
  }
}
