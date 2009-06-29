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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.util.KeyExtractorFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
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

    public final void define(final NewVocabulary vocabulary) throws UserFailureException
    {
        assert vocabulary != null : "Unspecified vocabulary.";
        vocabularyPE = new VocabularyPE();
        vocabularyPE.setDatabaseInstance(getHomeDatabaseInstance());
        vocabularyPE.setCode(vocabulary.getCode());
        vocabularyPE.setRegistrator(findRegistrator());
        vocabularyPE.setDescription(vocabulary.getDescription());
        vocabularyPE.setChosenFromList(vocabulary.isChosenFromList());
        vocabularyPE.setURLTemplate(vocabulary.getURLTemplate());
        for (final VocabularyTerm term : vocabulary.getTerms())
        {
            addTerm(term.getCode());
        }
    }

    public void addNewTerms(List<String> newTerms)
    {
        assert vocabularyPE != null : "Unspecified vocabulary";
        if (vocabularyPE.isManagedInternally())
        {
            throw new UserFailureException(
                    "Not allowed to add terms to an internally managed vocabulary.");
        }

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

    public void delete(List<VocabularyTerm> termsToBeDeleted,
            List<VocabularyTermReplacement> termsToBeReplaced)
    {
        assert vocabularyPE != null : "Unspecified vocabulary";
        if (vocabularyPE.isManagedInternally())
        {
            throw new UserFailureException(
                    "Not allowed to delete terms from an internally managed vocabulary.");
        }

        Set<VocabularyTermPE> terms = vocabularyPE.getTerms();
        IKeyExtractor<String, VocabularyTermPE> keyExtractor =
                KeyExtractorFactory.<VocabularyTermPE> createCodeKeyExtractor();
        TableMap<String, VocabularyTermPE> termsMap =
                new TableMap<String, VocabularyTermPE>(terms, keyExtractor);
        Set<String> remainingTerms = new HashSet<String>(termsMap.keySet());
        for (VocabularyTerm termToBeDeleted : termsToBeDeleted)
        {
            remainingTerms.remove(termToBeDeleted.getCode());
        }
        for (VocabularyTermReplacement termToBeReplaced : termsToBeReplaced)
        {
            remainingTerms.remove(termToBeReplaced.getTerm().getCode());
        }
        if (remainingTerms.isEmpty())
        {
            throw new IllegalArgumentException("Deletion of all " + terms.size()
                    + " terms are not allowed.");
        }
        for (VocabularyTermReplacement termToBeReplaced : termsToBeReplaced)
        {
            String code = termToBeReplaced.getTerm().getCode();
            String replacement = termToBeReplaced.getReplacement();
            VocabularyTermPE term = termsMap.tryGet(replacement);
            if (term == null || remainingTerms.contains(replacement) == false)
            {
                throw new IllegalArgumentException(
                        "Invalid vocabulary replacement because of unknown replacement: "
                                + termToBeReplaced);
            }
            for (EntityKind entityKind : EntityKind.values())
            {
                IEntityPropertyTypeDAO dao = getEntityPropertyTypeDAO(entityKind);
                List<EntityPropertyPE> properties = dao.listPropertiesByVocabularyTerm(code);
                for (EntityPropertyPE entityProperty : properties)
                {
                    entityProperty.setVocabularyTerm(term);
                }
                try
                {
                    dao.updateProperties(properties);
                } catch (DataAccessException e)
                {
                    throwException(e, "Couldn't replace in " + entityKind.toString().toLowerCase()
                            + "s vocabulary term '" + code + "' by '" + term.getCode() + "'.");
                }
            }
        }
        for (VocabularyTerm termToBeDeleted : termsToBeDeleted)
        {
            termsMap.remove(termToBeDeleted.getCode()).setVocabulary(null);
        }
        for (VocabularyTermReplacement termToBeReplaced : termsToBeReplaced)
        {
            termsMap.remove(termToBeReplaced.getTerm().getCode()).setVocabulary(null);
        }
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

    public void update(IVocabularyUpdates updates)
    {
        loadDataByTechId(TechId.create(updates));

        vocabularyPE.setCode(updates.getCode());
        vocabularyPE.setDescription(updates.getDescription());
        vocabularyPE.setURLTemplate(updates.getURLTemplate());
        vocabularyPE.setChosenFromList(updates.isChosenFromList());

        validateAndSave();
    }

    private void validateAndSave()
    {
        getVocabularyDAO().validateAndSaveUpdatedEntity(vocabularyPE);
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

    public void loadDataByTechId(TechId vocabularyId)
    {
        try
        {
            vocabularyPE = getVocabularyDAO().getByTechId(vocabularyId);
        } catch (DataRetrievalFailureException exception)
        {
            throw new UserFailureException(exception.getMessage());
        }
    }

    public Set<VocabularyTermPE> enrichWithTerms()
    {
        HibernateUtils.initialize(vocabularyPE.getTerms());
        return vocabularyPE.getTerms();
    }
}
