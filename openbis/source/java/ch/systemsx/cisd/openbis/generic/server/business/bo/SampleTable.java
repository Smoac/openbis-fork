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
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.LocalExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

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

    private boolean businessRulesChecked;

    public SampleTable(final IDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
    }

    public final void loadSamplesByCriteria(final ListSamplesByPropertyCriteria criteria)
    {
        onlyNewSamples = false;
        SpacePE group = findGroup(criteria.getSpaceCode());
        List<SamplePE> foundSamples =
                getSampleDAO().listSamplesBySpaceAndProperty(criteria.getPropertyCode(),
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
                                experimentPropertyCode,
                                localExperimentIdentifier.getPropertyValue());
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
                            project.getSpace().getCode(), project.getCode(), propertyCode,
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

    private SpacePE findGroup(String groupCode)
    {
        SpacePE group =
                getSpaceDAO().tryFindSpaceByCodeAndDatabaseInstance(groupCode,
                        getHomeDatabaseInstance());
        if (group == null)
        {
            throw UserFailureException
                    .fromTemplate("No group with the name '%s' found!", groupCode);
        }
        return group;
    }

    public final List<SamplePE> getSamples()
    {
        return samples;
    }

    public void prepareForRegistration(List<NewSample> newSamples, PersonPE registratorOrNull)
            throws UserFailureException
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
            add(sample, sampleTypeCache, sampleOwnerCache, experimentCache, registratorOrNull);
        }

        setBatchUpdateMode(false);
    }

    private void add(final NewSample newSample, final Map<String, SampleTypePE> sampleTypeCache,
            final Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCache,
            Map<String, ExperimentPE> experimentCache, PersonPE registratorOrNull)
            throws UserFailureException
    {
        assert newSample != null : "Unspecified new sample.";
        samples.add(createSample(newSample, sampleTypeCache, sampleOwnerCache, experimentCache,
                registratorOrNull));
        dataChanged = true;
    }

    public void save() throws UserFailureException
    {
        assert samples != null : "Samples not loaded.";
        assert dataChanged : "Data have not been changed.";

        try
        {
            if (businessRulesChecked == false)
            {
                checkAllBusinessRules();
            }
            getSampleDAO().createOrUpdateSamples(samples);
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("One of samples"));
        }
        dataChanged = false;
        businessRulesChecked = false;
        onlyNewSamples = false;
    }

    private void checkAllBusinessRules()
    {
        final Map<EntityTypePE, List<EntityTypePropertyTypePE>> cache =
                new HashMap<EntityTypePE, List<EntityTypePropertyTypePE>>();
        for (SamplePE s : samples)
        {
            checkAllBusinessRules(s, getDataDAO(), cache);
        }
    }

    private void prepareBatchUpdate(SamplePE sample, SampleBatchUpdatesDTO updates,
            Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCache,
            Map<String, ExperimentPE> experimentCache,
            Map<EntityTypePE, List<EntityTypePropertyTypePE>> propertiesCache)
    {
        if (sample == null)
        {
            throw UserFailureException.fromTemplate(
                    "No sample could be found with given identifier '%s'.",
                    updates.getOldSampleIdentifierOrNull());
        }
        SampleBatchUpdateDetails details = updates.getDetails();
        batchUpdateProperties(sample, updates.getProperties(), details.getPropertiesToUpdate());
        checkPropertiesBusinessRules(sample, propertiesCache);

        // TODO 2010-10-09, Piotr Buczek: don't check business rules if value wasn't changed
        if (details.isExperimentUpdateRequested())
        {
            updateSpace(sample, updates.getSampleIdentifier(), sampleOwnerCache);
            updateExperiment(sample, updates.getExperimentIdentifierOrNull(), experimentCache);
            checkExperimentBusinessRules(getDataDAO(), sample);
        }
        if (details.isParentsUpdateRequested())
        {
            final String[] parents = updates.getModifiedParentCodesOrNull();
            if (parents != null)
            {
                setParents(sample, parents);
            }
        }
        if (details.isContainerUpdateRequested())
        {
            setContainer(updates.getSampleIdentifier(), sample,
                    updates.getContainerIdentifierOrNull());
        }
        // NOTE: Checking business rules with relationships is expensive.
        // Don't perform them unless relevant data were changed.
        if (details.isExperimentUpdateRequested() || details.isParentsUpdateRequested())
        {
            checkParentBusinessRules(sample);
        }
        if (details.isExperimentUpdateRequested() || details.isContainerUpdateRequested())
        {
            checkContainerBusinessRules(sample);
        }
    }

    private void batchUpdateProperties(SamplePE sample, List<IEntityProperty> properties,
            Set<String> propertiesToUpdate)
    {
        final Set<SamplePropertyPE> existingProperties = sample.getProperties();
        final SampleTypePE type = sample.getSampleType();
        final PersonPE registrator = findRegistrator();
        sample.setProperties(entityPropertiesConverter.updateProperties(existingProperties, type,
                properties, registrator, propertiesToUpdate));
    }

    public void prepareForUpdate(List<SampleBatchUpdatesDTO> updates)
    {
        assert updates != null : "Unspecified samples.";

        setBatchUpdateMode(true);
        final Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCache =
                new HashMap<SampleOwnerIdentifier, SampleOwner>();
        final Map<String, ExperimentPE> experimentCache = new HashMap<String, ExperimentPE>();
        final Map<EntityTypePE, List<EntityTypePropertyTypePE>> propertiesCache =
                new HashMap<EntityTypePE, List<EntityTypePropertyTypePE>>();
        samples = loadSamples(updates, sampleOwnerCache);
        Map<SampleIdentifier, SamplePE> samplesByIdentifiers =
                new HashMap<SampleIdentifier, SamplePE>();
        for (SamplePE sample : samples)
        {
            samplesByIdentifiers.put(sample.getSampleIdentifier(), sample);
        }
        for (SampleBatchUpdatesDTO sampleUpdates : updates)
        {
            final SamplePE sample =
                    samplesByIdentifiers.get(sampleUpdates.getOldSampleIdentifierOrNull());
            prepareBatchUpdate(sample, sampleUpdates, sampleOwnerCache, experimentCache,
                    propertiesCache);
        }

        dataChanged = true;
        businessRulesChecked = true;

        setBatchUpdateMode(false);
    }

    private List<SamplePE> loadSamples(List<SampleBatchUpdatesDTO> updates,
            Map<SampleOwnerIdentifier, SampleOwner> sampleOwnerCache)
    {
        final List<SamplePE> results = new ArrayList<SamplePE>();
        List<SampleIdentifier> identifiers = new ArrayList<SampleIdentifier>();
        for (SampleBatchUpdatesDTO sampleUpdates : updates)
        {
            SampleIdentifier identifier = sampleUpdates.getOldSampleIdentifierOrNull();
            identifiers.add(identifier);
        }
        results.addAll(listSamplesByIdentifiers(identifiers, sampleOwnerCache));
        return results;
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
            throwException(ex, "Sample", EntityKind.SAMPLE);
        }
    }

    public int trashByTechIds(List<TechId> sampleIds, DeletionPE deletion)
            throws UserFailureException
    {
        try
        {
            return getSampleDAO().trash(sampleIds, deletion);
        } catch (final DataAccessException ex)
        {
            throwException(ex, "Sample", EntityKind.SAMPLE);
        }
        return -1; // not possible
    }

}
