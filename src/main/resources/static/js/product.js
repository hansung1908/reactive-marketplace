async function saveProduct(event) {
    event.preventDefault(); // 폼 제출 기본 동작 방지

    try {
        const data = {
            title: document.querySelector('#title').value,
            description: document.querySelector('#description').value,
            price: document.querySelector('#price').value
        }

        const response = await fetch("/product/save", {
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

    if (currentPath === '/product/saveForm') {
        document.getElementById('product-save').addEventListener('submit', saveProduct); // 폼 제출 이벤트 리스너 등록
    }
});