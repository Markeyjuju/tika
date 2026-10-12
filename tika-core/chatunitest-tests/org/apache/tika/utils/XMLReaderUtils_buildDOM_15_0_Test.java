package org.apache.tika.utils;

import org.apache.tika.parser.ParseContext;
import org.apache.tika.utils.XMLReaderUtils;
import org.apache.tika.exception.TikaException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import javax.xml.parsers.DocumentBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.xml.XMLConstants;
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
public class XMLReaderUtils_buildDOM_15_0_Test {

    private static final String TEST_XML = "<xml><test>data</test></xml>";

    private static final InputStream TEST_STREAM = new ByteArrayInputStream(TEST_XML.getBytes(StandardCharsets.UTF_8));

    @Mock
    private DocumentBuilder mockBuilder;

    @Mock
    private ParseContext mockParseContext;

    @BeforeAll
    public static void setup() {
        try {
            XMLReaderUtils.setPoolSize(1);
            XMLReaderUtils.setMaxNumReuses(1);
        } catch (TikaException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testBuildDOMWithBuilderNull() throws TikaException, IOException, SAXException {
        when(mockBuilder.parse(any(InputStream.class))).thenReturn(mock(Document.class));
        Document doc = XMLReaderUtils.buildDOM(TEST_STREAM, mockParseContext);
        assertNotNull(doc);
        verify(mockBuilder, times(1)).parse(any(InputStream.class));
    }

    @Test
    public void testBuildDOMWithBuilderNotNull() throws TikaException, IOException, SAXException {
        when(mockBuilder.parse(any(InputStream.class))).thenReturn(mock(Document.class));
        Document doc = XMLReaderUtils.buildDOM(TEST_STREAM, mockParseContext);
        assertNotNull(doc);
        verify(mockBuilder, times(1)).parse(any(InputStream.class));
    }

    @Test
    public void testBuildDOMWithPoolBuilder() throws TikaException, IOException, SAXException {
        when(mockBuilder.parse(any(InputStream.class))).thenReturn(mock(Document.class));
        Document doc = XMLReaderUtils.buildDOM(TEST_STREAM, mockParseContext);
        assertNotNull(doc);
        verify(mockBuilder, times(1)).parse(any(InputStream.class));
    }

    @Test
    public void testBuildDOMWithPoolBuilderNull() throws TikaException, IOException, SAXException {
        when(mockBuilder.parse(any(InputStream.class))).thenReturn(mock(Document.class));
        Document doc = XMLReaderUtils.buildDOM(TEST_STREAM, mockParseContext);
        assertNotNull(doc);
        verify(mockBuilder, times(1)).parse(any(InputStream.class));
    }

    @Test
    public void testBuildDOMWithNullInputStream() throws TikaException, IOException, SAXException {
        InputStream nullStream = null;
        assertThrows(NullPointerException.class, () -> XMLReaderUtils.buildDOM(nullStream, mockParseContext));
    }

    @Test
    public void testBuildDOMWithNullParseContext() throws TikaException, IOException, SAXException {
        assertThrows(NullPointerException.class, () -> XMLReaderUtils.buildDOM(TEST_STREAM, null));
    }

    @Test
    public void testBuildDOMWithEmptyInputStream() throws TikaException, IOException, SAXException {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        assertThrows(IOException.class, () -> XMLReaderUtils.buildDOM(emptyStream, mockParseContext));
    }

    @Test
    public void testBuildDOMWithInvalidXML() throws TikaException, IOException, SAXException {
        String invalidXml = "<xml><test>data</invalid>";
        InputStream invalidStream = new ByteArrayInputStream(invalidXml.getBytes(StandardCharsets.UTF_8));
        assertThrows(SAXException.class, () -> XMLReaderUtils.buildDOM(invalidStream, mockParseContext));
    }

    @Test
    public void testBuildDOMWithMaxEntityExpansions() throws TikaException, IOException, SAXException {
        when(mockBuilder.parse(any(InputStream.class))).thenReturn(mock(Document.class));
        Document doc = XMLReaderUtils.buildDOM(TEST_STREAM, mockParseContext);
        assertNotNull(doc);
        verify(mockBuilder, times(1)).parse(any(InputStream.class));
    }
}
