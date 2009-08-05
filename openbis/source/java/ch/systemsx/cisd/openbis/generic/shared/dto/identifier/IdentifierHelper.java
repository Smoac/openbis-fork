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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

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
        final String sampleCode = extractCode(samplePE);
        if (databaseInstance != null)
        {
            return new SampleIdentifier(createDatabaseInstanceIdentifier(databaseInstance),
                    sampleCode);
        } else if (group != null)
        {
            return new SampleIdentifier(createGroupIdentifier(group), sampleCode);
        } else
        {
            return SampleIdentifier.createHomeGroup(sampleCode);
        }
    }

    /**
     * Extracts "sub" code from {@link SamplePE} that is in exactly the same as the one kept in DB
     * (so the same one that is mapped with {@link SamplePE#getCode()}).
     */
    public final static String extractSubCode(SamplePE samplePE)
    {
        return StringEscapeUtils.escapeHtml(samplePE.getCode());
    }

    /**
     * Extracts "full" sample code from {@link SamplePE}. For contained samples has a prefix
     * consisting of container sample DB code and a colon, otherwise it is just sample DB code,
     * where by "sample DB code" is the code kept in the DB.
     */
    public final static String extractCode(SamplePE samplePE)
    {
        final String subCode = extractSubCode(samplePE);

        String code = null;
        if (samplePE.getContainer() != null
                && HibernateUtils.isInitialized(samplePE.getContainer()))
        {
            String containerCode = StringEscapeUtils.escapeHtml(samplePE.getContainer().getCode());
            code = containerCode + ":" + subCode;
        } else
        {
            code = subCode;
        }
        return code;
    }

    /**
     * Creates a {@link ProjectIdentifier} from given <var>project</var>.
     */
    public final static ProjectIdentifier createProjectIdentifier(final ProjectPE project)
    {
        assert project != null : "Unspecified project";
        final GroupPE group = project.getGroup();
        final DatabaseInstancePE databaseInstance = group.getDatabaseInstance();
        String instanceCode =
                databaseInstance.isOriginalSource() ? null : databaseInstance.getCode();
        final ProjectIdentifier identifier =
                new ProjectIdentifier(instanceCode, group.getCode(), project.getCode());
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

    public static final List<SampleIdentifier> extractSampleIdentifiers(String[] samples)
    {
        List<SampleIdentifier> sampleIdentifiers = new ArrayList<SampleIdentifier>();
        for (String s : samples)
        {
            sampleIdentifiers.add(SampleIdentifierFactory.parse(s));
        }
        return sampleIdentifiers;
    }

    static public void fillAndCheckGroup(SampleIdentifier sample, String expectedGroupCode)
    {
        if (sample.isDatabaseInstanceLevel())
        {
            return;
        } else if (sample.isInsideHomeGroup())
        {
            sample.getGroupLevel().setGroupCode(expectedGroupCode);
        } else if (sample.getGroupLevel().getGroupCode().equalsIgnoreCase(expectedGroupCode))
        {
            return;
        } else
        {
            throw new UserFailureException(String.format(
                    "Sample '%s' does not belong to the group '%s'", sample, expectedGroupCode));
        }
    }
}
