package com.example.processor;

import com.example.annotation.RefreshableRecord;
import com.squareup.javapoet.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;

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
