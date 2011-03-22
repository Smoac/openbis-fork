/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import ch.systemsx.cisd.common.utilities.PropertyParametersUtil;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.ViewMode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailViewConfiguration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebClientConfiguration;

/**
 * Loads the Web Client configuration from given file and creates {@link WebClientConfiguration}.
 * 
 * @author Izabela Adamczyk
 */
public class WebClientConfigurationProvider
{
    private static final String CONFIGURATION_FILE_NOT_PROVIDED =
            "${web-client-configuration-file}";

    private static final String HIDE_SECTIONS = "hide-sections";

    private static final String TYPES = "types";

    private static final String VIEW = "view";

    private static final String DETAIL_VIEWS = "detail-views";

    private static final String HIDE_SMART_VIEW = "hide-smart-view";

    private static final String HIDE_FILE_VIEW = "hide-file-view";

    private static final String DEFAULT_VIEW_MODE = "default-view-mode";

    private static final ViewMode DEFAULT_VIEW_MODE_VALUE = ViewMode.NORMAL;

    private static final String MAX_VISIBLE_COLUMNS = "max-visible-columns";
    
    private static final int DEFAULT_MAX_VISIBLE_COLUMNS = 50;

    private static final String MAX_ENTITY_VISITS = "max-entity-visits";
    
    private static final int DEFAULT_MAX_ENTITY_VISITS = 20;
    
    private static final String DATA_SET_TYPES_WITH_IMAGE_OVERVIEW =
            "data-set-types-with-image-overview";

    static final String TECHNOLOGIES = "technologies";

    private WebClientConfiguration webClientConfiguration = new WebClientConfiguration();

    public WebClientConfigurationProvider(String configurationFile)
    {
        if (configurationFile.equals(CONFIGURATION_FILE_NOT_PROVIDED))
        {
            initDefaultValues();
            return;
        }
        Properties properties = PropertyUtils.loadProperties(configurationFile);
        init(properties);
    }

    public WebClientConfigurationProvider(Properties properties)
    {
        init(properties);
    }

    // sets default configuration values (to be used when configuration file is not specified)
    private void initDefaultValues()
    {
        webClientConfiguration.setDefaultViewMode(DEFAULT_VIEW_MODE_VALUE);
        webClientConfiguration.setMaxVisibleColumns(DEFAULT_MAX_VISIBLE_COLUMNS);
        webClientConfiguration.setMaxEntityVisits(DEFAULT_MAX_ENTITY_VISITS);
    }

    private void init(Properties properties)
    {
        webClientConfiguration.setDefaultViewMode(extractDefaultViewMode(properties));
        webClientConfiguration.setMaxVisibleColumns(extractMaxVisibleColumns(properties));
        webClientConfiguration.setMaxEntityVisits(PropertyUtils.getInt(properties,
                MAX_ENTITY_VISITS, DEFAULT_MAX_ENTITY_VISITS));
        webClientConfiguration
                .setDataSetTypesWithImageOverview(extractDataSetTypesWithImageOverview(properties));
        webClientConfiguration.setViews(extractHiddenSections(properties));
        SectionProperties[] props =
                PropertyParametersUtil.extractSectionProperties(properties, TECHNOLOGIES, false);
        for (SectionProperties sectionProperties : props)
        {
            Properties technologyProperties = sectionProperties.getProperties();
            Set<Entry<Object, Object>> entrySet = technologyProperties.entrySet();
            Map<String, String> map = new HashMap<String, String>();
            for (Entry<Object, Object> entry : entrySet)
            {
                map.put(entry.getKey().toString(), entry.getValue().toString());
            }
            webClientConfiguration.addPropertiesForTechnology(sectionProperties.getKey(), map);
        }
    }

    private Map<String, DetailViewConfiguration> extractHiddenSections(Properties properties)
    {
        Map<String, DetailViewConfiguration> viewConfigurations =
                new HashMap<String, DetailViewConfiguration>();
        SectionProperties[] viewsProperties = extractViewsProperties(properties);
        for (int i = 0; i < viewsProperties.length; i++)
        {
            Properties viewProperties = viewsProperties[i].getProperties();
            String viewId = extractViewId(viewProperties);
            List<String> types = extractEntityTypes(viewProperties);
            List<String> hideSectionsIdsOrNull = tryExtractHiddenSections(viewProperties);
            boolean hideSmartView =
                    PropertyUtils.getBoolean(viewProperties, HIDE_SMART_VIEW, false);
            boolean hideFileView = PropertyUtils.getBoolean(viewProperties, HIDE_FILE_VIEW, false);
            for (String type : types)
            {
                DetailViewConfiguration viewConfiguration = new DetailViewConfiguration();
                if (hideSectionsIdsOrNull != null)
                {
                    viewConfiguration.setDisabledTabs(new HashSet<String>(hideSectionsIdsOrNull));
                }
                viewConfiguration.setHideSmartView(hideSmartView);
                viewConfiguration.setHideFileView(hideFileView);
                viewConfigurations.put(viewId + type, viewConfiguration);
            }
        }
        return viewConfigurations;
    }

    private SectionProperties[] extractViewsProperties(Properties properties)
    {
        return PropertyParametersUtil.extractSectionProperties(properties, DETAIL_VIEWS, false);
    }

    private List<String> tryExtractHiddenSections(Properties viewProperties)
    {
        return PropertyUtils.tryGetList(viewProperties, HIDE_SECTIONS);
    }

    private List<String> extractEntityTypes(Properties viewProperties)
    {
        return PropertyUtils.getMandatoryList(viewProperties, TYPES);
    }

    private Set<String> extractDataSetTypesWithImageOverview(Properties properties)
    {
        List<String> list =
                PropertyUtils.tryGetList(properties, DATA_SET_TYPES_WITH_IMAGE_OVERVIEW);
        Set<String> result = new HashSet<String>();
        if (list != null)
        {
            result.addAll(list);
        }
        return result;
    }

    private String extractViewId(Properties viewProperties)
    {
        return PropertyUtils.getMandatoryProperty(viewProperties, VIEW);
    }

    private ViewMode extractDefaultViewMode(Properties properties)
    {
        String viewMode =
                PropertyUtils.getProperty(properties, DEFAULT_VIEW_MODE,
                        DEFAULT_VIEW_MODE_VALUE.name());
        try
        {
            return ViewMode.valueOf(viewMode.toUpperCase());
        } catch (IllegalArgumentException e)
        {
            return ViewMode.NORMAL;
        }
    }

    private int extractMaxVisibleColumns(Properties properties)
    {
        return PropertyUtils.getInt(properties, MAX_VISIBLE_COLUMNS, DEFAULT_MAX_VISIBLE_COLUMNS);
    }

    public WebClientConfiguration getWebClientConfiguration()
    {
        return webClientConfiguration;
    }

}
