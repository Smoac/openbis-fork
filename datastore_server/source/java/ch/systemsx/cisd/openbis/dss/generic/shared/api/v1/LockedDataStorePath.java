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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.v1;

import java.io.Serializable;

/**
 * Value object which defines a locked path into a data store.
 *
 * @author Franz-Josef Elmer
 */
public class LockedDataStorePath implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final String dataSetCode;
    private final String path;
    private final String lockToken;
    
    /**
     * Creates instance for specified data set code, path and lock token.
     */
    public LockedDataStorePath(String dataSetCode, String path, String lockToken)
    {
        this.dataSetCode = dataSetCode;
        this.path = path;
        this.lockToken = lockToken;
    }

    /**
     * Returns the data set code of the locked path.
     */
    public String getDataSetCode()
    {
        return dataSetCode;
    }

    /**
     * Returns an absolute path to the data set specified by the data set code.
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Returns a unique lock token.
     */
    public String getLockToken()
    {
        return lockToken;
    }
}
