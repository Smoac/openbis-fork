/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.vocabulary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.CreateVocabularyTermsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.CreateVocabularyTermsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyTermCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.create.CreateObjectsOperationExecutor;

/**
 * @author pkupczyk
 */
@Component
public class CreateVocabularyTermsOperationExecutor extends CreateObjectsOperationExecutor<VocabularyTermCreation, VocabularyTermPermId> implements
        ICreateVocabularyTermsOperationExecutor
{

    @Autowired
    private ICreateVocabularyTermExecutor executor;

    @Override
    protected Class<? extends CreateObjectsOperation<VocabularyTermCreation>> getOperationClass()
    {
        return CreateVocabularyTermsOperation.class;
    }

    @Override
    protected CreateObjectsOperationResult<VocabularyTermPermId> doExecute(IOperationContext context,
            CreateObjectsOperation<VocabularyTermCreation> operation)
    {
        return new CreateVocabularyTermsOperationResult(executor.create(context, operation.getCreations()));
    }

}
