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
    $(".message").val("");
}

function showMessages(message) {
    let currentUsername = $("#current-username").data("username");

    console.log('message: ' + message.content)
    console.log('receiver: ' + message.receiverUsername)
    console.log('sender: ' + message.senderUsername)

    if (message.senderUsername === currentUsername) {
        console.log("Вот такие пироги")
        $("#messages").append("<div class='my-account'><div class='user-message-o own-message'><p>" + message.content + "</p></div></div>");
    } else {
        console.log("Не пироги")
        $("#messages").append("<div class='user-message-n'><div class='user-message'><p>" + message.content + "</p></div></div>");
    }

/*    $(".own-message").append("<p>" + message.content + "</p>");*/
}

$(function () {
    $("form").on('submit', (e) => e.preventDefault());
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