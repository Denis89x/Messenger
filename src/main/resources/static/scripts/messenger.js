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