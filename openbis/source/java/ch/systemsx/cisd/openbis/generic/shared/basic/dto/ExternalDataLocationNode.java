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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

/**
 * Implementation of {@link IDatasetLocationNode} that uses {@link ExternalData} object as a source
 * of data set locations.
 * 
 * @author pkupczyk
 */
public class ExternalDataLocationNode implements IDatasetLocationNode
{

    private ExternalData externalData;

    public ExternalDataLocationNode(ExternalData externalData)
    {
        if (externalData == null)
        {
            throw new IllegalArgumentException("ExternalData cannot be null");
        }
        this.externalData = externalData;
    }

    @Override
    public IDatasetLocation getLocation()
    {
        PhysicalDataSet dataSet = externalData.tryGetAsDataSet();
        if (dataSet == null)
        {
            throw new IllegalArgumentException(
                    "Couldn't retrieve full data set infomation from data set "
                            + externalData.getCode());
        }
        return dataSet;
    }

    @Override
    public boolean isContainer()
    {
        return externalData.isContainer();
    }

    @Override
    public Collection<IDatasetLocationNode> getComponents()
    {
        if (isContainer() == false)
        {
            return Collections.emptyList();
        }

        ContainerDataSet container = externalData.tryGetAsContainerDataSet();
        List<ExternalData> containedExternalDatas = container.getContainedDataSets();

        if (containedExternalDatas != null)
        {
            TreeMap<String, IDatasetLocationNode> containedLocationNodes =
                    new TreeMap<String, IDatasetLocationNode>();

            for (ExternalData containedExternalData : containedExternalDatas)
            {
                containedLocationNodes.put(containedExternalData.getCode(),
                        new ExternalDataLocationNode(containedExternalData));
            }

            return containedLocationNodes.values();
        } else
        {
            return Collections.emptyList();
        }
    }

}
