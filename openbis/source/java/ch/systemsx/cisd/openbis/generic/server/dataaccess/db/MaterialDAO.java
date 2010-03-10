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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IMaterialDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;

/**
 * Data access object for {@link MaterialPE}.
 * 
 * @author Izabela Adamczyk
 */
public class MaterialDAO extends AbstractGenericEntityDAO<MaterialPE> implements IMaterialDAO
{

    private static final Class<MaterialPE> ENTITY_CLASS = MaterialPE.class;

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, MaterialDAO.class);

    protected MaterialDAO(final SessionFactory sessionFactory,
            final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance, ENTITY_CLASS);
    }

    public List<MaterialPE> listMaterialsWithProperties(final MaterialTypePE materialType)
            throws DataAccessException
    {
        assert materialType != null : "Unspecified material type.";

        final Criteria criteria = getSession().createCriteria(ENTITY_CLASS);
        criteria.add(Restrictions.eq("materialType", materialType));
        final int count = DAOUtils.getCount(criteria);
        if (count <= DAOUtils.MAX_COUNT_FOR_PROPERTIES)
        {
            criteria.setFetchMode("materialProperties", FetchMode.JOIN);
        } else
        {
            operationLog.info(String.format("Found %d materials, disable properties loading.",
                    count));
        }
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        final List<MaterialPE> list = cast(criteria.list());
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d materials have been found for material type '%s'.", list.size(),
                    materialType));
        }
        return list;
    }

    public void createMaterials(List<MaterialPE> materials)
    {
        assert materials != null && materials.size() > 0 : "Unspecified or empty materials.";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        for (final MaterialPE materialPE : materials)
        {
            internalCreateMaterial(materialPE, hibernateTemplate);
        }
        hibernateTemplate.flush();
    }

    private void internalCreateMaterial(MaterialPE material, HibernateTemplate hibernateTemplate)
    {
        assert material.getDatabaseInstance().isOriginalSource() : "Registration on a non-home database is not allowed";
        validatePE(material);
        material.setCode(CodeConverter.tryToDatabase(material.getCode()));
        hibernateTemplate.saveOrUpdate(material);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("ADD: material '%s'.", material));
        }
    }

    public MaterialPE tryFindMaterial(MaterialIdentifier identifier)
    {
        assert identifier != null : "identifier not given";

        String code = CodeConverter.tryToDatabase(identifier.getCode());
        String typeCode = CodeConverter.tryToDatabase(identifier.getTypeCode());

        final Criteria criteria = getSession().createCriteria(ENTITY_CLASS);
        criteria.add(Restrictions.eq("code", code));
        criteria.add(Restrictions.eq("databaseInstance", getDatabaseInstance()));
        criteria.createCriteria("materialType").add(Restrictions.eq("code", typeCode));
        final MaterialPE material = (MaterialPE) criteria.uniqueResult();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Following material '%s' has been found for "
                    + "code '%s' and type '%s'.", material, code, typeCode));
        }
        return material;
    }
}
