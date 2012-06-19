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
import java.util.Date;

import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

public class DataSetUpdateBuilder extends Builder<DataSetUpdatesDTO>
{
    private TechId datasetId;

    private Date version;

    private ExperimentIdentifier experimentIdentifier;

    private SampleIdentifier sampleIdentifier;

    private String fileFormatTypeCode;

    public DataSetUpdateBuilder(ICommonServerForInternalUse commonServer,
            IGenericServer genericServer, DataSet data)
    {
        super(commonServer, genericServer);
        this.datasetId = new TechId(data.getId());
        this.version = data.getModificationDate();
        this.fileFormatTypeCode = data.getFileFormatType().getCode();
    }

    public DataSetUpdateBuilder withSample(Sample sample)
    {
        withExperiment(sample.getExperiment());
        this.sampleIdentifier = id(sample);
        return this;
    }

    public DataSetUpdateBuilder withExperiment(Experiment experiment)
    {
        this.experimentIdentifier = new ExperimentIdentifier(experiment);
        return this;
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

        return updates;
    }
}