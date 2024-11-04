package com.hansung.reactive_marketplace.service;

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
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public Mono<Product> saveProduct(ProductSaveReqDto productSaveReqDto, User user) {
        Product product = new Product.Builder()
                .title(productSaveReqDto.getTitle())
                .description(productSaveReqDto.getDescription())
                .price(productSaveReqDto.getPrice())
                .userId(user.getId())
                .build();

        return productRepository.save(product);
    }

    public Mono<ProductDetailResDto> findProductDetail(String productId) {
        return productRepository.findById(productId)
                .flatMap(product -> userRepository.findById(product.getUserId())
                        .map(user -> new ProductDetailResDto(
                                product.getId(),
                                product.getTitle(),
                                product.getPrice(),
                                product.getDescription(),
                                product.getUserId(),
                                user.getNickname()
                        )));
    }

    public Flux<ProductListResDto> findProductList() {
        return productRepository.findProductList(Sort.by(Sort.Direction.DESC, "createdAt"))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<MyProductListResDto> findMyProductList(String userId) {
        return productRepository.findMyProductList(userId, Sort.by(Sort.Direction.DESC, "createdAt"))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> updateProduct(ProductUpdateReqDto productUpdateReqDto) {
        return productRepository.updateProduct(productUpdateReqDto.getId(), productUpdateReqDto.getDescription(), productUpdateReqDto.getPrice(), productUpdateReqDto.getStatus());
    }

    public Mono<Void> deleteProduct(ProductDeleteReqDto productDeleteReqDto) {
        return productRepository.deleteById(productDeleteReqDto.getId());
    }
}