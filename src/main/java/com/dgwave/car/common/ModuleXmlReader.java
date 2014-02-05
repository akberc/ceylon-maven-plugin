package com.dgwave.car.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Read dependencies from module.xml.
 * Adapted from Ceylon Module Resolver
 */
public final class ModuleXmlReader {
    
    /**
     * Private constructor.
     */
    private ModuleXmlReader() {
        
    }

    /**
     * Parse the module.xml file.
     * @param moduleXml The file to parse
     * @return Module representation
     * @throws Exception In case of parsing error
     */
    protected static Module parse(final File moduleXml) throws Exception {
        InputStream is = new FileInputStream(moduleXml);
        try {
            Document document = parseXml(is);
            Element root = document.getDocumentElement();
            ModuleIdentifier main = getModuleIdentifier(root);
            Module module = new Module(main);
            Element dependencies = getChildElement(root, "dependencies");
            if (dependencies != null) {
                for (Element dependency : getElements(dependencies, "module")) {
                    module.addDependency(getModuleIdentifier(dependency));
                }
            }
            return module;
        } finally {
            is.close();
        }
    }

    /**
     * ModuleIdentifier from XML element.
     * @param root The root element
     * @return ModuleIdentifier The name/version of the module
     */
    protected static ModuleIdentifier getModuleIdentifier(final Element root) {
        return new ModuleIdentifier(root.getAttribute("name"), root.getAttribute("slot"), 
            Boolean.parseBoolean(root.getAttribute("optional")), Boolean.parseBoolean(root.getAttribute("export")));
    }

    /**
     * Parse module.xml from an input stream.
     * @param inputStream The InputStream to parse
     * @return Document the parsed DOM
     * @throws ParserConfigurationException In case of parser mis-configuration
     * @throws SAXException In case of SAX exception
     * @throws IOException In case of IO error
     */
    protected static Document parseXml(final InputStream inputStream) 
        throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputStream);
        doc.getDocumentElement().normalize();
        return doc;
    }

    /**
     * Finds child elements in DOM.
     * @param parent The Element to find in
     * @param tagName The element to find
     * @return List of elements
     */
    protected static List<Element> getElements(final Element parent, final String tagName) {
        List<Element> elements = new ArrayList<Element>();
        NodeList nodes = parent.getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            if (node instanceof Element) {
                elements.add(Element.class.cast(node));
            }
        }
        return elements;
    }

    /**
     * Get a single child element.
     * @param parent The parent element
     * @param tagName The child element name
     * @return Element The first child element by that name
     */
    protected static Element getChildElement(final Element parent, final String tagName) {
        List<Element> elements = getElements(parent, tagName);
        if (elements.size() > 0) {
            return elements.get(0);
        } else {
            return null;
        }
    }
}
