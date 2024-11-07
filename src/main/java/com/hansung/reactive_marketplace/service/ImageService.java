package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.Image;
import com.hansung.reactive_marketplace.domain.ImageSource;
import com.hansung.reactive_marketplace.repository.ImageRepository;
import com.hansung.reactive_marketplace.util.ImageUtils;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;

@Service
public class ImageService {

    private final ImageRepository imageRepository;

    private final ImageUtils imageUtils;

    @Value("${image.profile.originalPath}")
    private String profileOriginalPath;

    @Value("${image.profile.thumbnailPath}")
    private String profileThumbnailPath;

    @Value("${image.product.originalPath}")
    private String productOriginalPath;

    @Value("${image.product.thumbnailPath}")
    private String productThumbnailPath;

    public ImageService(ImageRepository imageRepository, ImageUtils imageUtils) {
        this.imageRepository = imageRepository;
        this.imageUtils = imageUtils;
    }

    public Mono<Image> uploadImage(MultipartFile file, String id, ImageSource imageSource) {
        String imageName = imageUtils.generateUniqueImageName(file.getOriginalFilename());

        String imagePath = null;
        String thumbnailPath = null;

        switch (imageSource) {
            case PROFILE -> {
                File originalProfileImage = new File(profileOriginalPath, imageName);
                File resizedProfileImage = new File(profileThumbnailPath, "resized_" + imageName);

                try {
                    Thumbnails.of(originalProfileImage)
                            .size(45, 45) // 이미지 크기를 리사이즈
                            .outputQuality(0.8) // 품질을 80%로 설정 (0.0 ~ 1.0 범위)
                            .toFile(resizedProfileImage); // 리사이즈된 이미지 업로드

                    FileCopyUtils.copy(file.getBytes(), originalProfileImage); // 원본 이미지 업로드
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                imagePath =  imageUtils.generateImagePath(profileOriginalPath, imageName);
                thumbnailPath = imageUtils.generateImagePath(profileThumbnailPath, "resized_" + imageName);
            }
            case PRODUCT -> {
                File originalProductImage = new File(productOriginalPath, imageName);
                File resizedProductImage = new File(productThumbnailPath, "resized_" + imageName);

                try {
                    Thumbnails.of(originalProductImage)
                            .size(450, 300)
                            .outputQuality(0.8)
                            .toFile(resizedProductImage);

                    FileCopyUtils.copy(file.getBytes(), originalProductImage); // 원본 이미지 업로드
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                imagePath = imageUtils.generateImagePath(productOriginalPath, imageName);
                thumbnailPath = imageUtils.generateImagePath(productThumbnailPath, "resized_" + imageName);
            }
        }

        Image image = new Image.Builder()
                .imageSource(imageSource)
                .imageName(imageName)
                .imageType(file.getContentType())
                .imageSize(file.getSize())
                .imagePath(imagePath)
                .thumbnailPath(thumbnailPath)
                .build();

        switch (imageSource) {
            case PROFILE -> image.toBuilder().userId(id);
            case PRODUCT -> image.toBuilder().productId(id);
        }

        return imageRepository.save(image);
    }
}
