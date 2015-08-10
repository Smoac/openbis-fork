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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.material.ISearchMaterialIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.material.sql.IMaterialSqlTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.MaterialSearchCriterion;

/**
 * @author pkupczyk
 */
@Component
public class SearchMaterialSqlMethodExecutor extends
        AbstractSearchMethodExecutor<Material, Long, MaterialSearchCriterion, MaterialFetchOptions>
        implements ISearchMaterialMethodExecutor
{

    @Autowired
    private ISearchMaterialIdExecutor searchExecutor;

    @Autowired
    private IMaterialSqlTranslator translator;

    @Override
    protected ISearchObjectExecutor<MaterialSearchCriterion, Long> getSearchExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, Material, MaterialFetchOptions> getTranslator()
    {
        return translator;
    }

}
