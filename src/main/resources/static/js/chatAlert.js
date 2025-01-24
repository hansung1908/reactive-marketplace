document.addEventListener('DOMContentLoaded', () => {
    const isLoggedIn = localStorage.getItem('isLoggedIn');
    const currentPath = window.location.pathname;
    let eventSource;

    if (isLoggedIn) {
        eventSource = new EventSource('/chat/notifications');

        eventSource.addEventListener('chat-message', (event) => {
            alert(`New message : ${event.data}`);
        });

        eventSource.onerror = (error) => {
            console.error('SSE error:', error);
            if (eventSource) {
                eventSource.close();
            }
        };

        // 채팅방 페이지에서는 SSE 연결 종료
        if (currentPath === '/chat/chatRoom' && eventSource) {
            eventSource.close();
        }
    }

    // 페이지 언로드 시 SSE 연결 정리
    window.addEventListener('beforeunload', () => {
        if (eventSource) {
            eventSource.close();
        }
    });
});