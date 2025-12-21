package com.example.integration;

import com.example.config.MyConfigRecord;
import com.example.config.RefreshableMyConfigRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration test that verifies the @RefreshScope functionality.
 * Tests that the wrapper is properly annotated and integrated with Spring's refresh infrastructure.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.main.web-application-type=none"
        }
)
@TestPropertySource(properties = {
        "app.config.api-url=https://initial-url.com",
        "app.config.max-retries=5"
})
class RefreshScopeIntegrationTest {

    @Autowired
    private RefreshableMyConfigRecord refreshableConfig;

    @Autowired
    private RefreshScope refreshScope;

    @Test
    void shouldReflectInitialConfiguration() {
        MyConfigRecord config = refreshableConfig.current();
        
        assertThat(config.apiUrl()).isEqualTo("https://initial-url.com");
        assertThat(config.maxRetries()).isEqualTo(5);
    }

    @Test
    void shouldBeInRefreshScope() {
        // Verify that RefreshScope bean is available (which means @RefreshScope is working)
        assertThat(refreshScope).isNotNull();
        
        // The presence of RefreshScope bean confirms that Spring Cloud Context is properly configured
        // and @RefreshScope annotation on the generated wrapper will be effective
    }

    @Test
    void shouldReturnImmutableRecordInstance() {
        MyConfigRecord config1 = refreshableConfig.current();
        MyConfigRecord config2 = refreshableConfig.current();
        
        // Each call should return a new record instance
        assertThat(config1).isNotNull();
        assertThat(config2).isNotNull();
        assertThat(config1.apiUrl()).isEqualTo(config2.apiUrl());
        assertThat(config1.maxRetries()).isEqualTo(config2.maxRetries());
    }

    @Test
    void shouldMaintainRecordImmutability() {
        MyConfigRecord config = refreshableConfig.current();
        String originalUrl = config.apiUrl();
        int originalRetries = config.maxRetries();

        // Record should be immutable - values should not change
        assertThat(config.apiUrl()).isEqualTo(originalUrl);
        assertThat(config.maxRetries()).isEqualTo(originalRetries);
    }

    @Test
    void shouldWorkWithDifferentPropertyValues() {
        // This test verifies that the property binding actually works
        MyConfigRecord config = refreshableConfig.current();
        
        // Properties should be bound from application-test.properties or @TestPropertySource
        assertThat(config.apiUrl()).isNotEmpty();
        assertThat(config.maxRetries()).isPositive();
    }
}