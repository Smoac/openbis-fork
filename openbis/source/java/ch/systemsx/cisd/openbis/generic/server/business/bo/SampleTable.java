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
import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.SampleOwner;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSampleCriteriaDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
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
            return sampleDAO.listSamplesByTypeAndGroup(sampleType, owner.tryGetGroup());
        } else
        {
            return sampleDAO.listSamplesByTypeAndDatabaseInstance(sampleType, owner
                    .tryGetDatabaseInstance());
        }
    }

    //
    // ISampleTable
    //

    public final void loadSamplesByCriteria(final ListSampleCriteriaDTO criteria)
    {
        final SampleIdentifier containerIdentifier = criteria.getContainerIdentifier();
        final ExperimentIdentifier experimentIdentifier = criteria.getExperimentIdentifier();
        if (experimentIdentifier != null)
        {
            ProjectPE project =
                    getProjectDAO().tryFindProject(experimentIdentifier.getDatabaseInstanceCode(),
                            experimentIdentifier.getGroupCode(),
                            experimentIdentifier.getProjectCode());
            ExperimentPE experiment =
                    getExperimentDAO().tryFindByCodeAndProject(project,
                            experimentIdentifier.getExperimentCode());
            samples = new ArrayList<SamplePE>(experiment.getSamples());
            enrichWithHierarchy();
        } else if (containerIdentifier != null)
        {
            final SamplePE container = getSampleByIdentifier(containerIdentifier);
            samples = getSampleDAO().listSamplesByContainer(container);
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
            for (final SampleOwnerIdentifier sampleOwnerIdentifier : criteria.getOwnerIdentifiers())
            {
                final SampleOwner owner =
                        getSampleOwnerFinder().figureSampleOwner(sampleOwnerIdentifier);
                samples.addAll(listSamples(sampleType, owner));
            }
        }
    }

    public final void enrichWithExperimentAndProperties()
    {
        for (final SamplePE sample : samples)
        {
            HibernateUtils.initialize(sample.getExperiment());
            HibernateUtils.initialize(sample.getProperties());
        }
    }

    public final List<SamplePE> getSamples()
    {
        return samples;
    }

    public final void add(final NewSample newSample) throws UserFailureException
    {
        assert newSample != null : "Unspecified new sample.";
        if (samples == null)
        {
            samples = new ArrayList<SamplePE>();
        }
        samples.add(createSample(newSample));
        dataChanged = true;
    }

    public void save() throws UserFailureException
    {
        assert samples != null : "Samples not loaded.";
        assert dataChanged : "Data have not been changed.";

        try
        {
            getSampleDAO().createSamples(samples);
            checkBusinessRules();
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("One of samples"));
        }
        dataChanged = false;
    }

    private void checkBusinessRules()
    {
        for (SamplePE s : samples)
        {
            entityPropertiesConverter.checkMandatoryProperties(s.getProperties(), s
                    .getSampleType());
        }
    }

    private void enrichWithHierarchy()
    {
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
}
