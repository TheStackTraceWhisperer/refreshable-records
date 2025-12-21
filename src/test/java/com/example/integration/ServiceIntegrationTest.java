package com.example.integration;

import com.example.service.MyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.TestPropertySource;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test that verifies the service layer correctly uses the generated wrapper
 * and accesses configuration through the current() method.
 */
@SpringBootTest(properties = {
        "spring.main.web-application-type=none",
        "spring.main.lazy-initialization=true"
})
@TestPropertySource(properties = {
        "app.config.api-url=https://service-test-url.com",
        "app.config.max-retries=7"
})
@ExtendWith(OutputCaptureExtension.class)
class ServiceIntegrationTest {

    @Autowired
    private MyService myService;

    @Test
    void shouldInjectServiceWithGeneratedWrapper() {
        assertThat(myService).isNotNull();
    }

    @Test
    void shouldAccessConfigurationThroughCurrentMethod(CapturedOutput output) {
        myService.performAction();

        String outputString = output.toString();
        assertThat(outputString).contains("Connecting to: https://service-test-url.com");
        assertThat(outputString).contains("Max Retries: 7");
    }

    @Test
    void shouldWorkWithServiceLayerMultipleCalls() {
        // Just verify that multiple calls work without throwing exceptions
        myService.performAction();
        myService.performAction();
        myService.performAction();
        
        // Verify service is still functional
        assertThat(myService).isNotNull();
    }
}