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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Tests de {@link XMLReaderUtils#getAttrValue(String, org.xml.sax.Attributes)}.
 *
 * Abstraction exposée : méthode statique pure. Elle parcourt les attributs et
 * renvoie la valeur du PREMIER dont le localName est égal au nom demandé, sinon null.
 * Pas d'état, donc pas besoin de mock ni de @InjectMocks : on passe un vrai
 * Attributes (AttributesImpl) en paramètre.
 *
 * Erreur du test généré : il attendait "anotherValue" (le dernier gagne) alors que
 * (a) getLength() restait à 1, donc le 2e attribut n'était jamais lu, et
 * (b) même avec 2 attributs, le code s'arrête au premier.
 */
public class XMLReaderUtils_getAttrValue_35_1_Test {

    // Fixture : trois attributs ; "k" est volontairement présent deux fois.
    private static AttributesImpl attributs() {
        AttributesImpl atts = new AttributesImpl();
        // addAttribute(uri, localName, qName, type, value)
        atts.addAttribute("", "k", "k", "CDATA", "v1");
        atts.addAttribute("", "k", "k", "CDATA", "v2");
        atts.addAttribute("urn:x", "p", "x:p", "CDATA", "v3");
        return atts;
    }

    // ===== Recherche nominale =====

    // 1. Attribut présent : sa valeur est renvoyée.
    @Test
    public void testAttributPresent() {
        assertEquals("v3", XMLReaderUtils.getAttrValue("p", attributs()));
    }

    // 2. Attribut absent : null.
    @Test
    public void testAttributAbsent() {
        assertNull(XMLReaderUtils.getAttrValue("absent", attributs()));
    }

    // 3. Deux attributs de même localName : le premier gagne (cas mal attendu par le test généré).
    @Test
    public void testPremierAttributGagne() {
        assertEquals("v1", XMLReaderUtils.getAttrValue("k", attributs()));
    }

    // ===== La comparaison porte sur le localName =====

    // 4. "p" (localName) est trouvé, mais pas le nom qualifié "x:p".
    @Test
    public void testComparaisonSurLocalNameSeulement() {
        assertEquals("v3", XMLReaderUtils.getAttrValue("p", attributs()));
        assertNull(XMLReaderUtils.getAttrValue("x:p", attributs()));
    }

    // 5. La comparaison est sensible à la casse.
    @Test
    public void testSensibleALaCasse() {
        assertNull(XMLReaderUtils.getAttrValue("K", attributs()));
    }

    // ===== Entrées limites =====

    // 6. Aucun attribut : la boucle ne s'exécute pas, donc même un nom null donne null (pas de NPE).
    @Test
    public void testAucunAttributNomNull() {
        assertNull(XMLReaderUtils.getAttrValue(null, new AttributesImpl()));
    }

    // 7. Nom null avec des attributs : localName.equals(...) lève un NPE.
    @Test
    public void testNomNullAvecAttributs() {
        assertThrows(NullPointerException.class,
                () -> XMLReaderUtils.getAttrValue(null, attributs()));
    }

    // 8. Attributes null : atts.getLength() lève un NPE.
    @Test
    public void testAttributesNull() {
        assertThrows(NullPointerException.class,
                () -> XMLReaderUtils.getAttrValue("k", null));
    }
}