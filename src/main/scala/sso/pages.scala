package simple.sso.example
package sso

import scalatags.Text.all._
import scalatags.Text
import org.pac4j.core.profile.CommonProfile

final case class pages(cfg: config.Sso) {

  def renderProfiles(
      profiles: List[CommonProfile]
  ): List[Text.TypedTag[String]] =
    profiles.map { profile =>
      p()(b("Profile: "), profile.toString, br())
    }

  def profilePage(profiles: List[CommonProfile]): Text.TypedTag[String] =
    html(
      body(
        div()(
          h1()("Profile Page"),
          renderProfiles(profiles),
          br,
          a(href := "/logout")("Logout")
        )
      )
    )

  def authChoicePage(profiles: List[CommonProfile]): Text.TypedTag[String] =
    html(
      body(
        h1("Choice login method:"),
        br,
        a(href := "/auth/google")(
          "Login with google account"
        ),
        "(use a real account)",
        br,
        a(href := "/auth/form")(
          "Login with local user account"
        ),
        "(use username same as password)",
        br,
        a(href := "/logout")("Logout"),
        br,
        div()(
          h2("Corrent profiles:"),
          renderProfiles(profiles)
        )
      )
    )

  def localLoginPage: Text.TypedTag[String] =
    html(
      body(
        form(
          action := Pack4jConfig.clientCallbackUri(cfg, "FormClient").toString,
          method := "POST"
        )(
          input(`type` := "text", name := "username", value := "")(),
          p(),
          input(
            `type` := "password",
            name := "password",
            value := ""
          )(),
          p(),
          input(
            `type` := "submit",
            name := "submit",
            value := "Submit"
          )()
        )
      )
    )
}
