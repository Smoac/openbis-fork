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

package ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess;

import ch.rinn.restrictions.Private;

/**
 * @author Tomasz Pylak
 */
@Private
public class WellContentQueryResult
{
    // well pointer

    public long well_id;

    public String well_code;

    public String well_type_code;

    public String well_perm_id;

    // pointer to a plate to which a well belongs

    public long plate_id;

    public String plate_code;

    public String plate_type_code;

    public String plate_perm_id;

    // a pointer to the experiment of the plate

    public long exp_id;

    public String exp_code;

    public String exp_type_code;

    public String exp_perm_id;

    public String proj_code;

    public String space_code;

    // a pointer to a material which was being searched for inside a well
    // NOTE: this information is filled just in few queries.
    @Deprecated
    public String material_content_code;

    @Deprecated
    public String material_content_type_code;

}
