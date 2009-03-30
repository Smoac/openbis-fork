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

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats;

/**
 * Business object of a vocabulary. Holds an instance of {@link VocabularyPE}.
 * 
 * @author Christian Ribeaud
 */
public interface IVocabularyBO extends IBusinessObject
{
    /**
     * Defines a new vocabulary.
     * <p>
     * After invocation of this method {@link IBusinessObject#save()} should be invoked to store the
     * new vocabulary in the <i>Data Access Layer</i>.
     * </p>
     * 
     * @throws UserFailureException if given <var>vocabulary</var> does already exist.
     */
    public void define(final Vocabulary vocabulary) throws UserFailureException;

    /**
     * Returns the loaded {@link VocabularyPE}.
     */
    public VocabularyPE getVocabulary();

    /**
     * Loads specified vocabulary from the database.
     * 
     * @throws UserFailureException if no vocabulary found for <code>vocabularyCode</code>.
     */
    public void load(String vocabularyCode) throws UserFailureException;

    /** @return terms with their usage statistics for the loaded vocabulary */
    public List<VocabularyTermWithStats> countTermsUsageStatistics();
    
    /**
     * Add the specified terms to a loaded vocabulary.
     */
    public void addNewTerms(List<String> newTerms);

    /**
     * Deletes the specified terms from a loaded vocabulary and replaces terms which are used.
     */
    public void delete(List<VocabularyTerm> termsToBeDeleted,
            List<VocabularyTermReplacement> termsToBeReplaced);

}
