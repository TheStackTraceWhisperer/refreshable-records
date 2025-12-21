# refreshable-records

Here is the complete implementation of the Annotation Processor for Refreshable Records using Spring Boot 3.x, Spring Cloud, and JavaPoet. This implementation generates a @ConfigurationProperties wrapper instead of using manual binding.
Project Structure Overview
 * pom.xml: Dependencies for Spring Boot, Cloud, and JavaPoet.
 * RefreshableRecord.java: The marker annotation.
 * RefreshableRecordProcessor.java: The compiler-time generator.
 * SPI File: Registers the processor.
 * Usage Examples: A sample record and application config.
1. Build Configuration
File: pom.xml
Ensure you have the Spring Cloud BOM (Bill of Materials) configured so versions match your Spring Boot version.
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>refreshable-records</artifactId>
    <version>1.0.0-SNAPSHOT</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version> </parent>

    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-context</artifactId>
        </dependency>

        <dependency>
            <groupId>com.squareup</groupId>
            <artifactId>javapoet</artifactId>
            <version>1.13.0</version>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <annotationProcessorPaths>
                        </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>

2. The Annotation
File: src/main/java/com/example/annotation/RefreshableRecord.java
package com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Record to have a Refreshable ConfigurationProperties wrapper generated.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface RefreshableRecord {
    /**
     * The property prefix to bind to (e.g., "app.feature").
     */
    String prefix();
}

3. The Annotation Processor
File: src/main/java/com/example/processor/RefreshableRecordProcessor.java
This processor inspects the Record fields and generates a mirrored class with @ConfigurationProperties, @RefreshScope, and a conversion method.
package com.example.processor;

import com.example.annotation.RefreshableRecord;
import com.squareup.javapoet.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("com.example.annotation.RefreshableRecord")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class RefreshableRecordProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(RefreshableRecord.class)) {
            if (element.getKind() == ElementKind.RECORD) {
                generateRefreshableWrapper((TypeElement) element);
            } else {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "@RefreshableRecord must be used on a Java Record", element);
            }
        }
        return true;
    }

    private void generateRefreshableWrapper(TypeElement recordElement) {
        String packageName = processingEnv.getElementUtils().getPackageOf(recordElement).getQualifiedName().toString();
        String recordSimpleName = recordElement.getSimpleName().toString();
        String wrapperName = "Refreshable" + recordSimpleName;
        String prefix = recordElement.getAnnotation(RefreshableRecord.class).prefix();

        ClassName recordType = ClassName.get(packageName, recordSimpleName);

        // 1. Prepare Constructor and Fields
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        List<FieldSpec> fields = new ArrayList<>();
        
        // Prepare return statement for current() method: return new RecordType(arg1, arg2...)
        StringBuilder returnStmt = new StringBuilder("return new $T(");
        List<Object> returnArgs = new ArrayList<>();
        returnArgs.add(recordType);

        // 2. Iterate over Record Components
        List<? extends Element> enclosedElements = recordElement.getEnclosedElements();
        
        // Filter specifically for Record Components to match the canonical constructor
        List<VariableElement> recordComponents = enclosedElements.stream()
                .filter(e -> e.getKind() == ElementKind.RECORD_COMPONENT)
                .map(e -> (VariableElement) e)
                .toList();

        for (int i = 0; i < recordComponents.size(); i++) {
            VariableElement component = recordComponents.get(i);
            String name = component.getSimpleName().toString();
            TypeName type = TypeName.get(component.asType());

            // Field
            fields.add(FieldSpec.builder(type, name, Modifier.PRIVATE, Modifier.FINAL).build());

            // Constructor Param
            constructor.addParameter(type, name);
            // Constructor Assignment
            constructor.addStatement("this.$N = $N", name, name);

            // Return statement builder
            returnStmt.append("$N");
            returnArgs.add(name);
            if (i < recordComponents.size() - 1) {
                returnStmt.append(", ");
            }
        }
        returnStmt.append(")");

        // 3. Create conversion method
        MethodSpec currentMethod = MethodSpec.methodBuilder("current")
                .addModifiers(Modifier.PUBLIC)
                .returns(recordType)
                .addStatement(returnStmt.toString(), returnArgs.toArray())
                .build();

        // 4. Build Class
        TypeSpec classSpec = TypeSpec.classBuilder(wrapperName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Component.class) // Make it discoverable
                .addAnnotation(RefreshScope.class) // Make it refreshable
                .addAnnotation(AnnotationSpec.builder(ConfigurationProperties.class)
                        .addMember("prefix", "$S", prefix)
                        .build())
                .addFields(fields)
                .addMethod(constructor.build())
                .addMethod(currentMethod)
                .addJavadoc("Generated Refreshable Wrapper for {@link $T}.\n", recordType)
                .build();

        // 5. Write File
        JavaFile javaFile = JavaFile.builder(packageName, classSpec)
                .indent("    ")
                .build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, 
                    "Failed to generate wrapper: " + e.getMessage());
        }
    }
}

4. Processor Registration (SPI)
File: src/main/resources/META-INF/services/javax.annotation.processing.Processor
This file tells the Java compiler to use your processor.
com.example.processor.RefreshableRecordProcessor

5. Example Usage
File: src/main/resources/application.properties
# Management endpoints to expose /actuator/refresh
management.endpoints.web.exposure.include=refresh

# Example configuration
app.config.max-retries=5
app.config.api-url=https://initial-url.com

File: src/main/java/com/example/config/MyConfigRecord.java
package com.example.config;

import com.example.annotation.RefreshableRecord;

@RefreshableRecord(prefix = "app.config")
public record MyConfigRecord(
    String apiUrl, 
    int maxRetries
) {}

File: src/main/java/com/example/service/MyService.java
Notice that we inject the generated class (RefreshableMyConfigRecord), not the record itself.
package com.example.service;

import com.example.config.MyConfigRecord;
import com.example.config.RefreshableMyConfigRecord; // This is generated
import org.springframework.stereotype.Service;

@Service
public class MyService {

    private final RefreshableMyConfigRecord properties;

    public MyService(RefreshableMyConfigRecord properties) {
        this.properties = properties;
    }

    public void performAction() {
        // Calling .current() fetches the specific immutable Record instance
        // representing the state of configuration at this exact moment.
        MyConfigRecord currentConfig = properties.current();
        
        System.out.println("Connecting to: " + currentConfig.apiUrl());
        System.out.println("Max Retries: " + currentConfig.maxRetries());
    }
}

