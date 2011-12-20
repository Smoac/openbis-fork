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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractImageSelectionCriterion implements IImageSetSelectionCriterion
{

    public List<IImageSetMetaData> getMatching(List<IImageSetMetaData> imageMetaData)
    {
        List<IImageSetMetaData> filteredMetaData = new ArrayList<IImageSetMetaData>();
        for (IImageSetMetaData metaData : imageMetaData)
        {
            if (accept(metaData))
            {
                filteredMetaData.add(metaData);
            }
        }
        return filteredMetaData;
    }
    
    protected abstract boolean accept(IImageSetMetaData imageMetaData);

}
