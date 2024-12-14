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
import com.hansung.reactive_marketplace.dto.response.ProductUpdateResDto;
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

    public Mono<Product> saveProduct(ProductSaveReqDto productSaveReqDto, FilePart image, User user) {
        Product product = new Product.Builder()
                .title(productSaveReqDto.title())
                .description(productSaveReqDto.description())
                .price(productSaveReqDto.price())
                .userId(user.getId())
                .build();

        return productRepository.save(product)
                .flatMap(savedProduct -> {
                    if (image != null) {
                        return imageService.uploadImage(image, savedProduct.getId(), productSaveReqDto.imageSource())
                                .thenReturn(savedProduct);
                    }
                    return Mono.just(savedProduct);
                });
    }

    public Mono<ProductDetailResDto> findProductDetail(String productId, Authentication authentication) {
        Mono<Product> productMono = productRepository.findById(productId);
        Mono<Image> imageMono = imageService.findProductImageById(productId);

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
        Mono<Product> productMono = productRepository.findById(productId);
        Mono<Image> imageMono = imageService.findProductImageById(productId);

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
        Mono<Void> updateProductMono = productRepository.updateProduct(
                productUpdateReqDto.id(),
                productUpdateReqDto.description(),
                productUpdateReqDto.price(),
                productUpdateReqDto.status()
        );

        // 반환값이 없는 상태에서 조건문을 사용하기 위해 분리
        if (image != null) {
            return updateProductMono.then(
                    imageService.deleteProductImageById(productUpdateReqDto.id())
                            .then(imageService.uploadImage(image, productUpdateReqDto.id(), productUpdateReqDto.imageSource()))
                            .then() // Mono<Void> 반환을 위한 then
            );
        } else {
            return updateProductMono;
        }
    }

    public Mono<Void> deleteProduct(ProductDeleteReqDto productDeleteReqDto) {
        return productRepository.deleteById(productDeleteReqDto.id())
                .then(imageService.deleteProductImageById(productDeleteReqDto.id()));
    }
}