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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material.ISearchMaterialTypeExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.material.IMaterialTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class SearchMaterialTypeSqlMethodExecutor extends
        AbstractIdSearchMethodExecutor<MaterialType, MaterialTypePE, MaterialTypeSearchCriteria, MaterialTypeFetchOptions>
        implements ISearchMaterialTypeMethodExecutor
{
    @Autowired
    private ISearchMaterialTypeExecutor searchExecutor;

    @Autowired
    private IMaterialTypeTranslator translator;

    @Override
    protected List<MaterialTypePE> searchPEs(IOperationContext context, MaterialTypeSearchCriteria criteria)
    {
        return searchExecutor.search(context, criteria);
    }

    @Override
    protected ITranslator<Long, MaterialType, MaterialTypeFetchOptions> getTranslator()
    {
        return translator;
    }

}
