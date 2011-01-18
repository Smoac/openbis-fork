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

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;

/**
 * Abstract super class of {@link IOriginalDataProvider} implementations which return no
 * {@link TableModelColumnHeader} instances.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractOriginalDataProviderWithoutHeaders<T> implements
        IOriginalDataProvider<T>
{
    private static final List<TableModelColumnHeader> NO_HEADERS =
            new ArrayList<TableModelColumnHeader>();

    private List<T> originalData;

    public List<TableModelColumnHeader> getHeaders()
    {
        // Collections.emptyList() can not be serialized by GWT
        return NO_HEADERS;
    }
    
    public List<T> getOriginalData(int maxSize) throws UserFailureException
    {
        originalData = getOriginalData();
        List<T> list = new ArrayList<T>();
        for (T item : originalData)
        {
            if (list.size() == maxSize)
            {
                break;
            }
            list.add(item);
        }
        return list;
    }

    private List<T> getOriginalData() throws UserFailureException
    {
        if (originalData != null)
        {
            return originalData;
        }
        return getFullOriginalData();
    }

    public abstract List<T> getFullOriginalData();
}
