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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractSearchCriteria;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.datastore.search.DataStoreKindSearchCriteria")
public class DataStoreKindSearchCriteria extends AbstractSearchCriteria
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private DataStoreKind[] dataStoreKinds = new DataStoreKind[0];

    public void thatIn(final DataStoreKind... dataStoreKinds)
    {
        this.dataStoreKinds = Set.of(dataStoreKinds).toArray(new DataStoreKind[0]);
    }

    public DataStoreKind[] getDataStoreKinds()
    {
        return dataStoreKinds;
    }

    @Override
    public String toString()
    {
        return "with data store kinds " + dataStoreKinds;
    }

}
