package com.paola.paolarestapi.integration.service;

import com.paola.paolarestapi.integration.model.SnapshotUser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/*
  Executes XPath filtering on local XML snapshot.

  Matching rule:
  - case-insensitive partial match over email, first_name, last_name, avatar.

  Returns:
  - list of SnapshotUser objects built from matched <user> nodes.
*/
public class XPathUserSearchService {
    public List<SnapshotUser> search(Path xmlFilePath, String term) {
        try {
            String normalizedTerm = term == null ? "" : term.trim().toLowerCase(Locale.ROOT);
            String termForXPath = escapeForXPath(normalizedTerm);
            String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            String lower = "abcdefghijklmnopqrstuvwxyz";
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            Document document = factory.newDocumentBuilder().parse(xmlFilePath.toFile());

            XPath xPath = XPathFactory.newInstance().newXPath();
            // Build one XPath filter that searches across multiple fields.
            // translate(..., upper, lower) makes each field lowercase in XPath, so contains(...)
            // behaves as case-insensitive partial match with the normalized search term.
            String expression = "//user[" +
                    "contains(translate(email, '" + upper + "', '" + lower + "'), '" + termForXPath + "')" +
                    " or contains(translate(first_name, '" + upper + "', '" + lower + "'), '" + termForXPath + "')" +
                    " or contains(translate(last_name, '" + upper + "', '" + lower + "'), '" + termForXPath + "')" +
                    " or contains(translate(avatar, '" + upper + "', '" + lower + "'), '" + termForXPath + "')" +
                    "]";

            NodeList nodes = (NodeList) xPath.evaluate(expression, document, XPathConstants.NODESET);
            List<SnapshotUser> results = new ArrayList<SnapshotUser>();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                SnapshotUser user = new SnapshotUser();
                user.setId(parseInt(getChildText(node, "id")));
                user.setEmail(getChildText(node, "email"));
                user.setFirstName(getChildText(node, "first_name"));
                user.setLastName(getChildText(node, "last_name"));
                user.setAvatar(getChildText(node, "avatar"));
                results.add(user);
            }
            return results;
        } catch (Exception exception) {
            throw new IllegalStateException("XPath search failed.", exception);
        }
    }

    private String getChildText(Node userNode, String tagName) {
        NodeList children = userNode.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (tagName.equals(child.getNodeName())) {
                return child.getTextContent();
            }
        }
        return null;
    }

    private Integer parseInt(String value) {
        try {
            return value == null ? null : Integer.valueOf(value);
        } catch (Exception exception) {
            return null;
        }
    }

    private String escapeForXPath(String input) {
        return input.replace("'", "");
    }
}
