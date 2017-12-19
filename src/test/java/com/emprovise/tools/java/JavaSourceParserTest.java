package com.emprovise.tools.java;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.Assert.assertFalse;

public class JavaSourceParserTest {

    @Test
    public void getMethodExpressions() throws FileNotFoundException {

        File file = new File("JAVA_FILE_NAME_WITH_PATH");
        JavaSourceParser javaSourceParser = new JavaSourceParser(file);
        List<Expression> allExpressions = javaSourceParser.getAllExpressions();
        assertFalse(allExpressions.isEmpty());

        List<MethodDeclaration> publicMethods = javaSourceParser.getPublicMethods();
        assertFalse(publicMethods.isEmpty());

        for (MethodDeclaration publicMethod : publicMethods) {
            javaSourceParser.getVariableDeclarations(publicMethod);
        }
    }
}