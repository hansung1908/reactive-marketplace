import { productId, username } from 'js/product.js';

// SSE 연결
const eventSource = new EventSource(`http://localhost:8081/chat/${productId}`);

eventSource.onmessage = (event) => {

    const data = JSON.parse(event.data);
    if(data.sender === username) { // 로그인한 유저가 보내는 메세지
        // 파란박스 (오른쪽)
        initMyMessage(data);
    } else {
        // 회색박스 (왼쪽)
        initYourMessage(data);
    }
}

// 파란박스 만들기
function getSendMsgBox(data) {

    let md = data.createdAt.substring(5,10)
    let tm = data.createdAt.substring(11,16)
    convertTime = tm + " | " + md

    return `<div class="sent_msg">
                <p>${data.msg}</p>
                <span class="time_date"> ${convertTime} / ${data.sender} </span>
            </div>`;
}

//회색박스 만들기
function getReceiveMsgBox(data) {

    let md = data.createdAt.substring(5,10)
    let tm = data.createdAt.substring(11,16)
    convertTime = tm + " | " + md

    return `<div class="received_withd_msg">
                <p>${data.msg}</p>
                <span class="time_date"> ${convertTime} / ${data.sender} </span>
            </div>`;
}

// 최초 초기화될 때 1번방 3건이 있으면 3건을 다 가져옴
// addMessage() 함수 호출시 db에 insert 되고, 그 데이터가 자동으로 흘러들어옴 (SSE)
// 파란박스 초기화
function initMyMessage(data) {

    let chatBox = document.querySelector("#chat-box");

    let sendBox = document.createElement("div");

    sendBox.className = "outgoing_msg";
    sendBox.innerHTML = getSendMsgBox(data);

    chatBox.append(sendBox);

    document.documentElement.scrollTop = document.body.scrollHeight;
}

// 회색박스 초기화
function initYourMessage(data) {

    let chatBox = document.querySelector("#chat-box");

    let receivedBox = document.createElement("div");

    receivedBox.className = "incoming_msg";
    receivedBox.innerHTML = getReceiveMsgBox(data);

    chatBox.append(receivedBox);

    document.documentElement.scrollTop = document.body.scrollHeight;
}

// AJAX 채팅 메세지 전송
async function addMessage() {

    let msgInput = document.querySelector("#chat-outgoing-msg");

    let chat = {
        sender: username,
        roomNum: roomNum,
        msg: msgInput.value
    };

    fetch("http://localhost:8080/chat", {
        method: "post",
        body: JSON.stringify(chat), // js -> json
        headers: {
            "Content-Type": "application/json; charset=utf-8"
        }
    });

    msgInput.value = "";
}

// 버튼 클릭시 메세지 전송
document.querySelector("#chat-send").addEventListener("click", () => {
    addMessage();
});

// 엔터 누를시 메세지 전송
document.querySelector("#chat-outgoing-msg").addEventListener("keydown", (e) => {
    if (e.keyCode === 13) {
        addMessage();
    }
});

document.addEventListener('DOMContentLoaded', () => {
    myFunction(); // 페이지가 로드되면 실행할 함수
});