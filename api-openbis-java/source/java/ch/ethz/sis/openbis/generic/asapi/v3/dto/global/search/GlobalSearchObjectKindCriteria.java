/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractSearchCriteria;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Jakub Straszewski
 */
@JsonObject("as.dto.global.search.GlobalSearchObjectKindCriteria")
public class GlobalSearchObjectKindCriteria extends AbstractSearchCriteria
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private List<GlobalSearchObjectKind> objectKinds;

    public void thatIn(List<GlobalSearchObjectKind> kinds)
    {
        this.objectKinds = kinds;
    }

    public void thatIn(GlobalSearchObjectKind... kinds)
    {
        this.objectKinds = Arrays.asList(kinds);
    }

    public List<GlobalSearchObjectKind> getObjectKinds()
    {
        return objectKinds;
    }

    @Override
    public String toString()
    {
        return "with object kinds " + objectKinds;
    }
}
