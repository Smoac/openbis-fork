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

package ch.systemsx.cisd.openbis.uitest.type;

import ch.systemsx.cisd.openbis.uitest.functionality.Application;
import ch.systemsx.cisd.openbis.uitest.functionality.CreateDataSet;
import ch.systemsx.cisd.openbis.uitest.uid.UidGenerator;

/**
 * @author anttil
 */
@SuppressWarnings("hiding")
public class DataSetBuilder implements Builder<DataSet>
{
    private DataSetType type;

    private Sample sample;

    private Experiment experiment;

    private UidGenerator uid;

    public DataSetBuilder(UidGenerator uid)
    {
        this.uid = uid;
        this.sample = null;
        this.experiment = null;
    }

    public DataSetBuilder ofType(DataSetType type)
    {
        this.type = type;
        return this;
    }

    public DataSetBuilder in(Experiment experiment)
    {
        this.experiment = experiment;
        return this;
    }

    public DataSetBuilder in(Sample sample)
    {
        this.sample = sample;
        return this;
    }

    @Override
    public DataSet build(Application openbis)
    {
        if (type == null)
        {
            type = new DataSetTypeBuilder(uid).build(openbis);
        }

        if (sample == null && experiment == null)
        {
            sample = new SampleBuilder(uid).build(openbis);
        }

        return openbis.execute(new CreateDataSet(new DataSet(type, sample, experiment)));
    }
}
