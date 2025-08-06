package com.example.demo.communication.email.util;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EmailTemplateProcessorTest {

    @Test
    void processTemplate_shouldReplaceVariables() {
        EmailTemplateProcessor processor = new EmailTemplateProcessor();
        String template = "Hello, {{name}}! Welcome to {{company}}.";
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "John Doe");
        variables.put("company", "ACME Corp");

        String result = processor.processTemplate(template, variables);

        assertEquals("Hello, John Doe! Welcome to ACME Corp.", result);
    }

    @Test
    void processTemplate_withMissingVariables_shouldLeaveThemAsIs() {
        EmailTemplateProcessor processor = new EmailTemplateProcessor();
        String template = "Hello, {{name}}! Your code is {{code}}.";
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "Jane Doe");

        String result = processor.processTemplate(template, variables);

        assertEquals("Hello, Jane Doe! Your code is {{code}}.", result);
    }
}