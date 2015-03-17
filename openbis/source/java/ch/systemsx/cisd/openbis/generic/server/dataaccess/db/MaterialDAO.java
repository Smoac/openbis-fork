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

import static ch.systemsx.cisd.openbis.generic.shared.dto.ValidationMessages.CODE_PATTERN_MESSAGE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IMaterialDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.MaterialCodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.AbstractIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SequenceNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import ch.systemsx.cisd.openbis.generic.shared.util.MaterialConfigurationProvider;

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

    private static final Pattern STRICT_CODE_PATTERN = Pattern.compile(
            AbstractIdAndCodeHolder.CODE_PATTERN, Pattern.CASE_INSENSITIVE);

    private static final Pattern RELAXED_CODE_PATTERN = Pattern.compile("^[^\\s]+$",
            Pattern.CASE_INSENSITIVE);

    protected MaterialDAO(final PersistencyResources persistencyResources)
    {
        super(persistencyResources, ENTITY_CLASS);
    }

    @Override
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
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        final List<MaterialPE> list = cast(criteria.list());
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d materials have been found for material type '%s'.", list.size(),
                    materialType));
        }
        return list;
    }

    @Override
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
        validatePE(material);
        validateMaterialCode(material);
        hibernateTemplate.saveOrUpdate(material);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("ADD: material '%s'.", material));
        }
    }

    private void validateMaterialCode(MaterialPE material)
    {
        Pattern codePattern = null;
        if (isStrictCodeConstraints())
        {
            material.setCode(CodeConverter.tryToDatabase(material.getCode()));
            codePattern = STRICT_CODE_PATTERN;
        } else
        {
            codePattern = RELAXED_CODE_PATTERN;
        }

        if (false == isValidCode(material.getCode(), codePattern))
        {
            throw new DataIntegrityViolationException(String.format(CODE_PATTERN_MESSAGE,
                    material.getCode()));
        }
    }

    private boolean isStrictCodeConstraints()
    {
        return getMaterialConfig().isStrictCodeConstraints();
    }

    private MaterialConfigurationProvider getMaterialConfig()
    {
        return MaterialConfigurationProvider.getInstance();
    }

    @Override
    public MaterialPE tryFindMaterial(MaterialIdentifier identifier)
    {
        return tryFindMaterial(getSession(), identifier);
    }

    @Override
    public List<MaterialPE> listMaterialsByMaterialIdentifier(Collection<MaterialIdentifier> ids)
    {
        ArrayList<MaterialPE> result = new ArrayList<MaterialPE>();
        for (MaterialIdentifier materialIdentifier : ids)
        {
            MaterialPE material = tryFindMaterial(materialIdentifier);
            if (material != null)
            {
                result.add(material);
            }
        }
        return result;
    }

    @Override
    public MaterialPE tryFindMaterial(Session session, MaterialIdentifier identifier)
    {
        assert identifier != null : "identifier not given";

        String code =
                MaterialCodeConverter.tryMaterialCodeToDatabase(identifier.getCode(),
                        getMaterialConfig());
        String typeCode = CodeConverter.tryToDatabase(identifier.getTypeCode());

        final Criteria criteria = session.createCriteria(ENTITY_CLASS);
        criteria.add(Restrictions.eq("code", code));
        criteria.createCriteria("materialType").add(Restrictions.eq("code", typeCode));
        final MaterialPE material = (MaterialPE) criteria.uniqueResult();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Following material '%s' has been found for "
                    + "code '%s' and type '%s'.", material, code, typeCode));
        }
        return material;
    }

    @Override
    public List<MaterialPE> listMaterialsById(final Collection<Long> ids)
    {
        if (ids == null || ids.isEmpty())
        {
            return Collections.emptyList();
        }
        final List<MaterialPE> list =
                DAOUtils.listByCollection(getHibernateTemplate(), ENTITY_CLASS, "id", ids);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d materials have been found for ids: %s.",
                    list.size(), CollectionUtils.abbreviate(ids, 10)));
        }
        return list;
    }

    @Override
    public void delete(final List<TechId> materialIds, final PersonPE registrator,
            final String reason) throws DataAccessException
    {
        // TODO KE: speed up material bulk deletion
        final String sqlCodeAndType =
                String.format("SELECT m.code, mt.code as typeCode "
                        + " FROM %s as m, %s as mt WHERE m.id = :mId AND m.maty_id = mt.id",
                        TableNames.MATERIALS_TABLE, TableNames.MATERIAL_TYPES_TABLE);
        final String sqlDeleteProperties =
                "DELETE FROM " + TableNames.MATERIAL_PROPERTIES_TABLE + " WHERE mate_id = :mId";
        final String sqlDeleteSample =
                "DELETE FROM " + TableNames.MATERIALS_TABLE + " WHERE id = :mId";
        final String sqlInsertEvent =
                String.format(
                        "INSERT INTO %s (id, event_type, description, reason, pers_id_registerer, entity_type, identifiers) "
                                + "VALUES (nextval('%s'), :eventType, :description, :reason, :registratorId, :entityType, :identifier)",
                        TableNames.EVENTS_TABLE, SequenceNames.EVENT_SEQUENCE);

        executeStatelessAction(new StatelessHibernateCallback()
            {
                @Override
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
                                sqlQueryInsertEvent.setParameter("description", permId);
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

    @Override
    Logger getLogger()
    {
        return operationLog;
    }

    private final boolean isValidCode(String code, Pattern pattern)
    {
        if (StringUtils.isEmpty(code))
        {
            return false;
        }
        Matcher m = pattern.matcher(code);
        return m.matches();
    }

}
