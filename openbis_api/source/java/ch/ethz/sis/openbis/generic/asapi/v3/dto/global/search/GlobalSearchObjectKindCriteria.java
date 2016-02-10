/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search;

import java.util.EnumSet;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractSearchCriteria;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Jakub Straszewski
 */
@JsonObject("as.dto.global.search.GlobalSearchObjectKindCriteria")
public class GlobalSearchObjectKindCriteria extends AbstractSearchCriteria
{
    private static final long serialVersionUID = 1L;

    EnumSet<GlobalSearchObjectKind> objectKinds;

    public void in(List<GlobalSearchObjectKind> kinds)
    {
        this.objectKinds = EnumSet.copyOf(objectKinds);
    }

    public void in(GlobalSearchObjectKind... kinds)
    {
        this.objectKinds = EnumSet.copyOf(objectKinds);
    }

    public EnumSet<GlobalSearchObjectKind> getObjectKinds()
    {
        return objectKinds;
    }
}
