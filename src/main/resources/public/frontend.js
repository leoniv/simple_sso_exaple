function showProfile(id) {
  document.getElementById("current_profile").textContent = id
}

function getId(url) {
  showProfile("")
  fetchId(url)
    .then(rsp => {if (rsp.ok) {return rsp.text()} else {return "Error: " + rsp.status} })
    .then(txt => showProfile(txt))
}

function fetchId(url) {
  return fetch(url, {
    method: "GET",
    credentials: "include",
    headers: {"X-Requested-With": "XMLHttpRequest"}
  })
}

function fetchApi(url, id) {
  return fetch(url, {
    method: "GET",
    headers: {Authorization: "Basic " + id}
  })
}

function callApi(apiUrl, idUrl, authUrl) {
  fetchId(idUrl).then(rsp => rsp).then(
    rsp => {
      if (rsp.ok) {
        rsp.text().then(id => {
          fetchApi(apiUrl, id)
            .then(rsp => {
              if (rsp.ok) {
                return rsp.text()
              } else {
                return "Error: " + rsp.status
              }
            })
          .then(txt => showProfile(txt))
        }
        )
      } else if (rsp.status == 401) {
        document.location.href = authUrl
      } else {
        showProfile("Error: " + rsp.status)
      }
    }
  )

}
