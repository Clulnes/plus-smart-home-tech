package ru.yandex.practicum.commerce.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.product.model.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByActiveTrue();
    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String query);
}