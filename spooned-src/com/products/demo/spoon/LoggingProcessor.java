package com.products.demo.spoon;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtTypeReference;
public class LoggingProcessor extends AbstractProcessor<CtMethod<?>> {
    @Override
    public boolean isToBeProcessed(CtMethod<?> method) {
        boolean isRestController = (method.getDeclaringType() != null) && (method.getDeclaringType().getAnnotation(RestController.class) != null);
        if (isRestController) {
            System.out.println((("Processing method: " + method.getDeclaringType().getSimpleName()) + ".") + method.getSimpleName());
        }
        return isRestController;
    }

    @Override
    public void process(CtMethod<?> method) {
        if (method.getBody() == null) {
            System.out.println(("Skipping method " + method.getSimpleName()) + ": no body");
            return;
        }
        String operation = determineOperationType(method);
        if (operation == null) {
            System.out.println(("Skipping method " + method.getSimpleName()) + ": no valid operation type");
            return;
        }
        String endpoint = determineEndpoint(method);
        System.out.println((((("Method: " + method.getSimpleName()) + ", Operation: ") + operation) + ", Endpoint: ") + endpoint);
        // Remove existing logging statements to avoid duplicates
        method.getBody().getStatements().removeIf(statement -> statement.toString().contains("UserProfileLogger.logRequest"));
        // Special handling for getAllProducts
        // Special handling for getAllProducts
        if (method.getSimpleName().equals("getAllProducts") && method.getParameters().isEmpty()) {
            // Inject logging after productsService.getAllProducts()
            String injected = (((((((((("java.util.List<com.products.demo.Model.Products> products = productsService.getAllProducts();" + "for (int i = 0; i < products.size(); i++) {") + "com.products.demo.spoon.UserProfileLogger.logRequest(") + "org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication(),") + "\"") + escape(operation)) + "\",") + "\"") + escape(endpoint)) + "\",") + "products.get(i).getId());") + "}";
            CtCodeSnippetStatement snippet = getFactory().Code().createCodeSnippetStatement(injected);
            method.getBody().insertBegin(snippet);
            System.out.println("Injected logging for getAllProducts: " + injected);
            return;
        }
        // Special handling for findExpensiveProducts
        if (method.getSimpleName().equals("findExpensiveProducts") && (method.getParameters().size() == 1)) {
            CtParameter<?> param = method.getParameters().get(0);// priceThreshold

            String paramName = param.getSimpleName();
            String injected = (((((((((((("java.util.List<com.products.demo.Model.Products> products = productsService.findExpensiveProducts(" + paramName) + ");") + "for (int i = 0; i < products.size(); i++) {") + "com.products.demo.spoon.UserProfileLogger.logRequest(") + "org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication(),") + "\"") + escape(operation)) + "\",") + "\"") + escape(endpoint)) + "\",") + "products.get(i).getId());") + "}";
            CtCodeSnippetStatement snippet = getFactory().Code().createCodeSnippetStatement(injected);
            method.getBody().insertBegin(snippet);
            System.out.println("Injected logging for findExpensiveProducts: " + injected);
            return;
        }
        // Standard handling for other methods
        CtParameter<?> pathVariableParam = method.getParameters().stream().filter(p -> p.getAnnotation(PathVariable.class) != null).findFirst().orElse(null);
        String productIdParam;
        if (pathVariableParam != null) {
            CtTypeReference<?> paramType = pathVariableParam.getType();
            String paramName = pathVariableParam.getSimpleName();
            System.out.println((("Found @PathVariable: " + paramName) + ", type: ") + paramType.getSimpleName());
            if (paramType.getSimpleName().equals("Long") || paramType.getSimpleName().equals("long")) {
                productIdParam = "(java.lang.Long)" + paramName;
                System.out.println("Using Long: " + productIdParam);
            } else {
                productIdParam = "null";
                System.out.println("Non-Long type, using null");
            }
        } else {
            productIdParam = "null";
            System.out.println("No @PathVariable, using null");
        }
        // Inject standard logging statement
        String injected = (((((((("com.products.demo.spoon.UserProfileLogger.logRequest(" + "org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication(), ") + "\"") + escape(operation)) + "\", ") + "\"") + escape(endpoint)) + "\", ") + productIdParam) + ");";
        CtCodeSnippetStatement snippet = getFactory().Code().createCodeSnippetStatement(injected);
        method.getBody().insertBegin(snippet);
        System.out.println("Injected logging: " + injected);
    }

    private String determineOperationType(CtMethod<?> method) {
        if (method.getAnnotation(GetMapping.class) != null) {
            if ("ProductsController".equals(method.getDeclaringType().getSimpleName())) {
                String path = getMappingPath(method);
                if ("/expensive".equals(path) || path.endsWith("/expensive")) {
                    return "EXPENSIVE_SEARCH";
                }
            }
            return "READ";
        } else if (((method.getAnnotation(PostMapping.class) != null) || (method.getAnnotation(PutMapping.class) != null)) || (method.getAnnotation(DeleteMapping.class) != null)) {
            return "WRITE";
        }
        return null;
    }

    private String determineEndpoint(CtMethod<?> method) {
        String base = "";
        RequestMapping rm = method.getDeclaringType().getAnnotation(RequestMapping.class);
        if (rm != null)
            base = firstPath(rm.value(), rm.path());

        String mapping = getMappingPath(method);
        if (mapping.isEmpty())
            return base;

        if (base.endsWith("/") && mapping.startsWith("/")) {
            return base + mapping.substring(1);
        }
        if ((!base.isEmpty()) && (!mapping.startsWith("/"))) {
            return (base + "/") + mapping;
        }
        return base + mapping;
    }

    private String getMappingPath(CtMethod<?> method) {
        if (method.getAnnotation(GetMapping.class) != null) {
            GetMapping m = method.getAnnotation(GetMapping.class);
            return firstPath(m.value(), m.path());
        } else if (method.getAnnotation(PostMapping.class) != null) {
            PostMapping m = method.getAnnotation(PostMapping.class);
            return firstPath(m.value(), m.path());
        } else if (method.getAnnotation(PutMapping.class) != null) {
            PutMapping m = method.getAnnotation(PutMapping.class);
            return firstPath(m.value(), m.path());
        } else if (method.getAnnotation(DeleteMapping.class) != null) {
            DeleteMapping m = method.getAnnotation(DeleteMapping.class);
            return firstPath(m.value(), m.path());
        }
        return "";
    }

    private String firstPath(String[] values, String[] paths) {
        if (((values != null) && (values.length > 0)) && (!values[0].isEmpty()))
            return values[0];

        if (((paths != null) && (paths.length > 0)) && (!paths[0].isEmpty()))
            return paths[0];

        return "";
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}