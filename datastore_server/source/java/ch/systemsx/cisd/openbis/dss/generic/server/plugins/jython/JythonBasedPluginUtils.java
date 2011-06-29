/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.api.IDataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Utility methods for reporting and processing plugins delegating the work to a Jython script.
 * 
 * @author Piotr Buczek
 */
public class JythonBasedPluginUtils
{
    /**
     * @return an instance of {@link IDataSet} based on given {@link DatasetDescription} and
     *         {@link IHierarchicalContent}
     */
    static IDataSet createDataSet(final DatasetDescription description,
            final IHierarchicalContent content)
    {
        return new IDataSet()
            {
                public IHierarchicalContent getContent()
                {
                    return content;
                }

                // delegator to description

                public int getSpeedHint()
                {
                    return description.getSpeedHint();
                }

                public String getSpaceCode()
                {
                    return description.getSpaceCode();
                }

                public String getSampleTypeCode()
                {
                    return description.getSampleTypeCode();
                }

                public String getSampleIdentifier()
                {
                    return description.getSampleIdentifier();
                }

                public String getSampleCode()
                {
                    return description.getSampleCode();
                }

                public String getProjectCode()
                {
                    return description.getProjectCode();
                }

                public String getMainDataSetPattern()
                {
                    return description.getMainDataSetPattern();
                }

                public String getMainDataSetPath()
                {
                    return description.getMainDataSetPath();
                }

                public String getInstanceCode()
                {
                    return description.getInstanceCode();
                }

                public String getExperimentTypeCode()
                {
                    return description.getExperimentTypeCode();
                }

                public String getExperimentIdentifier()
                {
                    return description.getExperimentIdentifier();
                }

                public String getExperimentCode()
                {
                    return description.getExperimentCode();
                }

                public String getDataSetTypeCode()
                {
                    return description.getDatasetTypeCode();
                }

                public String getDatabaseInstanceCode()
                {
                    return description.getDatabaseInstanceCode();
                }

                public Long getDataSetSize()
                {
                    return description.getDataSetSize();
                }

                public String getDataSetLocation()
                {
                    return description.getDataSetLocation();
                }

                public String getDataSetCode()
                {
                    return description.getDataSetCode();
                }

            };
    }

    static List<IDataSet> convert(List<DatasetDescription> dataSets,
            IHierarchicalContentProvider contentProvider)
    {
        List<IDataSet> result = new ArrayList<IDataSet>();
        try
        {
            for (DatasetDescription dataSet : dataSets)
            {
                IHierarchicalContent content = contentProvider.asContent(dataSet.getDataSetCode());
                result.add(JythonBasedPluginUtils.createDataSet(dataSet, content));
            }
            return result;
        } catch (RuntimeException ex)
        {
            closeContent(result);
            throw ex;
        }
    }

    static void closeContent(List<IDataSet> dataSets)
    {
        for (IDataSet dataSet : dataSets)
        {
            dataSet.getContent().close();
        }
    }
}
