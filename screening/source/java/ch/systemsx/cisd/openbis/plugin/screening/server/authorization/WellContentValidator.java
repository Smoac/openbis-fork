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

package ch.systemsx.cisd.openbis.plugin.screening.server.authorization;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;

/**
 * A validator for a list of {@link WellContent} objects.
 * 
 * @author Tomasz Pylak
 */
public class WellContentValidator extends SpaceValidator<WellContent>
{
    @Override
    protected String getSpace(WellContent value)
    {
        return value.getExperiment().getSpaceCode();
    }

}
