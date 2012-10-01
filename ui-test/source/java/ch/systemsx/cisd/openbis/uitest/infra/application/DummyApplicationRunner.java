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

package ch.systemsx.cisd.openbis.uitest.infra.application;

import java.util.UUID;

import ch.systemsx.cisd.openbis.uitest.type.DataSet;
import ch.systemsx.cisd.openbis.uitest.type.DataSetType;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentType;
import ch.systemsx.cisd.openbis.uitest.type.Project;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeAssignment;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.type.Space;
import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;

/**
 * @author anttil
 */
public class DummyApplicationRunner implements ApplicationRunner
{

    @Override
    public String uid()
    {
        return UUID.randomUUID().toString();
    }

    @Override
    public void login(String userName, String password)
    {
    }

    @Override
    public void logout()
    {
    }

    @Override
    public Space create(Space space)
    {
        return null;
    }

    @Override
    public void delete(Space space)
    {
    }

    @Override
    public Project create(Project project)
    {
        return null;
    }

    @Override
    public void delete(Project project)
    {
    }

    @Override
    public SampleType create(SampleType sampleType)
    {
        return null;
    }

    @Override
    public void update(SampleType sampleType)
    {
    }

    @Override
    public void delete(SampleType sampleType)
    {
    }

    @Override
    public ExperimentType create(ExperimentType experimentType)
    {
        return null;
    }

    @Override
    public void delete(ExperimentType experimentType)
    {

    }

    @Override
    public PropertyType create(PropertyType propertyType)
    {
        return null;
    }

    @Override
    public void delete(PropertyType propertyType)
    {
    }

    @Override
    public Vocabulary create(Vocabulary vocabulary)
    {
        return null;
    }

    @Override
    public void delete(Vocabulary vocabulary)
    {
    }

    @Override
    public Sample create(Sample sample)
    {
        return null;
    }

    @Override
    public Experiment create(Experiment experiment)
    {
        return null;
    }

    @Override
    public PropertyTypeAssignment create(PropertyTypeAssignment assignment)
    {
        return null;
    }

    @Override
    public DataSetType create(DataSetType type)
    {
        return null;
    }

    @Override
    public DataSet create(DataSet dataSet)
    {
        return null;
    }
}