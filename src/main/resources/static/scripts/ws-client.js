const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/chat'
});

stompClient.onConnect = (frame) => {
    console.log('Connected: ' + frame);
    stompClient.subscribe('/topic/chatroom', (message) => {
        showMessages(JSON.parse(message.body).content);
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

function sendName() {
    let receiverUsername = $("#receiver-username").data("username");
    stompClient.publish({
        destination: "/messenger/send-message/" + receiverUsername,
        body: $("#message").val()
    });
}

function showMessages(message) {
    console.log('message: ' + message)

    $(".own-message").append("<p>" + message + "</p>");
}

$(function () {
    $("form").on('submit', (e) => e.preventDefault());
    connect();
    $("#disconnect").click(() => disconnect());
    $("#send").click(() => sendName());
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