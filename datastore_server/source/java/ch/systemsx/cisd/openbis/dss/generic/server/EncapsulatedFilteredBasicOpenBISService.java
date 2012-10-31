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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.common.action.IMapper;
import ch.systemsx.cisd.common.exception.UserFailureException;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.AuthorizationHelper;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedBasicOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * The basic version of encapsulated openbis service, that calls the etl service and filters the
 * result to visible only to the specified user.
 * 
 * @author Jakub Straszewski
 */
public class EncapsulatedFilteredBasicOpenBISService implements IEncapsulatedBasicOpenBISService
{
    private final IETLLIMSService etlService;

    private final String systemSessionToken;

    private final IEncapsulatedOpenBISService encapsulatedService;

    private final String userName;

    public EncapsulatedFilteredBasicOpenBISService(String userName, IETLLIMSService etlService,
            IEncapsulatedOpenBISService encapsulatedService, String systemSessionToken)
    {
        this.etlService = etlService;
        this.encapsulatedService = encapsulatedService;

        this.systemSessionToken = systemSessionToken;

        this.userName = userName;
    }

    @Override
    public DataSetTypeWithVocabularyTerms getDataSetType(String dataSetTypeCode)
    {

        return etlService.getDataSetType(systemSessionToken, dataSetTypeCode);
    }

    @Override
    public List<Sample> searchForSamples(SearchCriteria searchCriteria)
    {
        IMapper<Sample, String> idMapper = new IMapper<Sample, String>()
            {
                @Override
                public String map(Sample item)
                {
                    return item.getIdentifier();
                }
            };

        List<Sample> samples = etlService.searchForSamples(systemSessionToken, searchCriteria);

        return AuthorizationHelper.filterToVisibleSamples(encapsulatedService, userName, samples,
                idMapper);

    }

    @Override
    public List<ExternalData> searchForDataSets(SearchCriteria searchCriteria)
    {
        IMapper<ExternalData, String> codeMapper = new IMapper<ExternalData, String>()
            {
                @Override
                public String map(ExternalData item)
                {
                    return item.getCode();
                }
            };

        List<ExternalData> datasets =
                etlService.searchForDataSets(systemSessionToken, searchCriteria);

        return AuthorizationHelper.filterToVisibleDatasets(encapsulatedService, userName, datasets,
                codeMapper);
    }

    @Override
    public List<Experiment> listExperiments(ProjectIdentifier projectIdentifier)
    {
        IMapper<Experiment, String> codeMapper = new IMapper<Experiment, String>()
            {
                @Override
                public String map(Experiment item)
                {
                    return item.getIdentifier();
                }
            };

        List<Experiment> datasets =
                etlService.listExperiments(systemSessionToken, projectIdentifier);

        return AuthorizationHelper.filterToVisibleExperiments(encapsulatedService, userName,
                datasets, codeMapper);
    }

    @Override
    public Collection<VocabularyTerm> listVocabularyTerms(String vocabularyCode)
            throws UserFailureException
    {
        return etlService.listVocabularyTerms(systemSessionToken, vocabularyCode);
    }

    @Override
    public Vocabulary tryGetVocabulary(String code)
    {
        return etlService.tryGetVocabulary(systemSessionToken, code);
    }

    @Override
    public List<Material> listMaterials(ListMaterialCriteria criteria, boolean withProperties)
    {
        return etlService.listMaterials(systemSessionToken, criteria, withProperties);
    }
}
