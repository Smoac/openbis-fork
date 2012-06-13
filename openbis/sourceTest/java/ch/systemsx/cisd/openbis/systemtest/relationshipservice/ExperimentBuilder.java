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

package ch.systemsx.cisd.openbis.systemtest.relationshipservice;

import java.util.ArrayList;
import java.util.UUID;

import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

public class ExperimentBuilder extends Builder<Experiment>
{
    private Project project;

    public ExperimentBuilder(ICommonServerForInternalUse commonServer, IGenericServer genericServer)
    {
        super(commonServer, genericServer);
    }

    @SuppressWarnings("hiding")
    public ExperimentBuilder inProject(Project project)
    {
        this.project = project;
        return this;
    }

    @Override
    public Experiment create()
    {

        String experimentTypeCode = UUID.randomUUID().toString();
        ExperimentType experimentType = new ExperimentType();
        experimentType.setCode(experimentTypeCode);
        experimentType.setDatabaseInstance(this.project.getSpace().getInstance());
        experimentType.setDescription("description");
        experimentType
                .setExperimentTypePropertyTypes(new ArrayList<ExperimentTypePropertyType>());

        commonServer.registerExperimentType(systemSession, experimentType);

        String experimentId = "/" + this.project.getSpace().getCode() + "/" +
                this.project.getCode() + "/"
                + UUID.randomUUID().toString();

        NewExperiment details = new NewExperiment(experimentId, experimentType.getCode());
        details.setAttachments(new ArrayList<NewAttachment>());
        details.setGenerateCodes(false);
        details.setNewSamples(new ArrayList<NewSamplesWithTypes>());
        details.setProperties(new IEntityProperty[0]);
        details.setRegisterSamples(false);
        details.setSamples(new String[0]);
        genericServer.registerExperiment(systemSession, details,
                new ArrayList<NewAttachment>());
        return getExperiment(experimentId);
    }

    private Experiment getExperiment(String experimentId)
    {
        String[] codes = experimentId.split("/");
        return commonServer.getExperimentInfo(systemSession, new ExperimentIdentifier(
                "CISD",
                codes[1], codes[2], codes[3]));
    }

}
