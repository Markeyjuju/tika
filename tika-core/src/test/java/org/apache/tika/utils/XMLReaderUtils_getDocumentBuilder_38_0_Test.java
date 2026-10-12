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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import org.apache.tika.parser.ParseContext;

/**
 * Tests de {@link XMLReaderUtils#getDocumentBuilder(ParseContext)}.
 *
 * Abstraction exposée : deux chemins seulement.
 *   - le contexte contient un DocumentBuilder : il est renvoyé tel quel ;
 *   - sinon : délégation à la méthode STATIQUE getDocumentBuilder(), qui fabrique
 *     un nouveau builder sécurisé (pas de pool sur ce chemin).
 * ParseContext est un simple conteneur (Map interne) : on utilise un vrai objet et
 * on observe le résultat, plutôt que de mocker et de vérifier le nombre d'appels
 * de get(...). Le test généré passait déjà en grande partie, mais ses oracles
 * (assertNotNull + verify) étaient faibles et le test par réflexion n'ajoutait rien
 * (la méthode est publique).
 */
public class XMLReaderUtils_getDocumentBuilder_38_0_Test {

    // ===== Chemin 1 : builder fourni par le contexte =====

    // 1. Le builder du contexte est renvoyé tel quel (même instance, pas une copie).
    @Test
    public void testBuilderDuContexte() throws Exception {
        DocumentBuilder fourni = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        ParseContext context = new ParseContext();
        context.set(DocumentBuilder.class, fourni);

        assertSame(fourni, XMLReaderUtils.getDocumentBuilder(context));
    }

    // ===== Chemin 2 : contexte vide, builder par défaut =====

    // 2. Un builder est créé, configuré namespace-aware et non validant.
    @Test
    public void testBuilderParDefautConfiguration() throws Exception {
        DocumentBuilder builder = XMLReaderUtils.getDocumentBuilder(new ParseContext());

        assertNotNull(builder);
        assertTrue(builder.isNamespaceAware());
        assertFalse(builder.isValidating());
    }

    // 3. Pas de pool sur ce chemin : chaque appel renvoie une nouvelle instance.
    @Test
    public void testNouvelleInstanceAChaqueAppel() throws Exception {
        DocumentBuilder b1 = XMLReaderUtils.getDocumentBuilder(new ParseContext());
        DocumentBuilder b2 = XMLReaderUtils.getDocumentBuilder(new ParseContext());

        assertNotSame(b1, b2);
    }

    // 4. Défense XXE : une entité externe n'est pas résolue (résolveur qui renvoie "").
    @Test
    public void testEntiteExterneNonResolue() throws Exception {
        File secret = File.createTempFile("secret", ".txt");
        secret.deleteOnExit();
        try (FileWriter w = new FileWriter(secret)) {
            w.write("SECRET");
        }
        String xml = "<?xml version=\"1.0\"?><!DOCTYPE l [<!ENTITY x SYSTEM \"file://"
                + secret.getAbsolutePath() + "\">]><l>&x;</l>";

        DocumentBuilder builder = XMLReaderUtils.getDocumentBuilder(new ParseContext());
        Document doc = builder.parse(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        assertEquals("", doc.getDocumentElement().getTextContent());
    }

    // ===== Entrée limite =====

    // 5. Contexte null : context.get(...) lève un NPE.
    @Test
    public void testContexteNull() {
        assertThrows(NullPointerException.class, () -> XMLReaderUtils.getDocumentBuilder(null));
    }
}