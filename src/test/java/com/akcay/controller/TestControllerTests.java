package com.akcay.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
@RunWith(SpringRunner.class)
@WebMvcTest(TestController.class)
public class TestControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void test() throws Exception {
		this.mockMvc.perform(get("/test?text=asdf")).andExpect(status().isOk()).andExpect(content().string("asdf"));
	}
}
