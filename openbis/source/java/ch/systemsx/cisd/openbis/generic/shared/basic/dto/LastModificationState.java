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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Stores information about the time and kind of the last modification, separately for each kind of
 * database object.
 * 
 * @author Tomasz Pylak
 */
public class LastModificationState implements IsSerializable
{
    private Map<DatabaseModificationKind, Long/* timestamp */> state;

    public LastModificationState()
    {
        this.state = new HashMap<DatabaseModificationKind, Long>();
    }

    public synchronized void registerModification(DatabaseModificationKind kind,
            long currentTimestamp)
    {
        Long prev = state.get(kind);
        if (prev != null && prev > currentTimestamp)
        {
            return;
        }
        state.put(kind, currentTimestamp);
    }

    public Long tryGetLastModification(DatabaseModificationKind kind)
    {
        return state.get(kind);
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        Set<DatabaseModificationKind> keys = state.keySet();
        for (DatabaseModificationKind modification : keys)
        {
            sb.append("Time ");
            sb.append(new Date(state.get(modification)));
            sb.append(": ");
            sb.append(modification);
            sb.append("\n");
        }
        return sb.toString();
    }
}
