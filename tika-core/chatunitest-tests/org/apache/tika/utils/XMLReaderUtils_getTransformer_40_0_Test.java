package org.apache.tika.utils;

import org.apache.tika.utils.XMLReaderUtils;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.exception.TikaException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashMap;
import java.util.Map;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
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
import org.apache.tika.sax.OfflineContentHandler;

@ExtendWith(MockitoExtension.class)
public class XMLReaderUtils_getTransformer_40_0_Test {

    @Mock
    private ParseContext parseContext;

    @InjectMocks
    private XMLReaderUtils xmlReaderUtils;

    @BeforeEach
    public void setUp() {
        // Set up initial state
        when(parseContext.get(Transformer.class)).thenReturn(null);
        when(parseContext.getJsonConfigs()).thenReturn(new HashMap<>());
        when(parseContext.getContextMap()).thenReturn(new HashMap<>());
    }

    @Test
    public void testGetTransformerWithNullTransformer() throws TikaException {
        // Arrange
        when(parseContext.get(Transformer.class)).thenReturn(null);
        // Act
        Transformer transformer = xmlReaderUtils.getTransformer(parseContext);
        // Assert
        assertNotNull(transformer);
        verify(parseContext).get(Transformer.class);
    }

    @Test
    public void testGetTransformerWithExistingTransformer() throws TikaException {
        // Arrange
        Transformer existingTransformer = mock(Transformer.class);
        when(parseContext.get(Transformer.class)).thenReturn(existingTransformer);
        // Act
        Transformer transformer = xmlReaderUtils.getTransformer(parseContext);
        // Assert
        assertEquals(existingTransformer, transformer);
        verify(parseContext).get(Transformer.class);
    }

    @Test
    public void testGetTransformerWithoutExistingTransformer() throws TikaException {
        // Arrange
        when(parseContext.get(Transformer.class)).thenReturn(null);
        // Act
        Transformer transformer = xmlReaderUtils.getTransformer(parseContext);
        // Assert
        assertNotNull(transformer);
        verify(parseContext).get(Transformer.class);
    }

    @Test
    public void testGetTransformerWithTransformerFactory() throws TikaException {
        // Arrange
        TransformerFactory transformerFactory = mock(TransformerFactory.class);
        when(xmlReaderUtils.getTransformerFactory()).thenReturn(transformerFactory);
        // Act
        Transformer transformer = xmlReaderUtils.getTransformer(parseContext);
        // Assert
        assertNotNull(transformer);
        verify(xmlReaderUtils, times(1)).getTransformerFactory();
    }

    @Test
    public void testGetTransformerWithTransformerFactoryFailure() throws TikaException {
        // Arrange
        TransformerFactory transformerFactory = mock(TransformerFactory.class);
        when(xmlReaderUtils.getTransformerFactory()).thenThrow(new TransformerFactoryConfigurationError());
        // Act & Assert
        assertThrows(TransformerFactoryConfigurationError.class, () -> xmlReaderUtils.getTransformer(parseContext));
    }
}
