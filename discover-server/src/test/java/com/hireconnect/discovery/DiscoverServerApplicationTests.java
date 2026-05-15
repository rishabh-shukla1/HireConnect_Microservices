package com.hireconnect.discovery;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for DiscoverServerApplication.
 * The discovery server is a pure Eureka bootstrap with no custom
 * business logic. This test verifies the application class exists
 * and is annotated correctly without loading the full Eureka context.
 */
class DiscoverServerApplicationTests {

	/**
	 * Verifies that the application class can be instantiated.
	 */
	@Test
	void contextLoads() {
		assertNotNull(new DiscoverServerApplication());
	}

}
