package com.akcay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.akcay.entity.Todo;
import com.akcay.repository.TodoRepository;

@Component
public class CustomCommandLineRunner implements CommandLineRunner {

	private final Logger logger = LoggerFactory.getLogger(CustomCommandLineRunner.class);

	@Autowired
	private TodoRepository todoRepository;

	@Override
	public void run(final String... arg0) throws Exception {
		this.logger.info("COMMAND LINE RUNNER");
		this.logger.info(this.todoRepository.save(new Todo("first todo")).toString());
		this.logger.info(this.todoRepository.save(new Todo("second todo")).toString());
		this.logger.info(this.todoRepository.save(new Todo("third todo")).toString());
	}

}
