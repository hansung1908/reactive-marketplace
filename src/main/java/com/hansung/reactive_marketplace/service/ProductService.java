package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.Image;
import com.hansung.reactive_marketplace.domain.Product;
import com.hansung.reactive_marketplace.domain.User;
import com.hansung.reactive_marketplace.dto.request.ProductDeleteReqDto;
import com.hansung.reactive_marketplace.dto.request.ProductSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.ProductUpdateReqDto;
import com.hansung.reactive_marketplace.dto.response.MyProductListResDto;
import com.hansung.reactive_marketplace.dto.response.ProductDetailResDto;
import com.hansung.reactive_marketplace.dto.response.ProductListResDto;
import com.hansung.reactive_marketplace.repository.ProductRepository;
import com.hansung.reactive_marketplace.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    private final ImageService imageService;

    public ProductService(ProductRepository productRepository, UserRepository userRepository, ImageService imageService) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.imageService = imageService;
    }

    public Mono<Product> saveProduct(ProductSaveReqDto productSaveReqDto, FilePart image, User user) {
        Product product = new Product.Builder()
                .title(productSaveReqDto.getTitle())
                .description(productSaveReqDto.getDescription())
                .price(productSaveReqDto.getPrice())
                .userId(user.getId())
                .build();

        return productRepository.save(product)
                .flatMap(savedProduct -> {
                    if (image != null) {
                        return imageService.uploadImage(image, savedProduct.getId(), productSaveReqDto.getImageSource())
                                .thenReturn(savedProduct);
                    }
                    return Mono.just(savedProduct);
                });
    }

    public Mono<ProductDetailResDto> findProductDetail(String productId) {
        Mono<Product> productMono = productRepository.findById(productId);
        Mono<Image> imageMono = imageService.findProductImageById(productId);

        return Mono.zip(productMono, imageMono)
                .flatMap(tuple -> {
                    Product product = tuple.getT1();
                    Image image = tuple.getT2();

                    return userRepository.findById(product.getUserId())
                            .map(user -> new ProductDetailResDto(
                                    product.getId(),
                                    product.getTitle(),
                                    product.getPrice(),
                                    product.getDescription(),
                                    product.getUserId(),
                                    user.getNickname(),
                                    image.getImagePath()));
                });
    }

    public Flux<ProductListResDto> findProductList() {
        return productRepository.findProductList(Sort.by(Sort.Direction.DESC, "createdAt"))
                .concatMap(product -> imageService.findProductImageById(product.getId()) // 순차적 처리를 보장하기 위한 concatMap
                        .map(image -> new ProductListResDto(
                                product.getId(),
                                product.getTitle(),
                                product.getPrice(),
                                image.getThumbnailPath()
                        )));
    }

    public Flux<MyProductListResDto> findMyProductList(String userId) {
        return productRepository.findMyProductList(userId, Sort.by(Sort.Direction.DESC, "createdAt"))
                .concatMap(product -> imageService.findProductImageById(product.getId()) // 순차적 처리를 보장하기 위한 concatMap
                        .map(image -> new MyProductListResDto(
                                product.getId(),
                                product.getTitle(),
                                product.getDescription(),
                                product.getPrice(),
                                product.getStatus(),
                                product.getCreatedAt(),
                                image.getThumbnailPath()
                        )));
    }

    public Mono<Void> updateProduct(ProductUpdateReqDto productUpdateReqDto, FilePart image) {
        Mono<Void> updateProductMono = productRepository.updateProduct(
                productUpdateReqDto.getId(),
                productUpdateReqDto.getDescription(),
                productUpdateReqDto.getPrice(),
                productUpdateReqDto.getStatus()
        );

        // 반환값이 없는 상태에서 조건문을 사용하기 위해 분리
        if (image != null) {
            return updateProductMono.then(
                    imageService.deleteProductImageById(productUpdateReqDto.getId())
                            .then(imageService.uploadImage(image, productUpdateReqDto.getId(), productUpdateReqDto.getImageSource()))
                            .then() // Mono<Void> 반환을 위한 then
            );
        } else {
            return updateProductMono;
        }
    }

    public Mono<Void> deleteProduct(ProductDeleteReqDto productDeleteReqDto) {
        return productRepository.deleteById(productDeleteReqDto.getId())
                .then(imageService.deleteProductImageById(productDeleteReqDto.getId()));
    }
}