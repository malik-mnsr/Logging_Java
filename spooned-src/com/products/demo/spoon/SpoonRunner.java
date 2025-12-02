package com.products.demo.spoon;
import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.support.compiler.FileSystemFolder;
public class SpoonRunner {
    public static void main(String[] args) {
        Launcher launcher = new Launcher();
        Environment env = launcher.getEnvironment();
        env.setNoClasspath(false);
        env.setAutoImports(true);
        env.setCommentEnabled(true);
        env.setComplianceLevel(17);
        launcher.addInputResource(new FileSystemFolder("src/main/java/com/products/demo"));
        launcher.setSourceOutputDirectory("spooned-src");
        launcher.addProcessor(new LoggingProcessor());
        launcher.run();
        System.out.println("Spoon finished. Instrumented sources in spooned-src");
    }
}