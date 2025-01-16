document.addEventListener("DOMContentLoaded", function() {
    const roomId = document.getElementById("roomId").value;
    const senderId = document.getElementById("senderId").value;

    const eventSource = new EventSource(`http://localhost:8081/chat/${roomId}`);

    eventSource.onmessage = (event) => {

        const data = JSON.parse(event.data);
        if(data.senderId === senderId) { // 로그인한 유저가 보내는 메세지
            // 파란박스 (오른쪽)
            initMyMessage(data);
        } else {
            // 회색박스 (왼쪽)
            initYourMessage(data);
        }
    }
});

// 파란박스 만들기
function getSendMsgBox(data) {
    return `<div class="sent_msg">
                <p>${data.msg}</p>
                <span class="time_date"> ${data.createdAt} </span>
            </div>`;
}

//회색박스 만들기
function getReceiveMsgBox(data) {
    return `<div class="received_withd_msg">
                <p>${data.msg}</p>
                <span class="time_date"> ${data.createdAt} </span>
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

// 채팅 메세지 전송
async function addMessage() {
    try {
        const msgInput = document.querySelector("#chat-outgoing-msg");
        const data = {
            msg: msgInput.value,
            senderId: document.querySelector("#senderId").value,
            receiverId: document.querySelector("#receiverId").value,
            roomId: document.querySelector("#roomId").value
        };

        const response = await fetch("/chat", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(data)
        });

        if (response.ok) {
            msgInput.value = ""; // 입력 필드를 비움
        } else {
            const errorData = await response.json();
            alert(`메시지 전송 실패: ${errorData.message || '알 수 없는 오류가 발생했습니다.'}`);
        }
    } catch (error) {
        console.error('Error:', error);
    }
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