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
public class PropertyTypeBuilder implements Builder<PropertyType>
{

    private ApplicationRunner openbis;

    private String code;

    private String label;

    private String description;

    private PropertyTypeDataType dataType;

    private Vocabulary vocabulary;

    public PropertyTypeBuilder(ApplicationRunner openbis)
    {
        this.openbis = openbis;
        this.code = UUID.randomUUID().toString();
        this.label = "label of " + code;
        this.description = "description of " + code;
        this.dataType = PropertyTypeDataType.INTEGER;
        this.vocabulary = null;
    }

    public PropertyTypeBuilder withVocabulary(Vocabulary vocabulary)
    {
        this.dataType = PropertyTypeDataType.CONTROLLED_VOCABULARY;
        this.vocabulary = vocabulary;
        return this;
    }

    @Override
    public PropertyType build()
    {
        return openbis.create(new PropertyType(code, label, description, dataType, vocabulary));
    }

}
