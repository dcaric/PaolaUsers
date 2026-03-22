package com.paola.paolarestapi.integration.service;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/*
  Validates local snapshot XML file against users-snapshot.xsd.

  Output:
  - empty list => snapshot is valid
  - non-empty list => warning/error/fatal messages

  This validation is called before XPath/SOAP search to enforce structural safety.
*/
public class SnapshotValidationService {
    private static final Schema SNAPSHOT_SCHEMA = loadSchema();

    public List<String> validate(Path xmlFilePath) {
        final List<String> messages = new ArrayList<String>();
        if (!Files.exists(xmlFilePath)) {
            messages.add("Snapshot file does not exist: " + xmlFilePath.toAbsolutePath());
            return messages;
        }

        try {
            Validator validator = SNAPSHOT_SCHEMA.newValidator();
            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) {
                    messages.add("WARNING line " + exception.getLineNumber() + ": " + exception.getMessage());
                }

                @Override
                public void error(SAXParseException exception) {
                    messages.add("ERROR line " + exception.getLineNumber() + ": " + exception.getMessage());
                }

                @Override
                public void fatalError(SAXParseException exception) {
                    messages.add("FATAL line " + exception.getLineNumber() + ": " + exception.getMessage());
                }
            });
            validator.validate(new StreamSource(xmlFilePath.toFile()));
        } catch (Exception exception) {
            messages.add("Validation exception: " + exception.getMessage());
        }

        return messages;
    }

    private static Schema loadSchema() {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            InputStream stream = SnapshotValidationService.class
                    .getClassLoader()
                    .getResourceAsStream("schemas/users-snapshot.xsd");
            if (stream == null) {
                throw new IllegalStateException("Missing XSD schema: schemas/users-snapshot.xsd");
            }
            return factory.newSchema(new StreamSource(stream));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to load snapshot XSD schema.", exception);
        }
    }
}
