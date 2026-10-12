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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.apache.tika.parser.ParseContext;

/**
 * Tests de {@link XMLReaderUtils#buildDOM(InputStream, ParseContext)}.
 *
 * Abstraction exposée : trois façons d'obtenir le DocumentBuilder, puis parse(is).
 *   1. le contexte en fournit un : on l'utilise ;
 *   2. sinon, pool de builders (taille par défaut 10) ;
 *   3. pool de taille 0 : un builder est créé à la volée.
 * Le builder est choisi À L'INTÉRIEUR de la méthode statique : un @Mock DocumentBuilder
 * déclaré dans le test n'est jamais vu par elle (le test généré le stubbait puis
 * vérifiait parse(...), d'où UnnecessaryStubbing + « Wanted but not invoked »).
 * Le seul point d'entrée réel est le ParseContext : on y place un vrai builder et
 * on observe le résultat.
 *
 * Autres erreurs du test généré :
 *   - TEST_STREAM statique partagé : un flux ne se lit qu'une fois, donc tous les tests
 *     suivants parsaient un flux vide (SAXParseException « Premature end of file ») ;
 *   - quatre tests identiques (BuilderNull / NotNull / PoolBuilder / PoolBuilderNull) ;
 *   - flux null : le code lève IllegalArgumentException (levée par DocumentBuilder.parse),
 *     pas NullPointerException ;
 *   - flux vide : SAXParseException (sous-classe de SAXException), pas IOException ;
 *   - @BeforeAll modifiait l'état STATIQUE global (setPoolSize(1), setMaxNumReuses(1)) sans
 *     le restaurer, ce qui pollue les autres classes de test exécutées dans la même JVM ;
 *   - « MaxEntityExpansions » ne testait aucune limite (corps identique aux autres). Dans mon
 *     environnement (JDK 21, sans Xerces) la limite n'est pas observable via buildDOM, donc je
 *     n'ai pas écrit d'oracle dessus.
 */
public class XMLReaderUtils_buildDOM_15_0_Test {

    // Chaque test reçoit son propre flux (un InputStream ne se rejoue pas).
    private static InputStream flux(String xml) {
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    }

    // ===== Chemin nominal (builder du pool) =====

    // 1. Document valide : racine et contenu texte attendus.
    @Test
    public void testDocumentValide() throws Exception {
        Document doc = XMLReaderUtils.buildDOM(
                flux("<xml><test>data</test></xml>"), new ParseContext());

        assertEquals("xml", doc.getDocumentElement().getNodeName());
        assertEquals("data", doc.getElementsByTagName("test").item(0).getTextContent());
    }

    // 2. Réutilisation du pool : 250 parses (> DEFAULT_NUM_REUSES = 100) restent corrects,
    //    ce qui traverse aussi le chemin « builder usé, on en reconstruit un ».
    @Test
    public void testPoolReutilise() throws Exception {
        for (int i = 0; i < 250; i++) {
            Document doc = XMLReaderUtils.buildDOM(flux("<a><b/></a>"), new ParseContext());
            assertEquals("a", doc.getDocumentElement().getNodeName(), "itération " + i);
        }
    }

    // ===== Builder fourni par le contexte =====

    // 3. Le builder du contexte est bien celui qui parse. Discriminant observable : un builder
    //    NON namespace-aware (celui du test) donne getLocalName() == null (DOM niveau 1),
    //    alors que celui du pool, namespace-aware, donnerait "a".
    @Test
    public void testBuilderDuContexteUtilise() throws Exception {
        DocumentBuilder fourni = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        ParseContext context = new ParseContext();
        context.set(DocumentBuilder.class, fourni);

        Document doc = XMLReaderUtils.buildDOM(flux("<a/>"), context);

        assertNull(doc.getDocumentElement().getLocalName());
    }

    // ===== Pool désactivé =====

    // 4. Pool de taille 0 : un builder est créé à la volée. On restaure l'état global ensuite.
    @Test
    public void testPoolDeTailleZero() throws Exception {
        int ancienneTaille = XMLReaderUtils.getPoolSize();
        try {
            XMLReaderUtils.setPoolSize(0);

            Document doc = XMLReaderUtils.buildDOM(flux("<a/>"), new ParseContext());

            assertNotNull(doc);
            assertEquals("a", doc.getDocumentElement().getLocalName());
        } finally {
            XMLReaderUtils.setPoolSize(ancienneTaille);
        }
    }

    // ===== Sécurité =====

    // 5. Défense XXE : le contenu d'une entité externe n'est pas lu.
    @Test
    public void testEntiteExterneNonResolue() throws Exception {
        File secret = File.createTempFile("secret", ".txt");
        secret.deleteOnExit();
        try (FileWriter w = new FileWriter(secret)) {
            w.write("SECRET");
        }
        String xml = "<?xml version=\"1.0\"?><!DOCTYPE l [<!ENTITY x SYSTEM \"file://"
                + secret.getAbsolutePath() + "\">]><l>&x;</l>";

        Document doc = XMLReaderUtils.buildDOM(flux(xml), new ParseContext());

        assertEquals("", doc.getDocumentElement().getTextContent());
    }

    // ===== Entrées invalides =====

    // 6. XML mal formé : SAXException (en pratique SAXParseException).
    @Test
    public void testXmlMalForme() {
        assertThrows(SAXException.class, () -> XMLReaderUtils.buildDOM(
                flux("<xml><test>data</invalid>"), new ParseContext()));
    }

    // 7. Flux vide : SAXParseException « Premature end of file », pas IOException.
    @Test
    public void testFluxVide() {
        assertThrows(SAXException.class,
                () -> XMLReaderUtils.buildDOM(flux(""), new ParseContext()));
    }

    // 8. Flux null : IllegalArgumentException levée par DocumentBuilder.parse, pas un NPE.
    @Test
    public void testFluxNull() {
        assertThrows(IllegalArgumentException.class,
                () -> XMLReaderUtils.buildDOM((InputStream) null, new ParseContext()));
    }

    // 9. Contexte null : context.get(...) lève un NPE.
    @Test
    public void testContexteNull() {
        assertThrows(NullPointerException.class,
                () -> XMLReaderUtils.buildDOM(flux("<a/>"), null));
    }
}