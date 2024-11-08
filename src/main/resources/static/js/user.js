async function saveUser() {
    try {
        const formData = new FormData();

        const image = document.querySelector('input[type="file"]').files[0];
            if (image) {
                formData.append("image", image);
            }

        formData.append('user', JSON.stringify({
            username: document.querySelector('#username').value,
            nickname: document.querySelector('#nickname').value,
            password: document.querySelector('#password').value,
            email: document.querySelector('#email').value,
            imageSource: document.querySelector('#imageSource').value
        }));

        const response = await fetch("/user/save", {
            method: "POST",
            body: formData
        });

        if(response.ok) {
            window.location.href = '/';
        }
    } catch (error) {
        console.error('Error:', error); // 오류 처리
    }
}

async function updateUser() {
    try {
        const data = {
            id : document.querySelector('#id').value,
            nickname: document.querySelector('#nickname').value,
            password: document.querySelector('#password').value,
            email: document.querySelector('#email').value
        }

        const response = await fetch("/user/update", {
            method: "PUT",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if(response.ok) {
            window.location.href = '/user/profileForm';
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

async function deleteUser() {
    try {
        const data = {
            id : document.querySelector('#id').value
        }

        const response = await fetch("/user/delete", {
            method: "DELETE",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if(response.ok) {
            window.location.href = '/';
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    // 현재 페이지 URL 경로 가져오기
    const currentPath = window.location.pathname;

    if (currentPath === '/user/saveForm') {
        document.getElementById('user-save').addEventListener('submit', function(event) {
            event.preventDefault(); // 기본 폼 제출 동작을 방지

            const confirmed = confirm("해당 정보로 회원가입을 하시겠습니까?");
            if (confirmed) {
                saveUser();
            }
        });
    }

    else if (currentPath === '/user/profileForm') {
        document.getElementById('user-update').addEventListener('submit', function(event) {
            event.preventDefault(); // 기본 폼 제출 동작을 방지

            const confirmed = confirm("해당 정보로 회원수정을 하시겠습니까?");
            if (confirmed) {
                updateUser();
            }
        });

        document.getElementById('user-delete').addEventListener('submit', function(event) {
            event.preventDefault(); // 기본 폼 제출 동작을 방지

            const confirmed = confirm("정말로 회원탈퇴를 하시겠습니까?");
            if (confirmed) {
                deleteUser();
            }
        });
    }
});