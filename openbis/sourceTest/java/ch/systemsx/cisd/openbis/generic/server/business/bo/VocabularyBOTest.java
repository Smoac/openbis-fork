/*
 * Copyright 2008 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.jmock.Expectations;
import org.springframework.dao.DataIntegrityViolationException;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * Test cases for corresponding {@link VocabularyBO} class.
 * 
 * @author Christian Ribeaud
 */
public final class VocabularyBOTest extends AbstractBOTest
{
    static final String VOCABULARY_CODE = "USER.COLOR";

    static final String VOCABULARY_DESCRIPTION = "Some predefined colors";

    private final VocabularyBO createVocabularyBO()
    {
        return new VocabularyBO(daoFactory, EXAMPLE_SESSION);
    }

    static final Vocabulary createVocabulary()
    {
        final Vocabulary vocabulary = new Vocabulary();
        vocabulary.setCode(VOCABULARY_CODE);
        vocabulary.setDescription(VOCABULARY_DESCRIPTION);
        vocabulary.setTerms(createTerms());
        return vocabulary;
    }

    final static List<VocabularyTerm> createTerms()
    {
        final List<VocabularyTerm> terms = new ArrayList<VocabularyTerm>();
        terms.add(createVocabularyTerm("RED"));
        terms.add(createVocabularyTerm("YELLOW"));
        terms.add(createVocabularyTerm("WHITE"));
        return terms;
    }

    final static VocabularyTerm createVocabularyTerm(final String code)
    {
        final VocabularyTerm term = new VocabularyTerm();
        term.setCode(code);
        return term;
    }

    static void assertVocabularyEquals(final Vocabulary vocabulary, final VocabularyPE vocabularyPE)
    {
        assertEquals(vocabulary.getCode(), vocabularyPE.getCode());
        assertEquals(vocabulary.getDescription(), vocabularyPE.getDescription());
        final List<VocabularyTerm> terms = vocabulary.getTerms();
        assertNotNull(terms);
        assertEquals(terms.size(), vocabularyPE.getTerms().size());
    }

    @Test
    public final void testWithNull()
    {
        boolean fail = true;
        try
        {
            createVocabularyBO().define(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        fail = true;
        try
        {
            createVocabularyBO().save();
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        fail = true;
        try
        {
            createVocabularyBO().getVocabulary();
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        context.assertIsSatisfied();
    }

    @Test
    public final void testDefine()
    {
        final VocabularyBO vocabularyBO = createVocabularyBO();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getHomeDatabaseInstance();
                    will(returnValue(ManagerTestTool.EXAMPLE_DATABASE_INSTANCE));
                }
            });
        final Vocabulary vocabulary = createVocabulary();
        vocabularyBO.define(vocabulary);
        final VocabularyPE vocabularyPE = vocabularyBO.getVocabulary();
        assertVocabularyEquals(vocabulary, vocabularyPE);
        assertFalse(vocabularyPE.isInternalNamespace());
        context.assertIsSatisfied();
    }

    @Test
    public final void testSave()
    {
        final VocabularyBO vocabularyBO = createVocabularyBO();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getHomeDatabaseInstance();
                    will(returnValue(ManagerTestTool.EXAMPLE_DATABASE_INSTANCE));

                    one(vocabularyDAO).createOrUpdateVocabulary(with(aNonNull(VocabularyPE.class)));
                }
            });
        final Vocabulary vocabulary = createVocabulary();
        vocabularyBO.define(vocabulary);
        vocabularyBO.save();
        context.assertIsSatisfied();
    }

    @Test
    public final void testSaveWithException()
    {
        final VocabularyBO vocabularyBO = createVocabularyBO();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getHomeDatabaseInstance();
                    will(returnValue(ManagerTestTool.EXAMPLE_DATABASE_INSTANCE));

                    one(vocabularyDAO).createOrUpdateVocabulary(with(aNonNull(VocabularyPE.class)));
                    will(throwException(new DataIntegrityViolationException(null)));
                }
            });
        final Vocabulary vocabulary = createVocabulary();
        vocabularyBO.define(vocabulary);
        try
        {
            vocabularyBO.save();
            fail(String.format("'%s' expected.", UserFailureException.class.getSimpleName()));
        } catch (final UserFailureException ex)
        {
            // Nothing to do here.
        }
        context.assertIsSatisfied();
    }
    
    @Test
    public void testLoad()
    {
        final VocabularyPE vocabulary = new VocabularyPE();
        context.checking(new Expectations()
            {
                {
                    one(vocabularyDAO).tryFindVocabularyByCode("voc-code");
                    will(returnValue(vocabulary));
                }
            });
        
        VocabularyBO vocabularyBO = createVocabularyBO();
        vocabularyBO.load("voc-code");
        
        assertSame(vocabulary, vocabularyBO.getVocabulary());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testAddNewTerms()
    {
        final VocabularyPE vocabulary = new VocabularyPE();
        context.checking(new Expectations()
            {
                {
                    one(vocabularyDAO).tryFindVocabularyByCode("voc-code");
                    will(returnValue(vocabulary));
                }
            });
        
        VocabularyBO vocabularyBO = createVocabularyBO();
        vocabularyBO.load("voc-code");
        List<String> newTerms = Arrays.asList("a");
        vocabularyBO.addNewTerms(newTerms);
        
        Set<VocabularyTermPE> terms = vocabularyBO.getVocabulary().getTerms();
        assertEquals(1, terms.size());
        VocabularyTermPE term = terms.iterator().next();
        assertEquals("A", term.getCode());
        assertSame(EXAMPLE_SESSION.tryGetPerson(), term.getRegistrator());
        context.assertIsSatisfied();
    }
}
