/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.vocabulary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.delete.VocabularyTermDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractDeleteEntityExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IVocabularyBO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * @author pkupczyk
 */
@Component
public class DeleteVocabularyTermExecutor
        extends AbstractDeleteEntityExecutor<Void, IVocabularyTermId, VocabularyTermPE, VocabularyTermDeletionOptions> implements
        IDeleteVocabularyTermExecutor
{

    @Autowired
    private IMapVocabularyTermByIdExecutor mapTermByIdExecutor;

    @Override
    protected Map<IVocabularyTermId, VocabularyTermPE> map(IOperationContext context, List<? extends IVocabularyTermId> entityIds)
    {
        return mapTermByIdExecutor.map(context, entityIds);
    }

    @Override
    protected void checkAccess(IOperationContext context, IVocabularyTermId entityId, VocabularyTermPE entity)
    {
        // nothing to do
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, VocabularyTermPE entity)
    {
        // nothing to do
    }

    @Override
    protected Void delete(IOperationContext context, Collection<VocabularyTermPE> terms, VocabularyTermDeletionOptions deletionOptions)
    {
        Map<VocabularyTermPE, VocabularyTermPE> replacementMap = getReplacementMap(context, deletionOptions);
        Map<VocabularyPE, List<VocabularyTerm>> termsToBeDeletedMap = new HashMap<VocabularyPE, List<VocabularyTerm>>();
        Map<VocabularyPE, List<VocabularyTermReplacement>> termsToBeReplacedMap = new HashMap<VocabularyPE, List<VocabularyTermReplacement>>();

        for (VocabularyTermPE term : terms)
        {
            if (replacementMap.get(term) == null)
            {
                List<VocabularyTerm> termsToBeDeleted = termsToBeDeletedMap.get(term.getVocabulary());
                if (termsToBeDeleted == null)
                {
                    termsToBeDeleted = new ArrayList<VocabularyTerm>();
                    termsToBeDeletedMap.put(term.getVocabulary(), termsToBeDeleted);
                }
                termsToBeDeleted.add(createDeleted(term.getCode()));
            } else
            {
                List<VocabularyTermReplacement> termsToBeReplaced = termsToBeReplacedMap.get(term.getVocabulary());
                if (termsToBeReplaced == null)
                {
                    termsToBeReplaced = new ArrayList<VocabularyTermReplacement>();
                    termsToBeReplacedMap.put(term.getVocabulary(), termsToBeReplaced);
                }
                termsToBeReplaced.add(createReplaced(term.getCode(), replacementMap.get(term).getCode()));
            }
        }

        Set<VocabularyTermPE> unmatchedReplacements = new HashSet<VocabularyTermPE>(replacementMap.keySet());
        unmatchedReplacements.removeAll(terms);
        if (unmatchedReplacements.size() > 0)
        {
            throw new UserFailureException(
                    "The following terms where not chosen to be deleted but had replacements specified: " + unmatchedReplacements + ".");
        }

        Set<VocabularyPE> vocabularies = new HashSet<VocabularyPE>();
        vocabularies.addAll(termsToBeDeletedMap.keySet());
        vocabularies.addAll(termsToBeReplacedMap.keySet());

        for (VocabularyPE vocabulary : vocabularies)
        {
            List<VocabularyTerm> termsToBeDeleted = termsToBeDeletedMap.get(vocabulary);
            List<VocabularyTermReplacement> termsToBeReplaced = termsToBeReplacedMap.get(vocabulary);

            if (termsToBeDeleted == null)
            {
                termsToBeDeleted = Collections.emptyList();
            }
            if (termsToBeReplaced == null)
            {
                termsToBeReplaced = Collections.emptyList();
            }

            IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(context.getSession());
            vocabularyBO.loadDataByTechId(new TechId(vocabulary.getId()));
            vocabularyBO.delete(termsToBeDeleted, termsToBeReplaced);
            vocabularyBO.save();
        }

        return null;
    }

    private Map<VocabularyTermPE, VocabularyTermPE> getReplacementMap(IOperationContext context, VocabularyTermDeletionOptions deletionOptions)
    {
        Collection<IVocabularyTermId> termIds = new HashSet<IVocabularyTermId>();
        for (Map.Entry<IVocabularyTermId, IVocabularyTermId> entry : deletionOptions.getReplacements().entrySet())
        {
            termIds.add(entry.getKey());
            termIds.add(entry.getValue());
        }
        Map<IVocabularyTermId, VocabularyTermPE> termMap = mapTermByIdExecutor.map(context, termIds);

        Map<VocabularyTermPE, VocabularyTermPE> replacementMap = new HashMap<VocabularyTermPE, VocabularyTermPE>();
        for (Map.Entry<IVocabularyTermId, IVocabularyTermId> entry : deletionOptions.getReplacements().entrySet())
        {
            VocabularyTermPE replaced = termMap.get(entry.getKey());
            VocabularyTermPE replacement = termMap.get(entry.getValue());

            if (replaced != null && replacement != null)
            {
                if (false == replaced.getVocabulary().equals(replacement.getVocabulary()))
                {
                    throw new UserFailureException("Replaced " + entry.getKey() + " and replacement " + entry.getValue()
                            + " terms cannot belong to different vocabularies.");
                } else
                {
                    replacementMap.put(replaced, replacement);
                }
            } else if (replacement == null)
            {
                throw new UserFailureException("Replacement term " + entry.getValue() + " does not exist.");
            }
        }

        return replacementMap;
    }

    private VocabularyTermReplacement createReplaced(String replacedCode, String replacementCode)
    {
        VocabularyTerm replaced = new VocabularyTerm();
        replaced.setCode(replacedCode);

        VocabularyTermReplacement replacement = new VocabularyTermReplacement();
        replacement.setTerm(replaced);
        replacement.setReplacementCode(replacementCode);

        return replacement;
    }

    private VocabularyTerm createDeleted(String code)
    {
        VocabularyTerm deleted = new VocabularyTerm();
        deleted.setCode(code);
        return deleted;
    }

}
