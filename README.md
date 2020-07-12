# Простой SSO на pac4j

Пример реализации простого SSO с использованием

- [http4s](https://http4s.org/v0.21/)
- [http4s-pac4j](https://github.com/pac4j/http4s-pac4j) версия из
[PR](https://github.com/pac4j/http4s-pac4j/pull/3)

## Общее описание

Есть приложение реализующее личный кабинет для некой персоны. Приложение
распределённое и имеет точку входа в виде сайта доступного для всех и два
компонента требующих аутентификации пользователя. У каждого компонента есть
UI в виде SPA, бэкенд API и своя точка входа для браузера загружающая UI.
API защищён токеном содержащим ID пользователя (для упрощения не используется
никакая криптография JWT и т.п).
Для идентификации пользователя оба компонента используют SSO обеспечивающий
единый механизм входа/выхода пользователя в/из учётной записи на SSO двумя
способами, первый через OpenID провайдера, второй путём ввода имени и пароля
для локальной учётной записи. Так-же SSO предоставляет идентифицированному UI
компонента, ID пользователя для доступа к API компонента.

При этом все персонажи, кроме пары UI/API, этой истории
развёрнуты на разных хостах но в одном домене. Для упрощения домен будет один -
localhost но будут разные порты, что с точки зрения браузера и CORS запросов
одно и тоже.

## Перечислим персонажи:

- PS - public site - http://localhost:8083
- SA - service A - http://localhost:8081
- SB - service B - http://localhost:8082
- SSO - SSO - http://localhost:8080

## Эндпоинты

#### PS
- / или index.html - точка входа на сайт

#### SA & SB

- / или index.html - точка входа для браузера
- /api/ping - метод API компонента

Основным защищаемым ресурсом является /api/ping, доступ получает тот у кого есть
ID пользователя. Точку входа браузера index.html можно назвать условно защищаемой
с той точки зрения, что если UI не сможет получить ID у SSO то оно не сможет
обратиться к API.

При обращении к /api/ping происходит запрос id у sso и если sso вернёт 401
браузер будет направлен на страницу выбора способа аутентификации sso. После
успешной аутентификации sso вернёт браузер обратно на страницу компонента.

#### SSO

- /logout - эндпоинт выхода из учётной записи
- /callback - точка возврата для т.н [iderect client](http://www.pac4j.org/docs/clients.html)
или в терминах OpenID/OAuth это т.н. [Redirection Endpoint](https://tools.ietf.org/html/rfc6749#section-3.1.2)
- /profile/id - возвращает ID пользователя идентифицированному агенту пользователя
ID пользователя
- /profile - страница отображающая профиль пользователя идентифицированному
агенту пользователя

Путь `/profile/*` является защищаемым ресурсом в терминах OpenID это т.н.
[UserInfo Endpoint](https://openid.net/specs/openid-connect-core-1_0.html#UserInfo)

## Запуск

    $sbt
    sbt:simpleSsoExample> reStart
    sbt:simpleSsoExample> reStop
