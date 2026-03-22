package com.paola.paolarestapi.users.service;

import com.paola.paolarestapi.users.dto.ValidationViolation;
import com.paola.paolarestapi.users.model.UserPayload;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/*
  XmlValidationService handles XML-side input validation and parsing for POST /api/users.

  What it does:
  - Validates incoming XML payload against schemas/user.xsd.
  - Converts XSD/parse problems into structured ValidationViolation entries.
  - Parses valid XML into UserPayload using JAXB.

  How it is used:
  - UserResource calls validate(...) for XML requests.
  - If no violations are returned, UserResource calls parse(...) to get UserPayload.

  Why we need it:
  - Enforces strict XML schema correctness before any persistence.
  - Produces readable, structured validation errors for API clients.
  - Keeps XML-specific logic out of UserResource.
*/
public class XmlValidationService {
    private static final Schema XML_SCHEMA = loadSchema();

    public List<ValidationViolation> validate(String xmlPayload) {
        final List<ValidationViolation> violations = new ArrayList<ValidationViolation>();
        try {
            Validator validator = XML_SCHEMA.newValidator();
            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) {
                    violations.add(toViolation(exception, "warning"));
                }

                @Override
                public void error(SAXParseException exception) {
                    violations.add(toViolation(exception, "schema_error"));
                }

                @Override
                public void fatalError(SAXParseException exception) {
                    violations.add(toViolation(exception, "fatal_schema_error"));
                }
            });
            validator.validate(new StreamSource(new StringReader(xmlPayload)));
        } catch (Exception exception) {
            violations.add(new ValidationViolation(
                    "xml",
                    "invalid_xml",
                    exception.getMessage()
            ));
        }
        return violations;
    }

    public UserPayload parse(String xmlPayload) throws Exception {
        JAXBContext context = JAXBContext.newInstance(UserPayload.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (UserPayload) unmarshaller.unmarshal(new StringReader(xmlPayload));
    }

    private static Schema loadSchema() {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            InputStream stream = XmlValidationService.class
                    .getClassLoader()
                    .getResourceAsStream("schemas/user.xsd");
            if (stream == null) {
                throw new IllegalStateException("Missing XSD: schemas/user.xsd");
            }
            return factory.newSchema(new StreamSource(stream));
        } catch (SAXException exception) {
            throw new IllegalStateException("Unable to load XSD schema.", exception);
        }
    }

    private ValidationViolation toViolation(SAXParseException exception, String rule) {
        String field = "line " + exception.getLineNumber();
        return new ValidationViolation(field, rule, exception.getMessage());
    }
}

