package com.example.processor;

import com.example.annotation.RefreshableRecord;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * Unit tests for RefreshableRecordProcessor using Google's compile-testing library.
 * Tests the annotation processor in isolation to verify code generation.
 */
class RefreshableRecordProcessorTest {

    @Test
    void shouldGenerateRefreshableWrapper() {
        JavaFileObject testRecord = JavaFileObjects.forSourceString(
                "com.example.test.TestConfig",
                """
                package com.example.test;
                
                import com.example.annotation.RefreshableRecord;
                
                @RefreshableRecord(prefix = "test.config")
                public record TestConfig(String host, int port) {}
                """
        );

        Compilation compilation = javac()
                .withProcessors(new RefreshableRecordProcessor())
                .compile(testRecord);

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("com.example.test.RefreshableTestConfig");
    }

    @Test
    void shouldGenerateWrapperForRecordWithMultipleTypes() {
        JavaFileObject testRecord = JavaFileObjects.forSourceString(
                "com.example.test.ComplexConfig",
                """
                package com.example.test;
                
                import com.example.annotation.RefreshableRecord;
                import java.util.List;
                
                @RefreshableRecord(prefix = "complex")
                public record ComplexConfig(
                    String name,
                    int count,
                    boolean enabled,
                    long timeout,
                    double ratio,
                    List<String> items
                ) {}
                """
        );

        Compilation compilation = javac()
                .withProcessors(new RefreshableRecordProcessor())
                .compile(testRecord);

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("com.example.test.RefreshableComplexConfig");
    }

    @Test
    void shouldFailForNonRecordType() {
        JavaFileObject testClass = JavaFileObjects.forSourceString(
                "com.example.test.NotARecord",
                """
                package com.example.test;
                
                import com.example.annotation.RefreshableRecord;
                
                @RefreshableRecord(prefix = "test")
                public class NotARecord {
                    private String value;
                }
                """
        );

        Compilation compilation = javac()
                .withProcessors(new RefreshableRecordProcessor())
                .compile(testClass);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("@RefreshableRecord must be used on a Java Record");
    }

    @Test
    void shouldGenerateWrapperWithCorrectPrefix() {
        JavaFileObject testRecord = JavaFileObjects.forSourceString(
                "com.example.test.PrefixTest",
                """
                package com.example.test;
                
                import com.example.annotation.RefreshableRecord;
                
                @RefreshableRecord(prefix = "app.custom.prefix")
                public record PrefixTest(String value) {}
                """
        );

        Compilation compilation = javac()
                .withProcessors(new RefreshableRecordProcessor())
                .compile(testRecord);

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("com.example.test.RefreshablePrefixTest");
    }

    @Test
    void shouldGenerateWrapperForEmptyRecord() {
        JavaFileObject testRecord = JavaFileObjects.forSourceString(
                "com.example.test.EmptyConfig",
                """
                package com.example.test;
                
                import com.example.annotation.RefreshableRecord;
                
                @RefreshableRecord(prefix = "empty")
                public record EmptyConfig() {}
                """
        );

        Compilation compilation = javac()
                .withProcessors(new RefreshableRecordProcessor())
                .compile(testRecord);

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("com.example.test.RefreshableEmptyConfig");
    }
    
    @Test
    void shouldGenerateWrapperForNestedRecord() {
        JavaFileObject testRecord = JavaFileObjects.forSourceString(
                "com.example.test.nested.NestedConfig",
                """
                package com.example.test.nested;
                
                import com.example.annotation.RefreshableRecord;
                
                @RefreshableRecord(prefix = "nested.config")
                public record NestedConfig(String value, int count) {}
                """
        );

        Compilation compilation = javac()
                .withProcessors(new RefreshableRecordProcessor())
                .compile(testRecord);

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("com.example.test.nested.RefreshableNestedConfig");
    }
}
