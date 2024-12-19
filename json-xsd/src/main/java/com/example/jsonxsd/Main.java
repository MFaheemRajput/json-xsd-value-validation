package com.example.jsonxsd;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {

            // Load validation rules from XSD (using InputStream)
            InputStream xsdInputStream = Main.class.getClassLoader().getResourceAsStream("schema.xsd");

            if (xsdInputStream == null) {
                System.out.println("The file schema.xsd was not found in the resources folder.");
                return;
            }

            Map<String, Range> validationRules = Validator.loadValidationRules(xsdInputStream);

            // Load JSON
            InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("data.json");

            if (inputStream == null) {
                System.out.println("The file data.json was not found in the resources folder.");
                return;
            }

            ObjectMapper objectMapper = new ObjectMapper();

            Person person = objectMapper.readValue(inputStream, Person.class);

            // Validate the person object against the XSD-defined validation rules
            if (validationRules.size() != 0) {
                Validator.validate(person, validationRules);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}