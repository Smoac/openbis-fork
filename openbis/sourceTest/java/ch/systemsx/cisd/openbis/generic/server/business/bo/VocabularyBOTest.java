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

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.springframework.dao.DataIntegrityViolationException;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;

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
        return new VocabularyBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION);
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

                    one(daoFactory).getVocabularyDAO();
                    will(returnValue(vocabularyDAO));

                    one(vocabularyDAO).createVocabulary(with(aNonNull(VocabularyPE.class)));
                }
            });
        final Vocabulary vocabulary = createVocabulary();
        vocabularyBO.define(vocabulary);
        vocabularyBO.save();
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

                    one(daoFactory).getVocabularyDAO();
                    will(returnValue(vocabularyDAO));

                    one(vocabularyDAO).createVocabulary(with(aNonNull(VocabularyPE.class)));
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
    }
}
