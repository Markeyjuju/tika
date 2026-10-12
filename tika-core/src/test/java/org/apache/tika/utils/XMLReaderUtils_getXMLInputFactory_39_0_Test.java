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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.xml.stream.XMLInputFactory;

import org.junit.jupiter.api.Test;

import org.apache.tika.parser.ParseContext;

/**
 * Tests de {@link XMLReaderUtils#getXMLInputFactory(ParseContext)}.
 *
 * Abstraction exposée : même schéma que les deux méthodes précédentes (contexte, sinon
 * méthode statique getXMLInputFactory() qui configure une fabrique StAX sécurisée).
 *
 * Erreurs des tests générés :
 *   - getXMLInputFactory(null) : le test attendait une fabrique, mais le code appelle
 *     context.get(...) et lève un NPE ;
 *   - testGetXMLInputFactory_PrivateMethodsCalled invoquait par réflexion
 *     setNamespaceAware / setValidating / setSupportingDtd / setSecurityManager sur
 *     XMLInputFactory : ces méthodes n'existent pas (API inventée, NoSuchMethodException)
 *     et le test ne contenait de toute façon aucune assertion. La configuration se
 *     vérifie avec getProperty(...), l'API réelle de XMLInputFactory ;
 *   - la méthode est publique : l'appel par réflexion n'apportait rien.
 */
public class XMLReaderUtils_getXMLInputFactory_39_0_Test {

    // ===== Chemin 1 : fabrique fournie par le contexte =====

    // 1. La fabrique du contexte est renvoyée telle quelle.
    @Test
    public void testFabriqueDuContexte() {
        XMLInputFactory fournie = XMLInputFactory.newFactory();
        ParseContext context = new ParseContext();
        context.set(XMLInputFactory.class, fournie);

        assertSame(fournie, XMLReaderUtils.getXMLInputFactory(context));
    }

    // ===== Chemin 2 : contexte vide, fabrique par défaut sécurisée =====

    // 2. Configuration : namespace-aware ; ni validation, ni DTD, ni entités externes ;
    //    un résolveur est installé (défense en profondeur).
    @Test
    public void testConfigurationSecurisee() {
        XMLInputFactory factory = XMLReaderUtils.getXMLInputFactory(new ParseContext());

        assertEquals(Boolean.TRUE, factory.getProperty(XMLInputFactory.IS_NAMESPACE_AWARE));
        assertEquals(Boolean.FALSE, factory.getProperty(XMLInputFactory.IS_VALIDATING));
        assertEquals(Boolean.FALSE, factory.getProperty(XMLInputFactory.SUPPORT_DTD));
        assertEquals(Boolean.FALSE,
                factory.getProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES));
        assertNotNull(factory.getXMLResolver());
    }

    // 3. Nouvelle instance à chaque appel (les fabriques StAX ne sont pas partagées).
    @Test
    public void testNouvelleInstanceAChaqueAppel() {
        XMLInputFactory f1 = XMLReaderUtils.getXMLInputFactory(new ParseContext());
        XMLInputFactory f2 = XMLReaderUtils.getXMLInputFactory(new ParseContext());

        assertNotSame(f1, f2);
    }

    // ===== Entrée limite =====

    // 4. Contexte null : NPE (et non une fabrique par défaut, contrairement au test généré).
    @Test
    public void testContexteNull() {
        assertThrows(NullPointerException.class,
                () -> XMLReaderUtils.getXMLInputFactory((ParseContext) null));
    }
}