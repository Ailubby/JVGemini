package com.my.jvgemini;

import com.my.jvgemini.util.ConverVideoUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class JvgeminiApplicationTests {

	@Autowired
	ConverVideoUtils converVideoUtils;

	@Test
	public void contextLoads() throws Exception{
		converVideoUtils.testAspect();
	}

}
