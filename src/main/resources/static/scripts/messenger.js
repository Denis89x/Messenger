document.getElementById("enterUsername").addEventListener("keyup", function(e) {
    if (e.key === "Enter") {
        document.getElementById("start-dialog-form").submit();
    }
});