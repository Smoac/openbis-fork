/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.business.IEntityOperationChecker;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.IServiceConversationClientManagerLocal;
import ch.systemsx.cisd.openbis.generic.server.business.bo.AbstractBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.AttachmentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.AuthorizationGroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.CorePluginTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataSetTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataStoreBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DeletedDataSetTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DeletionTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.EntityTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.EntityTypePropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ExperimentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.GridCustomColumnBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.GridCustomFilterBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IAttachmentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IAuthorizationGroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICorePluginTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataSetTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataStoreBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDeletedDataSetTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDeletionTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityTypePropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGridCustomFilterOrColumnBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMetaprojectBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IProjectBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IPropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IPropertyTypeTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IRoleAssignmentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IScriptBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISpaceBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ITrashBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IVocabularyBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IVocabularyTermBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.MaterialBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.MaterialTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.MetaprojectBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ProjectBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.PropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.PropertyTypeTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.RoleAssignmentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.SampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.SampleTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ScriptBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.SpaceBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.TrashBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.VocabularyBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.VocabularyTermBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.DatasetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.MaterialLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.SampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.IMasterDataScriptRegistrationRunner;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * The unique {@link ICommonBusinessObjectFactory} implementation.
 * 
 * @author Tomasz Pylak
 */
public final class CommonBusinessObjectFactory extends AbstractBusinessObjectFactory implements
        ICommonBusinessObjectFactory
{

    public CommonBusinessObjectFactory(IDAOFactory daoFactory, IDataStoreServiceFactory dssFactory,
            IRelationshipService relationshipService,
            IEntityOperationChecker entityOperationChecker,
            IServiceConversationClientManagerLocal conversationClient)
    {
        super(daoFactory, dssFactory, relationshipService, entityOperationChecker,
                conversationClient);
    }

    @Override
    public final IAttachmentBO createAttachmentBO(final Session session)
    {
        return new AttachmentBO(getDaoFactory(), session);
    }

    @Override
    public final ISpaceBO createSpaceBO(final Session session)
    {
        return new SpaceBO(getDaoFactory(), session);
    }

    @Override
    public final IScriptBO createScriptBO(final Session session)
    {
        return new ScriptBO(getDaoFactory(), session);
    }

    @Override
    public final IRoleAssignmentTable createRoleAssignmentTable(final Session session)
    {
        return new RoleAssignmentTable(getDaoFactory(), session);
    }

    @Override
    public final ISampleTable createSampleTable(final Session session)
    {
        return new SampleTable(getDaoFactory(), session, getRelationshipService(),
                getEntityOperationChecker());
    }

    @Override
    public ISampleLister createSampleLister(Session session)
    {
        return SampleLister.create(getDaoFactory(), session.getBaseIndexURL());
    }

    @Override
    public IDatasetLister createDatasetLister(Session session)
    {
        return DatasetLister.create(getDaoFactory(), session.getBaseIndexURL());
    }

    @Override
    public IMaterialLister createMaterialLister(Session session)
    {
        return MaterialLister.create(getDaoFactory(), session.getBaseIndexURL());
    }

    @Override
    public final ISampleBO createSampleBO(final Session session)
    {
        return new SampleBO(getDaoFactory(), session, getRelationshipService(),
                getEntityOperationChecker());
    }

    @Override
    public IDataBO createDataBO(Session session)
    {
        return new DataBO(getDaoFactory(), session, getRelationshipService(),
                getConversationClient());
    }

    @Override
    public final IDataSetTable createDataSetTable(final Session session)
    {
        return new DataSetTable(getDaoFactory(), getDSSFactory(), session,
                getRelationshipService(), getConversationClient());
    }

    @Override
    public IDeletedDataSetTable createDeletedDataSetTable(Session session)
    {
        return new DeletedDataSetTable(getDaoFactory(), getDSSFactory(), session,
                getRelationshipService(), getConversationClient());
    }

    @Override
    public IExperimentTable createExperimentTable(final Session session)
    {
        return new ExperimentTable(getDaoFactory(), session, getRelationshipService());
    }

    @Override
    public IMaterialTable createMaterialTable(final Session session)
    {
        return new MaterialTable(getDaoFactory(), session);
    }

    @Override
    public final IExperimentBO createExperimentBO(final Session session)
    {
        return new ExperimentBO(getDaoFactory(), session, getRelationshipService());
    }

    @Override
    public final IPropertyTypeTable createPropertyTypeTable(final Session session)
    {
        return new PropertyTypeTable(getDaoFactory(), session);
    }

    @Override
    public final IPropertyTypeBO createPropertyTypeBO(final Session session)
    {
        return new PropertyTypeBO(getDaoFactory(), session);
    }

    @Override
    public final IVocabularyBO createVocabularyBO(Session session)
    {
        return new VocabularyBO(getDaoFactory(), session);
    }

    @Override
    public final IVocabularyTermBO createVocabularyTermBO(Session session)
    {
        return new VocabularyTermBO(getDaoFactory(), session);
    }

    @Override
    public IEntityTypePropertyTypeBO createEntityTypePropertyTypeBO(Session session,
            EntityKind entityKind)
    {
        return new EntityTypePropertyTypeBO(getDaoFactory(), session, entityKind);
    }

    @Override
    public IProjectBO createProjectBO(Session session)
    {
        return new ProjectBO(getDaoFactory(), session, getRelationshipService());
    }

    @Override
    public IEntityTypeBO createEntityTypeBO(Session session)
    {
        return new EntityTypeBO(getDaoFactory(), session);
    }

    @Override
    public IMaterialBO createMaterialBO(Session session)
    {
        return new MaterialBO(getDaoFactory(), session);
    }

    @Override
    public IAuthorizationGroupBO createAuthorizationGroupBO(Session session)
    {
        return new AuthorizationGroupBO(getDaoFactory(), session);
    }

    @Override
    public IGridCustomFilterOrColumnBO createGridCustomFilterBO(Session session)
    {
        return new GridCustomFilterBO(getDaoFactory(), session);
    }

    @Override
    public IGridCustomFilterOrColumnBO createGridCustomColumnBO(Session session)
    {
        return new GridCustomColumnBO(getDaoFactory(), session);
    }

    @Override
    public ITrashBO createTrashBO(Session session)
    {
        return new TrashBO(getDaoFactory(), this, session);
    }

    @Override
    public IDeletionTable createDeletionTable(Session session)
    {
        return new DeletionTable(getDaoFactory(), session);
    }

    @Override
    public ICorePluginTable createCorePluginTable(Session session,
            IMasterDataScriptRegistrationRunner masterDataScriptRunner)
    {
        return new CorePluginTable(getDaoFactory(), session, masterDataScriptRunner);
    }

    @Override
    public IDataStoreBO createDataStoreBO(Session session)
    {
        return new DataStoreBO(getDaoFactory(), session, getDSSFactory());
    }

    @Override
    public IMetaprojectBO createMetaprojectBO(Session session)
    {
        return new MetaprojectBO(getDaoFactory(), createExperimentBO(session),
                createSampleBO(session), createDataBO(session), createMaterialBO(session), session);
    }
}
