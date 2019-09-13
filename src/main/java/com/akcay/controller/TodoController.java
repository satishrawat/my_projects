package com.akcay.controller;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.akcay.entity.Todo;
import com.akcay.repository.TodoRepository;

@RestController
@RequestMapping("todos")
public class TodoController {

	@Autowired
	private TodoRepository todoRepository;

	@GetMapping
	public List<Todo> getAllTodos() {
		return (List<Todo>) this.todoRepository.findAll();
	}

	@GetMapping("/{id}")
	public Todo getTodo(@PathVariable("id") final Todo todo) {
		return todo;
	}

	@PostMapping
	public ResponseEntity<Todo> addTodo(@RequestBody final Todo todo) {
		final Todo newTodo = this.todoRepository.save(todo);

		final URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(newTodo.getId()).toUri();

		return ResponseEntity.created(location).build();
	}

	@PutMapping("/{id}")
	public Todo updateTodo(@PathVariable("id") final Long id, @RequestBody final Todo todo) {
		todo.setId(id);
		return this.todoRepository.save(todo);
	}

	@DeleteMapping("/{id}")
	public void deleteTodo(@PathVariable("id") final Long id) {
		this.todoRepository.delete(id);
	}
}
