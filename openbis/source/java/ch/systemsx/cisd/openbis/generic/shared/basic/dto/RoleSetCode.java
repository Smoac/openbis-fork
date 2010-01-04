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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author     Franz-Josef Elmer
 */
public enum RoleSetCode implements IsSerializable
{
    OBSERVER(true), USER(true), POWER_USER(true), GROUP_ETL_SERVER(true), GROUP_ADMIN(true),
    INSTANCE_ETL_SERVER(false), INSTANCE_ADMIN(false), INSTANCE_ADMIN_OBSERVER(false);

    private final boolean groupLevel;

    private RoleSetCode(boolean groupLevel)
    {
        this.groupLevel = groupLevel;
    }

    public final boolean isGroupLevel()
    {
        return groupLevel;
    }

}
