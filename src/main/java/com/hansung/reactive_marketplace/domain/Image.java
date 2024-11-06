package com.hansung.reactive_marketplace.domain;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Document(collection = "image")
public class Image {
    @Id
    private String id;

    private ImageSource imageSource;

    private String userId;

    private String productId;

    private String imageName;

    private String imageType;

    private long imageSize;

    private String imagePath;

    @CreatedDate
    private LocalDateTime createdDate;

    private String thumbnailPath;

    protected Image() {
    }

    private Image(Builder builder) {
        this.imageSource = builder.imageSource;
        this.userId = builder.userId;
        this.imageName = builder.imageName;
        this.imageType = builder.imageType;
        this.imageSize = builder.imageSize;
        this.imagePath = builder.imagePath;
        this.thumbnailPath = builder.thumbnailPath;
        this.productId = builder.productId;
    }

    public Builder toBuilder() {
        return new Builder()
                .userId(this.userId)
                .productId(this.productId)
                .imageName(this.imageName)
                .imageType(this.imageType)
                .imageSize(this.imageSize)
                .imagePath(this.imagePath)
                .thumbnailPath(this.thumbnailPath);
    }

    public static class Builder {
        private ImageSource imageSource;
        private String userId;
        private String productId;
        private String imageName;
        private String imageType;
        private long imageSize;
        private String imagePath;
        private String thumbnailPath;

        public Builder imageSource(ImageSource imageSource) {
            this.imageSource = imageSource;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder imageName(String imageName) {
            this.imageName = imageName;
            return this;
        }

        public Builder imageType(String imageType) {
            this.imageType = imageType;
            return this;
        }

        public Builder imageSize(long imageSize) {
            this.imageSize = imageSize;
            return this;
        }

        public Builder imagePath(String imagePath) {
            this.imagePath = imagePath;
            return this;
        }

        public Builder thumbnailPath(String thumbnailPath) {
            this.thumbnailPath = thumbnailPath;
            return this;
        }

        public Image build() {
            return new Image(this);
        }
    }
}
