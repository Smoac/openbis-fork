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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

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
public class DisplaySettings implements Serializable, IsSerializable
{
    private static final long serialVersionUID = 1L;

    private Map<String, List<ColumnSetting>> columnSettings =
            new LinkedHashMap<String, List<ColumnSetting>>();

    private Map<String, Boolean> sectionSettings = new HashMap<String, Boolean>();

    // for new users with clean display settings basic search mode will be used by default
    private boolean useWildcardSearchMode = false; 
    
    private RealNumberFormatingParameters realNumberFormatingParameters =
            new RealNumberFormatingParameters();

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public final Map<String, List<ColumnSetting>> getColumnSettings()
    {
        return columnSettings;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public Map<String, Boolean> getSectionSettings()
    {
        if (sectionSettings == null)
        {
            sectionSettings = new HashMap<String, Boolean>();
        }
        return sectionSettings;
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
    private final void setColumnSettings(Map<String, List<ColumnSetting>> columnSettings)
    {
        this.columnSettings = columnSettings;
    }

    @SuppressWarnings("unused")
    private final void setSectionSettings(Map<String, Boolean> sectionSettings)
    {
        this.sectionSettings = sectionSettings;
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
}

