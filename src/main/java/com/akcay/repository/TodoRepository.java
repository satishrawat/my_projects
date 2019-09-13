package com.akcay.repository;

import org.springframework.data.repository.CrudRepository;

import com.akcay.entity.Todo;

public interface TodoRepository extends CrudRepository<Todo, Long> {
	Todo findOneByDescription(String description);
}
