/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

/**
 * Properties for configuration of web applications in OpenBIS.
 * 
 * @author pkupczyk
 */
public enum WebAppProperty
{

    LABEL("label"), SORTING("sorting"), CONTEXTS("openbisui-contexts"), EXPERIMENT_TYPES(
            "experiment-entity-types"), SAMPLE_TYPES("sample-entity-types"), DATA_SET_TYPES(
            "data-set-entity-types"), MATERIAL_TYPES("material-entity-types");

    private final String name;

    private WebAppProperty(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

}
