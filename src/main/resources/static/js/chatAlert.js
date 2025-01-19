const eventSource = new EventSource('/chat/notifications');

eventSource.addEventListener('chat-message', (event) => {
    alert(`New message : ${event.data}`);
});

eventSource.onerror = (error) => {
    console.error('SSE error:', error);
    eventSource.close();
};

document.addEventListener('DOMContentLoaded', () => {
    // 현재 페이지 URL 경로 가져오기
    const currentPath = window.location.pathname;

    if (currentPath === '/chat/chatRoom') {
        eventSource.close();
    }
});