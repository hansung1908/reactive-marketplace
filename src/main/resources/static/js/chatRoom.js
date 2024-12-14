async function openChatBySeller() {
    try {
        const productId = document.querySelector('#productIdBySeller').value;
        const sellerId = document.querySelector('#sellerIdBySeller').value;
        const buyerId = document.querySelector('#buyerIdBySeller').value;

        const url = `/chat/chatRoom?productId=${encodeURIComponent(productId)}&sellerId=${encodeURIComponent(sellerId)}&buyerId=${encodeURIComponent(buyerId)}`;

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

async function openChatByBuyer() {
    try {
        const productId = document.querySelector('#productIdByBuyer').value;
        const sellerId = document.querySelector('#sellerIdByBuyer').value;
        const buyerId = document.querySelector('#buyerIdByBuyer').value;

        const url = `/chat/chatRoom?productId=${encodeURIComponent(productId)}&sellerId=${encodeURIComponent(sellerId)}&buyerId=${encodeURIComponent(buyerId)}`;

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
        document.getElementById('chat-open-buyer').addEventListener('click', function(event) {
            event.preventDefault(); // 기본 폼 제출 동작을 방지

            openChatByBuyer();
        });
    }

    else if (currentPath.startsWith('/chat/chatRoom')) {
        document.getElementById('chat-open-seller').addEventListener('click', function(event) {
            event.preventDefault(); // 기본 폼 제출 동작을 방지

            openChatBySeller();
        });

        document.getElementById('chat-open-buyer').addEventListener('click', function(event) {
            event.preventDefault(); // 기본 폼 제출 동작을 방지

            openChatByBuyer();
        });
    }
});