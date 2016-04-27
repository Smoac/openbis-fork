/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.db;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Useful utility method concerning database code.
 * 
 * @author Basil Neff
 */
public final class DBUtils
{
    /**
     * Translates the specified timestamp to a {@link Date} object.
     * 
     * @return <code>null</code> if <code>timestamp == null</code>.
     */
    public final static Date tryToTranslateTimestampToDate(final Timestamp timestampOrNull)
    {
        return timestampOrNull == null ? null : new Date(timestampOrNull.getTime());
    }

    private DBUtils()
    {
    }
}
