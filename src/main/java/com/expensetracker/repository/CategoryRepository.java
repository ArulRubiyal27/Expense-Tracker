package com.expensetracker.repository;

import com.expensetracker.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    java.util.List<Category> findAllByOrderByNameAsc();

@Query("SELECT c.id FROM Category c WHERE c.name = :name")
    Optional<Long> findIdByName(@Param("name") String name);

}
