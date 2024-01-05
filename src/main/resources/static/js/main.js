let currentState = ""

Array.from(document.getElementsByTagName("form")).forEach(form => {
    if (form.id === "matching") {
        initMatchingForm(form)
    } else {
        initBankForm(form)
    }
})

function initMatchingForm(form) {
    form.onsubmit = function (event) {
        let xhr = new XMLHttpRequest();
        let submitButton = Array.from(form.getElementsByTagName("button"))[0]

        xhr.open('POST', '/matching')
        xhr.setRequestHeader("Content-Type", "application/json");
        xhr.send();
        setLoading(form, submitButton, true)

        xhr.onreadystatechange = function () {
            if (xhr.readyState == XMLHttpRequest.DONE) {
                setLoading(form, submitButton, true)
                document.getElementById("matching-result").innerText = xhr.response
            }
        }
        return false;
    }
}

function initBankForm(form) {
    form.onsubmit = function (event) {
        let xhr = new XMLHttpRequest();
        let formData = new FormData(form);
        let bank = form.id.split("-")[1]
        let submitButton = Array.from(form.getElementsByTagName("button"))[0]

        updateBankForm(form, bank)
        xhr.open('POST', '/import/' + bank + (currentState === "OTP_SENT" ? "/complete" : ""))
        xhr.setRequestHeader("Content-Type", "application/json");
        xhr.send(JSON.stringify(Object.fromEntries(formData)));
        setLoading(form, submitButton, true)

        xhr.onreadystatechange = function () {
            if (xhr.readyState == XMLHttpRequest.DONE) {
                setLoading(form, submitButton, false)
                currentState = xhr.response
                switch (currentState) {
                    case "OTP_SENT" :
                        sendOtp(form, submitButton)
                        break
                    case "DATA_SAVED" :
                        markFormSucceed(form, submitButton)
                        // case "DESTROYED" : destroySession()
                        break
                    default:
                        return
                }
            }
        }
        //Fail the onsubmit to avoid page refresh.
        return false;
    }
}

function updateBankForm(form, bank) {
    switch (bank) {
        case "alfa" :
            let inputs = Array.from(form.getElementsByTagName("input"))
            inputs.forEach(element => element.disabled = true)
            break
        case "tinkoff":
            form.getElementsByTagName("input").phone.disabled = false
            break
    }
}

function setLoading(form, button, loading) {
    let determinate = Array.from(form.getElementsByClassName("determinate"))[0]
    let indeterminate = Array.from(form.getElementsByClassName("indeterminate"))[0]
    determinate.display = loading ? "none" : "block";
    indeterminate.display = loading ? "block" : "none";
    button.classList.toggle("disabled", loading)
}

function sendOtp(form, button) {
    form.getElementsByTagName("input").otpCode.disabled = false
    form.getElementsByTagName("input").otpCode.value = ""
    button.innerHTML = "Send OTP"
}

function markFormSucceed(form, button) {
    button.classList.add("disabled")
    button.innerHTML = "Success"
    form.getElementsByTagName("input").otpCode.value = ""
}

function destroySession() {
    let xhr = new XMLHttpRequest();
    xhr.open('DELETE', '/session')
    xhr.send()
    xhr.onreadystatechange = function () {
        location.reload();
    }
}