package com.example.gateway;

import com.example.gateway.service.GatewayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public class GatewayApplicationTests {

	@Autowired
	private GatewayService gatewayService;

	@Test
	public void contextLoads() {
		// Basic test to verify that the application context loads successfully
	}
}
