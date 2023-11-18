let socket = new SockJS('/chat');
let stompClient = Stomp.over(socket);

function connectAndSendMessage() {
    stompClient.connect({}, function (frame) {
        console.log('Подключено');

        let receiverUsername = document.getElementById('receiver-username').value;
        console.log('receiver1', receiverUsername);

        sendMessage();
    });
}

function sendMessage() {
    let receiverUsername = document.getElementById('receiver-username').value;
    console.log('receiver2', receiverUsername);
    let messageContent = document.getElementById('message-content-input').value;

    stompClient.send("/messenger/sendMessage/" + receiverUsername, {}, JSON.stringify({'messageContent': messageContent}));
    document.getElementById('message-content-input').value = '';
}

// Вызов функции для установки соединения и отправки сообщения
connectAndSendMessage();
