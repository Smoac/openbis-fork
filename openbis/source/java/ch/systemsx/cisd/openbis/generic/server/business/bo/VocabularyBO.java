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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.util.KeyExtractorFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * The only productive implementation of {@link IVocabularyBO}.
 * 
 * @author Christian Ribeaud
 */
public class VocabularyBO extends AbstractBusinessObject implements IVocabularyBO
{
    @Private
    static final String UPDATING_CONTENT_OF_INTERNALLY_MANAGED_VOCABULARIES_IS_NOT_ALLOWED =
            "Updating content of internally managed vocabularies is not allowed.";

    private static final String UNSPECIFIED_VOCABULARY = "Unspecified vocabulary";

    private static final int MAX_NUMBER_OF_INVAID_TERMS_IN_ERROR_MESSAGE = 10;

    private VocabularyPE vocabularyPE;

    public VocabularyBO(final IDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
    }

    // For tests only
    @Private
    VocabularyBO(final IDAOFactory daoFactory, final Session session, VocabularyPE vocabulary)
    {
        super(daoFactory, session);
        vocabularyPE = vocabulary;
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
        Long currentTermOrdinal = 1L;
        for (final VocabularyTerm term : vocabulary.getTerms())
        {
            addTerm(term, currentTermOrdinal++);
        }
    }

    public void addNewTerms(List<String> newTermCodes, Long previousTermOrdinal)
    {
        assert vocabularyPE != null : UNSPECIFIED_VOCABULARY;
        assert previousTermOrdinal != null : "Unspecified previous term ordinal";
        if (vocabularyPE.isManagedInternally())
        {
            throw new UserFailureException(
                    "Not allowed to add terms to an internally managed vocabulary.");
        }

        // need to shift existing terms to create space for new terms
        increaseVocabularyTermOrdinals(previousTermOrdinal + 1, newTermCodes.size());
        Long currentTermOrdinal = previousTermOrdinal + 1;
        for (String code : newTermCodes)
        {
            addTerm(code, currentTermOrdinal++);
        }
    }

    /** shift terms in vocabulary by specified increment starting from term with specified ordinal */
    private void increaseVocabularyTermOrdinals(Long startOrdinal, int increment)
    {
        getVocabularyTermDAO()
                .increaseVocabularyTermOrdinals(vocabularyPE, startOrdinal, increment);
    }

    private void addTerm(String code, String description, String label, Long ordinal)
    {
        final VocabularyTermPE vocabularyTermPE = new VocabularyTermPE();
        vocabularyTermPE.setCode(code);
        vocabularyTermPE.setDescription(description);
        vocabularyTermPE.setLabel(label);
        vocabularyTermPE.setRegistrator(findRegistrator());
        vocabularyTermPE.setOrdinal(ordinal);
        vocabularyPE.addTerm(vocabularyTermPE);
    }

    private void addTerm(String code, Long ordinal)
    {
        addTerm(code, null, null, ordinal);
    }

    private void addTerm(VocabularyTerm term, Long ordinal)
    {
        addTerm(term.getCode(), term.getDescription(), term.getLabel(), ordinal);
    }

    public void delete(List<VocabularyTerm> termsToBeDeleted,
            List<VocabularyTermReplacement> termsToBeReplaced)
    {
        assert vocabularyPE != null : UNSPECIFIED_VOCABULARY;
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
            String replacement = termToBeReplaced.getReplacementCode();
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
            removeTerm(termsMap, termToBeDeleted.getCode());
        }
        for (VocabularyTermReplacement termToBeReplaced : termsToBeReplaced)
        {
            removeTerm(termsMap, termToBeReplaced.getTerm().getCode());
        }
    }

    private void removeTerm(TableMap<String, VocabularyTermPE> termsMap, String termCode)
    {
        VocabularyTermPE term = termsMap.remove(termCode);
        term.setVocabulary(null);
    }

    public void save() throws UserFailureException
    {
        assert vocabularyPE != null : UNSPECIFIED_VOCABULARY;
        try
        {
            StringBuilder builder = new StringBuilder();
            int numberOfInvalidTerms = 0;
            for (VocabularyTermPE term : vocabularyPE.getTerms())
            {
                try
                {
                    getVocabularyTermDAO().validate(term);
                } catch (DataIntegrityViolationException ex)
                {
                    numberOfInvalidTerms++;
                    if (numberOfInvalidTerms <= MAX_NUMBER_OF_INVAID_TERMS_IN_ERROR_MESSAGE)
                    {
                        builder.append('\n').append(ex.getMessage());
                    }
                }
            }
            if (builder.length() > 0)
            {
                builder.insert(0, "Invalid terms:");
                int additionalTerms =
                        numberOfInvalidTerms - MAX_NUMBER_OF_INVAID_TERMS_IN_ERROR_MESSAGE;
                if (additionalTerms > 0)
                {
                    builder.append("\n").append("and ").append(additionalTerms);
                    builder.append(" more invalid terms.");
                }
                throw new UserFailureException("Invalid terms:" + builder);
            }
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
        assert vocabularyPE != null : UNSPECIFIED_VOCABULARY;
        return vocabularyPE;
    }

    public List<VocabularyTermWithStats> countTermsUsageStatistics()
    {
        assert vocabularyPE != null : UNSPECIFIED_VOCABULARY;
        enrichWithTerms();
        Set<VocabularyTermPE> terms = vocabularyPE.getTerms();
        return createTermsWithStatistics(terms);
    }

    private List<VocabularyTermWithStats> createTermsWithStatistics(Set<VocabularyTermPE> terms)
    {
        List<VocabularyTermWithStats> results = new ArrayList<VocabularyTermWithStats>();
        for (VocabularyTermPE term : terms)
        {
            results.add(new VocabularyTermWithStats(term));
        }
        Collections.sort(results);
        for (EntityKind entityKind : EntityKind.values())
        {
            getEntityPropertyTypeDAO(entityKind).fillTermUsageStatistics(results, vocabularyPE);
        }
        return results;
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

    public void deleteByTechId(TechId vocabularyId, String reason) throws UserFailureException
    {
        loadDataByTechId(vocabularyId);
        try
        {
            getVocabularyDAO().delete(vocabularyPE);
            getEventDAO()
                    .persist(createDeletionEvent(vocabularyPE, session.tryGetPerson(), reason));
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Vocabulary '%s'", vocabularyPE.getCode()));
        }
    }

    public static EventPE createDeletionEvent(VocabularyPE vocabularyPE, PersonPE registrator,
            String reason)
    {
        EventPE event = new EventPE();
        event.setEventType(EventType.DELETION);
        event.setEntityType(EntityType.VOCABULARY);
        event.setIdentifier(vocabularyPE.getCode());
        event.setDescription(getDeletionDescription(vocabularyPE));
        event.setReason(reason);
        event.setRegistrator(registrator);

        return event;
    }

    private static String getDeletionDescription(VocabularyPE vocabularyPE)
    {
        return String.format("%s", vocabularyPE.getCode());
    }

    public void updateTerms(List<VocabularyTerm> terms)
    {
        assert vocabularyPE != null : UNSPECIFIED_VOCABULARY;
        if (vocabularyPE.isManagedInternally())
        {
            throw new UserFailureException(
                    UPDATING_CONTENT_OF_INTERNALLY_MANAGED_VOCABULARIES_IS_NOT_ALLOWED);
        }
        checkAllTermsPresent(vocabularyPE.getTerms(), terms);
        Map<String, VocabularyTerm> newTermsMap = prepareUpdateMap(terms);
        updateExistingTermsAndRemoveFromMap(newTermsMap);
        addNewTerms(newTermsMap);
    }

    private Map<String, VocabularyTerm> prepareUpdateMap(List<VocabularyTerm> terms)
    {
        // additionally check if all terms are present only once
        Map<String, VocabularyTerm> newTermsMap = new HashMap<String, VocabularyTerm>();
        Set<String> multipliedCodes = new LinkedHashSet<String>(); // keep order
        for (VocabularyTerm v : terms)
        {
            VocabularyTerm previousTermOrNull = newTermsMap.put(v.getCode(), v);
            if (previousTermOrNull != null)
            {
                multipliedCodes.add(v.getCode());
            }
        }
        if (multipliedCodes.size() > 0)
        {
            throw new UserFailureException(String.format("Mulitiple rows found for terms: [%s]",
                    StringUtils.join(multipliedCodes, ",")));
        }
        return newTermsMap;
    }

    private void updateExistingTermsAndRemoveFromMap(Map<String, VocabularyTerm> newTermsMap)
    {
        for (VocabularyTermPE oldTerm : vocabularyPE.getTerms())
        {
            String code = oldTerm.getCode();
            VocabularyTerm update = newTermsMap.get(code);
            oldTerm.setDescription(update.getDescription());
            oldTerm.setLabel(update.getLabel());
            oldTerm.setOrdinal(update.getOrdinal());
            newTermsMap.remove(code);
        }
    }

    private void addNewTerms(Map<String, VocabularyTerm> newTermsMap)
    {
        for (VocabularyTerm newTerm : newTermsMap.values())
        {
            addTerm(newTerm, newTerm.getOrdinal());
        }
    }

    private void checkAllTermsPresent(Set<VocabularyTermPE> oldTerms, List<VocabularyTerm> newTerms)
    {
        Collection<String> undetected = convert(oldTerms);
        undetected.removeAll(convert(newTerms));
        if (undetected.size() > 0)
        {
            throw new UserFailureException(String.format("Missing vocabulary terms: [%s]",
                    StringUtils.join(undetected, ",")));
        }
    }

    private <T extends ICodeProvider> Collection<String> convert(Collection<T> terms)
    {
        ArrayList<String> list = new ArrayList<String>();
        for (ICodeProvider t : terms)
        {
            list.add(t.getCode());
        }
        return list;
    }

}
