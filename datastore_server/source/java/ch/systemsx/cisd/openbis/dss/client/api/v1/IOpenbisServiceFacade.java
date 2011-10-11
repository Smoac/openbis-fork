/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.api.v1;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.NewVocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;

/**
 * Provides a fully-blown API for openBIS integration. The internals of the openBIS architecture are
 * abstracted away from the API users (e.g. they do not need to know openBIS is actually two servers
 * - AS and DSS).
 * <p>
 * For the most basic operations (simple listing operations, data set upload/download) you can use
 * instances of {@link IOpenbisServiceFacade} as "simple" {@link ISimpleOpenbisServiceFacade}.
 * </p>
 * 
 * @author Kaloyan Enimanev
 */
public interface IOpenbisServiceFacade extends ISimpleOpenbisServiceFacade
{

    /**
     * Return all samples that match the search criteria.
     * 
     * @param searchCriteria The sample metadata values to be matched against.
     */
    public List<Sample> searchForSamples(SearchCriteria searchCriteria);

    /**
     * Return all data sets matching a specified search criteria.
     * 
     * @param searchCriteria the criteria used for searching.
     */
    public List<DataSet> searchForDataSets(SearchCriteria searchCriteria);

    /**
     * Return all data sets attached to the given samples with connections.
     * 
     * @param samples The samples for which we return attached data sets.
     */
    public List<DataSet> listDataSets(List<Sample> samples, EnumSet<Connections> connectionsToGet);

    /**
     * Adds new ad-hoc terms to a vocabulary starting from specified ordinal + 1.
     * 
     * @param vocabularyId The id of vocabulary which should be extended.
     * @param code Code of new vocabulary term.
     * @param label Label of new vocabulary term.
     * @param description Free text describing new vocabulary term.
     * @param previousTermOrdinal new vocabulary term will be placed right after vocabulary term
     *            with given ordinal number.
     * @deprecated Please use the {@link #addAdHocVocabularyTerm(Long, NewVocabularyTerm)} method
     *             instead.
     */
    @Deprecated
    public void addAdHocVocabularyTerm(TechId vocabularyId, String code, String label,
            String description, Long previousTermOrdinal);

    /**
     * Adds new ad-hoc terms to a vocabulary starting from specified ordinal + 1.
     * 
     * @param vocabularyId The id of vocabulary which should be extended.
     * @param term the vocabulary term to be added.
     */
    public void addAdHocVocabularyTerm(Long vocabularyId, NewVocabularyTerm term);

    /**
     * Returns map of avaialable vocabulary terms. Available since minor version 6.
     * 
     * @deprecated Please use the {@link #listVocabularies()} method instead.
     */
    @Deprecated
    public HashMap<Vocabulary, List<ControlledVocabularyPropertyType.VocabularyTerm>> getVocabularyTermsMap();
}
