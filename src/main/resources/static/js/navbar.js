const guestNavbar = document.getElementById('guestNavbar');
const userNavbar = document.getElementById('userNavbar');

// 페이지 로드시 토큰 체크
document.addEventListener('DOMContentLoaded', () => {
    checkAuthStatus();

    document.getElementById('user-logout').addEventListener('click', function(event) {
        event.preventDefault(); // 기본 폼 제출 동작을 방지

        logoutUser();
    });
});

// 토큰 상태 체크 및 화면 표시
function checkAuthStatus() {
    const token = localStorage.getItem('isLoggedIn');

    if (token) {
        guestNavbar.classList.add('hidden');
        userNavbar.classList.remove('hidden');
    } else {
        guestNavbar.classList.remove('hidden');
        userNavbar.classList.add('hidden');
    }
}

async function logoutUser() {
    try {
        const response = await fetch("/auth/logout", {
            method: "POST"
        });

        if(response.ok) {
            localStorage.removeItem('isLoggedIn'); // 로그인 확인 값 제거

            window.location.href = '/'; // 로그아웃 후 메인 페이지로 redirect
        } else { // 서버에서 오류 응답을 받은 경우
            const errorData = await response.json();
            alert(`로그아웃 실패: ${errorData.message || '알 수 없는 오류가 발생했습니다.'}`);
    } catch (error) {
        console.error('Error:', error);
    }
}