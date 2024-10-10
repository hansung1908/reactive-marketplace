async function saveUser(event) {
    event.preventDefault(); // 폼 제출 기본 동작 방지

    try {
        const data = {
            userId: document.querySelector('#userId').value,
            nickname: document.querySelector('#nickname').value,
            password: document.querySelector('#password').value,
            email: document.querySelector('#email').value
        }
        console.log(data)

        const response = await fetch("/user/save", {
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if(response.ok) {
            window.location.href = '/';
        }
    } catch (error) {
        console.error('Error:', error); // 오류 처리
    }
}

document.addEventListener('DOMContentLoaded', () => {
    // 현재 페이지 URL 경로 가져오기
    const currentPath = window.location.pathname;
    console.log(currentPath)

    if (currentPath === '/user/saveForm') {
        document.getElementById('user-save').addEventListener('submit', saveUser); // 폼 제출 이벤트 리스너 등록
    }
});