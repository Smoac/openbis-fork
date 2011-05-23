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

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IMaterialDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SequenceNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

/**
 * Data access object for {@link MaterialPE}.
 * 
 * @author Izabela Adamczyk
 */
public class MaterialDAO extends AbstractGenericEntityWithPropertiesDAO<MaterialPE> implements
        IMaterialDAO
{

    private static final Class<MaterialPE> ENTITY_CLASS = MaterialPE.class;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MaterialDAO.class);

    protected MaterialDAO(final PersistencyResources persistencyResources,
            final DatabaseInstancePE databaseInstance)
    {
        super(persistencyResources, databaseInstance, ENTITY_CLASS);
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

    public void createOrUpdateMaterials(List<MaterialPE> materials)
    {
        assert materials != null && materials.size() > 0 : "Unspecified or empty materials.";

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        for (final MaterialPE materialPE : materials)
        {
            internalCreateMaterial(materialPE, hibernateTemplate);
        }
        hibernateTemplate.flush();

        // if session is not cleared registration of many materials slows down after each batch
        hibernateTemplate.clear();
        scheduleDynamicPropertiesEvaluation(materials);
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
        return tryFindMaterial(getSession(), identifier);
    }

    public MaterialPE tryFindMaterial(Session session, MaterialIdentifier identifier)
    {
        assert identifier != null : "identifier not given";

        String code = CodeConverter.tryToDatabase(identifier.getCode());
        String typeCode = CodeConverter.tryToDatabase(identifier.getTypeCode());

        final Criteria criteria = session.createCriteria(ENTITY_CLASS);
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

    public List<MaterialPE> listMaterialsById(final List<Long> ids)
    {
        if (ids == null || ids.isEmpty())
        {
            return Collections.emptyList();
        }
        Criteria criteria = getSession().createCriteria(ENTITY_CLASS);
        criteria.add(Restrictions.in("id", ids));
        final List<MaterialPE> list = cast(criteria.list());
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d materials have been found for ids: %s.",
                    list.size(), CollectionUtils.abbreviate(ids, 10)));
        }
        return list;
    }

    public void delete(final List<TechId> materialIds, final PersonPE registrator,
            final String reason) throws DataAccessException
    {
        final String sqlCodeAndType =
                String.format("SELECT m.code, mt.code as typeCode "
                        + " FROM %s as m, %s as mt WHERE m.id = :mId AND m.maty_id = mt.id",
                        TableNames.MATERIALS_TABLE,
                        TableNames.MATERIAL_TYPES_TABLE);
        final String sqlDeleteProperties =
                "DELETE FROM " + TableNames.MATERIAL_PROPERTIES_TABLE + " WHERE mate_id = :mId";
        final String sqlDeleteSample =
                "DELETE FROM " + TableNames.MATERIALS_TABLE + " WHERE id = :mId";
        final String sqlInsertEvent =
                String.format(
                        "INSERT INTO %s (id, event_type, description, reason, pers_id_registerer, entity_type, identifier) "
                                + "VALUES (nextval('%s'), :eventType, :description, :reason, :registratorId, :entityType, :identifier)",
                        TableNames.EVENTS_TABLE, SequenceNames.EVENT_SEQUENCE);


        executeStatelessAction(new StatelessHibernateCallback()
            {
                public Object doInStatelessSession(StatelessSession session)
                {
                    final SQLQuery sqlQueryCodeAndType = session.createSQLQuery(sqlCodeAndType);
                    final SQLQuery sqlQueryDeleteProperties =
                            session.createSQLQuery(sqlDeleteProperties);
                    final SQLQuery sqlQueryDeleteSample = session.createSQLQuery(sqlDeleteSample);
                    final SQLQuery sqlQueryInsertEvent = session.createSQLQuery(sqlInsertEvent);
                    sqlQueryInsertEvent.setParameter("eventType", EventType.DELETION.name());
                    sqlQueryInsertEvent.setParameter("reason", reason);
                    sqlQueryInsertEvent.setParameter("registratorId", registrator.getId());
                    sqlQueryInsertEvent.setParameter("entityType", EntityType.MATERIAL.name());
                    int counter = 0;
                    for (TechId techId : materialIds)
                    {
                        sqlQueryCodeAndType.setParameter("mId", techId.getId());
                        Object[] codeAndType = (Object[]) sqlQueryCodeAndType.uniqueResult();
                        if (codeAndType != null)
                        {
                            String materialCode = (String) codeAndType[0];
                            String materialTypeCode = (String) codeAndType[1];
                            String permId = MaterialPE.createPermId(materialCode, materialTypeCode);
                            try
                            {
                                // delete properties
                                sqlQueryDeleteProperties.setParameter("mId", techId.getId());
                                sqlQueryDeleteProperties.executeUpdate();
                                // delete material
                                sqlQueryDeleteSample.setParameter("mId", techId.getId());
                                sqlQueryDeleteSample.executeUpdate();
                                // create event
                                sqlQueryInsertEvent.setParameter("description",
                                        permId);
                                sqlQueryInsertEvent.setParameter("identifier", materialCode);
                                sqlQueryInsertEvent.executeUpdate();
                                if (++counter % 1000 == 0)
                                {
                                    operationLog.info(String.format(
                                            "%d materials have been deleted...", counter));
                                }
                            } catch (ConstraintViolationException cve)
                            {
                                // re-wrap ConstraintViolationException to contain
                                // information about the problematic material.
                                throw new ConstraintViolationException(permId,
                                        cve.getSQLException(), cve.getSQL(),
                                        cve.getConstraintName());
                            }
                        }
                    }
                    return null;
                }
            });

        List<Long> ids = TechId.asLongs(materialIds);
        scheduleRemoveFromFullTextIndex(ids);
    }

}
