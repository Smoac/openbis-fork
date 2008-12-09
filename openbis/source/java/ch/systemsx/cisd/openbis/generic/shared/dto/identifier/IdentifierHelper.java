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

package ch.systemsx.cisd.openbis.generic.shared.dto.identifier;

import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * Some useful methods around identifiers.
 * 
 * @author Christian Ribeaud
 */
public final class IdentifierHelper
{

    private IdentifierHelper()
    {
        // Can not be instantiated.
    }

    /**
     * Creates a {@link GroupIdentifier} from given <var>groupPE</var>.
     */
    public final static GroupIdentifier createGroupIdentifier(final GroupPE groupPE)
    {
        assert groupPE != null : "Unspecified group";
        assert groupPE.getDatabaseInstance() != null : "Any group must "
                + "be attached to a database instance";
        return new GroupIdentifier(groupPE.getDatabaseInstance().getCode(), groupPE.getCode());
    }

    /**
     * Creates a {@link GroupIdentifier} from given <var>groupPE</var>.
     */
    public final static DatabaseInstanceIdentifier createDatabaseInstanceIdentifier(
            final DatabaseInstancePE databaseInstancePE)
    {
        assert databaseInstancePE != null : "Unspecified database instance";
        return new DatabaseInstanceIdentifier(databaseInstancePE.getCode());
    }

    /**
     * Creates a {@link SampleIdentifier} from given <var>samplePE</var>.
     */
    public final static SampleIdentifier createSampleIdentifier(final SamplePE samplePE)
    {
        assert samplePE != null : "Unspecified sample";
        final DatabaseInstancePE databaseInstance = samplePE.getDatabaseInstance();
        final GroupPE group = samplePE.getGroup();
        final String sampleCode = samplePE.getCode();
        if (databaseInstance != null)
        {
            return new SampleIdentifier(createDatabaseInstanceIdentifier(databaseInstance),
                    sampleCode);
        } else if (group != null)
        {
            return new SampleIdentifier(createGroupIdentifier(group), samplePE.getCode());
        } else
        {
            return SampleIdentifier.createHomeGroup(sampleCode);
        }
    }

    /**
     * Creates a {@link ProjectIdentifier} from given <var>project</var>.
     */
    public final static ProjectIdentifier createProjectIdentifier(final ProjectPE project)
    {
        assert project != null : "Unspecified project";
        final GroupPE group = project.getGroup();
        final DatabaseInstancePE databaseInstance = group.getDatabaseInstance();
        final ProjectIdentifier identifier =
                new ProjectIdentifier(databaseInstance.getCode(), group.getCode(), project
                        .getCode());
        return identifier;
    }

    /**
     * Creates a {@link ExperimentIdentifier} from given <var>experiment</var>.
     */
    public final static ExperimentIdentifier createExperimentIdentifier(
            final ExperimentPE experiment)
    {
        assert experiment != null : "Unspecified experiment";
        final ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifier(createProjectIdentifier(experiment.getProject()),
                        experiment.getCode());
        return experimentIdentifier;
    }
}
