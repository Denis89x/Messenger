document.getElementById("enterUsername").addEventListener("keyup", function (e) {
    if (e.key === "Enter") {
        document.getElementById("start-dialog-form").submit();
    }
});

document.addEventListener("DOMContentLoaded", function () {
    let messagesContainer = document.getElementById("messages");
    smoothScrollTo(messagesContainer, messagesContainer.scrollHeight, 700);
});

function smoothScrollTo(element, target, duration) {
    let start = element.scrollTop;
    let startTime;

    function scrollToTop(currentTime) {
        if (!startTime) {
            startTime = currentTime;
        }

        let progress = currentTime - startTime;
        let easeInOutQuad = progress => progress < 0.5 ? 2 * progress * progress : 1 - Math.pow(-2 * progress + 2, 2) / 2;

        element.scrollTop = start + (target - start) * easeInOutQuad(Math.min(progress / duration, 1));

        if (progress < duration) {
            requestAnimationFrame(scrollToTop);
        }
    }

    requestAnimationFrame(scrollToTop);
}

let clearModalWindow = document.getElementById("clear-modal-window");
let deleteModalWindow = document.getElementById("delete-modal-window");

let openHistory = document.getElementById("open-clear-history");
let cancelHistory = document.getElementById("cancel-clear-history");
let clearHistory = document.getElementById("clear-history")

let openConversation = document.getElementById("open-delete-conversation");
let cancelConversation = document.getElementById("cancel-delete-conversation");
let clearConversation = document.getElementById("delete-conversation")

openHistory.onclick = function () {
    clearModalWindow.style.display = "block";
}

openConversation.onclick = function () {
    deleteModalWindow.style.display = "block";
}

cancelHistory.onclick = function () {
    clearModalWindow.style.display = "none";
}

cancelConversation.onclick = function () {
    deleteModalWindow.style.display = "none";
}

clearHistory.onclick = function () {
    clearModalWindow.style.display = "none"
}

clearConversation.onclick = function () {
    deleteModalWindow.style.display = "none"
}


window.onclick = function (event) {
    if (event.target === clearModalWindow) {
        clearModalWindow.style.display = "none";
    }
    if (event.target === deleteModalWindow) {
        deleteModalWindow.style.display = "none";
    }
}