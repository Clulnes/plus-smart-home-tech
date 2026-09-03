package ru.yandex.practicum.commerce.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.product.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByActiveTrue();
    Optional<Product> findByIdAndActiveTrue(Long id);
    List<Product> findAllByCategoryIdAndActiveTrue(Long categoryId);
    List<Product> findAllByNameContainingIgnoreCaseAndActiveTrue(String query);
}