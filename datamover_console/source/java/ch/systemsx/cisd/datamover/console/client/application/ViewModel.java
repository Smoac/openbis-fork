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

package ch.systemsx.cisd.datamover.console.client.application;

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.datamover.console.client.dto.ApplicationInfo;
import ch.systemsx.cisd.datamover.console.client.dto.DatamoverInfo;
import ch.systemsx.cisd.datamover.console.client.dto.User;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ViewModel
{
    private ApplicationInfo applicationInfo;
    private User user;
    private Map<String, String> targets;
    private List<DatamoverInfo> infos;

    public final ApplicationInfo getApplicationInfo()
    {
        return applicationInfo;
    }

    public final void setApplicationInfo(ApplicationInfo info)
    {
        this.applicationInfo = info;
    }

    public final User getUser()
    {
        return user;
    }

    public final void setUser(User user)
    {
        this.user = user;
    }

    public final Map<String, String> getTargets()
    {
        return targets;
    }

    public final void setTargets(Map<String, String> targets)
    {
        this.targets = targets;
    }

    public final List<DatamoverInfo> getInfos()
    {
        return infos;
    }

    public final void setInfos(List<DatamoverInfo> infos)
    {
        if (this.infos == null || this.infos.size() != infos.size())
        {
            this.infos = infos;
            return;
        }
        for (int i = 0, n = infos.size(); i < n; i++)
        {
            DatamoverInfo info = infos.get(i);
            String currentTargetLocation = info.getTargetLocation();
            if (currentTargetLocation == null)
            {
                String oldTargetLocation = this.infos.get(i).getTargetLocation();
                info.setTargetLocation(oldTargetLocation);
            }
            this.infos.set(i, info);
        }
    }
    
}
