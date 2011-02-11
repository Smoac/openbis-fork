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

import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperimentImmutable;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class Sample extends SampleImmutable implements ISample
{

    public Sample(String sampleIdentifier, String permId)
    {
        super(new ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample());
        getSample().setIdentifier(sampleIdentifier);
        getSample().setPermId(permId);
    }

    public Sample(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample)
    {
        super(sample);
    }

    public void setExperiment(IExperimentImmutable experiment)
    {
        ExperimentImmutable exp = (ExperimentImmutable) experiment;
        getSample().setExperiment(exp.getExperiment());
    }

    public void setCode(String code)
    {
        getSample().setCode(code);
    }

    public void setPropertyValue(String propertyCode, String propertyValue)
    {
        EntityHelper.createOrUpdateProperty(getSample(), propertyCode, propertyValue);
    }

    public void setType(String type)
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode(type);

        getSample().setSampleType(sampleType);
    }

}
