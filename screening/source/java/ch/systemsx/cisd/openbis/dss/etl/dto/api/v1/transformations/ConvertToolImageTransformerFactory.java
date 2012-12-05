/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.transformations;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * This class is obsolete, and should not be used. Use
 * {@link ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.ConvertToolImageTransformerFactory}
 * instead
 * 
 * @author Jakub Straszewski
 */
@JsonObject(value="ConvertToolImageTransformerFactory_obsolete")
public class ConvertToolImageTransformerFactory extends
        ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.ConvertToolImageTransformerFactory
{

    private static final long serialVersionUID = 1L;

    public ConvertToolImageTransformerFactory(String convertCliArguments)
    {
        super(convertCliArguments);
    }

    public ConvertToolImageTransformerFactory(String convertCliArguments, ToolChoice choice)
    {
        super(convertCliArguments, choice);
    }
}
