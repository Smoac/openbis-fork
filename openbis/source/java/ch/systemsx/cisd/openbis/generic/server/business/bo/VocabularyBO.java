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
import java.util.Set;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * The only productive implementation of {@link IVocabularyBO}.
 * 
 * @author Christian Ribeaud
 */
public class VocabularyBO extends AbstractBusinessObject implements IVocabularyBO
{
    private VocabularyPE vocabularyPE;

    public VocabularyBO(final IDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
    }

    //
    // AbstractVocabularyBusinessObject
    //

    public final void define(final Vocabulary vocabulary) throws UserFailureException
    {
        assert vocabulary != null : "Unspecified vocabulary.";
        vocabularyPE = new VocabularyPE();
        vocabularyPE.setCode(vocabulary.getCode());
        vocabularyPE.setDescription(vocabulary.getDescription());
        vocabularyPE.setDatabaseInstance(getHomeDatabaseInstance());
        vocabularyPE.setRegistrator(findRegistrator());
        for (final VocabularyTerm term : vocabulary.getTerms())
        {
            addTerm(term.getCode());
        }
    }

    public void addNewTerms(List<String> newTerms)
    {
        for (String term : newTerms)
        {
            addTerm(term);
        }
    }

    private void addTerm(String term)
    {
        final VocabularyTermPE vocabularyTermPE = new VocabularyTermPE();
        vocabularyTermPE.setCode(term);
        vocabularyTermPE.setRegistrator(findRegistrator());
        vocabularyPE.addTerm(vocabularyTermPE);
    }

    public void save() throws UserFailureException
    {
        assert vocabularyPE != null : "Unspecified vocabulary";
        try
        {
            getVocabularyDAO().createOrUpdateVocabulary(vocabularyPE);
        } catch (final DataAccessException e)
        {
            throwException(e, String.format("Vocabulary '%s'.", vocabularyPE.getCode()));
        }
    }

    public final VocabularyPE getVocabulary()
    {
        assert vocabularyPE != null : "Unspecified vocabulary";
        return vocabularyPE;
    }

    public List<VocabularyTermWithStats> countTermsUsageStatistics()
    {
        assert vocabularyPE != null : "Unspecified vocabulary";
        enrichWithTerms();
        Set<VocabularyTermPE> terms = vocabularyPE.getTerms();
        List<VocabularyTermWithStats> stats = new ArrayList<VocabularyTermWithStats>();
        for (VocabularyTermPE term : terms)
        {
            stats.add(countTermUsageStatistics(term));
        }
        return stats;
    }

    private VocabularyTermWithStats countTermUsageStatistics(VocabularyTermPE term)
    {
        VocabularyTermWithStats stats = new VocabularyTermWithStats(term);
        for (EntityKind entityKind : EntityKind.values())
        {
            long numberOfUsages =
                    getEntityPropertyTypeDAO(entityKind).countTermUsageStatistics(term);
            stats.registerUsage(entityKind, numberOfUsages);
        }
        return stats;
    }

    public void load(String vocabularyCode) throws UserFailureException
    {
        vocabularyPE = getVocabularyDAO().tryFindVocabularyByCode(vocabularyCode);
        if (vocabularyPE == null)
        {
            throw UserFailureException.fromTemplate("Vocabulary '%s' does not exist.",
                    vocabularyCode);
        }
    }

    private void enrichWithTerms()
    {
        HibernateUtils.initialize(vocabularyPE.getTerms());
    }
}
