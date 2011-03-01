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
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * TODO
 * 
 * @author Kaloyan Enimanev
 */
public class TemplateBasedLinkNamingStrategy implements IHierarchicalStorageLinkNamingStrategy
{

    public static final String DEFAULT_LINK_TEMPLATE =
            "${space}/${project}/${experiment}/${datasettype}+${sample}+${dataset}";

    private static final String LINKS_TEMPLATE_PROP_NAME =
            HierarchicalStorageUpdater.HIERARCHY_LINK_NAMING_STRATEGY + ".template";

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
    public String createHierarchicalPath(SimpleDataSetInformationDTO data)
    {
        ExtendedProperties props = new ExtendedProperties();
        for (PathVariable pathElement : PathVariable.values())
        {
            String pathElementValue = pathElement.extractValueFromData(data);
            String pathElementName = pathElement.name().toLowerCase();
            if (pathElementValue == null)
            {
                pathElementValue = StringUtils.EMPTY;
            }
            props.put(pathElementName, pathElementValue);
        }

        props.put("template", linkTemplate);
        // this will evaluate and replace all variables in the value of the property
        return props.getProperty("template");

    }

    /**
     * Creates a {@link Set} of data set paths located inside <code>root</code>.
     */
    public Set<String> extractPaths(File root)
    {
        HashSet<String> set = new HashSet<String>();
        Pattern matchingFilesFilter = createMatchingFilesFilter(root);
        accumulatePaths(set, root, matchingFilesFilter, getNestedDirectoryLevels());
        return set;
    }

    private Pattern createMatchingFilesFilter(File root)
    {
        // TODO KE: refactor with constants
        final String allMatcher = "([^" + File.separator + "]*)";
        ExtendedProperties props = new ExtendedProperties();
        for (PathVariable var : PathVariable.values())
        {
            props.put(var.name().toLowerCase(), allMatcher);
        }

        props.put("template", linkTemplate);
        String subPathRegex = props.getProperty("template");
        return Pattern.compile(root.getAbsolutePath() + File.separator + subPathRegex);
    }

    private int getNestedDirectoryLevels()
    {
        return StringUtils.countMatches(linkTemplate, File.separator);
    }

    @Private
    static void accumulatePaths(HashSet<String> paths, File dir, Pattern matchingFilesFilter,
            int maxNestedLevel)
    {
        File[] children = dir.listFiles();
        if (children != null)
        {
            for (File child : children)
            {
                if (maxNestedLevel > 0)
                {
                    accumulatePaths(paths, child, matchingFilesFilter, maxNestedLevel - 1);
                } else
                {
                    String absolutePath = child.getAbsolutePath();
                    if (matchingFilesFilter.matcher(absolutePath).matches())
                    {
                        paths.add(absolutePath);
                    }
                }
            }
        }
    }

    enum PathVariable
    {
        Dataset
        {
            @Override
            String extractValueFromData(SimpleDataSetInformationDTO data)
            {
                return data.getDataSetCode();
            }

        },

        DataSetType
        {

            @Override
            String extractValueFromData(SimpleDataSetInformationDTO data)
            {
                return data.getDataSetType();
            }

        },

        Sample
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

        Experiment
        {

            @Override
            String extractValueFromData(SimpleDataSetInformationDTO data)
            {
                return data.getExperimentCode();
            }

        },

        Project
        {
            @Override
            String extractValueFromData(SimpleDataSetInformationDTO data)
            {
                return data.getProjectCode();
            }
        },

        Space
        {

            @Override
            String extractValueFromData(SimpleDataSetInformationDTO data)
            {
                return data.getGroupCode();
            }
        },

        Instance
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