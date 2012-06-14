/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ch.systemsx.cisd.etlserver.registrator.api.v1.ISample;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISampleImmutable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class Sample extends SampleImmutable implements ISample
{

    /**
     * This code is derived from
     * {@link ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder}, which is in
     * a test source folder.
     */
    private static ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample buildSampleWithIdentifier(
            String identifier)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample =
                new ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample();
        sample.setProperties(new ArrayList<IEntityProperty>());

        sample.setIdentifier(identifier);
        SampleIdentifier sampleIdentifier = SampleIdentifierFactory.parse(identifier);
        sample.setCode(sampleIdentifier.getSampleCode());
        String sampleSubCode = sampleIdentifier.getSampleSubCode();
        sample.setSubCode(sampleSubCode);
        if (sampleIdentifier.isSpaceLevel())
        {
            Space space = new Space();
            SpaceIdentifier spaceLevel = sampleIdentifier.getSpaceLevel();
            space.setCode(spaceLevel.getSpaceCode());
            DatabaseInstance databaseInstance = new DatabaseInstance();
            databaseInstance.setCode(spaceLevel.getDatabaseInstanceCode());
            space.setInstance(databaseInstance);
            sample.setSpace(space);
        } else
        {
            DatabaseInstance databaseInstance = new DatabaseInstance();
            databaseInstance.setCode(sampleIdentifier.getDatabaseInstanceLevel()
                    .getDatabaseInstanceCode());
            sample.setDatabaseInstance(databaseInstance);
        }

        return sample;
    }

    public Sample(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample)
    {
        super(sample);
    }

    public Sample(SampleImmutable sample)
    {
        super(sample.getSample());
    }

    public Sample(String sampleIdentifier, String permId)
    {
        super(buildSampleWithIdentifier(sampleIdentifier), false);
        getSample().setPermId(permId);
    }

    @Override
    public void setExperiment(IExperimentImmutable experiment)
    {
        ExperimentImmutable exp = (ExperimentImmutable) experiment;
        getSample().setExperiment(exp.getExperiment());
    }

    @Override
    public void setPropertyValue(String propertyCode, String propertyValue)
    {
        EntityHelper.createOrUpdateProperty(getSample(), propertyCode, propertyValue);
    }

    @Override
    public void setSampleType(String type)
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode(type);

        getSample().setSampleType(sampleType);
    }

    @Override
    public void setContainer(ISampleImmutable container)
    {
        SampleImmutable containerImpl = (SampleImmutable) container;
        getSample().setContainer(containerImpl.getSample());
    }

    @Override
    public void setParentSampleIdentifiers(List<String> parentSampleIdentifiers)
    {
        HashSet<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample> parents =
                new HashSet<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample>();
        for (String identifier : parentSampleIdentifiers)
        {
            parents.add(buildSampleWithIdentifier(identifier));
        }

        getSample().setParents(parents);
    }

}
