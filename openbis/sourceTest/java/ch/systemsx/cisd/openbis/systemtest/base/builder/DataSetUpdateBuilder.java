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

package ch.systemsx.cisd.openbis.systemtest.base.builder;

import static ch.systemsx.cisd.openbis.systemtest.base.BaseTest.id;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

public class DataSetUpdateBuilder extends UpdateBuilder<DataSetUpdatesDTO>
{
    private TechId datasetId;

    private Date version;

    private ExperimentIdentifier experimentIdentifier;

    private SampleIdentifier sampleIdentifier;

    private String fileFormatTypeCode;

    private List<ExternalData> parents;

    private String containerCode;

    private List<ExternalData> components;

    public DataSetUpdateBuilder(ICommonServerForInternalUse commonServer,
            IGenericServer genericServer, ExternalData data)
    {
        super(commonServer, genericServer);
        this.datasetId = new TechId(data.getId());
        this.version = data.getModificationDate();
        if (data instanceof DataSet)
        {
            this.fileFormatTypeCode = ((DataSet) data).getFileFormatType().getCode();
        }
        this.parents = null;
        this.experimentIdentifier = new ExperimentIdentifier(data.getExperiment());
    }

    public DataSetUpdateBuilder toSample(Sample sample)
    {
        toExperiment(sample.getExperiment());
        this.sampleIdentifier = id(sample);
        return this;
    }

    public DataSetUpdateBuilder toExperiment(Experiment experiment)
    {
        if (experiment != null)
        {
            this.experimentIdentifier = new ExperimentIdentifier(experiment);
        }
        return this;
    }

    public DataSetUpdateBuilder withParents(ExternalData first, ExternalData... rest)
    {
        this.parents = new ArrayList<ExternalData>();
        this.parents.add(first);
        this.parents.addAll(Arrays.asList(rest));
        return this;
    }

    public DataSetUpdateBuilder withParent(ExternalData dataSet)
    {
        return this.withParents(dataSet);
    }

    public DataSetUpdateBuilder removingParents()
    {
        this.parents = new ArrayList<ExternalData>();
        return this;
    }

    public DataSetUpdateBuilder removingSample()
    {
        this.sampleIdentifier = null;
        return this;
    }

    public DataSetUpdateBuilder withContainer(ExternalData dataSet)
    {
        this.containerCode = dataSet.getCode();
        return this;
    }

    public DataSetUpdateBuilder withComponents(ExternalData... dataSets)
    {
        this.components = new ArrayList<ExternalData>();
        for (ExternalData component : dataSets)
        {
            this.components.add(component);
        }
        return this;
    }

    public DataSetUpdateBuilder withComponent(ExternalData dataSet)
    {
        return this.withComponents(dataSet);
    }

    @Override
    public DataSetUpdatesDTO create()
    {
        DataSetUpdatesDTO updates = new DataSetUpdatesDTO();
        updates.setDatasetId(this.datasetId);
        updates.setVersion(this.version);
        updates.setFileFormatTypeCode(this.fileFormatTypeCode);
        updates.setProperties(new ArrayList<IEntityProperty>());

        updates.setExperimentIdentifierOrNull(this.experimentIdentifier);
        updates.setSampleIdentifierOrNull(this.sampleIdentifier);

        if (this.parents != null)
        {
            String[] parentCodes = new String[this.parents.size()];
            for (int i = 0; i < parentCodes.length; i++)
            {
                parentCodes[i] = this.parents.get(i).getCode();
            }
            updates.setModifiedParentDatasetCodesOrNull(parentCodes);
        }

        updates.setModifiedContainerDatasetCodeOrNull(this.containerCode);

        if (this.components != null)
        {
            String[] componentCodes = new String[this.components.size()];
            for (int i = 0; i < componentCodes.length; i++)
            {
                componentCodes[i] = this.components.get(i).getCode();
            }
            updates.setModifiedContainedDatasetCodesOrNull(componentCodes);
        }

        return updates;
    }

    @Override
    public void perform()
    {
        commonServer.updateDataSet(this.sessionToken, this.create());
    }
}