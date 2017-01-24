/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.IArchiveDataSetsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.ICreateDataSetTypesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.ICreateDataSetsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.IDeleteDataSetsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.IGetDataSetsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.ISearchDataSetTypesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.ISearchDataSetsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.IUnarchiveDataSetsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.IUpdateDataSetsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.IVerifyDataSetsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.deletion.IConfirmDeletionsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.deletion.IRevertDeletionsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.deletion.ISearchDeletionsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.ICreateExperimentTypesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.ICreateExperimentsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.IDeleteExperimentsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.IGetExperimentsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.ISearchExperimentTypesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.ISearchExperimentsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.IUpdateExperimentsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.IVerifyExperimentsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.globalsearch.ISearchGloballyOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material.ICreateMaterialTypesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material.ICreateMaterialsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material.IDeleteMaterialsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material.IGetMaterialsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material.ISearchMaterialTypesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material.ISearchMaterialsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material.IUpdateMaterialsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material.IVerifyMaterialsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.objectkindmodification.ISearchObjectKindModificationsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.delete.IDeleteOperationExecutionsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.get.IGetOperationExecutionsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.internal.IInternalOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.search.ISearchOperationExecutionsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.update.IUpdateOperationExecutionsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.project.ICreateProjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.project.IDeleteProjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.project.IGetProjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.project.ISearchProjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.project.IUpdateProjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.ICreateSampleTypesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.ICreateSamplesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.IDeleteSamplesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.IGetSamplesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.ISearchSampleTypesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.ISearchSamplesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.IUpdateSamplesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.IVerifySamplesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.service.IExecuteCustomASServiceOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.service.ISearchCustomASServicesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.session.IGetSessionInformationOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space.ICreateSpacesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space.IDeleteSpacesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space.IGetSpacesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space.ISearchSpacesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space.IUpdateSpacesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag.ICreateTagsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag.IDeleteTagsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag.IGetTagsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag.ISearchTagsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag.IUpdateTagsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.vocabulary.ICreateVocabularyTermsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.vocabulary.IDeleteVocabularyTermsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.vocabulary.IGetVocabularyTermsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.vocabulary.ISearchVocabularyTermsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.vocabulary.IUpdateVocabularyTermsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.utils.ExceptionUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;

/**
 * @author pkupczyk
 */
@Component
public class OperationsExecutor implements IOperationsExecutor
{
    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IDeleteSpacesOperationExecutor deleteSpacesExecutor;

    @Autowired
    private IDeleteProjectsOperationExecutor deleteProjectsExecutor;

    @Autowired
    private IDeleteExperimentsOperationExecutor deleteExperimentsExecutor;

    @Autowired
    private IDeleteSamplesOperationExecutor deleteSamplesExecutor;

    @Autowired
    private IDeleteDataSetsOperationExecutor deleteDataSetsExecutor;

    @Autowired
    private IDeleteMaterialsOperationExecutor deleteMaterialsExecutor;

    @Autowired
    private IDeleteTagsOperationExecutor deleteTagsExecutor;

    @Autowired
    private IDeleteVocabularyTermsOperationExecutor deleteVocabularyTermsExecutor;

    @Autowired
    private IDeleteOperationExecutionsOperationExecutor deleteOperationExecutionsExecutor;

    @Autowired
    private ICreateSpacesOperationExecutor createSpacesExecutor;

    @Autowired
    private ICreateProjectsOperationExecutor createProjectsExecutor;

    @Autowired
    private ICreateExperimentsOperationExecutor createExperimentsExecutor;

    @Autowired
    private ICreateSamplesOperationExecutor createSamplesExecutor;

    @Autowired
    private ICreateDataSetsOperationExecutor createDataSetsExecutor;

    @Autowired
    private ICreateMaterialsOperationExecutor createMaterialsExecutor;

    @Autowired
    private ICreateTagsOperationExecutor createTagsExecutor;

    @Autowired
    private ICreateVocabularyTermsOperationExecutor createVocabularyTermsExecutor;

    @Autowired
    private ICreateExperimentTypesOperationExecutor createExperimentTypesExecutor;

    @Autowired
    private ICreateSampleTypesOperationExecutor createSampleTypesExecutor;

    @Autowired
    private ICreateDataSetTypesOperationExecutor createDataSetTypesExecutor;

    @Autowired
    private ICreateMaterialTypesOperationExecutor createMaterialTypesExecutor;

    @Autowired
    private IUpdateSpacesOperationExecutor updateSpacesExecutor;

    @Autowired
    private IUpdateProjectsOperationExecutor updateProjectsExecutor;

    @Autowired
    private IUpdateExperimentsOperationExecutor updateExperimentsExecutor;

    @Autowired
    private IUpdateSamplesOperationExecutor updateSamplesExecutor;

    @Autowired
    private IUpdateDataSetsOperationExecutor updateDataSetsExecutor;

    @Autowired
    private IUpdateMaterialsOperationExecutor updateMaterialsExecutor;

    @Autowired
    private IUpdateTagsOperationExecutor updateTagsExecutor;

    @Autowired
    private IUpdateVocabularyTermsOperationExecutor updateVocabularyTermsExecutor;

    @Autowired
    private IUpdateOperationExecutionsOperationExecutor updateOperationExecutionsExecutor;

    @Autowired
    private IVerifyExperimentsOperationExecutor verifyExperimentsExecutor;

    @Autowired
    private IVerifySamplesOperationExecutor verifySamplesExecutor;

    @Autowired
    private IVerifyDataSetsOperationExecutor verifyDataSetsExecutor;

    @Autowired
    private IVerifyMaterialsOperationExecutor verifyMaterialsExecutor;

    @Autowired
    private IInternalOperationExecutor internalOperationExecutor;

    @Autowired
    private IGetSpacesOperationExecutor getSpacesExecutor;

    @Autowired
    private IGetProjectsOperationExecutor getProjectsExecutor;

    @Autowired
    private IGetExperimentsOperationExecutor getExperimentsExecutor;

    @Autowired
    private IGetSamplesOperationExecutor getSamplesExecutor;

    @Autowired
    private IGetDataSetsOperationExecutor getDataSetsExecutor;

    @Autowired
    private IGetMaterialsOperationExecutor getMaterialsExecutor;

    @Autowired
    private IGetTagsOperationExecutor getTagsExecutor;

    @Autowired
    private IGetVocabularyTermsOperationExecutor getVocabularyTermsExecutor;

    @Autowired
    private IGetOperationExecutionsOperationExecutor getOperationExecutionsExecutor;

    @Autowired
    private ISearchSpacesOperationExecutor searchSpacesExecutor;

    @Autowired
    private ISearchProjectsOperationExecutor searchProjectsExecutor;

    @Autowired
    private ISearchExperimentsOperationExecutor searchExperimentsExecutor;

    @Autowired
    private ISearchSamplesOperationExecutor searchSamplesExecutor;

    @Autowired
    private ISearchDataSetsOperationExecutor searchDataSetsExecutor;

    @Autowired
    private ISearchMaterialsOperationExecutor searchMaterialsExecutor;

    @Autowired
    private ISearchTagsOperationExecutor searchTagsExecutor;

    @Autowired
    private ISearchVocabularyTermsOperationExecutor searchVocabularyTermsExecutor;

    @Autowired
    private ISearchExperimentTypesOperationExecutor searchExperimentTypesExecutor;

    @Autowired
    private ISearchSampleTypesOperationExecutor searchSampleTypesExecutor;

    @Autowired
    private ISearchDataSetTypesOperationExecutor searchDataSetTypesExecutor;

    @Autowired
    private ISearchMaterialTypesOperationExecutor searchMaterialTypesExecutor;

    @Autowired
    private ISearchCustomASServicesOperationExecutor searchCustomASServicesExecutor;

    @Autowired
    private ISearchDeletionsOperationExecutor searchDeletionsExecutor;

    @Autowired
    private ISearchGloballyOperationExecutor searchGloballyExecutor;

    @Autowired
    private ISearchObjectKindModificationsOperationExecutor searchObjectKindModificationsExecutor;

    @Autowired
    private ISearchOperationExecutionsOperationExecutor searchOperationExecutionsExecutor;

    @Autowired
    private IExecuteCustomASServiceOperationExecutor executeCustomASServiceExecutor;

    @Autowired
    private IRevertDeletionsOperationExecutor revertDeletionsExecutor;

    @Autowired
    private IConfirmDeletionsOperationExecutor confirmDeletionsExecutor;

    @Autowired
    private IArchiveDataSetsOperationExecutor archiveDataSetsExecutor;

    @Autowired
    private IUnarchiveDataSetsOperationExecutor unarchiveDataSetsExecutor;

    @Autowired
    private IGetSessionInformationOperationExecutor getSessionInformationExecutor;

    @Override
    public List<IOperationResult> execute(IOperationContext context, List<? extends IOperation> operations)
    {
        Map<IOperation, IOperationResult> resultMap = new HashMap<IOperation, IOperationResult>();

        try
        {
            executeDeletions(operations, resultMap, context);
            executeCreations(operations, resultMap, context);
            executeUpdates(operations, resultMap, context);
            resultMap.putAll(internalOperationExecutor.execute(context, operations));

            flushCurrentSession();
            clearCurrentSession();

            verify(operations, resultMap, context);

            executeGets(operations, resultMap, context);
            executeSearches(operations, resultMap, context);
            executeOthers(operations, resultMap, context);

            List<IOperationResult> resultList = new ArrayList<IOperationResult>();
            for (IOperation operation : operations)
            {
                resultList.add(resultMap.get(operation));
            }

            return resultList;
        } catch (Throwable e)
        {
            throw ExceptionUtils.create(context, e);
        } finally
        {
            clearCurrentSession();
        }
    }

    private void executeOthers(List<? extends IOperation> operations,
            Map<IOperation, IOperationResult> resultMap, IOperationContext context)
    {
        resultMap.putAll(executeCustomASServiceExecutor.execute(context, operations));
        resultMap.putAll(revertDeletionsExecutor.execute(context, operations));
        resultMap.putAll(confirmDeletionsExecutor.execute(context, operations));
        resultMap.putAll(archiveDataSetsExecutor.execute(context, operations));
        resultMap.putAll(unarchiveDataSetsExecutor.execute(context, operations));
        resultMap.putAll(getSessionInformationExecutor.execute(context, operations));
    }

    private void executeSearches(List<? extends IOperation> operations,
            Map<IOperation, IOperationResult> resultMap, IOperationContext context)
    {
        resultMap.putAll(searchSpacesExecutor.execute(context, operations));
        resultMap.putAll(searchProjectsExecutor.execute(context, operations));
        resultMap.putAll(searchExperimentsExecutor.execute(context, operations));
        resultMap.putAll(searchSamplesExecutor.execute(context, operations));
        resultMap.putAll(searchDataSetsExecutor.execute(context, operations));
        resultMap.putAll(searchMaterialsExecutor.execute(context, operations));
        resultMap.putAll(searchTagsExecutor.execute(context, operations));
        resultMap.putAll(searchVocabularyTermsExecutor.execute(context, operations));
        resultMap.putAll(searchExperimentTypesExecutor.execute(context, operations));
        resultMap.putAll(searchSampleTypesExecutor.execute(context, operations));
        resultMap.putAll(searchDataSetTypesExecutor.execute(context, operations));
        resultMap.putAll(searchMaterialTypesExecutor.execute(context, operations));
        resultMap.putAll(searchCustomASServicesExecutor.execute(context, operations));
        resultMap.putAll(searchDeletionsExecutor.execute(context, operations));
        resultMap.putAll(searchGloballyExecutor.execute(context, operations));
        resultMap.putAll(searchObjectKindModificationsExecutor.execute(context, operations));
        resultMap.putAll(searchOperationExecutionsExecutor.execute(context, operations));
    }

    private void executeGets(List<? extends IOperation> operations,
            Map<IOperation, IOperationResult> resultMap, IOperationContext context)
    {
        resultMap.putAll(getSpacesExecutor.execute(context, operations));
        resultMap.putAll(getProjectsExecutor.execute(context, operations));
        resultMap.putAll(getExperimentsExecutor.execute(context, operations));
        resultMap.putAll(getSamplesExecutor.execute(context, operations));
        resultMap.putAll(getDataSetsExecutor.execute(context, operations));
        resultMap.putAll(getMaterialsExecutor.execute(context, operations));
        resultMap.putAll(getTagsExecutor.execute(context, operations));
        resultMap.putAll(getVocabularyTermsExecutor.execute(context, operations));
        resultMap.putAll(getOperationExecutionsExecutor.execute(context, operations));
    }

    private void verify(List<? extends IOperation> operations,
            Map<IOperation, IOperationResult> resultMap, IOperationContext context)
    {
        verifyMaterialsExecutor.verify(context, operations, resultMap);
        verifyExperimentsExecutor.verify(context, operations, resultMap);
        verifySamplesExecutor.verify(context, operations, resultMap);
        verifyDataSetsExecutor.verify(context, operations, resultMap);
    }

    private void executeUpdates(List<? extends IOperation> operations,
            Map<IOperation, IOperationResult> resultMap, IOperationContext context)
    {
        resultMap.putAll(updateOperationExecutionsExecutor.execute(context, operations));
        resultMap.putAll(updateVocabularyTermsExecutor.execute(context, operations));
        resultMap.putAll(updateTagsExecutor.execute(context, operations));
        resultMap.putAll(updateMaterialsExecutor.execute(context, operations));
        resultMap.putAll(updateSpacesExecutor.execute(context, operations));
        resultMap.putAll(updateProjectsExecutor.execute(context, operations));
        resultMap.putAll(updateExperimentsExecutor.execute(context, operations));
        resultMap.putAll(updateSamplesExecutor.execute(context, operations));
        resultMap.putAll(updateDataSetsExecutor.execute(context, operations));
    }

    private void executeCreations(List<? extends IOperation> operations,
            Map<IOperation, IOperationResult> resultMap, IOperationContext context)
    {
        resultMap.putAll(createVocabularyTermsExecutor.execute(context, operations));
        resultMap.putAll(createExperimentTypesExecutor.execute(context, operations));
        resultMap.putAll(createSampleTypesExecutor.execute(context, operations));
        resultMap.putAll(createDataSetTypesExecutor.execute(context, operations));
        resultMap.putAll(createMaterialTypesExecutor.execute(context, operations));
        resultMap.putAll(createMaterialsExecutor.execute(context, operations));
        resultMap.putAll(createSpacesExecutor.execute(context, operations));
        resultMap.putAll(createProjectsExecutor.execute(context, operations));
        resultMap.putAll(createExperimentsExecutor.execute(context, operations));
        resultMap.putAll(createSamplesExecutor.execute(context, operations));
        resultMap.putAll(createDataSetsExecutor.execute(context, operations));
        resultMap.putAll(createTagsExecutor.execute(context, operations));
    }

    private void executeDeletions(List<? extends IOperation> operations,
            Map<IOperation, IOperationResult> resultMap, IOperationContext context)
    {
        resultMap.putAll(deleteExperimentsExecutor.execute(context, operations));
        resultMap.putAll(deleteSamplesExecutor.execute(context, operations));
        resultMap.putAll(deleteDataSetsExecutor.execute(context, operations));
        resultMap.putAll(deleteProjectsExecutor.execute(context, operations));
        resultMap.putAll(deleteSpacesExecutor.execute(context, operations));
        resultMap.putAll(deleteMaterialsExecutor.execute(context, operations));
        resultMap.putAll(deleteTagsExecutor.execute(context, operations));
        resultMap.putAll(deleteVocabularyTermsExecutor.execute(context, operations));
        resultMap.putAll(deleteOperationExecutionsExecutor.execute(context, operations));
    }

    protected void clearCurrentSession()
    {
        daoFactory.getSessionFactory().getCurrentSession().clear();
    }

    protected void flushCurrentSession()
    {
        daoFactory.getSessionFactory().getCurrentSession().flush();
    }

}
