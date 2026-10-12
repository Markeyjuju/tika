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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.Test;

import org.apache.tika.parser.ParseContext;

/**
 * Tests de {@link XMLReaderUtils#getTransformer(ParseContext)}.
 *
 * Abstraction exposée : le même schéma que getDocumentBuilder(ParseContext).
 *   - le contexte contient un Transformer : il est renvoyé tel quel ;
 *   - sinon : appel de la méthode STATIQUE getTransformer(), qui passe par
 *     getTransformerFactory() (une autre méthode statique).
 *
 * Pourquoi les tests générés échouaient tous :
 *   - xmlReaderUtils est un vrai objet (@InjectMocks), pas un mock/spy : when(...) et
 *     verify(...) dessus lèvent une NotAMockException ;
 *   - de toute façon getTransformerFactory() est STATIQUE : Mockito ne la stubbe pas
 *     avec when(...), et l'appel interne de getTransformer() ne passerait pas par le stub ;
 *   - le setUp stubbait getJsonConfigs()/getContextMap(), jamais appelés : sous
 *     MockitoExtension (mode strict) cela provoque une UnnecessaryStubbingException
 *     dans CHAQUE test, même ceux qui auraient passé.
 * Le cas « la fabrique échoue » (TransformerFactoryConfigurationError) n'est pas
 * simulable sans changer le fournisseur JAXP ; le code l'enveloppe en TikaException.
 */
public class XMLReaderUtils_getTransformer_40_0_Test {

    // ===== Chemin 1 : transformer fourni par le contexte =====

    // 1. Le transformer du contexte est renvoyé tel quel.
    @Test
    public void testTransformerDuContexte() throws Exception {
        Transformer fourni = TransformerFactory.newInstance().newTransformer();
        ParseContext context = new ParseContext();
        context.set(Transformer.class, fourni);

        assertSame(fourni, XMLReaderUtils.getTransformer(context));
    }

    // ===== Chemin 2 : contexte vide, transformer par défaut =====

    // 2. Un transformer est créé, et c'est une nouvelle instance à chaque appel
    //    (les Transformer ne sont pas thread-safe, donc jamais partagés).
    @Test
    public void testNouvelleInstanceAChaqueAppel() throws Exception {
        Transformer t1 = XMLReaderUtils.getTransformer(new ParseContext());
        Transformer t2 = XMLReaderUtils.getTransformer(new ParseContext());

        assertNotNull(t1);
        assertNotSame(t1, t2);
    }

    // 3. Le transformer est fonctionnel : transformation identité d'un petit document.
    @Test
    public void testTransformationIdentite() throws Exception {
        Transformer transformer = XMLReaderUtils.getTransformer(new ParseContext());
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter sortie = new StringWriter();

        transformer.transform(new StreamSource(new StringReader("<a><b/></a>")),
                new StreamResult(sortie));

        String xml = sortie.toString();
        assertTrue(xml.contains("<a>") && xml.contains("<b"), xml);
    }

    // 4. La fabrique utilisée est en traitement sécurisé (FEATURE_SECURE_PROCESSING).
    @Test
    public void testFabriqueEnTraitementSecurise() throws Exception {
        TransformerFactory fabrique = XMLReaderUtils.getTransformerFactory();

        assertTrue(fabrique.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
    }

    // ===== Entrée limite =====

    // 5. Contexte null : context.get(...) lève un NPE.
    @Test
    public void testContexteNull() {
        assertThrows(NullPointerException.class, () -> XMLReaderUtils.getTransformer(null));
    }
}