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

import ch.systemsx.cisd.openbis.generic.server.business.bo.AbstractBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.EntityTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.EntityTypePropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ExperimentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ExternalDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.GroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityTypePropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IProcedureBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IProjectBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IPropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IPropertyTypeTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IRoleAssignmentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IVocabularyBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.MaterialTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ProcedureBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ProjectBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.PropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.PropertyTypeTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.RoleAssignmentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.SampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.SampleTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.VocabularyBO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
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
    public CommonBusinessObjectFactory(final IDAOFactory daoFactory)
    {
        super(daoFactory);
    }

    //
    // IGenericBusinessObjectFactory
    //

    public final IGroupBO createGroupBO(final Session session)
    {
        return new GroupBO(getDaoFactory(), session);
    }

    public final IRoleAssignmentTable createRoleAssignmentTable(final Session session)
    {
        return new RoleAssignmentTable(getDaoFactory(), session);
    }

    public final ISampleTable createSampleTable(final Session session)
    {
        return new SampleTable(getDaoFactory(), session);
    }

    public final ISampleBO createSampleBO(final Session session)
    {
        return new SampleBO(getDaoFactory(), session);
    }

    public IExternalDataBO createExternalDataBO(Session session)
    {
        return new ExternalDataBO(getDaoFactory(), session);
    }

    public final IExternalDataTable createExternalDataTable(final Session session)
    {
        return new ExternalDataTable(getDaoFactory(), session);
    }

    public IExperimentTable createExperimentTable(final Session session)
    {
        return new ExperimentTable(getDaoFactory(), session);
    }

    public IMaterialTable createMaterialTable(final Session session)
    {
        return new MaterialTable(getDaoFactory(), session);
    }

    public final IExperimentBO createExperimentBO(final Session session)
    {
        return new ExperimentBO(getDaoFactory(), session);
    }

    public final IPropertyTypeTable createPropertyTypeTable(final Session session)
    {
        return new PropertyTypeTable(getDaoFactory(), session);
    }

    public final IPropertyTypeBO createPropertyTypeBO(final Session session)
    {
        return new PropertyTypeBO(getDaoFactory(), session);
    }

    public final IVocabularyBO createVocabularyBO(Session session)
    {
        return new VocabularyBO(getDaoFactory(), session);
    }

    public IProcedureBO createProcedureBO(Session session)
    {
        return new ProcedureBO(getDaoFactory(), session);
    }

    public IEntityTypePropertyTypeBO createEntityTypePropertyTypeBO(Session session,
            EntityKind entityKind)
    {
        return new EntityTypePropertyTypeBO(getDaoFactory(), session, entityKind);
    }

    public IProjectBO createProjectBO(Session session)
    {
        return new ProjectBO(getDaoFactory(), session);
    }

    public IEntityTypeBO createEntityTypeBO(Session session)
    {
        return new EntityTypeBO(getDaoFactory(), session);
    }
}
