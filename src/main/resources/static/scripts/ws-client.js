const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/chat'
});

stompClient.onConnect = (frame) => {
    console.log('Connected: ' + frame);
    stompClient.subscribe('/topic/chatroom', (message) => {
        showMessages(JSON.parse(message.body));
    });
};

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

function connect() {
    stompClient.activate();
}

function disconnect() {
    stompClient.deactivate();
    console.log("Disconnected");
}

function sendMessage() {
    let receiverUsername = $("#receiver-username").data("username");
    stompClient.publish({
        destination: "/messenger/send-message/" + receiverUsername,
        body: $("#message").val()
    });
    $("#message").val("");

    let messagesContainer = document.getElementById("messages");
    smoothScrollTo(messagesContainer, messagesContainer.scrollHeight, 700);
}

let previousMessageSender = null;
let streak = 0;
let receiverPicture = $("#receiver-picture").data("picture");

function showMessages(message) {
    let currentUsername = $("#current-username").data("username");

    if (receiverPicture === undefined)
        receiverPicture = "/images/person.jpg";

    if (message.senderUsername === currentUsername) {
        $("#messages").append("" +
            "<div class='my-account'>" +
            "<div class='user-message-o own-message'>" +
            "<p>" + message.content + "</p>" +
            "</div>" +
            "</div>");
        previousMessageSender = null;
        streak = 0;
    } else {
        if (previousMessageSender === message.senderUsername) {
            if (streak === 0) {
                console.log('Нашли зироу');
                let elementToModify = $(".user-message-n[data-streak='" + streak + "']");
                let textFromParagraph = elementToModify.find('.user-message p').text();
                console.log("streak: " + streak + ", contentFounded: " + textFromParagraph)
                elementToModify.html(
                    "<p class='messenger-username'>" + message.senderUsername + "</p>" +
                    "<div class='user-message-picture'>" +
                    "<p>" + textFromParagraph +
                    "</p>" +
                    "</div>"
                );
            } else {
                console.log('не нашли зироу: ' + streak);
                let elementToModify = $(".user-message-n[data-streak='" + streak + "']");
                let textFromParagraph = elementToModify.find('.user-message p').text();
                console.log("streak: " + streak + ", contentFounded: " + textFromParagraph)
                elementToModify.html(
                    "<div class='user-message-picture'>" +
                    "<p>" + textFromParagraph + "</p>" +
                    "</div>"
                );
            }

            streak++;

            $("#messages").append(
                "<div class='user-message-n' data-streak='" + streak + "'>" +
                "<div>" +
                "<img src='" + receiverPicture + "' alt='Profile Picture' class='message-pic'/>" +
                " " +
                "<div class='user-message'>" +
                "<p>" + message.content +
                "</p>" +
                "</div>" +
                "</div>" +
                "</div>");
        } else {
            $("#messages").append(
                "<div class='user-message-n' data-streak='" + streak + "'>" +
                "<p class='messenger-username'>" + message.senderUsername + "</p>" +
                "<div>" +
                "<img src='" + receiverPicture + "' alt='Profile Picture' class='message-pic'/>" +
                "<div class='user-message'>" +
                "<p>" + message.content + "</p>" +
                "</div>" +
                "</div>" +
                "</div>");
        }
        previousMessageSender = message.senderUsername;
    }
}

$(function () {
    $(".message-form").on('submit', (e) => e.preventDefault());
    connect();
    $("#disconnect").click(() => disconnect());
    $("#send").click(() => sendMessage());
});

/*function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#messages").html("");
}*/