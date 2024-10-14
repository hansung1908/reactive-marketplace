async function findAllProduct() {
    try {
        const response = await fetch('/products'); // 서버에서 제품 데이터 요청
        const products = await response.json(); // JSON 형태로 응답 받기
        const productList = document.getElementById('product-list'); // 제품 목록을 담을 요소 선택

        // 제품 데이터를 HTML로 추가
        products.forEach(product => {
            const productCard = `
                <div class="col mb-5">
                    <div class="card h-100">
                        <img class="card-img-top" src="https://dummyimage.com/450x300/dee2e6/6c757d.jpg"/>
                        <div class="card-body p-4">
                            <div class="text-center">
                                <h5 class="fw-bolder">${product.title}</h5>
                                <span>${product.price} 원</span>
                            </div>
                        </div>
                        <div class="card-footer p-4 pt-0 border-top-0 bg-transparent">
                            <div class="text-center">
                                <a class="btn btn-outline-dark mt-auto" href="#">View options</a>
                            </div>
                        </div>
                    </div>
                </div>
            `;
            productList.insertAdjacentHTML('afterbegin', productCard); // 제품 카드를 HTML에 추가
        });
    } catch (error) {
        console.error('Error:', error);
    }
}

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

    if (currentPath === '/') {
        findAllProduct(); // 제품 목록을 초기 로드
    }

    else if (currentPath === '/product/saveForm') {
        document.getElementById('product-save').addEventListener('submit', saveProduct); // 폼 제출 이벤트 리스너 등록
    }
});