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

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.HibernateSearchContext;
import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.UuidUtil;

/**
 * Abstract test case for database related unit testing.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses =
    { AbstractDAO.class })
public abstract class AbstractDAOWithoutContextTest extends
        AbstractTransactionalTestNGSpringContextTests
{
    static
    {
        TestInitializer.init();
    }

    static final Long ANOTHER_DATABASE_INSTANCE_ID = new Long(2);

    static final String EXCEED_CODE_LENGTH_CHARACTERS =
            StringUtils.repeat("A", Code.CODE_LENGTH_MAX + 1);

    protected IDAOFactory daoFactory;

    protected SessionFactory sessionFactory;

    HibernateSearchContext hibernateSearchContext;

    private Long origDatabaseInstanceId;

    private Object currentDAO;

    @BeforeMethod(alwaysRun = true)
    public void setUp()
    {
        createAnotherDatabaseInstanceId();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown()
    {
        if (currentDAO != null)
        {
            resetDatabaseInstanceId(currentDAO);
        }
    }

    /**
     * Sets <code>daoFactory</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setDaoFactory(final IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    /**
     * Sets <code>hibernate session factory</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setHibernateSessionFactory(final SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Sets <code>hibernate search context</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setHibernateSearchContext(final HibernateSearchContext hibernateSearchContext)
    {
        this.hibernateSearchContext = hibernateSearchContext;
    }

    /**
     * Changes the database instance id of given {@link AbstractDAO} to a new value.
     */
    final void changeDatabaseInstanceId(final Object dao)
    {
        assertNull(origDatabaseInstanceId);
        assertTrue(dao instanceof AbstractDAO);
        final DatabaseInstancePE databaseInstance = getDatabaseInstanceReference(dao);
        origDatabaseInstanceId = databaseInstance.getId();
        databaseInstance.setId(ANOTHER_DATABASE_INSTANCE_ID);
        currentDAO = dao;
    }

    private DatabaseInstancePE getDatabaseInstanceReference(final Object dao)
    {
        final AbstractDAO abstractDAO = (AbstractDAO) dao;
        final DatabaseInstancePE databaseInstance = abstractDAO.getDatabaseInstance();
        assertNotNull(databaseInstance);
        return databaseInstance;
    }

    /**
     * Resets the database instance id of given {@link AbstractDAO} to its original value.
     */
    final void resetDatabaseInstanceId(final Object dao)
    {
        assertTrue(dao instanceof AbstractDAO);
        assertNotNull(origDatabaseInstanceId);
        final DatabaseInstancePE databaseInstance = getDatabaseInstanceReference(dao);
        databaseInstance.setId(origDatabaseInstanceId);
        origDatabaseInstanceId = null;
        currentDAO = null;
    }

    /**
     * Creates <code>ANOTHER_DATABASE_INSTANCE_ID</code> in the database if needed.
     */
    private final Long createAnotherDatabaseInstanceId()
    {
        try
        {
            return simpleJdbcTemplate.queryForLong(String.format("select id from %s where id = ?",
                    TableNames.DATABASE_INSTANCES_TABLE), ANOTHER_DATABASE_INSTANCE_ID);
        } catch (final DataAccessException ex)
        {
            simpleJdbcTemplate.update(String.format(
                    "insert into %s (id, code, uuid) values (?, ?, ?)",
                    TableNames.DATABASE_INSTANCES_TABLE), ANOTHER_DATABASE_INSTANCE_ID,
                    "MY_INSTANCE", UuidUtil.generateUUID());
            return ANOTHER_DATABASE_INSTANCE_ID;
        }
    }

    protected PersonPE getSystemPerson()
    {
        return getPerson("system");
    }

    protected PersonPE getTestPerson()
    {
        return getPerson("test");
    }

    protected PersonPE getPerson(final String userID)
    {
        final PersonPE person = daoFactory.getPersonDAO().tryFindPersonByUserId(userID);
        assertNotNull("Person '" + userID + "' does not exists.", person);
        return person;
    }

    protected DatabaseInstancePE createDatabaseInstance(final String databaseInstanceCode)
    {
        final DatabaseInstancePE databaseInstance = new DatabaseInstancePE();
        databaseInstance.setCode(databaseInstanceCode);
        daoFactory.getDatabaseInstanceDAO().createDatabaseInstance(databaseInstance);
        return databaseInstance;
    }

    protected GroupPE createGroup(final String groupCode)
    {
        final DatabaseInstancePE databaseInstance = daoFactory.getHomeDatabaseInstance();
        return createGroup(groupCode, databaseInstance);
    }

    protected GroupPE createGroup(final String groupCode, final DatabaseInstancePE databaseInstance)
    {
        final GroupPE group = new GroupPE();
        group.setCode(groupCode);
        group.setDatabaseInstance(databaseInstance);
        group.setRegistrator(getSystemPerson());
        daoFactory.getGroupDAO().createGroup(group);
        return group;
    }

    protected ExternalDataPE findExternalData(String code)
    {
        final IExternalDataDAO externalDataDAO = daoFactory.getExternalDataDAO();
        ExternalDataPE externalData = externalDataDAO.tryToFindFullDataSetByCode(code, true, false);

        assertNotNull(externalData);

        return externalData;
    }

    protected ProjectPE findProject(String db, String group, String project)
    {
        return daoFactory.getProjectDAO().tryFindProject(db, group, project);
    }

    protected ExperimentTypePE findExperimentType(String expType)
    {
        return (ExperimentTypePE) daoFactory.getEntityTypeDAO(EntityKind.EXPERIMENT)
                .tryToFindEntityTypeByCode(expType);
    }

    protected ExperimentPE createExperiment(String db, String group, String project,
            String expCode, String expType)
    {
        final ExperimentPE result = new ExperimentPE();
        result.setCode(expCode);
        result.setPermId(daoFactory.getPermIdDAO().createPermId());
        result.setExperimentType(findExperimentType(expType));
        result.setProject(findProject(db, group, project));
        result.setRegistrator(getTestPerson());
        result.setRegistrationDate(new Date());
        return result;
    }

    protected MaterialPE createMaterial(MaterialTypePE type, String code)
    {
        final MaterialPE material = new MaterialPE();
        material.setCode(code);
        material.setMaterialType(type);
        material.setRegistrationDate(new Date());
        material.setRegistrator(getSystemPerson());
        material.setDatabaseInstance(daoFactory.getHomeDatabaseInstance());
        return material;
    }

    protected EntityTypePropertyTypePE createAssignment(EntityKind entityKind,
            EntityTypePE entityType, PropertyTypePE propertyType)
    {
        final PersonPE registrator = getTestPerson();
        EntityTypePropertyTypePE result =
                EntityTypePropertyTypePE.createEntityTypePropertyType(entityKind);
        result.setEntityType(entityType);
        result.setPropertyType(propertyType);
        result.setRegistrator(registrator);
        result.setRegistrationDate(new Date());
        result.setOrdinal(1L);
        return result;
    }

    protected final PropertyTypePE createPropertyType(final DataTypePE dataType, final String code,
            final VocabularyPE vocabularyOrNull, final MaterialTypePE materialTypeOrNull)
    {
        final PropertyTypePE propertyTypePE = new PropertyTypePE();
        propertyTypePE.setCode(code);
        propertyTypePE.setLabel(code);
        propertyTypePE.setDescription(code);
        propertyTypePE.setRegistrator(getSystemPerson());
        propertyTypePE.setType(dataType);
        propertyTypePE.setDatabaseInstance(daoFactory.getHomeDatabaseInstance());
        if (DataTypeCode.CONTROLLEDVOCABULARY.equals(dataType.getCode()))
        {
            assertNotNull(vocabularyOrNull);
            propertyTypePE.setVocabulary(vocabularyOrNull);
        }
        if (DataTypeCode.MATERIAL.equals(dataType.getCode()))
        {
            propertyTypePE.setMaterialType(materialTypeOrNull);
        }
        return propertyTypePE;
    }

    protected PropertyTypePE selectFirstPropertyType()
    {
        List<PropertyTypePE> propertyTypes = daoFactory.getPropertyTypeDAO().listPropertyTypes();
        Assert.assertTrue(propertyTypes.size() > 0);
        Collections.sort(propertyTypes);
        PropertyTypePE result = propertyTypes.get(0);
        return result;
    }

    /**
     * Returns the first experiment found in the database.
     */
    protected ExperimentPE selectFirstExperiment()
    {

        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experiments);
        return experiments.get(0);
    }

    /**
     * Returns the type of first experiment found in the database.
     */
    protected ExperimentTypePE selectFirstExperimentType()
    {
        return selectFirstExperiment().getExperimentType();
    }

    protected static void assertEqualsOrGreater(int minimalSize, int actualSize)
    {
        if (actualSize < minimalSize)
        {
            fail("At least " + minimalSize + " items expected, but only " + actualSize + " found.");
        }
    }

    protected AuthorizationGroupPE createAuthorizationGroup(String code, String desc)
    {
        AuthorizationGroupPE result = new AuthorizationGroupPE();
        result.setCode(code);
        result.setDescription(desc);
        result.setDatabaseInstance(daoFactory.getHomeDatabaseInstance());
        result.setRegistrator(getSystemPerson());
        return result;
    }
}
