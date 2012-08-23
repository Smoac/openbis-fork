/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;

/**
 * {@link IEntityAdaptor} implementation for {@link MaterialPE}.
 * 
 * @author Piotr Buczek
 */
public class MaterialAdaptor extends AbstractEntityAdaptor
{
    private final MaterialPE MaterialPE;

    public MaterialAdaptor(MaterialPE MaterialPE, IDynamicPropertyEvaluator evaluator)
    {
        super(MaterialPE.getCode());
        initProperties(MaterialPE, evaluator);
        this.MaterialPE = MaterialPE;
    }

    public MaterialPE materialPE()
    {
        return MaterialPE;
    }

    public MaterialPE entityPE()
    {
        return materialPE();
    }

}
