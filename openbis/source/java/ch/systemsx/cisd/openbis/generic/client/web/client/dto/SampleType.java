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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Izabela Adamczyk
 */
public class SampleType implements IsSerializable
{

    String code;

    String description;

    DatabaseInstance databaseInstance;

    private int generatedFromHierarchyDepth;

    private int partOfHierarchyDepth;

    private List<SampleTypePropertyType> sampleTypePropertyTypes;

    public String getCode()
    {
        return code;
    }

    public void setCode(String codel)
    {
        this.code = codel;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public DatabaseInstance getDatabaseInstance()
    {
        return databaseInstance;
    }

    public void setDatabaseInstance(DatabaseInstance databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    public void setGeneratedFromHierarchyDepth(int generatedFromHierarchyDepth)
    {
        this.generatedFromHierarchyDepth = generatedFromHierarchyDepth;
    }

    public void setPartOfHierarchyDepth(int partOfHierarchyDepth)
    {
        this.partOfHierarchyDepth = partOfHierarchyDepth;
    }

    public int getGeneratedFromHierarchyDepth()
    {
        return generatedFromHierarchyDepth;
    }

    public int getPartOfHierarchyDepth()
    {
        return partOfHierarchyDepth;
    }

    public List<SampleTypePropertyType> getSampleTypePropertyTypes()
    {
        return sampleTypePropertyTypes;
    }

    public void setSampleTypePropertyTypes(List<SampleTypePropertyType> sampleTypePropertyTypes)
    {
        this.sampleTypePropertyTypes = sampleTypePropertyTypes;
    }

}
