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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Class storing personalised display settings. This class implements {@link Serializable} not only
 * for transferring it's content remotely but also to store it in the database. Thus, CHANGES IN
 * THIS CLASS MIGHT LEAD TO A LOST OF PERSONAL SETTINGS. In all cases deserialisation leads to an
 * exception the default settings is used.
 * <p>
 * Note: This class has to be Java serializable and GWT serializable.
 * 
 * @author Franz-Josef Elmer
 */
public class DisplaySettings implements Serializable, IsSerializable
{
    private static final long serialVersionUID = 1L;
    
    private Map<String, List<ColumnSetting>> columnSettings =
            new LinkedHashMap<String, List<ColumnSetting>>();

    public final Map<String, List<ColumnSetting>> getColumnSettings()
    {
        return columnSettings;
    }

    public final void setColumnSettings(Map<String, List<ColumnSetting>> columnSettings)
    {
        this.columnSettings = columnSettings;
    }
    
    

}
