function route() {
  if (window.location.pathname == "/login") {
    document.getElementById("login").classList.add("active");
  } else {
    document.getElementById("register").classList.add("active");
  }
}

route();

function setupFormValidation(formId) {
  const form = document.getElementById(formId);
  const usernameInput = form.querySelector('input[name="username"]');
  const passwordInput = form.querySelector('input[name="password"]');
  const button = form.querySelector('button');

  function validate() {
    const isUsernameValid = usernameInput.value.length >= 4;
    const isPasswordValid = passwordInput.value.length >= 6;
    button.disabled = !(isUsernameValid && isPasswordValid);
  }

  usernameInput.addEventListener('input', validate);
  passwordInput.addEventListener('input', validate);
}

setupFormValidation('login-form');
setupFormValidation('register-form');

document.getElementById("login-form").addEventListener("submit", async function(event) {
  event.preventDefault();
  const form = event.target;
  const formData = new FormData(form);
  const url = form.action;
  try {
    const response = await fetch(url, {
      method: 'POST',
      body: JSON.stringify({
        username: formData.get('username'),
        password: formData.get('password')
      })
    });
    if (response.redirected) {
      setTimeout(window.location.href = response.url, 5000);
    }
    else {
      const data = await response.json();
      // messageDiv.innerText = data.detail || data.message || "Unknown error";
      console.log(data);
    }
  } catch (error) {
    console.error(error);
  }
  console.log("login");
})

document.getElementById("register-form").addEventListener("submit", async function(event) {
  event.preventDefault();
  const form = event.target;
  const formData = new FormData(form);
  const url = form.action;
  try {
    const response = await fetch(url, {
      method: 'POST',
      body: JSON.stringify({
        username: formData.get('username'),
        password: formData.get('password')
      })
    });
    if (response.redirected) {
      setTimeout(window.location.href = response.url, 5000);
    }
    else {
      const data = await response.json();
      // messageDiv.innerText = data.detail || data.message || "Unknown error";
      console.log(data);
    }
  } catch (error) {
    console.error(error);
  }
  console.log("login");
})