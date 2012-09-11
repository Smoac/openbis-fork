/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.type;

import java.util.UUID;

import ch.systemsx.cisd.openbis.uitest.infra.ApplicationRunner;

/**
 * @author anttil
 */
public class SpaceBuilder implements Builder<Space>
{

    private ApplicationRunner openbis;

    private String code;

    private String description;

    public SpaceBuilder(ApplicationRunner openbis)
    {
        this.openbis = openbis;
        this.code = UUID.randomUUID().toString();
        this.description = "";
    }

    public SpaceBuilder withCode(String code)
    {
        this.code = code;
        return this;
    }

    @Override
    public Space build()
    {
        return openbis.create(new Space(code, description));
    }
}
