const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/chat'
});

stompClient.onConnect = (frame) => {
    console.log('Connected: ' + frame);

    let receiverUsername = $("#receiver-username").data("username");
    let senderUsername = $("#current-username").data("username");
    let chatroom = [senderUsername, receiverUsername].sort().join("-");

    stompClient.subscribe(`/topic/chatroom/${chatroom}`, (message) => {
        showMessages(JSON.parse(message.body));
    });
};

/*afa*/

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
    let senderUsername = $("#current-username").data("username");

    let chatroom = [senderUsername, receiverUsername].sort().join("-");

    stompClient.publish({
        destination: `/messenger/send-message/${chatroom}/${receiverUsername}`,
        body: $("#message").val()
    });
    $("#message").val("");

    let messagesContainer = document.getElementById("messages");
    smoothScrollTo(messagesContainer, messagesContainer.scrollHeight, 700);
}

function deleteMessage(button) {
    const messageDiv = button.closest('.user-message-o');

    // Получаем значение атрибута data-messageId
    const messageId = messageDiv.getAttribute('data-messageId');

    // Отправляем запрос на удаление сообщения
    stompClient.publish({
        destination: "/messenger/delete-message/" + messageId,
        body: JSON.stringify(messageId)
    });

    // Убираем элемент из DOM
    messageDiv.remove();
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
        let lastSenderMessages = $(".user-message-n[data-streak='" + streak + "']");
        if (lastSenderMessages.length > 0) {
            lastSenderMessages.removeAttr('data-streak');
        }
        streak = 0;
    } else {
        if (previousMessageSender === message.senderUsername) {
            if (streak === 0) {
                let elementToModify = $(".user-message-n[data-streak='" + streak + "']");
                let textFromParagraph = elementToModify.find('.user-message p').text();
                elementToModify.html(
                    "<p class='messenger-username'>" + message.senderUsername + "</p>" +
                    "<div class='user-message-picture'>" +
                    "<p>" + textFromParagraph +
                    "</p>" +
                    "</div>"
                );
                elementToModify.removeAttr('data-streak');
            } else {
                let elementToModify = $(".user-message-n[data-streak='" + streak + "']");
                let textFromParagraph = elementToModify.find('.user-message p').text();
                elementToModify.html(
                    "<div class='user-message-picture'>" +
                    "<p>" + textFromParagraph +
                    "</p>" +
                    "</div>"
                );
                elementToModify.removeAttr('data-streak');
            }

            streak++;

            console.log("После увеличения стрика");
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
            let lastSenderMessages = $(".user-message-n[data-streak='" + streak + "']");
            if (lastSenderMessages.length > 0) {
                lastSenderMessages.removeAttr('data-streak');
            }
            streak = 0;

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