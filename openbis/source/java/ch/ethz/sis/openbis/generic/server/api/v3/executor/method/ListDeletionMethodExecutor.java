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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.deletion.IListDeletionExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.deletion.IDeletionTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.Deletion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.deletion.DeletionFetchOptions;

/**
 * @author pkupczyk
 */
@Component
public class ListDeletionMethodExecutor extends AbstractMethodExecutor implements IListDeletionMethodExecutor
{

    @Autowired
    private IListDeletionExecutor listExecutor;

    @Autowired
    private IDeletionTranslator translator;

    @Override
    public List<Deletion> listDeletions(final String sessionToken, final DeletionFetchOptions fetchOptions)
    {
        return executeInContext(sessionToken, new IMethodAction<List<Deletion>>()
            {
                @Override
                public List<Deletion> execute(IOperationContext context)
                {
                    List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion> deletions = listExecutor.list(context, fetchOptions);

                    Map<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion, Deletion> translatedMap =
                            translator.translate(new TranslationContext(context.getSession()), deletions, fetchOptions);

                    return new ArrayList<Deletion>(translatedMap.values());
                }
            });
    }

}
