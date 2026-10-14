/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.jupiter.api.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.tika.parser.ParseContext;

/**
 * Tests de {@link XMLReaderUtils#parseSAX(InputStream, org.xml.sax.ContentHandler, ParseContext)}.
 *
 * Abstraction exposée : choix du SAXParser (contexte, pool, ou création si pool de taille 0),
 * puis saxParser.parse(is, new OfflineContentHandler(handler)). L'oracle naturel est donc
 * ce que le handler REÇOIT, pas une exception.
 *
 * Erreur du test généré : un XML valide avec un DefaultHandler était censé lever une
 * TikaException. Rien dans ce chemin n'en lève : le parsing réussit. L'oracle était
 * inversé, et le DefaultHandler (qui ignore tout) ne permettait de rien vérifier.
 */
public class XMLReaderUtils_parseSAX_20_1_Test {

    // Handler « enregistreur » : garde les événements et le texte reçus.
    // C'est l'oracle : il rend observable ce que parseSAX transmet.
    private static class Enregistreur extends DefaultHandler {
        final List<String> evenements = new ArrayList<>();
        final StringBuilder texte = new StringBuilder();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) {
            evenements.add("debut:" + localName);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            evenements.add("fin:" + localName);
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            texte.append(ch, start, length);
        }
    }

    private static InputStream flux(String xml) {
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    }

    // ===== Chemin nominal (parser du pool) =====

    // 1. XML valide : le handler reçoit les bonnes balises, dans l'ordre, et le texte.
    @Test
    public void testEvenementsRecus() throws Exception {
        Enregistreur h = new Enregistreur();

        XMLReaderUtils.parseSAX(flux("<xml><data>Test</data></xml>"), h, new ParseContext());

        assertEquals(Arrays.asList("debut:xml", "debut:data", "fin:data", "fin:xml"), h.evenements);
        assertEquals("Test", h.texte.toString());
    }

    // 2. Réutilisation du pool : 250 parses (> DEFAULT_NUM_REUSES = 100) restent corrects.
    @Test
    public void testPoolReutilise() throws Exception {
        for (int i = 0; i < 250; i++) {
            Enregistreur h = new Enregistreur();
            XMLReaderUtils.parseSAX(flux("<a/>"), h, new ParseContext());
            assertEquals(Arrays.asList("debut:a", "fin:a"), h.evenements, "itération " + i);
        }
    }

    // ===== Parser fourni par le contexte =====

    // 3. Le parser du contexte est celui utilisé. Discriminant : un SAXParser NON namespace-aware
    //    (celui du test) donne localName vide, le parser du pool (namespace-aware) donnerait "a".
    @Test
    public void testParserDuContexteUtilise() throws Exception {
        SAXParser fourni = SAXParserFactory.newInstance().newSAXParser();
        ParseContext context = new ParseContext();
        context.set(SAXParser.class, fourni);
        Enregistreur h = new Enregistreur();

        XMLReaderUtils.parseSAX(flux("<a/>"), h, context);

        assertEquals(Arrays.asList("debut:", "fin:"), h.evenements);
    }

    // ===== Pool désactivé =====

    // 4. Pool de taille 0 : un parser est créé à la volée. On restaure l'état global ensuite.
    @Test
    public void testPoolDeTailleZero() throws Exception {
        int ancienneTaille = XMLReaderUtils.getPoolSize();
        try {
            XMLReaderUtils.setPoolSize(0);
            Enregistreur h = new Enregistreur();

            XMLReaderUtils.parseSAX(flux("<a/>"), h, new ParseContext());

            assertEquals(Arrays.asList("debut:a", "fin:a"), h.evenements);
        } finally {
            XMLReaderUtils.setPoolSize(ancienneTaille);
        }
    }

    // ===== Sécurité =====

    // 5. Défense XXE : le handler ne reçoit pas le contenu d'une entité externe.
    @Test
    public void testEntiteExterneNonResolue() throws Exception {
        File secret = File.createTempFile("secret", ".txt");
        secret.deleteOnExit();
        try (FileWriter w = new FileWriter(secret, StandardCharsets.UTF_8)) {
            w.write("SECRET");
        }
        String xml = "<?xml version=\"1.0\"?><!DOCTYPE l [<!ENTITY x SYSTEM \"file://"
                + secret.getAbsolutePath() + "\">]><l>&x;</l>";
        Enregistreur h = new Enregistreur();

        XMLReaderUtils.parseSAX(flux(xml), h, new ParseContext());

        assertEquals("", h.texte.toString());
    }

    // ===== Entrées invalides =====

    // 6. XML mal formé : SAXException (en pratique SAXParseException).
    @Test
    public void testXmlMalForme() {
        assertThrows(SAXException.class, () -> XMLReaderUtils.parseSAX(
                flux("<xml><data>"), new DefaultHandler(), new ParseContext()));
    }

    // 7. Flux null : IllegalArgumentException levée par SAXParser.parse.
    @Test
    public void testFluxNull() {
        assertThrows(IllegalArgumentException.class, () -> XMLReaderUtils.parseSAX(
                (InputStream) null, new DefaultHandler(), new ParseContext()));
    }

    // 8. Contexte null : context.get(...) lève un NPE.
    @Test
    public void testContexteNull() {
        assertThrows(NullPointerException.class, () -> XMLReaderUtils.parseSAX(
                flux("<a/>"), new DefaultHandler(), null));
    }
}
