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

package ch.systemsx.cisd.openbis.systemtest.base;

import static ch.systemsx.cisd.openbis.systemtest.base.BaseTest.id;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

public class ExternalDataBuilder extends Builder<ExternalData>
{
    private IETLLIMSService etlService;

    private SampleIdentifier sampleIdentifier;

    private ExperimentIdentifier experimentIdentifier;

    private List<String> parentCodes;

    private List<String> componentCodes;

    private String code;

    private boolean container;

    public ExternalDataBuilder(ICommonServerForInternalUse commonServer,
            IGenericServer genericServer,
            IETLLIMSService etlService)
    {
        super(commonServer, genericServer);
        this.etlService = etlService;
        this.code = UUID.randomUUID().toString();
        this.parentCodes = new ArrayList<String>();
        this.container = false;
        this.componentCodes = new ArrayList<String>();
    }

    public ExternalDataBuilder inSample(Sample sample)
    {
        this.sampleIdentifier = id(sample);
        if (sample.getExperiment() != null)
        {
            inExperiment(sample.getExperiment());
        }
        return this;
    }

    public ExternalDataBuilder inExperiment(Experiment experiment)
    {
        this.experimentIdentifier = new ExperimentIdentifier(experiment);
        return this;
    }

    public ExternalDataBuilder withParents(ExternalData... dataSets)
    {
        for (ExternalData parent : dataSets)
        {
            this.parentCodes.add(parent.getCode());
        }
        return this;
    }

    public ExternalDataBuilder withParent(ExternalData dataSet)
    {
        return this.withParents(dataSet);
    }

    public ExternalDataBuilder asContainer()
    {
        this.container = true;
        return this;
    }

    public ExternalDataBuilder withComponents(ExternalData... data)
    {
        for (ExternalData component : data)
        {
            this.componentCodes.add(component.getCode());
        }
        return this;
    }

    public ExternalDataBuilder withComponent(ExternalData data)
    {
        return this.withComponents(data);
    }

    @Override
    public ExternalData create()
    {
        DataSetType dataSetType = new DataSetType();
        dataSetType.setCode(UUID.randomUUID().toString());
        dataSetType.setDataSetKind(DataSetKind.EXTERNAL);
        commonServer.registerDataSetType(systemSession, dataSetType);

        NewExternalData data;
        if (this.container)
        {
            NewContainerDataSet cont = new NewContainerDataSet();
            cont.setContainedDataSetCodes(this.componentCodes);
            data = cont;
        } else
        {
            data = new NewExternalData();
        }
        data.setCode(this.code);
        data.setDataSetType(dataSetType);
        data.setFileFormatType(new FileFormatType(FileFormatType.DEFAULT_FILE_FORMAT_TYPE_CODE));
        data.setSampleIdentifierOrNull(this.sampleIdentifier);
        data.setLocatorType(new LocatorType(LocatorType.DEFAULT_LOCATOR_TYPE_CODE));
        data.setLocation(UUID.randomUUID().toString());
        data.setStorageFormat(StorageFormat.PROPRIETARY);
        data.setDataStoreCode("STANDARD");
        data.setExperimentIdentifierOrNull(this.experimentIdentifier);
        data.setParentDataSetCodes(this.parentCodes);

        if (this.sampleIdentifier != null)
        {
            etlService.registerDataSet(systemSession, sampleIdentifier, data);
        } else
        {
            etlService.registerDataSet(systemSession, experimentIdentifier, data);
        }

        return etlService.tryGetDataSet(systemSession, this.code);

    }
}