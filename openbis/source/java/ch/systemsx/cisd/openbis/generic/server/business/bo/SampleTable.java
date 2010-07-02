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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwner;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSampleCriteriaDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.LocalExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * The unique {@link ISampleBO} implementation.
 * 
 * @author Tomasz Pylak
 */
// TODO 2008-12-10, Christian Ribeaud: Unit test for this class?
public final class SampleTable extends AbstractSampleBusinessObject implements ISampleTable
{
    private List<SamplePE> samples;

    private boolean dataChanged;

    public SampleTable(final IDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
    }

    private final List<SamplePE> listSamples(final SampleTypePE sampleType, final SampleOwner owner)
    {
        final ISampleDAO sampleDAO = getSampleDAO();
        if (owner.isGroupLevel())
        {
            return sampleDAO.listSamplesWithPropertiesByTypeAndGroup(sampleType, owner
                    .tryGetGroup());
        } else
        {
            return sampleDAO.listSamplesWithPropertiesByTypeAndDatabaseInstance(sampleType, owner
                    .tryGetDatabaseInstance());
        }
    }

    private final List<SamplePE> listSamples(final SampleOwner owner)
    {
        final ISampleDAO sampleDAO = getSampleDAO();
        List<SamplePE> allSamples;
        if (owner.isGroupLevel())
        {
            allSamples = sampleDAO.listSamplesWithPropertiesByGroup(owner.tryGetGroup());
        } else
        {
            allSamples =
                    sampleDAO.listSamplesWithPropertiesByDatabaseInstance(owner
                            .tryGetDatabaseInstance());
        }
        return filterUnlistable(allSamples);
    }

    private List<SamplePE> filterUnlistable(List<SamplePE> allSamples)
    {
        List<SamplePE> result = new ArrayList<SamplePE>();
        for (SamplePE sample : allSamples)
        {
            if (sample.getSampleType().isListable())
            {
                result.add(sample);
            }
        }
        return result;
    }

    //
    // ISampleTable
    //

    public final void loadSamplesByCriteria(final ListSamplesByPropertyCriteria criteria)
    {
        onlyNewSamples = false;
        GroupPE group = findGroup(criteria.getSpaceCode());
        List<SamplePE> foundSamples =
                getSampleDAO().listSamplesByGroupAndProperty(criteria.getPropertyCode(),
                        criteria.getPropertyValue(), group);
        LocalExperimentIdentifier localExperimentIdentifier =
                criteria.tryGetLocalExperimentIdentifier();
        if (localExperimentIdentifier != null)
        {
            String experimentPropertyCode = localExperimentIdentifier.tryGetPropertyCode();
            if (experimentPropertyCode != null)
            {
                foundSamples =
                        filterSamplesByExperiment(foundSamples, criteria.getProjectIdentifier(),
                                experimentPropertyCode, localExperimentIdentifier
                                        .getPropertyValue());
            } else
            {
                ExperimentIdentifier ident =
                        new ExperimentIdentifier(criteria.getProjectIdentifier(),
                                localExperimentIdentifier.getExperimentCode());
                foundSamples = filterSamplesByExperiment(foundSamples, ident);
            }
        }
        samples = foundSamples;
    }

    private List<SamplePE> filterSamplesByExperiment(List<SamplePE> foundSamples,
            ProjectIdentifier projectIdentifier, String experimentPropertyCode, String propertyValue)
    {
        ProjectPE project = findProject(projectIdentifier);
        ExperimentPE expectedExperiment =
                findExperiment(project, experimentPropertyCode, propertyValue);
        return filterSamplesByExperiment(foundSamples, expectedExperiment);
    }

    private ExperimentPE findExperiment(ProjectPE project, String propertyCode, String propertyValue)
    {
        List<ExperimentPE> experiments =
                getExperimentDAO().listExperimentsByProjectAndProperty(propertyCode, propertyValue,
                        project);
        if (experiments.size() != 1)
        {
            throw UserFailureException
                    .fromTemplate(
                            "It was expected that there is exactly one experiment "
                                    + "in the '%s/%s' project with property '%s' set to '%s', but %d were found!",
                            project.getGroup().getCode(), project.getCode(), propertyCode,
                            propertyValue, experiments.size());
        }
        return experiments.get(0);
    }

    private List<SamplePE> filterSamplesByExperiment(List<SamplePE> foundSamples,
            ExperimentIdentifier experimentIdentifier)
    {
        ExperimentPE expectedExperiment = findExperiment(experimentIdentifier);
        return filterSamplesByExperiment(foundSamples, expectedExperiment);
    }

    private static List<SamplePE> filterSamplesByExperiment(List<SamplePE> foundSamples,
            ExperimentPE expectedExperiment)
    {
        List<SamplePE> filteredSamples = new ArrayList<SamplePE>();
        for (SamplePE sample : foundSamples)
        {
            if (expectedExperiment.equals(sample.getExperiment()))
            {
                filteredSamples.add(sample);
            }
        }
        return filteredSamples;
    }

    private GroupPE findGroup(String groupCode)
    {
        GroupPE group =
                getGroupDAO().tryFindGroupByCodeAndDatabaseInstance(groupCode,
                        getHomeDatabaseInstance());
        if (group == null)
        {
            throw UserFailureException
                    .fromTemplate("No group with the name '%s' found!", groupCode);
        }
        return group;
    }

    public final void loadSamplesByCriteria(final ListSampleCriteriaDTO criteria)
    {
        onlyNewSamples = false;
        final TechId containerSampleId = criteria.getContainerSampleId();
        final TechId experimentId = criteria.getExperimentId();
        if (experimentId != null)
        {
            ExperimentPE experiment = getExperimentDAO().getByTechId(experimentId);
            samples = getSampleDAO().listSamplesWithPropertiesByExperiment(experiment);
        } else if (containerSampleId != null)
        {
            final SamplePE container = getSampleByTechId(containerSampleId);
            samples = getSampleDAO().listSamplesWithPropertiesByContainer(container);
        } else
        {
            final SampleTypePE criteriaTypePE = criteria.getSampleType();
            assert criteriaTypePE != null : "criteria not set";
            if (EntityType.isAllTypesCode(criteriaTypePE.getCode()))
            {
                samples = new ArrayList<SamplePE>();
                for (final SampleOwnerIdentifier sampleOwnerIdentifier : criteria
                        .getOwnerIdentifiers())
                {
                    final SampleOwner owner =
                            getSampleOwnerFinder().figureSampleOwner(sampleOwnerIdentifier);
                    samples.addAll(listSamples(owner));
                }
            } else
            {
                final SampleTypePE sampleType =
                        getSampleTypeDAO().tryFindSampleTypeByExample(criteria.getSampleType());
                if (sampleType == null)
                {
                    throw new UserFailureException("Cannot find a sample type matching to "
                            + criteria.getSampleType());
                }
                samples = new ArrayList<SamplePE>();
                for (final SampleOwnerIdentifier sampleOwnerIdentifier : criteria
                        .getOwnerIdentifiers())
                {
                    final SampleOwner owner =
                            getSampleOwnerFinder().figureSampleOwner(sampleOwnerIdentifier);
                    samples.addAll(listSamples(sampleType, owner));
                }
            }
        }
    }

    public final List<SamplePE> getSamples()
    {
        return samples;
    }

    public void set(List<NewSample> newSamples) throws UserFailureException
    {
        onlyNewSamples = true;
        samples = new ArrayList<SamplePE>();

        setBatchUpdateMode(true);

        final Map<String, SampleTypePE> sampleTypeCache = new HashMap<String, SampleTypePE>();
        final Map<String, ExperimentPE> experimentCache = new HashMap<String, ExperimentPE>();
        final Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCache =
                new HashMap<SampleOwnerIdentifier, SampleOwner>();
        for (NewSample sample : newSamples)
        {
            add(sample, sampleTypeCache, sampleOwnerCache, experimentCache);
        }

        setBatchUpdateMode(false);
    }

    private void add(final NewSample newSample, final Map<String, SampleTypePE> sampleTypeCache,
            final Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCache,
            Map<String, ExperimentPE> experimentCache) throws UserFailureException
    {
        assert newSample != null : "Unspecified new sample.";
        samples.add(createSample(newSample, sampleTypeCache, sampleOwnerCache, experimentCache));
        dataChanged = true;
    }

    public void save() throws UserFailureException
    {
        assert samples != null : "Samples not loaded.";
        assert dataChanged : "Data have not been changed.";

        try
        {
            checkBusinessRules();
            getSampleDAO().createSamples(samples);
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("One of samples"));
        }
        dataChanged = false;
        onlyNewSamples = false;
    }

    private void checkBusinessRules()
    {
        final Map<EntityTypePE, List<EntityTypePropertyTypePE>> cache =
                new HashMap<EntityTypePE, List<EntityTypePropertyTypePE>>();
        for (SamplePE s : samples)
        {
            checkBusinessRules(s, getExternalDataDAO(), cache);
        }
    }

    // currently we don't display hierarchy in sample browser where all sample types are displayed
    // so we don't have to load hierarchy this way
    @SuppressWarnings("unused")
    private void enrichWithHierarchy()
    {
        // this part rather cannot be optimized with one SQL query (LMS-884)
        assert samples != null : "Samples not loaded.";
        for (SamplePE s : samples)
        {
            enrichParents(s);
        }
    }

    private final static void enrichParents(final SamplePE sample)
    {
        SamplePE container = sample;
        int containerHierarchyDepth = sample.getSampleType().getContainerHierarchyDepth();
        while (containerHierarchyDepth > 0 && container != null)
        {
            container = container.getContainer();
            HibernateUtils.initialize(container);
            containerHierarchyDepth--;
        }
        SamplePE generator = sample;
        int generatorHierarchyDepth = sample.getSampleType().getGeneratedFromHierarchyDepth();
        while (generatorHierarchyDepth > 0 && generator != null)
        {
            generator = generator.getGeneratedFrom();
            HibernateUtils.initialize(generator);
            generatorHierarchyDepth--;
        }
    }

    public SamplePE prepareBatchUpdate(SampleBatchUpdatesDTO updates,
            Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCache,
            Map<String, ExperimentPE> experimentCache)
    {
        // batch update doesn't use tech id, check version and update attributes
        SampleIdentifier identifier = updates.getOldSampleIdentifierOrNull();
        SamplePE sample = tryToGetSampleByIdentifier(identifier);
        if (sample == null)
        {
            throw UserFailureException.fromTemplate(
                    "No sample could be found with given identifier '%s'.", identifier);
        }
        SampleBatchUpdateDetails details = updates.getDetails();

        batchUpdateProperties(sample, updates.getProperties(), details.getPropertiesToUpdate());

        if (details.isExperimentUpdateRequested())
        {
            updateGroup(sample, updates.getSampleIdentifier(), sampleOwnerCache);
            updateExperiment(sample, updates.getExperimentIdentifierOrNull(), experimentCache);
        }
        if (details.isParentUpdateRequested())
        {
            setGeneratedFrom(updates.getSampleIdentifier(), sample, updates
                    .getParentIdentifierOrNull());
        }
        if (details.isContainerUpdateRequested())
        {
            setContainer(updates.getSampleIdentifier(), sample, updates
                    .getContainerIdentifierOrNull());
        }
        return sample;
    }

    private void batchUpdateProperties(SamplePE sample, List<IEntityProperty> properties,
            Set<String> propertiesToUpdate)
    {
        final Set<SamplePropertyPE> existingProperties = sample.getProperties();
        final EntityTypePE type = sample.getSampleType();
        final PersonPE registrator = findRegistrator();
        sample.setProperties(entityPropertiesConverter.updateProperties(existingProperties, type,
                properties, registrator, propertiesToUpdate));
    }

    public void update(List<SampleBatchUpdatesDTO> updates)
    {
        assert updates != null : "Unspecified samples.";
        setBatchUpdateMode(true);

        if (samples == null)
        {
            samples = new ArrayList<SamplePE>();
        }
        final Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCache =
                new HashMap<SampleOwnerIdentifier, SampleOwner>();
        final Map<String, ExperimentPE> experimentCache = new HashMap<String, ExperimentPE>();
        for (SampleBatchUpdatesDTO sample : updates)
        {
            samples.add(prepareBatchUpdate(sample, sampleOwnerCache, experimentCache));
        }
        dataChanged = true;
        setBatchUpdateMode(false);
    }

    public void deleteByTechIds(List<TechId> sampleIds, String reason) throws UserFailureException
    {
        try
        {
            getSessionFactory().getCurrentSession().flush();
            getSessionFactory().getCurrentSession().clear();
            getSampleDAO().delete(sampleIds, session.tryGetPerson(), reason);
        } catch (final DataAccessException ex)
        {
            throwException(ex, "Samples", EntityKind.SAMPLE);
        }
    }

}
