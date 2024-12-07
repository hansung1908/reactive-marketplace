const guestNavbar = document.getElementById('guestNavbar');
const userNavbar = document.getElementById('userNavbar');

// 페이지 로드시 토큰 체크
document.addEventListener('DOMContentLoaded', () => {
    checkAuthStatus();

    document.getElementById('user-profile').addEventListener('click', function(event) {
        event.preventDefault(); // 기본 폼 제출 동작을 방지

        getUserProfile();
    });

    document.getElementById('user-logout').addEventListener('click', function(event) {
        event.preventDefault(); // 기본 폼 제출 동작을 방지

        logoutUser();
    });
});

// 토큰 상태 체크 및 화면 표시
function checkAuthStatus() {
    const token = localStorage.getItem('jwtToken');

    if (token) {
        guestNavbar.classList.add('hidden');
        userNavbar.classList.remove('hidden');
    } else {
        guestNavbar.classList.remove('hidden');
        userNavbar.classList.add('hidden');
    }
}

async function getUserProfile() {
    try {
        // JWT 토큰을 로컬 스토리지에서 가져옵니다.
        const token = localStorage.getItem('jwtToken');

        const url = '/user/profileForm';

        const response = await fetch(url, {
            method: "GET",
            headers: {
                'X-Test-Header': 'TestValue', // 추가할 헤더
                'Authorization': token
            }
        });

        if(response.ok) {
            window.location.href = url;
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

function logoutUser() {
    localStorage.removeItem('jwtToken');

    window.location.href = '/'; // 로그아웃 후 메인 페이지로 redirect
}