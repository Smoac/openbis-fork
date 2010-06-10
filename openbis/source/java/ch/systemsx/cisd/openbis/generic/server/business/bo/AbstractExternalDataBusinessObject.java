/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractExternalDataBusinessObject extends
        AbstractSampleIdentifierBusinessObject
{

    protected final IEntityPropertiesConverter entityPropertiesConverter;

    public AbstractExternalDataBusinessObject(IDAOFactory daoFactory, Session session)
    {
        this(daoFactory, session, new EntityPropertiesConverter(EntityKind.DATA_SET, daoFactory));
    }

    public AbstractExternalDataBusinessObject(IDAOFactory daoFactory, Session session,
            IEntityPropertiesConverter entityPropertiesConverter)
    {
        super(daoFactory, session);
        this.entityPropertiesConverter = entityPropertiesConverter;
    }

    protected void enrichWithParentsAndExperiment(ExternalDataPE externalDataPE)
    {
        HibernateUtils.initialize(externalDataPE.getParents());
        HibernateUtils.initialize(externalDataPE.getExperiment());
    }

    protected void enrichWithChildren(ExternalDataPE externalDataPE)
    {
        HibernateUtils.initialize(externalDataPE.getChildren());
    }

    protected void updateBatchProperties(ExternalDataPE externalData,
            List<IEntityProperty> newProperties, Set<String> set)
    {
        final Set<DataSetPropertyPE> existingProperties = externalData.getProperties();
        final EntityTypePE type = externalData.getDataSetType();
        final PersonPE registrator = findRegistrator();
        externalData.setProperties(entityPropertiesConverter.updateProperties(existingProperties,
                type, newProperties, registrator, set));
    }

    protected void updateProperties(ExternalDataPE externalData, List<IEntityProperty> newProperties)
    {
        final Set<DataSetPropertyPE> existingProperties = externalData.getProperties();
        final EntityTypePE type = externalData.getDataSetType();
        final PersonPE registrator = findRegistrator();
        externalData.setProperties(entityPropertiesConverter.updateProperties(existingProperties,
                type, newProperties, registrator));
    }

}
