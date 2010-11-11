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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwner;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationshipTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
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
        SampleOwner sampleOwner = getSampleOwner(sampleOwnerCacheOrNull, sampleIdentifier);
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
        String containerIdentifier = newSample.getContainerIdentifier();
        setContainer(sampleIdentifier, samplePE, containerIdentifier);
        if (newSample.getParentsOrNull() != null)
        {
            final String[] parents = newSample.getParentsOrNull();
            setParents(samplePE, parents);
        }
        samplePE.setPermId(getPermIdDAO().createPermId());
        return samplePE;
    }

    protected SampleOwner getSampleOwner(
            Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCacheOrNull,
            final SampleIdentifier sampleIdentifier)
    {
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
        return sampleOwner;
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
        final SamplePE containerPE =
                tryGetValidNotContainedSample(containerIdentifier, sampleIdentifier);
        samplePE.setContainer(containerPE);
    }

    protected void setParents(final SamplePE childPE, final String[] parents)
    {
        final List<SampleIdentifier> parentIdentifiers =
                IdentifierHelper.extractSampleIdentifiers(parents);
        final SampleIdentifier childIdentifier = childPE.getSampleIdentifier();
        if (childIdentifier.isSpaceLevel())
        {
            final String spaceCode = childIdentifier.getSpaceLevel().getSpaceCode();
            for (SampleIdentifier si : parentIdentifiers)
            {
                IdentifierHelper.fillGroupIfNotSpecified(si, spaceCode);
            }
        }
        final Set<SamplePE> parentPEs = new HashSet<SamplePE>();
        for (SampleIdentifier si : parentIdentifiers)
        {
            // TODO 2010-11-10, Piotr Buczek: use cache
            SamplePE parent = getSampleByIdentifier(si);
            parentPEs.add(parent);
        }
        replaceParents(childPE, parentPEs);
    }

    private void replaceParents(SamplePE child, Set<SamplePE> newParents)
    {
        for (SamplePE parent : newParents)
        {
            checkParentInvalidation(parent, child.getSampleIdentifier());
        }
        List<SampleRelationshipPE> oldParents = new ArrayList<SampleRelationshipPE>();
        for (SampleRelationshipPE r : child.getParentRelationships())
        {
            if (r.getRelationship().getCode()
                    .equals(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP))
            {
                oldParents.add(r);
            }
        }
        for (SampleRelationshipPE r : oldParents)
        {
            if (newParents.contains(r.getParentSample()))
            {
                newParents.remove(r.getParentSample());
            } else
            {
                child.removeParentRelationship(r);
            }
        }
        RelationshipTypePE relationship = tryFindParentChildRelationshipType();
        for (SamplePE newParent : newParents)
        {
            child.addParentRelationship(new SampleRelationshipPE(newParent, child, relationship));
        }
    }

    protected RelationshipTypePE tryFindParentChildRelationshipType()
    {
        return tryFindRelationshipTypeByCode(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
    }

    protected RelationshipTypePE tryFindRelationshipTypeByCode(String code)
    {
        RelationshipTypePE result = getRelationshipTypeDAO().tryFindRelationshipTypeByCode(code);
        if (result == null)
        {
            throw UserFailureException.fromTemplate(
                    "'%s' relationship definition could not be found.", code);
        }
        return result;
    }

    private SamplePE tryGetValidParentSample(final String parentIdentifierOrNull,
            final SampleIdentifier childIdentifier)
    {
        if (parentIdentifierOrNull == null)
        {
            return null;
        }
        final SamplePE parentPE =
                getSampleByIdentifier(SampleIdentifierFactory.parse(parentIdentifierOrNull));
        checkParentInvalidation(parentPE, childIdentifier);
        return parentPE;
    }

    private void checkParentInvalidation(final SamplePE parentPE, final SampleIdentifier child)
    {
        if (parentPE.getInvalidation() != null)
        {
            throw UserFailureException.fromTemplate(
                    "Sample '%s' has been invalidated and can't become a parent of sample '%s'.",
                    parentPE.getIdentifier(), child);
        }
    }

    private SamplePE tryGetValidNotContainedSample(final String parentIdentifierOrNull,
            final SampleIdentifier sampleIdentifier)
    {
        SamplePE sample = tryGetValidParentSample(parentIdentifierOrNull, sampleIdentifier);
        if (sample != null && sample.getContainer() != null)
        {
            throw UserFailureException.fromTemplate(
                    "Cannot register sample '%s': parent '%s' is part of another sample.",
                    sampleIdentifier, parentIdentifierOrNull);
        }
        return sample;
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
                    identifier.getSpaceCode(), identifier.getProjectCode());
        }
        return experiment;
    }

    protected ProjectPE findProject(ProjectIdentifier projectIdentifier)
    {
        String spaceCode = projectIdentifier.getSpaceCode();
        String projectCode = projectIdentifier.getProjectCode();
        String databaseInstanceCode = projectIdentifier.getDatabaseInstanceCode();
        ProjectPE project =
                getProjectDAO().tryFindProject(databaseInstanceCode, spaceCode, projectCode);
        if (project == null)
        {
            throw UserFailureException.fromTemplate(
                    "No project '%s' could be found in the '%s' space!", projectCode, spaceCode);
        }
        return project;
    }

    protected void checkAllBusinessRules(SamplePE sample, IExternalDataDAO externalDataDAO,
            Map<EntityTypePE, List<EntityTypePropertyTypePE>> cacheOrNull)
    {
        checkPropertiesBusinessRules(sample, cacheOrNull);
        checkExperimentBusinessRules(externalDataDAO, sample);
        checkParentBusinessRules(sample);
        checkContainerBusinessRules(sample);
    }

    protected void checkPropertiesBusinessRules(SamplePE sample,
            Map<EntityTypePE, List<EntityTypePropertyTypePE>> cacheOrNull)
    {
        if (cacheOrNull != null)
        {
            entityPropertiesConverter.checkMandatoryProperties(sample.getProperties(),
                    sample.getSampleType(), cacheOrNull);
        } else
        {
            entityPropertiesConverter.checkMandatoryProperties(sample.getProperties(),
                    sample.getSampleType());
        }
    }

    protected void checkExperimentBusinessRules(IExternalDataDAO externalDataDAO, SamplePE sample)
    {
        final boolean hasDatasets = hasDatasets(externalDataDAO, sample);
        if (hasDatasets && sample.getExperiment() == null)
        {
            throw UserFailureException.fromTemplate(
                    "Cannot detach the sample '%s' from the experiment "
                            + "because there are already datasets attached to the sample.",
                    sample.getIdentifier());
        }
        if (hasDatasets && sample.getGroup() == null)
        {
            throw UserFailureException.fromTemplate("Cannot detach the sample '%s' from the space "
                    + "because there are already datasets attached to the sample.",
                    sample.getIdentifier());
        }
        if (sample.getExperiment() != null
                && (sample.getGroup() == null || sample.getExperiment().getProject().getGroup()
                        .equals(sample.getGroup()) == false))
        {
            throw new UserFailureException(
                    "Sample space must be the same as experiment space. Shared samples cannot be attached to experiments.");
        }
    }

    protected void checkParentBusinessRules(SamplePE sample)
    {
        SampleGenericBusinessRules.assertValidParents(sample);
        SampleGenericBusinessRules.assertValidChildren(sample);
    }

    protected void checkContainerBusinessRules(SamplePE sample)
    {
        SampleGenericBusinessRules.assertValidContainer(sample);
        SampleGenericBusinessRules.assertValidComponents(sample);
    }

    protected boolean hasDatasets(IExternalDataDAO externalDataDAO, SamplePE sample)
    {
        // If we just added new data sets in this BO, they won't have data sets, so no need to
        // check.
        return (onlyNewSamples == false) && SampleUtils.hasDatasets(externalDataDAO, sample);
    }

    protected void updateGroup(SamplePE sample, SampleIdentifier sampleOwnerIdentifier,
            Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCacheOrNull)
    {
        if (sampleOwnerIdentifier != null)
        {
            final SampleOwner sampleOwner =
                    getSampleOwner(sampleOwnerCacheOrNull, sampleOwnerIdentifier);
            GroupPE group = sampleOwner.tryGetGroup();
            sample.setDatabaseInstance(sampleOwner.tryGetDatabaseInstance());
            sample.setGroup(group);
        }
    }

    protected void updateExperiment(SamplePE sample, ExperimentIdentifier expIdentifierOrNull,
            Map<String, ExperimentPE> experimentCacheOrNull)
    {
        if (expIdentifierOrNull != null)
        {
            changeExperiment(sample, expIdentifierOrNull, experimentCacheOrNull);
        } else
        {
            removeFromExperiment(sample);
        }
    }

    private void removeFromExperiment(SamplePE sample)
    {
        if (hasDatasets(getExternalDataDAO(), sample))
        {
            throw UserFailureException.fromTemplate(
                    "Cannot detach the sample '%s' from the experiment "
                            + "because there are already datasets attached to the sample.",
                    sample.getIdentifier());
        }
        sample.setExperiment(null);
    }

    private void changeExperiment(SamplePE sample, ExperimentIdentifier identifier,
            Map<String, ExperimentPE> experimentCacheOrNull)
    {
        ExperimentPE newExperiment =
                tryFindExperiment(experimentCacheOrNull, identifier.toString());
        if (isExperimentUnchanged(newExperiment, sample.getExperiment()))
        {
            return;
        }
        ensureExperimentIsValid(identifier, newExperiment, sample);
        ensureSampleAttachableToExperiment(sample);

        changeDatasetsExperiment(sample.getDatasets(), newExperiment);
        sample.setExperiment(newExperiment);
    }

    private void changeDatasetsExperiment(Set<DataPE> datasets, ExperimentPE experiment)
    {
        for (DataPE dataset : datasets)
        {
            dataset.setExperiment(experiment);
        }
    }

    private void ensureSampleAttachableToExperiment(SamplePE sample)
    {
        if (sample.getGroup() == null)
        {
            throw UserFailureException.fromTemplate(
                    "It is not allowed to connect a shared sample '%s' to the experiment.",
                    sample.getIdentifier());
        }
    }

    private void ensureExperimentIsValid(ExperimentIdentifier identOrNull,
            ExperimentPE experimentOrNull, SamplePE sample)
    {
        if (experimentOrNull != null && experimentOrNull.getInvalidation() != null)
        {
            throw UserFailureException.fromTemplate(
                    "The sample '%s' cannot be assigned to the experiment '%s' "
                            + "because the experiment has been invalidated.",
                    sample.getSampleIdentifier(), identOrNull);
        }
    }

    private boolean isExperimentUnchanged(ExperimentPE newExperimentOrNull,
            ExperimentPE experimentOrNull)
    {
        return experimentOrNull == null ? newExperimentOrNull == null : experimentOrNull
                .equals(newExperimentOrNull);
    }

    /**
     * Throws {@link UserFailureException} if adding specified parents to this data set will create
     * a cycle in data set relationships.
     */
    protected void validateRelationshipGraph(Collection<SamplePE> parents, TechId relationship,
            SamplePE sample)
    {
        // DFS from new parents that are to be added to this business object going in direction
        // of parent relationship until:
        // - all related ancestors are visited == graph has no cycles
        // - we get to this business object == cycle is found
        // NOTE: The assumption is that there were no cycles in the graph of relationship before.
        // This algorithm will not find cycles that don't include this business object,
        // although such cycles shouldn't cause it to loop forever.

        // Algorithm operates only on data set ids to make it perform better
        // - there is no need to join DB tables.
        // To be able to inform user about the exact data set that cannot be connected as a parent
        // we need start seeking cycles starting from each parent to be added separately. Otherwise
        // we would need to get invoke more queries to DB (not going layer by layer of graph depth
        // per query) or use BFS instead (which would also be slower in a general case).

        for (SamplePE parentToAdd : parents)
        {
            validateRelationshipGraph(parentToAdd, relationship, sample);
        }
    }

    private void validateRelationshipGraph(SamplePE parentToAdd, TechId relationship,
            SamplePE sample)
    {
        final TechId sampleId = TechId.create(sample);
        final Set<TechId> visited = new HashSet<TechId>();
        Set<TechId> toVisit = new HashSet<TechId>();
        toVisit.add(TechId.create(parentToAdd));
        while (toVisit.isEmpty() == false)
        {
            if (toVisit.contains(sampleId))
            {
                throw UserFailureException.fromTemplate(
                        "Sample '%s' is an ancestor of Sample '%s' "
                                + "and cannot be at the same time set as its child.",
                        sample.getIdentifier(), parentToAdd.getIdentifier());
            } else
            {
                final Set<TechId> nextToVisit = getSampleDAO().listParents(toVisit, relationship);
                visited.addAll(toVisit);
                nextToVisit.removeAll(visited);
                toVisit = nextToVisit;
            }
        }
    }

    protected Set<String> extractDynamicProperties(final SampleTypePE type)
    {
        Set<String> dynamicProperties = new HashSet<String>();
        for (SampleTypePropertyTypePE etpt : type.getSampleTypePropertyTypes())
        {
            if (etpt.isDynamic())
            {
                dynamicProperties.add(etpt.getPropertyType().getCode());
            }
        }
        return dynamicProperties;
    }

    protected List<SamplePE> listSamplesByIdentifiers(
            final List<SampleIdentifier> sampleIdentifiers,
            Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCache)
    {
        assert sampleIdentifiers != null : "Sample identifiers unspecified.";

        Map<SampleOwnerWithContainer, List<String>> samplesByOwner =
                new HashMap<SampleOwnerWithContainer, List<String>>();
        for (SampleIdentifier sampleIdentifier : sampleIdentifiers)
        {
            final SampleOwner sampleOwner = getSampleOwner(sampleOwnerCache, sampleIdentifier);
            final String containerCodeOrNull = sampleIdentifier.getContainerCodeOrNull();
            final SampleOwnerWithContainer owner =
                    new SampleOwnerWithContainer(sampleOwner, containerCodeOrNull);

            List<String> ownerSamples = samplesByOwner.get(owner);
            if (ownerSamples == null)
            {
                ownerSamples = new ArrayList<String>();
                samplesByOwner.put(owner, ownerSamples);
            }
            ownerSamples.add(sampleIdentifier.getSampleSubCode());
        }

        final ISampleDAO sampleDAO = getSampleDAO();
        final List<SamplePE> results = new ArrayList<SamplePE>();
        for (Entry<SampleOwnerWithContainer, List<String>> entry : samplesByOwner.entrySet())
        {
            final SampleOwnerWithContainer owner = entry.getKey();
            final List<String> sampleCodes = entry.getValue();

            final SampleOwner sampleOwner = owner.getSampleOwner();
            final String containerCodeOrNull = owner.getContainerCodeOrNull();

            List<SamplePE> samples = null;
            if (sampleOwner.isDatabaseInstanceLevel())
            {
                samples =
                        sampleDAO.listByCodesAndDatabaseInstance(sampleCodes, containerCodeOrNull,
                                sampleOwner.tryGetDatabaseInstance());
            } else
            {
                assert sampleOwner.isGroupLevel() : "Must be of space level.";
                samples =
                        sampleDAO.listByCodesAndGroup(sampleCodes, containerCodeOrNull,
                                sampleOwner.tryGetGroup());
            }
            results.addAll(samples);
        }
        return results;
    }

    /** Helper class encapsulating {@link SampleOwner} and code of container of a sample. */
    private static class SampleOwnerWithContainer
    {
        private final SampleOwner sampleOwner;

        private final String containerCodeOrNull;

        public SampleOwnerWithContainer(SampleOwner sampleOwner, String containerCodeOrNull)
        {
            assert sampleOwner != null;
            this.sampleOwner = sampleOwner;
            this.containerCodeOrNull = containerCodeOrNull;
        }

        public SampleOwner getSampleOwner()
        {
            return sampleOwner;
        }

        public String getContainerCodeOrNull()
        {
            return containerCodeOrNull;
        }

        //
        // Object
        //

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result =
                    prime * result
                            + ((containerCodeOrNull == null) ? 0 : containerCodeOrNull.hashCode());
            result = prime * result + sampleOwner.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (!(obj instanceof SampleOwnerWithContainer))
            {
                return false;
            }
            SampleOwnerWithContainer other = (SampleOwnerWithContainer) obj;
            if (containerCodeOrNull == null)
            {
                if (other.containerCodeOrNull != null)
                {
                    return false;
                }
            } else if (!containerCodeOrNull.equals(other.containerCodeOrNull))
            {
                return false;
            }
            if (!sampleOwner.equals(other.sampleOwner))
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return "SampleOwnerWithContainer [sampleOwner=" + sampleOwner
                    + ", containerCodeOrNull=" + containerCodeOrNull + "]";
        }

    }
}
