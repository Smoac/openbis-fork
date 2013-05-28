/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.common;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.DataSetInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment.ExperimentInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample.SampleInitializer;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TestUtils
{

    public static DataSet dataSet(String code)
    {
        DataSetInitializer initializer = new DataSetInitializer();
        initializer.setDataSetTypeCode("DT");
        initializer.setCode(code);
        initializer.setRegistrationDetails(registrationDetails());
        return new DataSet(initializer);
    }

    public static Experiment experiment(Project project, String experimentCode)
    {
        return experiment(project.getIdentifier(), experimentCode);
    }

    public static Experiment experiment(String projectIdentifier, String experimentCode)
    {
        ExperimentInitializer initializer = new ExperimentInitializer();
        initializer.setCode(experimentCode);
        initializer.setExperimentTypeCode("ET");
        initializer.setIdentifier(projectIdentifier + "/" + experimentCode);
        initializer.setPermId("PERM-" + initializer.getIdentifier());
        initializer.setId((long) initializer.getIdentifier().hashCode());
        initializer.setRegistrationDetails(registrationDetails());
        return new Experiment(initializer);
    }

    public static SampleInitializer createSampleInitializer(String sampleTypeCode, String spaceCode,
            String sampleCode, String experimentIdentifierOrNull)
    {
        SampleInitializer initializer = new SampleInitializer();
        initializer.setSampleTypeCode(sampleTypeCode);
        initializer.setSampleTypeId(new Long(sampleTypeCode.hashCode()));
        initializer.setCode(sampleCode);
        initializer.setIdentifier("/" + spaceCode + "/" + sampleCode);
        initializer.setPermId("PERM-" + initializer.getIdentifier());
        initializer.setId((long) initializer.getIdentifier().hashCode());
        initializer.setExperimentIdentifierOrNull(experimentIdentifierOrNull);
        initializer.setRegistrationDetails(registrationDetails());
        return initializer;
    }

    public static Sample sample(String sampleTypeCode, String spaceCode, String sampleCode,
            String experimentIdentifierOrNull)
    {
        return new Sample(createSampleInitializer(sampleTypeCode, spaceCode, sampleCode,
                experimentIdentifierOrNull));
    }

    public static Sample sample(String sampleTypeCode, String spaceCode, String sampleCode)
    {
        return sample(sampleTypeCode, spaceCode, sampleCode, null);
    }

    private static EntityRegistrationDetails registrationDetails()
    {
        return new EntityRegistrationDetails(new EntityRegistrationDetailsInitializer());
    }

}
