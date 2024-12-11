async function openChat() {
    try {
        const productId = document.querySelector('#productId').value;

        const url = "/chat/open/" + productId;

        const response = await fetch(url, {
            method: "GET"
        });

        if(response.ok) {
            window.location.href = url;
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    // 현재 페이지 URL 경로 가져오기
    const currentPath = window.location.pathname;

    if (currentPath.startsWith('/product/detail')) {
        document.getElementById('chat-open').addEventListener('click', function(event) {
            event.preventDefault(); // 기본 폼 제출 동작을 방지

            openChat();
        });
    }

    else if (currentPath.startsWith('/chat/chatRoom')) {
        document.getElementById('chat-open').addEventListener('click', function(event) {
            event.preventDefault(); // 기본 폼 제출 동작을 방지

            openChat();
        });
    }
});