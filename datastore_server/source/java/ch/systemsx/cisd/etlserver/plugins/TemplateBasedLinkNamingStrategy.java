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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * A naming strategy based on a configurable string template.
 * 
 * @author Kaloyan Enimanev
 */
public class TemplateBasedLinkNamingStrategy implements IHierarchicalStorageLinkNamingStrategy
{

    public static final String DEFAULT_LINK_TEMPLATE =
            "${space}/${project}/${experiment}/${dataSetType}+${sample}+${dataSet}";

    private static final String LINKS_TEMPLATE_PROP_NAME = "template";

    private static final String NOT_DIRECTLY_CONNECTED = "NOT_DIRECTLY_CONNECTED";

    private final String linkTemplate;

    
    public TemplateBasedLinkNamingStrategy(String template)
    {
        if (template == null)
        {
            this.linkTemplate = DEFAULT_LINK_TEMPLATE;
        } else
        {
            this.linkTemplate = template;
        }
    }

    public TemplateBasedLinkNamingStrategy(Properties configurationProperties)
    {
        this(configurationProperties.getProperty(LINKS_TEMPLATE_PROP_NAME));

    }

    /**
     * For given {@link SimpleDataSetInformationDTO} creates relevant path part.
     * 
     * @return Instance_AAA/Group_BBB/Project_CCC...</code>
     */
    @Override
    public String createHierarchicalPath(SimpleDataSetInformationDTO data)
    {
        ExtendedProperties props = new ExtendedProperties();
        for (PathVariable pathElement : PathVariable.values())
        {
            String pathElementName = pathElement.name();
            String pathElementValue = pathElement.extractValueFromData(data);
            if (pathElementValue == null)
            {
                pathElementValue = StringUtils.EMPTY;
            }
            props.put(pathElementName, pathElementValue);
        }

        return evaluateTemplate(props);

    }

    /**
     * Creates a {@link Set} of data set paths located inside <code>root</code>.
     */
    @Override
    public Set<String> extractPaths(File root)
    {
        HashSet<String> set = new HashSet<String>();
        accumulateSymLinkPaths(set, root);
        return set;
    }

    private String evaluateTemplate(ExtendedProperties props)
    {
        props.put("template", linkTemplate);
        // this will evaluate and replace all variables in the value of the property
        return props.getProperty("template");
    }

    @Private
    static void accumulateSymLinkPaths(HashSet<String> paths, File dir)
    {
        File[] children = dir.listFiles();
        if (children != null)
        {
            for (File child : children)
            {
                if (FileUtilities.isSymbolicLink(child))
                {
                    String absolutePath = child.getAbsolutePath();
                    paths.add(absolutePath);
                } else if (child.isDirectory())
                {
                    accumulateSymLinkPaths(paths, child);
                }
            }
        }
    }

    enum PathVariable
    {
        dataSet
        {
            @Override
            String extractValueFromData(SimpleDataSetInformationDTO data)
            {
                return data.getDataSetCode();
            }

        },

        dataSetType
        {

            @Override
            String extractValueFromData(SimpleDataSetInformationDTO data)
            {
                return data.getDataSetType();
            }

        },

        sample
        {

            @Override
            String extractValueFromData(SimpleDataSetInformationDTO data)
            {
                String samplePathElement = data.getSampleCode();
                if (samplePathElement == null)
                {
                    samplePathElement = NOT_DIRECTLY_CONNECTED;
                }
                return samplePathElement;
            }

        },

        experiment
        {

            @Override
            String extractValueFromData(SimpleDataSetInformationDTO data)
            {
                return data.getExperimentCode();
            }

        },

        project
        {
            @Override
            String extractValueFromData(SimpleDataSetInformationDTO data)
            {
                return data.getProjectCode();
            }
        },

        space
        {

            @Override
            String extractValueFromData(SimpleDataSetInformationDTO data)
            {
                return data.getSpaceCode();
            }
        },

        instance
        {

            @Override
            String extractValueFromData(SimpleDataSetInformationDTO data)
            {
                return data.getDatabaseInstanceCode();
            }

        };

        
        abstract String extractValueFromData(SimpleDataSetInformationDTO data);

    }
}