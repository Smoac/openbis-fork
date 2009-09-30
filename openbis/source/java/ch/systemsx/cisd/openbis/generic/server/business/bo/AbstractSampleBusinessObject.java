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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwner;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * An <i>abstract</i> {@link AbstractSampleIdentifierBusinessObject} extension for <i>Business
 * Object</i> which has to do with {@link SamplePE}.
 * 
 * @author Christian Ribeaud
 */
abstract class AbstractSampleBusinessObject extends AbstractSampleIdentifierBusinessObject
{
    protected final IEntityPropertiesConverter entityPropertiesConverter;

    /**
     * Whether this object works with only new samples (that is: not yet saved into the database)
     * right now.
     */
    protected boolean onlyNewSamples = true;

    AbstractSampleBusinessObject(final IDAOFactory daoFactory, final Session session)
    {
        this(daoFactory, session, new EntityPropertiesConverter(EntityKind.SAMPLE, daoFactory));
    }

    AbstractSampleBusinessObject(final IDAOFactory daoFactory, final Session session,
            final IEntityPropertiesConverter entityPropertiesConverter)
    {
        super(daoFactory, session);
        this.entityPropertiesConverter = entityPropertiesConverter;
    }

    private final void defineSampleProperties(final SamplePE sample,
            final IEntityProperty[] sampleProperties)
    {
        final String sampleTypeCode = sample.getSampleType().getCode();
        final List<SamplePropertyPE> properties =
                entityPropertiesConverter.convertProperties(sampleProperties, sampleTypeCode,
                        sample.getRegistrator());
        for (final SamplePropertyPE sampleProperty : properties)
        {
            sample.addProperty(sampleProperty);
        }
    }

    /**
     * Creates an new {@link SamplePE} object out of given <var>newSample</var>.
     * <p>
     * Does not trigger any insert in the database.
     * </p>
     * 
     * @param experimentCacheOrNull
     */
    final SamplePE createSample(final NewSample newSample,
            Map<String, SampleTypePE> sampleTypeCacheOrNull,
            Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCacheOrNull,
            Map<String, ExperimentPE> experimentCacheOrNull) throws UserFailureException
    {
        final SampleIdentifier sampleIdentifier =
                SampleIdentifierFactory.parse(newSample.getIdentifier());
        final SampleOwnerIdentifier sampleOwnerIdentifier =
                sampleIdentifier.createSampleOwnerIdentifier();
        SampleOwner sampleOwner =
                (sampleOwnerCacheOrNull != null) ? sampleOwnerCacheOrNull
                        .get(sampleOwnerIdentifier) : null;
        if (sampleOwner == null)
        {
            sampleOwner = getSampleOwnerFinder().figureSampleOwner(sampleIdentifier);
            if (sampleOwnerCacheOrNull != null)
            {
                sampleOwnerCacheOrNull.put(sampleOwnerIdentifier, sampleOwner);
            }
        }
        SampleTypePE sampleTypePE =
                (sampleTypeCacheOrNull != null) ? sampleTypeCacheOrNull.get(newSample
                        .getSampleType().getCode()) : null;
        if (sampleTypePE == null)
        {
            sampleTypePE = getSampleType(newSample.getSampleType().getCode());
            if (sampleTypeCacheOrNull != null)
            {
                sampleTypeCacheOrNull.put(newSample.getSampleType().getCode(), sampleTypePE);
            }
        }
        String experimentIdentifier = newSample.getExperimentIdentifier();
        ExperimentPE experimentPE = tryFindExperiment(experimentCacheOrNull, experimentIdentifier);
        final SamplePE samplePE = new SamplePE();
        samplePE.setExperiment(experimentPE);
        samplePE.setCode(sampleIdentifier.getSampleSubCode());
        samplePE.setRegistrator(findRegistrator());
        samplePE.setSampleType(sampleTypePE);
        samplePE.setGroup(sampleOwner.tryGetGroup());
        samplePE.setDatabaseInstance(sampleOwner.tryGetDatabaseInstance());
        defineSampleProperties(samplePE, newSample.getProperties());
        String parentIdentifier = newSample.getParentIdentifier();
        setGeneratedFrom(sampleIdentifier, samplePE, parentIdentifier);
        String containerIdentifier = newSample.getContainerIdentifier();
        setContainer(sampleIdentifier, samplePE, containerIdentifier);
        samplePE.setPermId(getPermIdDAO().createPermId());
        return samplePE;
    }

    private ExperimentPE tryFindExperiment(Map<String, ExperimentPE> experimentCacheOrNull,
            String experimentIdentifier)
    {
        ExperimentPE experimentPE = null;
        if (experimentIdentifier != null)
        {
            experimentPE =
                    (experimentCacheOrNull != null) ? experimentCacheOrNull
                            .get(experimentIdentifier) : null;
            if (experimentPE == null)
            {
                ExperimentIdentifier expIdent =
                        new ExperimentIdentifierFactory(experimentIdentifier).createIdentifier();
                fillGroupIdentifier(expIdent);
                experimentPE = findExperiment(expIdent);
                if (experimentCacheOrNull != null)
                {
                    experimentCacheOrNull.put(experimentIdentifier, experimentPE);
                }
            }
        }
        return experimentPE;
    }

    protected void setContainer(final SampleIdentifier sampleIdentifier, final SamplePE samplePE,
            String containerIdentifier)
    {
        final SamplePE containerPE = tryGetValidSample(containerIdentifier, sampleIdentifier);
        samplePE.setContainer(containerPE);
    }

    protected void setGeneratedFrom(final SampleIdentifier sampleIdentifier,
            final SamplePE samplePE, String parentIdentifier)
    {
        final SamplePE parentPE = tryGetValidSample(parentIdentifier, sampleIdentifier);
        if (parentPE != null)
        {
            samplePE.setGeneratedFrom(parentPE);
            samplePE.setTop(parentPE.getTop() == null ? parentPE : parentPE.getTop());
        } else
        {
            samplePE.setGeneratedFrom(null);
            samplePE.setTop(null);
        }
    }

    private SamplePE tryGetValidSample(final String parentIdentifierOrNull,
            final SampleIdentifier sampleIdentifier)
    {
        if (parentIdentifierOrNull == null)
        {
            return null;
        }
        final SamplePE parentPE =
                getSampleByIdentifier(SampleIdentifierFactory.parse(parentIdentifierOrNull));
        if (parentPE.getInvalidation() != null)
        {
            throw UserFailureException.fromTemplate(
                    "Cannot register sample '%s': parent '%s' has been invalidated.",
                    sampleIdentifier, parentIdentifierOrNull);
        }
        if (parentPE.getContainer() != null)
        {
            throw UserFailureException.fromTemplate(
                    "Cannot register sample '%s': parent '%s' is part of another sample.",
                    sampleIdentifier, parentIdentifierOrNull);
        }
        return parentPE;
    }

    final SampleTypePE getSampleType(final String code) throws UserFailureException
    {
        final SampleTypePE sampleType = getSampleTypeDAO().tryFindSampleTypeByCode(code);
        if (sampleType == null)
        {
            throw UserFailureException.fromTemplate(
                    "No sample type with code '%s' could be found in the database.", code);
        }
        return sampleType;
    }

    protected ExperimentPE findExperiment(ExperimentIdentifier identifier)
    {
        ProjectPE project = findProject(identifier);
        String experimentCode = identifier.getExperimentCode();
        ExperimentPE experiment =
                getExperimentDAO().tryFindByCodeAndProject(project, experimentCode);
        if (experiment == null)
        {
            throw UserFailureException.fromTemplate(
                    "No experiment '%s' could be found in the '%s/%s' project!", experimentCode,
                    identifier.getGroupCode(), identifier.getProjectCode());
        }
        return experiment;
    }

    protected ProjectPE findProject(ProjectIdentifier projectIdentifier)
    {
        String groupCode = projectIdentifier.getGroupCode();
        String projectCode = projectIdentifier.getProjectCode();
        String databaseInstanceCode = projectIdentifier.getDatabaseInstanceCode();
        ProjectPE project =
                getProjectDAO().tryFindProject(databaseInstanceCode, groupCode, projectCode);
        if (project == null)
        {
            throw UserFailureException.fromTemplate(
                    "No project '%s' could be found in the '%s' group!", projectCode, groupCode);
        }
        return project;
    }

    protected void checkBusinessRules(SamplePE sample, IExternalDataDAO externalDataDAO,
            Map<EntityTypePE, List<EntityTypePropertyTypePE>> cacheOrNull)
    {
        if (cacheOrNull != null)
        {
            entityPropertiesConverter.checkMandatoryProperties(sample.getProperties(), sample
                    .getSampleType(), cacheOrNull);
        } else
        {
            entityPropertiesConverter.checkMandatoryProperties(sample.getProperties(), sample
                    .getSampleType());
        }
        final boolean hasDatasets = hasDatasets(externalDataDAO, sample);
        if (hasDatasets && sample.getExperiment() == null)
        {
            throw UserFailureException.fromTemplate(
                    "Cannot detach the sample '%s' from the experiment "
                            + "because there are already datasets attached to the sample.", sample
                            .getIdentifier());
        }
        if (hasDatasets && sample.getGroup() == null)
        {
            throw UserFailureException.fromTemplate("Cannot detach the sample '%s' from the group "
                    + "because there are already datasets attached to the sample.", sample
                    .getIdentifier());
        }
        if (sample.getExperiment() != null
                && (sample.getGroup() == null || sample.getExperiment().getProject().getGroup()
                        .equals(sample.getGroup()) == false))
        {
            throw new UserFailureException(
                    "Sample group must be the same as experiment group. Shared samples cannot be attached to experiments.");
        }
        SampleGenericBusinessRules.assertValidParents(sample);
    }

    protected boolean hasDatasets(IExternalDataDAO externalDataDAO, SamplePE sample)
    {
        // If we just added new data sets in this BO, they won't have data sets, so no need to
        // check.
        return (onlyNewSamples == false) && SampleUtils.hasDatasets(externalDataDAO, sample);
    }
}
