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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;

/**
 * Class storing personalised display settings. This class implements {@link Serializable} not only
 * for transferring it's content remotely but also to store it in the database. Thus, CHANGES IN
 * THIS CLASS MIGHT LEAD TO A LOST OF PERSONAL SETTINGS. In all cases deserialization leads to an
 * exception the default settings is used.
 * <p>
 * NOTE: This class has to be Java serializable and GWT serializable.
 * <p>
 * NOTE: Object of this class should be always managed by DisplaySettingsManager on client side.
 * 
 * @author Franz-Josef Elmer
 */
public class DisplaySettings implements ISerializable
{
    private static final long serialVersionUID = 1L;

    private Map<String, List<ColumnSetting>> columnSettings =
            new HashMap<String, List<ColumnSetting>>();

    private Map<String, ISerializable> technologySpecificSettings =
            new HashMap<String, ISerializable>();

    private Map<String, String> tabSettings = new HashMap<String, String>();

    private Map<String, String> dropDownSettings = new HashMap<String, String>();

    // for new users with clean display settings basic search mode will be used by default
    private boolean useWildcardSearchMode = false;

    // When debugging mode is on more information will be shown in UI:
    // - error messages for custom columns or managed properties will contain technical details
    // - raw output and input for managed properties is visible
    // For new users with clean display settings the debugging mode is disabled
    private boolean debugging = false;

    private RealNumberFormatingParameters realNumberFormatingParameters =
            new RealNumberFormatingParameters();

    private Map<String, Boolean> panelCollapsedSettings = new HashMap<String, Boolean>();

    private Map<String, Integer> panelSizeSettings = new HashMap<String, Integer>();

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public Map<String, ISerializable> getTechnologySpecificSettings()
    {
        if (technologySpecificSettings == null)
        {
            technologySpecificSettings = new HashMap<String, ISerializable>();
        }
        return technologySpecificSettings;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public final Map<String, List<ColumnSetting>> getColumnSettings()
    {
        if (columnSettings == null)
        {
            columnSettings = new HashMap<String, List<ColumnSetting>>();
        }
        return columnSettings;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public Map<String, String> getTabSettings()
    {
        if (tabSettings == null)
        {
            tabSettings = new HashMap<String, String>();
        }
        return tabSettings;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public Map<String, Boolean> getPanelCollapsedSettings()
    {
        if (panelCollapsedSettings == null)
        {
            panelCollapsedSettings = new HashMap<String, Boolean>();
        }
        return panelCollapsedSettings;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public Map<String, Integer> getPanelSizeSettings()
    {
        if (panelSizeSettings == null)
        {
            panelSizeSettings = new HashMap<String, Integer>();
        }
        return panelSizeSettings;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public Map<String, String> getDropDownSettings()
    {
        if (dropDownSettings == null)
        {
            dropDownSettings = new HashMap<String, String>();
        }
        return dropDownSettings;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public boolean isUseWildcardSearchMode()
    {
        return useWildcardSearchMode;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public RealNumberFormatingParameters getRealNumberFormatingParameters()
    {
        if (realNumberFormatingParameters == null)
        {
            realNumberFormatingParameters = new RealNumberFormatingParameters();
        }
        return realNumberFormatingParameters;
    }

    // for serialization

    @SuppressWarnings("unused")
    private void setTechnologySpecificSettings(Map<String, ISerializable> technologySpecificSettings)
    {
        this.technologySpecificSettings = technologySpecificSettings;
    }

    @SuppressWarnings("unused")
    private final void setColumnSettings(Map<String, List<ColumnSetting>> columnSettings)
    {
        this.columnSettings = columnSettings;
    }

    @SuppressWarnings("unused")
    private final void setPanelCollapsedSettings(Map<String, Boolean> panelCollapsedSettings)
    {
        this.panelCollapsedSettings = panelCollapsedSettings;
    }

    @SuppressWarnings("unused")
    private final void setPanelSizeSettings(Map<String, Integer> panelSizeSettings)
    {
        this.panelSizeSettings = panelSizeSettings;
    }

    @SuppressWarnings("unused")
    private final void setTabSettings(Map<String, String> tabSettings)
    {
        this.tabSettings = tabSettings;
    }

    @SuppressWarnings("unused")
    private final void setDropDownSettings(Map<String, String> dropDownSettings)
    {
        this.dropDownSettings = dropDownSettings;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public void setUseWildcardSearchMode(boolean useWildcardSearchMode)
    {
        this.useWildcardSearchMode = useWildcardSearchMode;
    }

    @SuppressWarnings("unused")
    private void setRealNumberFormatingParameters(
            RealNumberFormatingParameters realNumberFormatingParameters)
    {
        this.realNumberFormatingParameters = realNumberFormatingParameters;
    }

    /**
     * Are error messages in debugging format or user format?
     * 
     * @deprecated Should be used only by DisplaySettingsManager.
     */
    @Deprecated
    public boolean isDebuggingModeEnabled()
    {
        return debugging;
    }

    /**
     * Are error messages in debugging format or user format?
     * 
     * @deprecated Should be used only by DisplaySettingsManager.
     */
    @Deprecated
    public void setDebuggingModeEnabled(boolean isDebugging)
    {
        debugging = isDebugging;
    }
}
