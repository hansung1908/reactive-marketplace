async function saveProduct() {
    try {
        const formData = new FormData();

        const image = document.querySelector('input[type="file"]').files[0];
        if (image) {
            formData.append("image", image);
        }

        formData.append('product', JSON.stringify({
            title: document.querySelector('#title').value,
            description: document.querySelector('#description').value,
            price: document.querySelector('#price').value,
            imageSource: document.querySelector('#imageSource').value
        }));

        const response = await fetch("/product/save", {
            method: "POST",
            body: formData
        });

        if(response.ok) {
            window.location.href = '/';
        } else {
            const errorData = await response.json();
            alert(`상품 등록 실패: ${errorData.message || '알 수 없는 오류가 발생했습니다.'}`);
        }
    } catch (error) {
        console.error('Error:', error); // 오류 처리
    }
}

async function updateProduct() {
    try {
        const formData = new FormData();

        const image = document.querySelector('input[type="file"]').files[0];
        if (image) {
            formData.append("image", image);
        }

        const id = document.querySelector('#id').value;

        formData.append('product', JSON.stringify({
            id: id,
            description: document.querySelector('#description').value,
            price: document.querySelector('#price').value,
            status: document.querySelector('#productStatus').value,
            imageSource: document.querySelector('#imageSource').value
        }));

        const response = await fetch("/product/update", {
            method: "PUT",
            body: formData
        });

        if(response.ok) {
            window.location.href = '/product/detail/' + id;
        } else {
            const errorData = await response.json();
            alert(`상품 수정 실패: ${errorData.message || '알 수 없는 오류가 발생했습니다.'}`);
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
        } else {
            const errorData = await response.json();
            alert(`상품 삭제 실패: ${errorData.message || '알 수 없는 오류가 발생했습니다.'}`);
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

        document.getElementById('product-delete').addEventListener('click', function(event) {
            event.preventDefault(); // 기본 폼 제출 동작을 방지

            const confirmed = confirm("정말로 상품을 삭제 하시겠습니까?");
            if (confirmed) {
                 deleteProduct();
            }
        });
    }
});