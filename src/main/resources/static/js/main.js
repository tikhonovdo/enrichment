let currentState = ""

Array.from(document.getElementsByTagName("form")).forEach(form => {
    if (form.id === "matching") {
        initMatchingForm(form)
    } else {
        initBankForm(form)
    }
})

function initMatchingForm(form) {
    form.run.onclick = function (event) {
        let url = '/matching?'
        let formData = Object.fromEntries(new FormData(form));
        for (var key of Object.keys(formData)) {
            if (formData[key]) {
                url += key + "=" + formData[key];
            }
        }
        console.log("url: " + url);

        let xhr = new XMLHttpRequest();
        xhr.open('POST', url)
        xhr.setRequestHeader("Content-Type", "application/json");
        xhr.send();
        setLoading(form, this, true)
        let button = this

        xhr.onreadystatechange = function () {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                setLoading(form, button, false)
                requestMatchingRecordsCount()
            }
        }
        return false;
    }
    requestLastUpdateDate("tinkoff")
    requestLastUpdateDate("alfa")
    requestMatchingRecordsCount()
}

function initBankForm(form) {
    form.action.onclick = function (event) {
        let xhr = new XMLHttpRequest();
        let formData = new FormData(form);
        let bank = form.id.split("-")[1]

        updateBankForm(form, bank)
        xhr.open('POST', '/import/' + bank + (currentState === "OTP_SENT" ? "/complete" : ""))
        xhr.setRequestHeader("Content-Type", "application/json");
        xhr.send(JSON.stringify(Object.fromEntries(formData)));
        setLoading(form, this, true)
        let button = this

        xhr.onreadystatechange = function () {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                setLoading(form, button, false)
                currentState = xhr.response
                switch (currentState) {
                    case "OTP_SENT" :
                        sendOtp(form, button)
                        break
                    case "DATA_SAVED" :
                        markFormSucceed(form, button)
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
    form.cancel.onclick = function (event) {
        destroySession()
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
    if (determinate) {
        determinate.display = loading ? "none" : "block";
    }
    if (indeterminate) {
        indeterminate.display = loading ? "block" : "none";
    }
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

function requestLastUpdateDate(bank) {
    let xhr = new XMLHttpRequest();
    xhr.open('GET', '/import/' + bank + "/last-update")
    xhr.send();
    xhr.onreadystatechange = function () {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            document.getElementById(bank + "-last-update").innerText = xhr.response
        }
    }
    return false;
}

function requestMatchingRecordsCount() {
    let xhr = new XMLHttpRequest();
    xhr.open('GET', '/matching/count')
    xhr.send();
    xhr.onreadystatechange = function () {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            document.getElementById("matching-count").innerText = xhr.response
        }
    }
    return false;
}