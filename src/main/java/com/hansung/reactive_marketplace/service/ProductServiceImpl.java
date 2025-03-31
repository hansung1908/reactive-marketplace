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
import com.hansung.reactive_marketplace.redis.RedisCacheManager;
import com.hansung.reactive_marketplace.repository.ProductRepository;
import com.hansung.reactive_marketplace.util.AuthUtils;
import com.hansung.reactive_marketplace.util.DateTimeUtils;
import org.springframework.data.domain.Sort;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final UserService userService;

    private final ImageService imageService;

    private final RedisCacheManager redisCacheManager;

    private final TransactionalOperator transactionalOperator;

    public ProductServiceImpl(ProductRepository productRepository,
                              UserService userService,
                              ImageService imageService,
                              RedisCacheManager redisCacheManager,
                              TransactionalOperator transactionalOperator) {
        this.productRepository = productRepository;
        this.userService = userService;
        this.imageService = imageService;
        this.redisCacheManager = redisCacheManager;
        this.transactionalOperator = transactionalOperator;
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
                .as(transactionalOperator::transactional)
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.INTERNAL_SERVER_ERROR));
    }

//    public Mono<ProductDetailResDto> findProductDetail(String productId, Authentication authentication) {
//        return redisCacheManager.getOrFetch(
//                "product:" + productId,
//                ProductDetailResDto.class,
//                productRepository.findById(productId)
//                        .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.PRODUCT_NOT_FOUND)))
//                        .flatMap(product ->
//                                Mono.zip(Mono.just(product),
//                                        imageService.findProductImageByIdWithCache(productId),
//                                        userService.findUserById(product.getUserId())
//                                )
//                        )
//                        .map(TupleUtils.function((product, image, user) -> new ProductDetailResDto(
//                                product.getId(),
//                                product.getTitle(),
//                                product.getPrice(),
//                                product.getDescription(),
//                                user.getNickname(),
//                                image.getImagePath(),
//                                product.getUserId(),
//                                AuthUtils.getAuthenticationUser(authentication).getId()))
//                        ),
//                Duration.ofHours(1)
//        );
//    }

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
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.PRODUCT_NOT_FOUND)))
                .flatMap(product ->
                        Mono.zip(
                                Mono.just(product),
                                imageService.findProductImageById(productId),
                                userService.findUserById(product.getUserId())
                        )
                )
                .map(TupleUtils.function((product, image, user) ->
                        new ProductUpdateResDto(
                                product.getId(),
                                product.getTitle(),
                                product.getPrice(),
                                product.getDescription(),
                                user.getNickname(),
                                image.getImagePath()
                        )
                ));
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

    public Mono<Void> updateProduct(ProductUpdateReqDto productUpdateReqDto, FilePart image, Authentication authentication) {
        return productRepository.findById(productUpdateReqDto.id())
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.PRODUCT_NOT_FOUND)))
                .flatMap(product -> productRepository.updateProduct(
                                productUpdateReqDto.id(),
                                productUpdateReqDto.description(),
                                productUpdateReqDto.price(),
                                productUpdateReqDto.status()
                        )
                )
                .then(Mono.justOrEmpty(image)
                        .flatMap(img -> imageService.deleteProductImageById(productUpdateReqDto.id())
                                .then(imageService.uploadImage(img, productUpdateReqDto.id(), productUpdateReqDto.imageSource()))
                        )
                )
                .as(transactionalOperator::transactional)
                .then(Mono.defer(() -> redisCacheManager.deleteValue("product:" + productUpdateReqDto.id())))
                .then()
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.INTERNAL_SERVER_ERROR));
    }

    public Mono<Void> deleteProduct(ProductDeleteReqDto productDeleteReqDto, Authentication authentication) {
        return productRepository.findById(productDeleteReqDto.id())
                .switchIfEmpty(Mono.error(new ApiException(ExceptionMessage.PRODUCT_NOT_FOUND)))
                .flatMap(product -> productRepository.deleteById(product.getId()).thenReturn(product))
                .flatMap(product -> imageService.deleteProductImageById(product.getId()))
                .as(transactionalOperator::transactional)
                .then(Mono.defer(() ->redisCacheManager.deleteValue("product:" + productDeleteReqDto.id())))
                .then()
                .onErrorMap(e -> !(e instanceof ApiException),
                        e -> new ApiException(ExceptionMessage.INTERNAL_SERVER_ERROR));
    }
}