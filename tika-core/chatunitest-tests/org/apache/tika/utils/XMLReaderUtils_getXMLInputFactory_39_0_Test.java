package org.apache.tika.utils;

import org.apache.tika.parser.ParseContext;
import org.apache.tika.utils.XMLReaderUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.tika.exception.TikaException;
import org.apache.tika.sax.OfflineContentHandler;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class XMLReaderUtils_getXMLInputFactory_39_0_Test {

    @InjectMocks
    private XMLReaderUtils xmlReaderUtils;

    @Mock
    private ParseContext parseContext;

    @BeforeEach
    public void setUp() {
        // Set up mock objects if needed
    }

    @Test
    public void testGetXMLInputFactory_NullContext() throws Exception {
        XMLInputFactory factory = XMLReaderUtils.getXMLInputFactory(null);
        assertNotNull(factory);
        assertTrue(factory instanceof javax.xml.stream.XMLInputFactory);
    }

    @Test
    public void testGetXMLInputFactory_WithContext() throws Exception {
        when(parseContext.get(javax.xml.stream.XMLInputFactory.class)).thenReturn(null);
        XMLInputFactory factory = XMLReaderUtils.getXMLInputFactory(parseContext);
        assertNotNull(factory);
        assertTrue(factory instanceof javax.xml.stream.XMLInputFactory);
    }

    @Test
    public void testGetXMLInputFactory_ExistingFactory() throws Exception {
        javax.xml.stream.XMLInputFactory existingFactory = mock(javax.xml.stream.XMLInputFactory.class);
        when(parseContext.get(javax.xml.stream.XMLInputFactory.class)).thenReturn(existingFactory);
        XMLInputFactory factory = XMLReaderUtils.getXMLInputFactory(parseContext);
        assertNotNull(factory);
        assertEquals(existingFactory, factory);
    }

    @Test
    public void testGetXMLInputFactory_PrivateMethodsCalled() throws Exception {
        XMLInputFactory factory = XMLReaderUtils.getXMLInputFactory(parseContext);
        // Verify private methods are called
        Method setNamespaceAwareMethod = javax.xml.stream.XMLInputFactory.class.getMethod("setNamespaceAware", boolean.class);
        setNamespaceAwareMethod.invoke(factory, true);
        Method setValidatingMethod = javax.xml.stream.XMLInputFactory.class.getMethod("setValidating", boolean.class);
        setValidatingMethod.invoke(factory, false);
        Method setSupportingDtdMethod = javax.xml.stream.XMLInputFactory.class.getMethod("setSupportingDtd", boolean.class);
        setSupportingDtdMethod.invoke(factory, false);
        Method setSupportingExternalEntitiesMethod = javax.xml.stream.XMLInputFactory.class.getMethod("setSupportingExternalEntities", boolean.class);
        setSupportingExternalEntitiesMethod.invoke(factory, false);
        Method setSecurityManagerMethod = javax.xml.stream.XMLInputFactory.class.getMethod("setSecurityManager", Object.class);
        setSecurityManagerMethod.invoke(factory, null);
    }

    @Test
    public void testGetXMLInputFactory_Reflection() throws Exception {
        Method getXMLInputFactoryMethod = XMLReaderUtils.class.getDeclaredMethod("getXMLInputFactory", ParseContext.class);
        getXMLInputFactoryMethod.setAccessible(true);
        XMLInputFactory factory = (XMLInputFactory) getXMLInputFactoryMethod.invoke(xmlReaderUtils, parseContext);
        assertNotNull(factory);
        assertTrue(factory instanceof javax.xml.stream.XMLInputFactory);
    }
}
