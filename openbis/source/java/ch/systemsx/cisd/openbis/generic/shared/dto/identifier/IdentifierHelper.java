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

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletedSamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
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
     * Creates a {@link SpaceIdentifier} from given <var>groupPE</var>.
     */
    public final static SpaceIdentifier createGroupIdentifier(final SpacePE groupPE)
    {
        assert groupPE != null : "Unspecified space";
        assert groupPE.getDatabaseInstance() != null : "Any space must "
                + "be attached to a database instance";
        return new SpaceIdentifier(createDatabaseInstanceIdentifier(groupPE.getDatabaseInstance()),
                groupPE.getCode());
    }

    /**
     * Creates a {@link DatabaseInstanceIdentifier} from given <var>groupPE</var>.
     */
    public final static DatabaseInstanceIdentifier createDatabaseInstanceIdentifier(
            final DatabaseInstancePE databaseInstancePE)
    {
        assert databaseInstancePE != null : "Unspecified database instance";
        if (databaseInstancePE.isOriginalSource())
        {
            return DatabaseInstanceIdentifier.createHome();
        } else
        {
            return new DatabaseInstanceIdentifier(databaseInstancePE.getCode());
        }
    }

    /**
     * Creates a {@link SampleIdentifier} from given <var>samplePE</var>.
     */
    public final static SampleIdentifier createSampleIdentifier(final SamplePE samplePE)
    {
        assert samplePE != null : "Unspecified sample";
        final DatabaseInstancePE databaseInstance = samplePE.getDatabaseInstance();
        final SpacePE group = samplePE.getSpace();
        final String sampleCode = extractCode(samplePE);
        return createSampleIdentifier(databaseInstance, group, sampleCode);
    }

    public static final SampleIdentifier sample(final SamplePE sample)
    {
        SampleIdentifier sampleId;
        if (sample.getSpace() == null)
        {
            sampleId =
                    new SampleIdentifier(new DatabaseInstanceIdentifier(sample
                            .getDatabaseInstance().getCode()), sample.getCode());
        } else
        {
            sampleId =
                    new SampleIdentifier(new SpaceIdentifier(new DatabaseInstanceIdentifier(sample
                            .getSpace().getDatabaseInstance().getCode()), sample.getSpace()
                            .getCode()), sample.getCode());
        }

        if (sample.getContainer() != null)
        {
            sampleId.addContainerCode(sample.getContainer().getCode());
        }

        return sampleId;
    }

    public static final SpaceIdentifier space(final SpacePE space)
    {
        return new SpaceIdentifier(new DatabaseInstanceIdentifier(space.getDatabaseInstance()
                .getCode()), space.getCode());
    }

    /**
     * Creates a {@link SampleIdentifier} from given <var>samplePE</var>.
     */
    public final static SampleIdentifier createSampleIdentifier(
            final DeletedSamplePE deletedSamplePE)
    {
        assert deletedSamplePE != null : "Unspecified sample";
        final DatabaseInstancePE databaseInstance = deletedSamplePE.getDatabaseInstance();
        final SpacePE group = deletedSamplePE.getSpace();
        final String sampleCode = deletedSamplePE.getCode();
        return createSampleIdentifier(databaseInstance, group, sampleCode);
    }

    public final static SampleIdentifier createSampleIdentifier(
            final DatabaseInstanceIdentifier databaseInstanceIdentifier,
            SpaceIdentifier spaceIdentifier, String sampleCode, String sampleContainerCode)
    {
        String fullSampleCode = convertCode(sampleCode, sampleContainerCode);

        if (databaseInstanceIdentifier != null)
        {
            return new SampleIdentifier(databaseInstanceIdentifier, fullSampleCode);
        } else if (spaceIdentifier != null)
        {
            return new SampleIdentifier(spaceIdentifier, fullSampleCode);
        } else
        {
            return SampleIdentifier.createHomeGroup(fullSampleCode);
        }
    }

    private static SampleIdentifier createSampleIdentifier(
            final DatabaseInstancePE databaseInstance, final SpacePE group, final String sampleCode)
    {
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
     * Creates a {@link SampleIdentifier} from given <var>sample</var>.
     * <p>
     * NOTE: Specified sample should have full code set (with container code part).
     */
    public static SampleIdentifier createSampleIdentifier(Sample sample)
    {
        assert sample != null : "Unspecified sample";
        final Space space = sample.getSpace();
        if (space != null)
        {
            DatabaseInstanceIdentifier instanceIdentifier =
                    space.getInstance().isHomeDatabase() ? DatabaseInstanceIdentifier.HOME_INSTANCE
                            : new DatabaseInstanceIdentifier(space.getInstance().getCode());
            SpaceIdentifier groupIdentifier =
                    new SpaceIdentifier(instanceIdentifier, space.getCode());
            return new SampleIdentifier(groupIdentifier, sample.getCode());
        } else
        {
            DatabaseInstanceIdentifier instanceIdentifier =
                    (sample.getDatabaseInstance().isHomeDatabase()) ? DatabaseInstanceIdentifier.HOME_INSTANCE
                            : new DatabaseInstanceIdentifier(sample.getDatabaseInstance().getCode());
            return new SampleIdentifier(instanceIdentifier, sample.getCode());
        }
    }

    /**
     * Extracts "sub" code from {@link SamplePE} that is in exactly the same as the one kept in DB
     * (so the same one that is mapped with {@link SamplePE#getCode()}).
     */
    public final static String extractSubCode(SamplePE samplePE)
    {
        return samplePE.getCode();
    }

    /**
     * Converts "sub" code from <var>sampleCode</var> that is in exactly the same as the one kept in
     * DB.
     */
    public final static String convertSubCode(String sampleCode)
    {
        return sampleCode;
    }

    /**
     * Extracts "full" sample code from {@link SamplePE}. For contained samples has a prefix
     * consisting of container sample DB code and a colon, otherwise it is just sample DB code,
     * where by "sample DB code" is the code kept in the DB.
     */
    public final static String extractCode(SamplePE samplePE)
    {
        final String subCode = extractSubCode(samplePE);

        final String code;
        if (samplePE.getContainer() != null
                && HibernateUtils.isInitialized(samplePE.getContainer()))
        {
            final String containerCode = samplePE.getContainer().getCode();
            code = containerCode + ":" + subCode;
        } else
        {
            code = subCode;
        }
        return code;
    }

    /**
     * Converts "full" sample code from the <var>sampleCode</var> and
     * <var>containerCodeOrNull</var>. For contained samples (i.e.
     * <code>containerCodeOrNull != null</code>) has a prefix consisting of container sample DB code
     * and a colon, otherwise it is just sample DB code, where by "sample DB code" is the code kept
     * in the DB.
     */
    public final static String convertCode(String sampleCode, String containerCodeOrNull)
    {
        final String subCode = convertSubCode(sampleCode);

        final String code;
        if (containerCodeOrNull != null)
        {
            final String containerCode = containerCodeOrNull;
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
        final SpacePE group = project.getSpace();
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

    public static final List<SampleIdentifier> extractSampleIdentifiers(NewExperiment newExperiment)
    {
        final List<SampleIdentifier> result = new ArrayList<SampleIdentifier>();
        for (NewSamplesWithTypes samplesWithTypes : newExperiment.getNewSamples())
        {
            for (NewSample sample : samplesWithTypes.getNewEntities())
            {
                SampleIdentifier identifier = SampleIdentifierFactory.parse(sample);
                // if default container is not specified, use the "new container" if it's specified
                if (identifier.tryGetContainerCode() == null
                        && StringUtils.isEmpty(sample.getContainerIdentifier()) == false)
                {
                    final SampleIdentifier containerIdentifier =
                            SampleIdentifierFactory.parse(sample.getContainerIdentifier());
                    identifier.addContainerCode(containerIdentifier.getSampleCode());
                }
                result.add(identifier);
            }
        }
        return result;
    }

    public static final List<SampleIdentifier> extractSampleIdentifiers(String[] samples,
            String defaultSpace)
    {
        List<SampleIdentifier> sampleIdentifiers = new ArrayList<SampleIdentifier>();
        for (String s : samples)
        {
            sampleIdentifiers.add(SampleIdentifierFactory.parse(s, defaultSpace));
        }
        return sampleIdentifiers;
    }

    static public void fillAndCheckGroup(SampleIdentifier sample, String expectedGroupCode)
    {
        if (sample.isDatabaseInstanceLevel())
        {
            return;
        } else if (sample.isInsideHomeSpace())
        {
            sample.getSpaceLevel().setSpaceCode(expectedGroupCode);
        } else if (sample.getSpaceLevel().getSpaceCode().equalsIgnoreCase(expectedGroupCode))
        {
            return;
        } else
        {
            throw new UserFailureException(String.format(
                    "Sample '%s' does not belong to the space '%s'", sample, expectedGroupCode));
        }
    }

    static public void fillSpaceIfNotSpecified(SampleIdentifier sample, String defaultSpaceCode)
    {
        if (sample.isInsideHomeSpace())
        {
            sample.getSpaceLevel().setSpaceCode(defaultSpaceCode);
        }
    }

}
