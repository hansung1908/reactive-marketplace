async function saveProduct() {
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

async function updateProduct() {
    try {
        const data = {
            id : document.querySelector('#id').value,
            description: document.querySelector('#description').value,
            price: document.querySelector('#price').value,
            status: document.querySelector('#productStatus').value
        }

        const response = await fetch("/product/update", {
            method: "PUT",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if(response.ok) {
            window.location.href = '/product/detail/' + data.id;
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

async function deleteProduct() {
    try {
        const data = {
            id : document.querySelector('#id').value
        }

        const response = await fetch("/product/delete", {
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

    if (currentPath === '/product/saveForm') {
        document.getElementById('product-save').addEventListener('submit', function(event) {
            event.preventDefault(); // 기본 폼 제출 동작을 방지

            const confirmed = confirm("해당 정보로 상품등록을 하시겠습니까?");
            if (confirmed) {
                saveProduct();
            }
        });
    }

    else if (currentPath.startsWith('/product/updateForm')) {
        document.getElementById('product-update').addEventListener('submit', function(event) {
            event.preventDefault(); // 기본 폼 제출 동작을 방지

            const confirmed = confirm("해당 정보로 상품정보를 수정 하시겠습니까?");
            if (confirmed) {
                updateProduct();
            }
        });
    }

    else if (currentPath.startsWith('/product/detail')) {
        document.getElementById('product-delete').addEventListener('submit', function(event) {
            event.preventDefault(); // 기본 폼 제출 동작을 방지

            const confirmed = confirm("정말로 상품을 삭제 하시겠습니까?");
            if (confirmed) {
                deleteProduct();
            }
        });
    }
});