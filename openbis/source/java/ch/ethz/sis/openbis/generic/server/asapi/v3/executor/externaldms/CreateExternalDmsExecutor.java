/// *
// * Copyright 2014 ETH Zuerich, CISD
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
// package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.externaldms;
//
// import java.util.ArrayList;
// import java.util.Collection;
// import java.util.LinkedList;
// import java.util.List;
//
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.dao.DataAccessException;
// import org.springframework.stereotype.Component;
//
// import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.create.ExternalDmsCreation;
// import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
// import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
// import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
// import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractCreateEntityExecutor;
// import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
// import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
// import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
// import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.CreateProgress;
// import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
// import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
// import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystemType;
// import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;
//
/// **
// * @author anttil
// */
// @Component
// public class CreateExternalDmsExecutor extends AbstractCreateEntityExecutor<ExternalDmsCreation, ExternalDataManagementSystemPE, ExternalDmsPermId>
// implements ICreateExternalDmsExecutor
// {
//
// @Autowired
// private IDAOFactory daoFactory;
//
// @Override
// protected List<ExternalDataManagementSystemPE> createEntities(IOperationContext context, CollectionBatch<ExternalDmsCreation> batch)
// {
//
// final List<ExternalDataManagementSystemPE> systems = new LinkedList<ExternalDataManagementSystemPE>();
//
// new CollectionBatchProcessor<ExternalDmsCreation>(context, batch)
// {
// @Override
// public void process(ExternalDmsCreation object)
// {
// ExternalDataManagementSystemPE edms = new ExternalDataManagementSystemPE();
// edms.setCode(object.getCode());
// edms.setLabel(object.getLabel());
// edms.setUrlTemplate(object.getUrlTemplate());
//
// ExternalDataManagementSystemType type;
// switch (object.getType())
// {
// case OPENBIS:
// type = ExternalDataManagementSystemType.OPENBIS;
// break;
// case GIT:
// type = ExternalDataManagementSystemType.GIT;
// break;
// default:
// type = ExternalDataManagementSystemType.UNDEFINED;
// break;
// }
//
// edms.setType(type);
// systems.add(edms);
// }
//
// @Override
// public IProgress createProgress(ExternalDmsCreation object, int objectIndex, int totalObjectCount)
// {
// return new CreateProgress(object, objectIndex, totalObjectCount);
// }
// };
//
// return systems;
// }
//
// @Override
// protected ExternalDmsPermId createPermId(IOperationContext context, ExternalDataManagementSystemPE entity)
// {
// return new ExternalDmsPermId(entity.getCode());
// }
//
// @Override
// protected void checkData(IOperationContext context, ExternalDmsCreation creation)
// {
// }
//
// @Override
// protected void checkAccess(IOperationContext context)
// {
// }
//
// @Override
// protected void checkAccess(IOperationContext context, ExternalDataManagementSystemPE entity)
// {
// }
//
// @Override
// protected void updateBatch(IOperationContext context, MapBatch<ExternalDmsCreation, ExternalDataManagementSystemPE> batch)
// {
// }
//
// @Override
// protected void updateAll(IOperationContext context, MapBatch<ExternalDmsCreation, ExternalDataManagementSystemPE> batch)
// {
// }
//
// @Override
// protected List<ExternalDataManagementSystemPE> list(IOperationContext context, Collection<Long> ids)
// {
// List<ExternalDataManagementSystemPE> result = new ArrayList<>();
// for (ExternalDataManagementSystemPE e : daoFactory.getExternalDataManagementSystemDAO().listExternalDataManagementSystems())
// {
// if (ids.contains(e.getId()))
// {
// result.add(e);
// }
// }
// return result;
// }
//
// @Override
// protected void save(IOperationContext context, List<ExternalDataManagementSystemPE> entities, boolean clearCache)
// {
// for (ExternalDataManagementSystemPE entity : entities)
// {
// daoFactory.getExternalDataManagementSystemDAO().createOrUpdateExternalDataManagementSystem(entity);
// }
// daoFactory.getSessionFactory().getCurrentSession().flush();
// }
//
// @Override
// protected void handleException(DataAccessException e)
// {
// DataAccessExceptionTranslator.throwException(e, "external data management system", null);
// }
//
// }
