package com.emprovise.tools.java;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @see <a href="https://www.programcreek.com/java-api-examples/index.php?api=com.github.javaparser.ast.expr.MethodCallExpr">MethodCallExpr</a>
 */
public class JavaSourceParser {

    private CompilationUnit compilationUnit;

    public JavaSourceParser(File javaFile) throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(javaFile);
        this.compilationUnit = JavaParser.parse(fileInputStream);
    }

    public String getPackageName() {
        return compilationUnit.getPackageDeclaration().get().getNameAsString();
    }

    public String getPublicClassName() {
        ClassOrInterfaceDeclaration publicClass = getPublicClass();
        return publicClass != null ? publicClass.getNameAsString() : null;
    }

    public List<MethodDeclaration> getPublicMethods() {
        ClassOrInterfaceDeclaration publicClass = getPublicClass();
        return publicClass != null ? publicClass.getMethods() : null;
    }

    public List<Statement> getAllStatements() {
        return compilationUnit.findAll(Statement.class);
    }

    public List<Expression> getAllExpressions() {
        return compilationUnit.findAll(Expression.class);
    }

    public List<Expression> getMethodExpressions(MethodDeclaration method) {
        List<Expression> expressions = new ArrayList<>();
        Optional<BlockStmt> body = method.getBody();

        if(body.isPresent()) {
            BlockStmt blockStmt = body.get();
            for (Statement statement : blockStmt.getStatements()) {
                processStatements(statement, expressions);
            }
        }

        return expressions;
    }

    public List<VariableDeclarator> getVariableDeclarations(MethodDeclaration method) {
        List<VariableDeclarator> variables = new ArrayList<>();
        List<Expression> expressions = getMethodExpressions(method);

        for (Expression expression : expressions) {
            if(expression.isVariableDeclarationExpr()) {
                VariableDeclarationExpr varExpr = expression.asVariableDeclarationExpr();
                variables.addAll(varExpr.getVariables());
            }
        }
        return variables;
    }

    private void processStatements(NodeList<Statement> statements, List<Expression> collector) {
        for (Statement stmt : statements) {
            processStatements(stmt, collector);
        }
    }

    private void processStatements(Statement statement, List<Expression> collector) {

        if(statement.isExpressionStmt()) {
            Expression expression = statement.asExpressionStmt().getExpression();
            collector.add(expression);
        } else if(statement.isIfStmt()) {
            IfStmt ifStmt = statement.asIfStmt();
            processStatements(ifStmt.getThenStmt(), collector);

            if (ifStmt.getElseStmt().isPresent()) {
                processStatements(ifStmt.getElseStmt().get(), collector);
            }
        } else if(statement.isForeachStmt()) {
            ForeachStmt foreachStmt = statement.asForeachStmt();
            processStatements(foreachStmt.getBody(), collector);
        } else if(statement.isForStmt()) {
            ForStmt forStmt = statement.asForStmt();
            processStatements(forStmt.getBody(), collector);
        } else if(statement.isBlockStmt()) {
            BlockStmt blockStmt = statement.asBlockStmt();
            processStatements(blockStmt.getStatements(), collector);
        } else if(statement.isDoStmt()) {
            DoStmt doStmt = statement.asDoStmt();
            processStatements(doStmt.getBody(), collector);
        } else if(statement.isWhileStmt()) {
            WhileStmt whileStmt = statement.asWhileStmt();
            processStatements(whileStmt.getBody(), collector);
        } else if(statement.isReturnStmt()) {
            ReturnStmt returnStmt = statement.asReturnStmt();
            if(returnStmt.getExpression().isPresent()) {
                collector.add(returnStmt.getExpression().get());
            }
        } else if(statement.isSwitchEntryStmt()) {
            SwitchEntryStmt switchEntryStmt = statement.asSwitchEntryStmt();
            processStatements(switchEntryStmt.getStatements(), collector);
        } else if(statement.isSwitchStmt()) {
            SwitchStmt switchStmt = statement.asSwitchStmt();
            NodeList<SwitchEntryStmt> entries = switchStmt.getEntries();
            for (SwitchEntryStmt entryStmt : entries) {
                processStatements(entryStmt, collector);
            }
        } else if(statement.isSynchronizedStmt()) {
            SynchronizedStmt synchronizedStmt = statement.asSynchronizedStmt();
            processStatements(synchronizedStmt.getBody(), collector);
        } else if(statement.isTryStmt()) {
            TryStmt tryStmt = statement.asTryStmt();
            processStatements(tryStmt.getTryBlock(), collector);

            NodeList<CatchClause> catchClauses = tryStmt.getCatchClauses();
            for (CatchClause catchClause : catchClauses) {
                processStatements(catchClause.getBody(), collector);
            }

            if(tryStmt.getFinallyBlock().isPresent()) {
                processStatements(tryStmt.getFinallyBlock().get(), collector);
            }

        } /*else {
            // Ignore
        }*/
    }

    private void processExpression(Expression expression) {
        if(expression.isVariableDeclarationExpr()) {
            VariableDeclarationExpr varExpr = expression.asVariableDeclarationExpr();
            NodeList<VariableDeclarator> variables = varExpr.getVariables();

            for (VariableDeclarator var : variables) {
                System.out.println("--------------------- " + var.getName() + " === " + var.getType());
            }
        } else if (expression.isAssignExpr()) {
            AssignExpr assignExpr = expression.asAssignExpr();
            Expression target = assignExpr.getTarget();
            Expression value = assignExpr.getValue();
            System.out.println(String.format("--------------------- Target: %s , Value: %s", target, value));
        } else if(expression.isMethodCallExpr()) {
            MethodCallExpr methodCallExpr = expression.asMethodCallExpr();
            SimpleName methodName = methodCallExpr.getName();
            NodeList<Expression> arguments = methodCallExpr.getArguments();
            Optional<NodeList<Type>> typeArguments = methodCallExpr.getTypeArguments();
            Optional<Expression> scope = methodCallExpr.getScope();
        }
    }

    public List<MethodCallExpr> getMethodCalls(MethodDeclaration method) {
        List<MethodCallExpr> exprList = new ArrayList<>();
        method.accept(new MethodCallCollector(), exprList);
        return exprList;
    }

    private ClassOrInterfaceDeclaration getPublicClass() {
        for (TypeDeclaration<?> type : compilationUnit.getTypes()) {
            if (type instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration classDec = (ClassOrInterfaceDeclaration) type;

                if (classDec.getModifiers().contains(Modifier.PUBLIC)) {
                    return classDec;
                }
            }
        }

        return null;
    }

    /**
     * Simple visitor implementation for visiting MethodDeclaration nodes.
     */
    private static class MethodVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(MethodDeclaration n, Void arg) {
            System.out.println(n.getName());
            super.visit(n, arg);
        }
    }

    private static class MethodCallCollector extends VoidVisitorAdapter<List<MethodCallExpr>> {
        @Override
        public void visit(MethodCallExpr expr, List<MethodCallExpr> collector) {
            // Found a method call
            Optional<Expression> scope = expr.getScope();
            if(scope.isPresent()) {
                System.out.println(scope.get() + " - " + expr.getName());
            } else {
                System.out.println("EMPTY - " + expr.getName());
            }

            super.visit(expr, collector);
            collector.add(expr);
        }
    }

    private static class AssignmentExprCollector extends VoidVisitorAdapter<List<AssignExpr>> {
        @Override
        public void visit(AssignExpr expr, List<AssignExpr> collector) {
            System.out.println("EMPTY - " + expr.getTarget());
            super.visit(expr, collector);
            collector.add(expr);
        }
    }
}
