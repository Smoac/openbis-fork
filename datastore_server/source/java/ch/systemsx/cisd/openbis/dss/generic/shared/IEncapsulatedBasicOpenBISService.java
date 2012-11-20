/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * The basic subset of the {@link IEncapsulatedOpenBISService}, that requires only service and a
 * sessionToken.
 * 
 * @author Jakub Straszewski
 */
public interface IEncapsulatedBasicOpenBISService
{
    /**
     * Returns the data set type together with assigned property types for the specified data set
     * type code.
     */
    @ManagedAuthentication
    public DataSetTypeWithVocabularyTerms getDataSetType(String dataSetTypeCode);

    /**
     * {@link IETLLIMSService#searchForSamples(String, SearchCriteria)}
     */
    @ManagedAuthentication
    public List<Sample> searchForSamples(SearchCriteria searchCriteria);

    /**
     * {@link IETLLIMSService#searchForDataSets(String, SearchCriteria)}
     */
    @ManagedAuthentication
    public List<ExternalData> searchForDataSets(SearchCriteria searchCriteria);

    /**
     * {@link IETLLIMSService#listExperiments(String, ProjectIdentifier)}
     */
    @ManagedAuthentication
    public List<Experiment> listExperiments(ProjectIdentifier projectIdentifier);

    /**
     * Lists vocabulary terms.
     */
    @ManagedAuthentication
    public Collection<VocabularyTerm> listVocabularyTerms(String vocabularyCode)
            throws UserFailureException;

    /**
     * Returns a vocabulary with given code
     */
    @ManagedAuthentication
    public Vocabulary tryGetVocabulary(String code);

    /**
     * Lists materials using given criteria.
     * 
     * @return a sorted list of {@link Material}.
     */
    @ManagedAuthentication
    public List<Material> listMaterials(ListMaterialCriteria criteria, boolean withProperties);

    /**
     * List property definitions for the given entity type
     */
    @ManagedAuthentication
    public List<? extends EntityTypePropertyType<?>> listPropertyDefinitionsForEntityType(
            String code,
            ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind entityKind);

    /**
     * List metaproject for the current user.
     */
    @ManagedAuthentication
    public List<Metaproject> listMetaprojects();

}
