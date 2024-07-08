let currentState = "start"
let currentBank = null

Array.from(document.getElementsByTagName("form")).forEach(form => {
    if (form.id === "matching") {
        initMatchingForm(form)
    } else {
        initBankForm(form)
    }
})

function updateState() {
    requestLastUpdateDate("tinkoff")
    requestLastUpdateDate("alfa")
    requestLastUpdateDate("yandex")
    requestMatchingRecordsCount()
}

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
                updateState()
            }
        }
        return false;
    }
    updateState()
}

function initBankForm(form) {
    form.action.onclick = function (event) {
        let xhr = new XMLHttpRequest();
        let formData = new FormData(form);
        let bank = form.id.split("-")[1]
        currentBank = bank;

        xhr.open('POST', '/import/' + bank + '/' + currentState);
        xhr.setRequestHeader("Content-Type", "application/json");
        xhr.send(JSON.stringify(Object.fromEntries(formData)));
        setLoading(form, this,  true)
        let button = this

        xhr.onreadystatechange = function () {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                if (xhr.status === 200) {
                    currentState = xhr.response
                    setLoading(form, button, false)
                    updateFormState(form, button)
                } else {
                    console.log(xhr.response)
                }
            }
        }
        //Fail the onsubmit to avoid page refresh.
        return false;
    }
    form.cancel.onclick = function (event) {
        destroySession()
    }

    updateFormState(form, null)
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

function updateFormState(form, button) {
    disableAllBlocks(form)
    enableCurrentStateBlock(form)
    if (button) {
        updateButtonState(button)
    }
}

function disableAllBlocks(form) {
    let stateBlocks = Array.from(form.querySelectorAll(".state"));
    stateBlocks.forEach((item) => {
        item.style.display = 'none';
        Array.from(item.getElementsByTagName("input")).forEach((field) => {
            field.classList.add("disabled")
            field.disabled = true
        })
    })
}

function enableCurrentStateBlock(form) {
    let currentStateBlock = form.querySelector(".state."+ currentState);
    currentStateBlock.style.display = 'block';
    Array.from(currentStateBlock.getElementsByTagName("input")).forEach((field) => {
        field.classList.remove("disabled")
        field.disabled = false
    })
}

function updateButtonState(button) {
    switch (currentState.toUpperCase()) {
        case "OTP_SENT" :
            button.innerHTML = "Send OTP"
            break
        case "DATA_SAVED" :
            button.classList.add("disabled")
            button.innerHTML = "Finished!"
            break
        default:
            return
    }
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