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
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.springframework.dao.DataIntegrityViolationException;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for corresponding {@link VocabularyBO} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = VocabularyBO.class)
public final class VocabularyBOTest extends AbstractBOTest
{
    static final String VOCABULARY_CODE = "USER.COLOR";

    static final String VOCABULARY_DESCRIPTION = "Some predefined colors";

    private final VocabularyBO createVocabularyBO()
    {
        return new VocabularyBO(daoFactory, EXAMPLE_SESSION);
    }

    static final NewVocabulary createVocabulary()
    {
        final NewVocabulary vocabulary = new NewVocabulary();
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

    static void assertVocabularyEquals(final NewVocabulary vocabulary,
            final VocabularyPE vocabularyPE)
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
        final NewVocabulary vocabulary = createVocabulary();
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
        final NewVocabulary vocabulary = createVocabulary();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getHomeDatabaseInstance();
                    will(returnValue(ManagerTestTool.EXAMPLE_DATABASE_INSTANCE));

                    exactly(vocabulary.getTerms().size()).of(vocabularyTermDAO).validate(
                            with(aNonNull(VocabularyTermPE.class)));

                    one(vocabularyDAO).createOrUpdateVocabulary(with(aNonNull(VocabularyPE.class)));
                }
            });
        vocabularyBO.define(vocabulary);
        vocabularyBO.save();
        context.assertIsSatisfied();
    }

    @Test
    public final void testSaveWithException()
    {
        final VocabularyBO vocabularyBO = createVocabularyBO();
        final NewVocabulary vocabulary = createVocabulary();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getHomeDatabaseInstance();
                    will(returnValue(ManagerTestTool.EXAMPLE_DATABASE_INSTANCE));

                    exactly(vocabulary.getTerms().size()).of(vocabularyTermDAO).validate(
                            with(aNonNull(VocabularyTermPE.class)));

                    one(vocabularyDAO).createOrUpdateVocabulary(with(aNonNull(VocabularyPE.class)));
                    will(throwException(new DataIntegrityViolationException(null)));
                }
            });
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
    public final void testSaveWithTermValidationException()
    {
        final VocabularyBO vocabularyBO = createVocabularyBO();
        final NewVocabulary vocabulary = createVocabulary();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getHomeDatabaseInstance();
                    will(returnValue(ManagerTestTool.EXAMPLE_DATABASE_INSTANCE));

                    one(vocabularyTermDAO).validate(with(aNonNull(VocabularyTermPE.class)));
                    will(throwException(new DataIntegrityViolationException(null)));
                }
            });
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
        final VocabularyBO vocabularyBO = createVocabularyBO();
        final List<String> newTerms = Arrays.asList("a", "b");
        final Long previousTermPosition = 5L;

        context.checking(new Expectations()
            {
                {
                    one(vocabularyDAO).tryFindVocabularyByCode("voc-code");
                    will(returnValue(vocabulary));

                    one(vocabularyTermDAO).increaseVocabularyTermOrdinals(vocabulary,
                            previousTermPosition + 1, newTerms.size());
                }
            });

        vocabularyBO.load("voc-code");
        vocabularyBO.addNewTerms(newTerms, previousTermPosition);

        List<VocabularyTermPE> terms =
                new ArrayList<VocabularyTermPE>(vocabularyBO.getVocabulary().getTerms());
        Collections.sort(terms);
        assertEquals(2, terms.size());
        VocabularyTermPE term1 = terms.get(0);
        assertEquals("A", term1.getCode());
        assertEquals(new Long(previousTermPosition + 1), term1.getOrdinal());
        assertSame(EXAMPLE_SESSION.tryGetPerson(), term1.getRegistrator());
        VocabularyTermPE term2 = terms.get(1);
        assertEquals("B", term2.getCode());
        assertEquals(new Long(previousTermPosition + 2), term2.getOrdinal());
        assertSame(EXAMPLE_SESSION.tryGetPerson(), term2.getRegistrator());
        context.assertIsSatisfied();
    }

    @Test
    public void testAddNewTermsToAnInternallyManaggedVocabulary()
    {
        final VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.setManagedInternally(true);
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
        try
        {
            vocabularyBO.addNewTerms(newTerms, 0L);
        } catch (UserFailureException e)
        {
            assertEquals("Not allowed to add terms to an internally managed vocabulary.", e
                    .getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteTermsFromAnInternallyManaggedVocabulary()
    {
        final VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.setManagedInternally(true);
        context.checking(new Expectations()
            {
                {
                    one(vocabularyDAO).tryFindVocabularyByCode("voc-code");
                    will(returnValue(vocabulary));
                }
            });

        VocabularyBO vocabularyBO = createVocabularyBO();
        vocabularyBO.load("voc-code");
        try
        {
            vocabularyBO.delete(Collections.<VocabularyTerm> emptyList(), Collections
                    .<VocabularyTermReplacement> emptyList());
        } catch (UserFailureException e)
        {
            assertEquals("Not allowed to delete terms from an internally managed vocabulary.", e
                    .getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteAllTerms()
    {
        final VocabularyPE vocabulary = new VocabularyPE();
        VocabularyTerm term1 = createTerm("1");
        vocabulary.addTerm(translate(term1));
        context.checking(new Expectations()
            {
                {
                    one(vocabularyDAO).tryFindVocabularyByCode("voc-code");
                    will(returnValue(vocabulary));
                }
            });

        VocabularyBO vocabularyBO = createVocabularyBO();
        vocabularyBO.load("voc-code");
        try
        {
            vocabularyBO.delete(Arrays.asList(term1), Collections
                    .<VocabularyTermReplacement> emptyList());
        } catch (IllegalArgumentException e)
        {
            assertEquals("Deletion of all 1 terms are not allowed.", e.getMessage());
        }

        assertEquals(1, vocabulary.getTerms().size());
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteTermsWithDeletedReplacement()
    {
        final VocabularyPE vocabulary = new VocabularyPE();
        VocabularyTerm term1 = createTerm("1");
        VocabularyTerm term2 = createTerm("2");
        VocabularyTerm term3 = createTerm("3");
        vocabulary.addTerm(translate(term1));
        vocabulary.addTerm(translate(term2));
        vocabulary.addTerm(translate(term3));
        context.checking(new Expectations()
            {
                {
                    one(vocabularyDAO).tryFindVocabularyByCode("voc-code");
                    will(returnValue(vocabulary));
                }
            });

        VocabularyBO vocabularyBO = createVocabularyBO();
        vocabularyBO.load("voc-code");
        try
        {
            vocabularyBO.delete(Arrays.asList(term1), Arrays.asList(createTermWithReplacement(
                    term2, term1)));
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Invalid vocabulary replacement because of unknown replacement: 2 -> [1]",
                    e.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteTermsWithUnkownReplacement()
    {
        final VocabularyPE vocabulary = new VocabularyPE();
        VocabularyTerm term1 = createTerm("1");
        VocabularyTerm term2 = createTerm("2");
        VocabularyTerm term3 = createTerm("3");
        vocabulary.addTerm(translate(term1));
        vocabulary.addTerm(translate(term2));
        context.checking(new Expectations()
            {
                {
                    one(vocabularyDAO).tryFindVocabularyByCode("voc-code");
                    will(returnValue(vocabulary));
                }
            });

        VocabularyBO vocabularyBO = createVocabularyBO();
        vocabularyBO.load("voc-code");
        try
        {
            vocabularyBO.delete(Collections.<VocabularyTerm> emptyList(), Arrays
                    .asList(createTermWithReplacement(term1, term3)));
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e)
        {
            assertEquals("Invalid vocabulary replacement because of unknown replacement: 1 -> [3]",
                    e.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteTerms()
    {
        final VocabularyPE vocabulary = new VocabularyPE();
        VocabularyTerm term1 = createTerm("1");
        VocabularyTerm term2 = createTerm("2");
        vocabulary.addTerm(translate(term1));
        vocabulary.addTerm(translate(term2));
        context.checking(new Expectations()
            {
                {
                    one(vocabularyDAO).tryFindVocabularyByCode("voc-code");
                    will(returnValue(vocabulary));
                }
            });

        VocabularyBO vocabularyBO = createVocabularyBO();
        vocabularyBO.load("voc-code");
        vocabularyBO.delete(Arrays.asList(term1), Collections
                .<VocabularyTermReplacement> emptyList());

        assertEquals(1, vocabulary.getTerms().size());
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteAndReplaceTerms()
    {
        final VocabularyPE vocabulary = new VocabularyPE();
        VocabularyTerm term1 = createTerm("1");
        final VocabularyTerm term2 = createTerm("2");
        VocabularyTerm term3 = createTerm("3");
        vocabulary.addTerm(translate(term1));
        vocabulary.addTerm(translate(term2));
        vocabulary.addTerm(translate(term3));
        final MaterialPropertyPE entityPropertyPE = new MaterialPropertyPE();
        context.checking(new Expectations()
            {
                {
                    one(vocabularyDAO).tryFindVocabularyByCode("voc-code");
                    will(returnValue(vocabulary));

                    for (EntityKind entityKind : EntityKind.values())
                    {
                        one(daoFactory).getEntityPropertyTypeDAO(entityKind);
                        will(returnValue(entityPropertyTypeDAO));

                        one(entityPropertyTypeDAO).listPropertiesByVocabularyTerm(term2.getCode());
                        List<EntityPropertyPE> properties =
                                Arrays.<EntityPropertyPE> asList(entityPropertyPE);
                        will(returnValue(properties));

                        one(entityPropertyTypeDAO).updateProperties(properties);
                    }
                }
            });

        VocabularyBO vocabularyBO = createVocabularyBO();
        vocabularyBO.load("voc-code");
        vocabularyBO.delete(Arrays.asList(term1), Arrays.asList(createTermWithReplacement(term2,
                term3)));

        assertEquals(term3.getCode(), entityPropertyPE.getVocabularyTerm().getCode());
        assertEquals(1, vocabulary.getTerms().size());
        assertEquals(term3.getCode(), vocabulary.getTerms().iterator().next().getCode());
        context.assertIsSatisfied();
    }

    private VocabularyTerm createTerm(String code)
    {
        VocabularyTerm vocabularyTerm = new VocabularyTerm();
        vocabularyTerm.setCode(code);
        return vocabularyTerm;
    }

    private VocabularyTermReplacement createTermWithReplacement(VocabularyTerm term,
            VocabularyTerm replacement)
    {
        VocabularyTermReplacement vocabularyTermReplacement = new VocabularyTermReplacement();
        vocabularyTermReplacement.setTerm(term);
        vocabularyTermReplacement.setReplacementCode(replacement.getCode());
        return vocabularyTermReplacement;
    }

    private VocabularyTermPE translate(VocabularyTerm term)
    {
        VocabularyTermPE vocabularyTermPE = new VocabularyTermPE();
        vocabularyTermPE.setCode(term.getCode());
        return vocabularyTermPE;
    }
}
