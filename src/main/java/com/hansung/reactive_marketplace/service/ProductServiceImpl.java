package com.hansung.reactive_marketplace.service;

import com.hansung.reactive_marketplace.domain.Image;
import com.hansung.reactive_marketplace.domain.Product;
import com.hansung.reactive_marketplace.dto.request.ProductDeleteReqDto;
import com.hansung.reactive_marketplace.dto.request.ProductSaveReqDto;
import com.hansung.reactive_marketplace.dto.request.ProductUpdateReqDto;
import com.hansung.reactive_marketplace.dto.response.MyProductListResDto;
import com.hansung.reactive_marketplace.dto.response.ProductDetailResDto;
import com.hansung.reactive_marketplace.dto.response.ProductListResDto;
import com.hansung.reactive_marketplace.dto.response.ProductUpdateResDto;
import com.hansung.reactive_marketplace.exception.ApiException;
import com.hansung.reactive_marketplace.exception.ExceptionMessage;
import com.hansung.reactive_marketplace.repository.ProductRepository;
import com.hansung.reactive_marketplace.util.AuthUtils;
import org.springframework.data.domain.Sort;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceImpl implements ProductService{

    private final ProductRepository productRepository;

    private final UserService userService;

    private final ImageService imageService;

    public ProductServiceImpl(ProductRepository productRepository, UserService userService, ImageService imageService) {
        this.productRepository = productRepository;
        this.userService = userService;
        this.imageService = imageService;
    }

    public Mono<Product> saveProduct(ProductSaveReqDto productSaveReqDto, FilePart image, Authentication authentication) {
        Product product = new Product.Builder()
                .title(productSaveReqDto.title())
                .description(productSaveReqDto.description())
                .price(productSaveReqDto.price())
                .userId(AuthUtils.getAuthenticationUser(authentication).getId())
                .build();

        return productRepository.save(product)
                .flatMap(savedProduct -> {
                    if (image != null) {
                        return imageService.uploadImage(image, savedProduct.getId(), productSaveReqDto.imageSource())
                                .thenReturn(savedProduct);
                    }
                    return Mono.just(savedProduct);
                })
                .onErrorMap(e -> new ApiException(ExceptionMessage.INTERNAL_SERVER_ERROR)); // 저장 중 에러 처리
    }

    public Mono<ProductDetailResDto> findProductDetail(String productId, Authentication authentication) {
        Mono<Product> productMono = productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.PRODUCT_NOT_FOUND))); // 상품을 찾을 수 없음

        Mono<Image> imageMono = imageService.findProductImageById(productId)
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.IMAGE_NOT_FOUND))); // 이미지가 없음

        return Mono.zip(productMono, imageMono)
                .flatMap(tuple -> {
                    Product product = tuple.getT1();
                    Image image = tuple.getT2();

                    return userService.findUserById(product.getUserId())
                            .map(user -> new ProductDetailResDto(
                                    product.getId(),
                                    product.getTitle(),
                                    product.getPrice(),
                                    product.getDescription(),
                                    user.getNickname(),
                                    image.getImagePath(),
                                    product.getUserId(),
                                    AuthUtils.getAuthenticationUser(authentication).getId()));
                });
    }

    public Mono<ProductUpdateResDto> findProductForUpdateForm(String productId) {
        Mono<Product> productMono = productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.PRODUCT_NOT_FOUND)));

        Mono<Image> imageMono = imageService.findProductImageById(productId)
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.IMAGE_NOT_FOUND)));

        return Mono.zip(productMono, imageMono)
                .flatMap(tuple -> {
                    Product product = tuple.getT1();
                    Image image = tuple.getT2();

                    return userService.findUserById(product.getUserId())
                            .map(user -> new ProductUpdateResDto(
                                    product.getId(),
                                    product.getTitle(),
                                    product.getPrice(),
                                    product.getDescription(),
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

    public Flux<MyProductListResDto> findMyProductList(Authentication authentication) {
        return productRepository.findMyProductList(AuthUtils.getAuthenticationUser(authentication).getId(), Sort.by(Sort.Direction.DESC, "createdAt"))
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

    public Mono<Product> findProductById(String productId) {
        return productRepository.findById(productId);
    }

    public Mono<Void> updateProduct(ProductUpdateReqDto productUpdateReqDto, FilePart image) {
        return productRepository.findById(productUpdateReqDto.id())
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.PRODUCT_NOT_FOUND))) // 상품을 찾을 수 없음
                .flatMap(existingProduct -> {
                    Mono<Void> updateProductMono = productRepository.updateProduct(
                            productUpdateReqDto.id(),
                            productUpdateReqDto.description(),
                            productUpdateReqDto.price(),
                            productUpdateReqDto.status()
                    );

                    if (image != null) {
                        return updateProductMono.then(
                                imageService.deleteProductImageById(productUpdateReqDto.id())
                                        .then(imageService.uploadImage(image, productUpdateReqDto.id(), productUpdateReqDto.imageSource()))
                                        .then()
                        );
                    } else {
                        return updateProductMono;
                    }
                })
                .onErrorMap(e -> new ApiException(ExceptionMessage.INTERNAL_SERVER_ERROR)); // 업데이트 중 에러 처리
    }

    public Mono<Void> deleteProduct(ProductDeleteReqDto productDeleteReqDto) {
        return productRepository.findById(productDeleteReqDto.id())
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.PRODUCT_NOT_FOUND))) // 상품을 찾을 수 없음
                .flatMap(existingProduct ->
                        productRepository.deleteById(existingProduct.getId())
                                .then(imageService.deleteProductImageById(existingProduct.getId()))
                                .onErrorMap(e -> new ApiException(ExceptionMessage.INTERNAL_SERVER_ERROR)) // 삭제 중 에러 처리
                );
    }
}