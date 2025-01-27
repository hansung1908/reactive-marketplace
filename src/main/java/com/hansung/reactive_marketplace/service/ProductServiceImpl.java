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
import com.hansung.reactive_marketplace.util.DateTimeUtils;
import org.springframework.data.domain.Sort;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final UserService userService;

    private final ImageService imageService;

    public ProductServiceImpl(ProductRepository productRepository, UserService userService, ImageService imageService) {
        this.productRepository = productRepository;
        this.userService = userService;
        this.imageService = imageService;
    }

    public Mono<Product> saveProduct(ProductSaveReqDto productSaveReqDto, FilePart image, Authentication authentication) {
        return Mono.just(new Product.Builder()
                        .title(productSaveReqDto.title())
                        .description(productSaveReqDto.description())
                        .price(productSaveReqDto.price())
                        .userId(AuthUtils.getAuthenticationUser(authentication).getId())
                        .build())
                .flatMap(product -> productRepository.save(product))
                .flatMap(savedProduct ->
                        Mono.justOrEmpty(image)
                                .flatMap(img -> imageService.uploadImage(img, savedProduct.getId(), productSaveReqDto.imageSource())
                                        .thenReturn(savedProduct))
                                .defaultIfEmpty(savedProduct))
                .onErrorMap(e -> new ApiException(ExceptionMessage.INTERNAL_SERVER_ERROR)); // 저장 중 에러 처리
    }

    public Mono<ProductDetailResDto> findProductDetail(String productId, Authentication authentication) {
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.PRODUCT_NOT_FOUND)))
                .flatMap(product ->
                        Mono.zip(Mono.just(product),
                                imageService.findProductImageById(productId),
                                userService.findUserById(product.getUserId())
                        )
                )
                .map(TupleUtils.function((product, image, user) -> new ProductDetailResDto(
                        product.getId(),
                        product.getTitle(),
                        product.getPrice(),
                        product.getDescription(),
                        user.getNickname(),
                        image.getImagePath(),
                        product.getUserId(),
                        AuthUtils.getAuthenticationUser(authentication).getId()))
                );
    }

    public Mono<ProductUpdateResDto> findProductForUpdateForm(String productId) {
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.PRODUCT_NOT_FOUND)))
                .flatMap(product ->
                        Mono.zip(Mono.just(product),
                                imageService.findProductImageById(productId),
                                userService.findUserById(product.getUserId())
                        )
                )
                .map(TupleUtils.function((product, image, user) -> new ProductUpdateResDto(
                        product.getId(),
                        product.getTitle(),
                        product.getPrice(),
                        product.getDescription(),
                        user.getNickname(),
                        image.getImagePath()))
                );
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
                                DateTimeUtils.format(product.getCreatedAt()),
                                image.getThumbnailPath()
                        )));
    }

    public Mono<Product> findProductById(String productId) {
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.PRODUCT_NOT_FOUND)));
    }

    public Mono<Void> updateProduct(ProductUpdateReqDto productUpdateReqDto, FilePart image) {
        return productRepository.findById(productUpdateReqDto.id())
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.PRODUCT_NOT_FOUND)))
                .flatMap(existingProduct -> {
                    Mono<Void> updateProductMono = productRepository.updateProduct(
                            productUpdateReqDto.id(),
                            productUpdateReqDto.description(),
                            productUpdateReqDto.price(),
                            productUpdateReqDto.status()
                    );

                    return Mono.justOrEmpty(image)
                            .flatMap(img ->
                                    updateProductMono
                                            .then(imageService.deleteProductImageById(productUpdateReqDto.id()))
                                            .then(imageService.uploadImage(img, productUpdateReqDto.id(), productUpdateReqDto.imageSource()))
                                            .then()
                            )
                            .switchIfEmpty(updateProductMono);
                })
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.INTERNAL_SERVER_ERROR));
    }

    public Mono<Void> deleteProduct(ProductDeleteReqDto productDeleteReqDto) {
        return productRepository.findById(productDeleteReqDto.id())
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.PRODUCT_NOT_FOUND)))
                .flatMap(existingProduct ->
                        Mono.when(
                                productRepository.deleteById(existingProduct.getId()),
                                imageService.deleteProductImageById(existingProduct.getId())
                        )
                )
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.INTERNAL_SERVER_ERROR));
    }
}