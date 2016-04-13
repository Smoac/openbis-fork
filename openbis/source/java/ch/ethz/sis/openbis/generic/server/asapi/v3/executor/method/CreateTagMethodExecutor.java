/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.ICreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag.ICreateTagExecutor;

/**
 * @author pkupczyk
 */
@Component
public class CreateTagMethodExecutor extends AbstractCreateMethodExecutor<TagPermId, TagCreation> implements
        ICreateTagMethodExecutor
{

    @Autowired
    private ICreateTagExecutor createExecutor;

    @Override
    protected ICreateEntityExecutor<TagCreation, TagPermId> getCreateExecutor()
    {
        return createExecutor;
    }

}
