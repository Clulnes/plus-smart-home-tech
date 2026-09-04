package ru.yandex.practicum.commerce.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.product.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}