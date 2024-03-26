package br.com.suporte.moovinAudit;

import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import groovy.lang.Binding;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

@SpringBootApplication
@Configuration
@ComponentScan(basePackages = "br.com.suporte.moovinAudit")
public class MoovinAuditApplication {
    public static void main(String[] args) throws IOException, ScriptException, ResourceException {
        //SpringApplication.run(MoovinAuditApplication.class, args);
        String[] root = {"src/main/resources/templates"};
        GroovyScriptEngine scriptEngine = new GroovyScriptEngine(root);
        Binding binding = new Binding();
        Object result = scriptEngine.run("AuditMain.groovy", binding);
    }
}
