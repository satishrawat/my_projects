package com.akcay.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.akcay.TodoAppApplication;
import com.akcay.entity.Todo;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TodoAppApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TodoControllerIntegrationTests {
	@Autowired
	private TestRestTemplate restTemplate;
	@Autowired
	private ObjectMapper mapper;

	@LocalServerPort
	private int port;

	private String URL;

	@Before
	public void before() {
		this.URL = "http://localhost:" + this.port;

	}

	@Test
	public void _findAllTodos() throws JsonParseException, JsonMappingException, IOException {
		final String response = this.restTemplate.getForObject(this.URL + "/todos", String.class);
		final List<Todo> todos = Arrays.asList(this.mapper.readValue(response, Todo[].class));

		assertThat(todos.size()).isEqualTo(3);
	}

	@Test
	public void findOne() throws JsonParseException, JsonMappingException, IOException {
		final String response = this.restTemplate.getForObject(this.URL + "/todos/1", String.class);
		final Todo todo = this.mapper.readValue(response, Todo.class);

		assertThat(todo.getId()).isEqualTo(1);
		assertThat(todo.getDescription()).isEqualToIgnoringCase("first todo");
		assertThat(todo.getCreatedDate()).isBefore(new Date());
		assertThat(todo.getLastModifiedBy()).isNull();
	}

	@Test
	public void addTodo() {
		final Todo newTodo = new Todo("This is a new todo");

		final ResponseEntity<Todo> todo = this.restTemplate.postForEntity(this.URL + "/todos", newTodo, Todo.class);
		assertThat(todo.getHeaders().getLocation().toString()).isEqualTo(this.URL + "/todos/4");
	}

	@Test
	public void updateTodo() {
		final Todo updatedTodo = new Todo("This is an updated todo");
		final HttpEntity<Todo> entity = new HttpEntity<Todo>(updatedTodo);
		final Map<String, String> urlVariables = new HashMap<String, String>();
		urlVariables.put("id", "1");
		final ResponseEntity<Todo> responseEntity = this.restTemplate.exchange(this.URL + "/todos/{id}", HttpMethod.PUT,
				entity, Todo.class, urlVariables);

		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseEntity.getBody().getId()).isEqualTo(1L);
		assertThat(responseEntity.getBody().getDescription()).isEqualTo("This is an updated todo");
	}

	@Test
	public void deleteTodo() {
		this.restTemplate.delete(this.URL + "/todos/2");
		final String response = this.restTemplate.getForObject(this.URL + "/todos/2", String.class);

		assertThat(response).isNull();
	}
}
