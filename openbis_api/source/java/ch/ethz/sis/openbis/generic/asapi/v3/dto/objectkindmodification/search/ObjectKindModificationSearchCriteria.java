/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.ObjectKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.OperationKind;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.objectkindmodification.search.ObjectKindModificationSearchCriteria")
public class ObjectKindModificationSearchCriteria implements ISearchCriteria
{
    private List<ObjectKind> objectKinds = new ArrayList<>();

    private List<OperationKind> operationKinds = new ArrayList<>();

    private static final long serialVersionUID = 1L;

    public ObjectKindModificationSearchCriteria withObjectKinds(ObjectKind... someObjectKinds)
    {
        this.objectKinds.addAll(Arrays.asList(someObjectKinds));
        return this;
    }

    public ObjectKindModificationSearchCriteria withOperationKinds(OperationKind... someOperationKinds)
    {
        this.operationKinds.addAll(Arrays.asList(someOperationKinds));
        return this;
    }

    public List<ObjectKind> getObjectKinds()
    {
        return objectKinds;
    }

    public List<OperationKind> getOperationKinds()
    {
        return operationKinds;
    }
}
