async function findAllProduct() {
    try {
        const response = await fetch('/products'); // 서버에서 제품 데이터 요청
        const products = await response.json(); // JSON 형태로 응답 받기
        console.log(products); // 데이터 구조를 확인
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
                                <span>${product.price}</span>
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
        console.error('Error fetching products:', error); // 오류 처리
    }
}

// 페이지가 로드될 때 제품 목록을 가져오는 이벤트 리스너
document.addEventListener('DOMContentLoaded', findAllProduct);
