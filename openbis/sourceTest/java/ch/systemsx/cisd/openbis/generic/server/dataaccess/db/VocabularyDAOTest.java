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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.fail;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Test cases for corresponding {@link VocabularyDAO} class.
 * 
 * @author Christian Ribeaud
 */
@Test(groups =
    { "db" })
public final class VocabularyDAOTest extends AbstractDAOTest
{
    private final VocabularyTermPE createVocabularyTerm(final String code)
    {
        final VocabularyTermPE vocabularyTermPE = new VocabularyTermPE();
        vocabularyTermPE.setRegistrator(getSystemPerson());
        vocabularyTermPE.setCode(code);
        return vocabularyTermPE;
    }

    @Test
    public final void testTryFindVocabularyByCode()
    {
        final IVocabularyDAO vocabularyDAO = daoFactory.getVocabularyDAO();
        boolean fail = true;
        try
        {
            vocabularyDAO.tryFindVocabularyByCode(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        assertNull(vocabularyDAO.tryFindVocabularyByCode("DOES_NOT_EXIST"));
        final VocabularyPE vocabulary = vocabularyDAO.tryFindVocabularyByCode("PLATE_GEOMETRY");
        assertNotNull(vocabulary);
        assertEquals(3, vocabulary.getTerms().size());
    }

    @Test
    public final void testCreateVocabulary()
    {
        final IVocabularyDAO vocabularyDAO = daoFactory.getVocabularyDAO();
        boolean fail = true;
        try
        {
            vocabularyDAO.createOrUpdateVocabulary(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        final PersonPE registrator = getSystemPerson();
        final VocabularyPE vocabularyPE = new VocabularyPE();
        final String vocabularyCode = "FORMAT";
        vocabularyPE.setCode(vocabularyCode);
        vocabularyPE.setDatabaseInstance(daoFactory.getHomeDatabaseInstance());
        vocabularyPE.setDescription("The format description");
        vocabularyPE.setRegistrator(registrator);
        vocabularyPE.addTerm(createVocabularyTerm("SMALL"));
        vocabularyPE.addTerm(createVocabularyTerm("MEDIUM"));
        vocabularyPE.addTerm(createVocabularyTerm("BIG:.-_-"));
        try
        {
            vocabularyDAO.createOrUpdateVocabulary(vocabularyPE);
            fail(String.format("'%s' expected.", DataIntegrityViolationException.class
                    .getSimpleName()));
        } catch (final DataIntegrityViolationException ex)
        {
            // Nothing to do here.
        }
        vocabularyPE.setCode("USER.FORMAT");
        vocabularyDAO.createOrUpdateVocabulary(vocabularyPE);
        // Check saved vocabulary.
        assertNull(vocabularyDAO.tryFindVocabularyByCode(vocabularyCode));
        final VocabularyPE savedVocabulary =
                vocabularyDAO.tryFindVocabularyByCode("USER." + vocabularyCode);
        assertNotNull(savedVocabulary);
        assertNotNull(savedVocabulary.getDescription());
        assertEquals(3, savedVocabulary.getTerms().size());
    }

    @Test
    public final void testListVocabularies()
    {
        final IVocabularyDAO vocabularyDAO = daoFactory.getVocabularyDAO();
        final List<VocabularyPE> vocabularies = vocabularyDAO.listVocabularies(true);
        assertEquals(3, vocabularies.size());
        final VocabularyPE vocabularyPE = vocabularies.get(0);
        assertFalse(HibernateUtils.isInitialized(vocabularyPE.getTerms()));
    }

    @Test
    public final void testListAllVocabularies()
    {
        final IVocabularyDAO vocabularyDAO = daoFactory.getVocabularyDAO();
        final List<VocabularyPE> vocabularies = vocabularyDAO.listVocabularies(false);
        assertEquals(5, vocabularies.size());
        final VocabularyPE vocabularyPE = vocabularies.get(0);
        assertFalse(HibernateUtils.isInitialized(vocabularyPE.getTerms()));
    }
}
