package com.emprovise.tools.java;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class JavaCodeBuilder {

    private String className;
    private String packageName;
    private TypeSpec.Builder classBuilder;

    public JavaCodeBuilder(String packageName, String className) {
        this.className = className;
        this.packageName = packageName;
        this.classBuilder = TypeSpec.classBuilder(className)
                                    .addModifiers(Modifier.PUBLIC);
    }

    public MethodSpec mainMethod(String code) {
        return getMainMethodBuilder()
                .addCode(code)
                .build();
    }

    public MethodSpec mainMethod(String statement, Object... args) {
        return getMainMethodBuilder()
                .addStatement(statement, args)
                .build();
    }

    public void addMethods(MethodSpec... methodSpec) {
        if(methodSpec != null && methodSpec.length > 1) {
            classBuilder.addMethods(Arrays.asList(methodSpec));
        } else if(methodSpec != null && methodSpec.length == 1) {
            classBuilder.addMethod(methodSpec[0]);
        }
    }

    public File generateJavaFile() throws IOException {
        File file = new File(className);
        JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build()).build();
        javaFile.writeTo(file);
        return file;
    }

    private MethodSpec.Builder getMainMethodBuilder() {
        return MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args");
    }
}
