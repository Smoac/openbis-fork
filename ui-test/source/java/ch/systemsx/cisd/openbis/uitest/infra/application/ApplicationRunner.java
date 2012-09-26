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
public interface ApplicationRunner
{

    public String uid();

    public void login(String userName, String password);

    public void logout();

    public Space create(Space space);

    public void delete(Space space);

    public Project create(Project project);

    public void delete(Project project);

    public SampleType create(SampleType sampleType);

    public void update(SampleType sampleType);

    public void delete(SampleType sampleType);

    public ExperimentType create(ExperimentType experimentType);

    public void delete(ExperimentType experimentType);

    public PropertyType create(PropertyType propertyType);

    public void delete(PropertyType propertyType);

    public Vocabulary create(Vocabulary vocabulary);

    public void delete(Vocabulary vocabulary);

    public Sample create(Sample sample);

    public Experiment create(Experiment experiment);

    public PropertyTypeAssignment create(PropertyTypeAssignment assignment);

    public DataSetType create(DataSetType type);

    public DataSet create(DataSet dataSet);
}
