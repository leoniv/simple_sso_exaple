function showProfile(id) {
  document.getElementById("current_profile").textContent = id
}

function getId(url) {
  showProfile("")
  fetch(url, {
    method: "GET",
    credentials: "include",
    headers: {"X-Requested-With": "XMLHttpRequest"}
  })
    .then(rsp => {if (rsp.ok) {return rsp.text()} else {return "Error: " + rsp.status} })
    .then(txt => showProfile(txt))
}
