function toggleUsernameForm() {
    let usernameForm = document.getElementById("usernameForm");
    let validationForm = document.getElementById("validationForm")

    if (usernameForm.style.display === "none") {
        usernameForm.style.display = "block";
        validationForm.style.display = "none"
    } else {
        usernameForm.style.display = "none";
    }

    let errorMessage = document.querySelector(".error-message");
    errorMessage.classList.remove("error-visible");
}

function toggleEmailForm() {
    let emailForm = document.getElementById("emailForm");
    let validationEmailForm = document.getElementById("validationEmailForm")

    if (emailForm.style.display === "none") {
        emailForm.style.display = "block";
        validationEmailForm.style.display = "none"
    } else {
        emailForm.style.display = "none";
    }

    let errorMessage = document.querySelector(".error-message");
    errorMessage.classList.remove("error-visible");
}

function togglePasswordForm() {
    let passwordForm = document.getElementById("passwordForm");
    let passwordError = document.getElementById("passwordError");
    let oldPasswordDiv = document.getElementById("oldPassword");
    let newPasswordDiv = document.getElementById("newPassword");

    if (passwordForm.style.display === "none") {
        passwordForm.style.display = "block";
        passwordError.style.display = "none";
        oldPasswordDiv.style.display = "block";
        newPasswordDiv.style.display = "none";
    } else {
        passwordForm.style.display = "none";
    }

    let errorMessage = document.querySelector(".error-message");
    errorMessage.classList.remove("error-visible");
}

function showNewPasswordDiv() {
    let oldPasswordDiv = document.getElementById("oldPassword");
    let newPasswordDiv = document.getElementById("newPassword");

    oldPasswordDiv.style.display = "none";
    newPasswordDiv.style.display = "block";
}

function updateSettingsBlockHeight() {
    let settingsBlock = document.querySelector(".settings-block");
    let profileInformation = document.querySelector(".profile-information");
    let profileContentHeight = profileInformation.offsetHeight;

    if (profileContentHeight > settingsBlock.offsetHeight) {
        settingsBlock.style.height = profileContentHeight + "px";
    }
}

window.addEventListener("resize", updateSettingsBlockHeight);
document.addEventListener("DOMContentLoaded", updateSettingsBlockHeight);

function toggleVerificationCodeBlock() {
    let verificationCodeBlock = document.getElementById("verifyEmailBlock");
    if (verificationCodeBlock.style.display === "none") {
        verificationCodeBlock.style.display = "block";
    } else {
        verificationCodeBlock.style.display = "none";
    }
}

function showArrow() {
    let arrow = document.getElementById('upload-arrow');
    arrow.style.display = 'block';
}

function hideArrow() {
    let arrow = document.getElementById('upload-arrow');
    arrow.style.display = 'none';
}

function uploadNewPicture() {
    let fileInput = document.getElementById('file-input');
    fileInput.click();
}

function uploadPicture() {
    let uploadForm = document.getElementById('upload-form');
    uploadForm.submit();
}




