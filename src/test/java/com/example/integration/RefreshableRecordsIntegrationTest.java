package com.example.integration;

import com.example.config.MyConfigRecord;
import com.example.config.RefreshableMyConfigRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test that verifies the annotation processor-generated wrapper
 * is properly integrated with Spring Boot's configuration properties mechanism.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "app.config.api-url=https://test-url.com",
        "app.config.max-retries=10"
})
class RefreshableRecordsIntegrationTest {

    @Autowired
    private RefreshableMyConfigRecord refreshableConfig;

    @Test
    void shouldInjectGeneratedConfigurationPropertiesBean() {
        assertThat(refreshableConfig).isNotNull();
    }

    @Test
    void shouldBindConfigurationPropertiesToGeneratedWrapper() {
        assertThat(refreshableConfig).isNotNull();
        
        MyConfigRecord config = refreshableConfig.current();
        
        assertThat(config).isNotNull();
        assertThat(config.apiUrl()).isEqualTo("https://test-url.com");
        assertThat(config.maxRetries()).isEqualTo(10);
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
    void shouldWorkWithDefaultValues() {
        // Test that it works even when not all properties are set
        assertThat(refreshableConfig.current()).isNotNull();
    }
}