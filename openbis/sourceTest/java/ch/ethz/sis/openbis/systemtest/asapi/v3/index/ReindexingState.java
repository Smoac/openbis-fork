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

package ch.ethz.sis.openbis.systemtest.asapi.v3.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.DynamicPropertyEvaluationOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.DynamicPropertyEvaluationScheduler;

/**
 * @author pkupczyk
 */
public class ReindexingState extends IndexState
{

    public ReindexingState()
    {
        super(createOperations());
    }

    private static Collection<ReindexingOperation> createOperations()
    {
        List<DynamicPropertyEvaluationOperation> propertyOperations = DynamicPropertyEvaluationScheduler.getThreadOperations();
        List<ReindexingOperation> reindexingOperations = new ArrayList<ReindexingOperation>();

        for (DynamicPropertyEvaluationOperation propertyOperation : propertyOperations)
        {
            reindexingOperations.add(new ReindexingOperation(propertyOperation));
        }

        return reindexingOperations;
    }

}
