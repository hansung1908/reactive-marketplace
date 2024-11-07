function previewImage() {
    const imageInput = document.getElementById('image');
    const previewImage = document.getElementById('preview');

    if (imageInput.files && imageInput.files[0]) {
        const reader = new FileReader();
        reader.onload = function (e) {
            previewImage.src = e.target.result;
            previewImage.style.display = 'block';
        }
        reader.readAsDataURL(imageInput.files[0]);
    } else {
        previewImage.style.display = 'none';
    }
}