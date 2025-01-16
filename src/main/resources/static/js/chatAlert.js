const eventSource = new EventSource('/chat/notifications');

eventSource.addEventListener('chat-message', (event) => {
    const chatMessage = JSON.parse(event.data);

    alert(`New message from ${chatMessage.roomId}: ${chatMessage.msg}`);
});

eventSource.onerror = (error) => {
    console.error('SSE error:', error);
    eventSource.close();
};