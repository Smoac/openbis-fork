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

import org.springframework.dao.DataRetrievalFailureException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyTermUpdates;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * The only productive implementation of {@link IVocabularyTermBO}.
 * 
 * @author Christian Ribeaud
 */
public final class VocabularyTermBO extends AbstractBusinessObject implements IVocabularyTermBO
{
    private VocabularyTermPE vocabularyTermPE;

    public VocabularyTermBO(final IDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
    }

    //
    // IVocabularyTermBO
    //

    public void update(IVocabularyTermUpdates updates)
    {
        loadDataByTechId(TechId.create(updates));

        vocabularyTermPE.setDescription(updates.getDescription());
        vocabularyTermPE.setLabel(updates.getLabel());
        // if ordinal was changed some terms in vocabulary need to be shifted by 1
        if (vocabularyTermPE.getOrdinal().equals(updates.getOrdinal()) == false)
        {
            increaseVocabularyTermOrdinals(updates.getOrdinal());
            vocabularyTermPE.setOrdinal(updates.getOrdinal());
        }
        validateAndSave();
    }

    private void increaseVocabularyTermOrdinals(Long fromOrdinal)
    {
        getVocabularyTermDAO().increaseVocabularyTermOrdinals(vocabularyTermPE.getVocabulary(),
                fromOrdinal, 1);
    }

    private void validateAndSave()
    {
        getVocabularyTermDAO().validateAndSaveUpdatedEntity(vocabularyTermPE);
    }

    public final VocabularyTermPE getVocabularyTerm()
    {
        assert vocabularyTermPE != null : "Vocabulary term not defined.";
        return vocabularyTermPE;
    }

    private void loadDataByTechId(TechId vocabularyTermId)
    {
        try
        {
            vocabularyTermPE = getVocabularyTermDAO().getByTechId(vocabularyTermId);
        } catch (DataRetrievalFailureException exception)
        {
            throw new UserFailureException(exception.getMessage());
        }
    }
}
