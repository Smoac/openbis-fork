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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * Builder class for creating an instance of {@link DataSet}.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetBuilder
{
    private final DataSet dataSet;

    public DataSetBuilder()
    {
        dataSet = new DataSet();
        dataSet.setDataSetProperties(new ArrayList<IEntityProperty>());
    }

    public DataSetBuilder(long id)
    {
        this();
        dataSet.setId(id);
    }

    public DataSetBuilder code(String code)
    {
        dataSet.setCode(code);
        return this;
    }

    public DataSetBuilder type(String dataSetTypeCode)
    {
        dataSet.setDataSetType(new DataSetType(dataSetTypeCode));
        return this;
    }

    public DataSetBuilder location(String location)
    {
        dataSet.setLocation(location);
        return this;
    }

    public DataSetBuilder experiment(Experiment experiment)
    {
        dataSet.setExperiment(experiment);
        return this;
    }

    public DataSetBuilder sample(Sample sample)
    {
        dataSet.setSample(sample);
        return this;
    }

    public DataSetBuilder store(DataStore dataStore)
    {
        dataSet.setDataStore(dataStore);
        return this;
    }

    public DataSetBuilder shareID(String shareID)
    {
        dataSet.setShareId(shareID);
        return this;
    }

    public DataSetBuilder size(long size)
    {
        dataSet.setSize(size);
        return this;
    }

    public PropertyBuilder property(String key)
    {
        List<IEntityProperty> properties = dataSet.getProperties();
        PropertyBuilder propertyBuilder = new PropertyBuilder(key);
        properties.add(propertyBuilder.getProperty());
        return propertyBuilder;
    }

    public DataSetBuilder property(String key, String value)
    {
        property(key).value(value);
        return this;
    }

    public DataSetBuilder registrationDate(Date registrationDate)
    {
        dataSet.setRegistrationDate(registrationDate);
        return this;
    }

    public DataSetBuilder parent(DataSet parent)
    {
        Collection<ExternalData> parents = dataSet.getParents();
        if (parents == null)
        {
            parents = new ArrayList<ExternalData>();
            dataSet.setParents(parents);
        }
        parents.add(parent);
        return this;
    }

    public DataSetBuilder child(DataSet child)
    {
        List<ExternalData> children = dataSet.getChildren();
        if (children == null)
        {
            children = new ArrayList<ExternalData>();
            dataSet.setChildren(children);
        }
        children.add(child);
        return this;
    }

    public final DataSet getDataSet()
    {
        return dataSet;
    }

}
