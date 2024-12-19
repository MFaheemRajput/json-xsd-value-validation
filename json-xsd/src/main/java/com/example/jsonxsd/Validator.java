package com.example.jsonxsd;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

// Validator class that loads validation rules and validates the object
class Validator {

    // Loads validation rules from the XSD InputStream
    public static Map<String, Range> loadValidationRules(InputStream xsdInputStream) {
        Map<String, Range> validationRules = new HashMap<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xsdInputStream);

            // Look for the person element
            NodeList personNodes = doc.getElementsByTagName("xs:element");
            for (int i = 0; i < personNodes.getLength(); i++) {
                Node personNode = personNodes.item(i);
                if (personNode instanceof Element) {
                    Element personElement = (Element) personNode;

                    if ("person".equals(personElement.getAttribute("name"))) {
                        NodeList complexTypeNodes = personElement.getElementsByTagName("xs:complexType");
                        for (int j = 0; j < complexTypeNodes.getLength(); j++) {
                            Element complexTypeElement = (Element) complexTypeNodes.item(j);

                            NodeList sequenceNodes = complexTypeElement.getElementsByTagName("xs:sequence");
                            for (int k = 0; k < sequenceNodes.getLength(); k++) {
                                Element sequenceElement = (Element) sequenceNodes.item(k);

                                NodeList elementNodes = sequenceElement.getElementsByTagName("xs:element");
                                for (int l = 0; l < elementNodes.getLength(); l++) {
                                    Element element = (Element) elementNodes.item(l);
                                    String elementName = element.getAttribute("name");

                                    if ("age".equals(elementName)) {
                                        // Parse age restrictions
                                        NodeList restrictionNodes = element.getElementsByTagName("xs:restriction");
                                        for (int m = 0; m < restrictionNodes.getLength(); m++) {
                                            Element restrictionElement = (Element) restrictionNodes.item(m);

                                            NodeList minNodes = restrictionElement
                                                    .getElementsByTagNameNS("\"http://www.w3.org/2001/XMLSchema\"",
                                                            "xs:minInclusive");

                                            NodeList maxNodes = restrictionElement
                                                    .getElementsByTagNameNS("\"http://www.w3.org/2001/XMLSchema\"",
                                                            "xs:maxInclusive");

                                            Element minInclusiveElement = (Element) minNodes.item(0);
                                            Element maxInclusiveElement = (Element) maxNodes.item(0);

                                            String minValue = minInclusiveElement.getAttribute("value");
                                            String maxValue = maxInclusiveElement.getAttribute("value");

                                            // int minValue = minNodes.getLength() > 0
                                            // ? Integer.parseInt(minNodes.getAttributes().getNamedItem("value"))
                                            // : Integer.MIN_VALUE;
                                            // int maxValue = maxNodes.getLength() > 0
                                            // ? Integer.parseInt(maxNodes.item(0).getAttribute("value"))
                                            // : Integer.MAX_VALUE;

                                            validationRules.put("age",
                                                    new Range(Integer.parseInt(minValue), Integer.parseInt(maxValue)));
                                        }
                                    } else if ("name".equals(elementName)) {
                                        // No range for name, so we skip
                                        validationRules.put("name", new Range(Integer.MIN_VALUE, Integer.MAX_VALUE));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return validationRules;
    }

    // Validate the object fields using the rules loaded from XSD
    public static void validate(Object obj, Map<String, Range> validationRules) throws IllegalAccessException {
        // Iterate through the fields of the object (Person class in this case)
        for (var field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(obj);

            // Check if validation rule exists for this field
            if (validationRules.containsKey(field.getName())) {
                Range range = validationRules.get(field.getName());

                if (field.getType() == int.class) {
                    int intValue = (int) value;
                    if (intValue < range.getMin() || intValue > range.getMax()) {
                        System.out.println("Invalid value for " + field.getName() + ": " + intValue
                                + ". Must be between " + range.getMin() + " and " + range.getMax());
                    } else {
                        System.out.println("Valid value for " + field.getName() + ": " + intValue);
                    }
                } else if (field.getType() == String.class) {
                    // For the 'name' field, we don't have range validation, just a check for the
                    // presence of value
                    String stringValue = (String) value;
                    if (stringValue == null || stringValue.isEmpty()) {
                        System.out.println("Invalid value for " + field.getName() + ": " + stringValue
                                + ". Name cannot be empty.");
                    } else {
                        System.out.println("Valid value for " + field.getName() + ": " + stringValue);
                    }
                }
            }
        }
    }
}

// Range class to store min and max validation values
class Range {
    private int min;
    private int max;

    public Range(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}