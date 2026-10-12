package org.apache.tika.utils;

import org.apache.tika.parser.ParseContext;
import org.apache.tika.utils.XMLReaderUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
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

@ExtendWith(MockitoExtension.class)
public class XMLReaderUtils_getDocumentBuilder_38_0_Test {

    private static final Logger LOG = LoggerFactory.getLogger(XMLReaderUtils.class);

    private static final int DEFAULT_POOL_SIZE = 10;

    private static final int DEFAULT_MAX_ENTITY_EXPANSIONS = 20;

    private static final int DEFAULT_NUM_REUSES = 100;

    @Mock
    private ParseContext parseContext;

    @InjectMocks
    private XMLReaderUtils xmlReaderUtils;

    @BeforeEach
    public void setUp() throws Exception {
        // Set up the initial state of the ParseContext
        when(parseContext.get(any(Class.class))).thenReturn(null);
    }

    @Test
    public void testGetDocumentBuilder_WithExistingDocumentBuilder() throws TikaException {
        // Arrange
        DocumentBuilder mockDocumentBuilder = mock(DocumentBuilder.class);
        when(parseContext.get(DocumentBuilder.class)).thenReturn(mockDocumentBuilder);
        // Act
        DocumentBuilder result = XMLReaderUtils.getDocumentBuilder(parseContext);
        // Assert
        assertEquals(mockDocumentBuilder, result);
        verify(parseContext, times(1)).get(DocumentBuilder.class);
    }

    @Test
    public void testGetDocumentBuilder_WithNoExistingDocumentBuilder() throws TikaException {
        // Act
        DocumentBuilder result = XMLReaderUtils.getDocumentBuilder(parseContext);
        // Assert
        assertNotNull(result);
        verify(parseContext, times(1)).get(DocumentBuilder.class);
    }

    @Test
    public void testGetDocumentBuilder_ReflectionInvoke() throws Exception {
        // Arrange
        Method getDocumentBuilderMethod = XMLReaderUtils.class.getDeclaredMethod("getDocumentBuilder", ParseContext.class);
        getDocumentBuilderMethod.setAccessible(true);
        // Act
        DocumentBuilder result = (DocumentBuilder) getDocumentBuilderMethod.invoke(xmlReaderUtils, parseContext);
        // Assert
        assertNotNull(result);
        verify(parseContext, times(1)).get(DocumentBuilder.class);
    }
}
