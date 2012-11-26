/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISpaceDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Franz-Josef Elmer
 */
public class EntityExistenceCheckerTest extends AssertJUnit
{
    private static final String DATABASE_INSTANCE_CODE = "MY-DB";

    private Mockery context;

    private ISpaceDAO spaceDAO;

    private IProjectDAO projectDAO;

    private IExperimentDAO experimentDAO;

    private ISampleDAO sampleDAO;

    private IEntityTypeDAO materialTypeDAO;

    private ISampleTypeDAO sampleTypeDAO;

    private EntityExistenceChecker checker;

    private DatabaseInstancePE databaseInstance;

    @BeforeMethod
    public void setUp()
    {
        LogInitializer.init();
        context = new Mockery();
        final IDAOFactory daoFactory = context.mock(IDAOFactory.class);
        spaceDAO = context.mock(ISpaceDAO.class);
        projectDAO = context.mock(IProjectDAO.class);
        experimentDAO = context.mock(IExperimentDAO.class);
        sampleDAO = context.mock(ISampleDAO.class);
        materialTypeDAO = context.mock(IEntityTypeDAO.class);
        sampleTypeDAO = context.mock(ISampleTypeDAO.class);
        databaseInstance = new DatabaseInstancePE();
        databaseInstance.setCode(DATABASE_INSTANCE_CODE);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getHomeDatabaseInstance();
                    will(returnValue(databaseInstance));

                    allowing(daoFactory).getSpaceDAO();
                    will(returnValue(spaceDAO));

                    allowing(daoFactory).getProjectDAO();
                    will(returnValue(projectDAO));

                    allowing(daoFactory).getExperimentDAO();
                    will(returnValue(experimentDAO));

                    allowing(daoFactory).getSampleDAO();
                    will(returnValue(sampleDAO));

                    allowing(daoFactory).getEntityTypeDAO(EntityKind.MATERIAL);
                    will(returnValue(materialTypeDAO));

                    allowing(daoFactory).getSampleTypeDAO();
                    will(returnValue(sampleTypeDAO));
                }
            });
        checker = new EntityExistenceChecker(daoFactory);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testCheckNewMaterials()
    {
        MaterialType type = new MaterialType();
        type.setCode("T1");
        prepareForAssertMaterialTypeExists(type.getCode(), materialType("ALPHA", "BETA"));

        checker.checkNewMaterials(Arrays.asList(new NewMaterialsWithTypes(type, Arrays.asList(
                material("A", "alpha:12", "beta:42"), material("B", "BETa:47", "Alpha:11")))));

        assertThat(checker.getErrors().size(), is(0));
    }

    @Test
    public void testCheckNewMaterialsWithUnknownType()
    {
        MaterialType type = new MaterialType();
        type.setCode("T1");
        prepareForAssertMaterialTypeExists(type.getCode(), null);

        checker.checkNewMaterials(Arrays.asList(new NewMaterialsWithTypes(type, Arrays
                .asList(material("A", "alpha:12", "beta:42")))));

        assertThat(checker.getErrors(), containsExactly("Unknown material type: T1"));
    }

    @Test
    public void testCheckNewMaterialsWithUnknownPropertyType()
    {
        MaterialType type = new MaterialType();
        type.setCode("T1");
        prepareForAssertMaterialTypeExists(type.getCode(), materialType("ALPHA"));

        checker.checkNewMaterials(Arrays.asList(new NewMaterialsWithTypes(type, Arrays
                .asList(material("A", "alpha:12", "beta:42")))));

        assertThat(checker.getErrors(),
                containsExactly("Material type T1 has no property type BETA assigned."));
    }

    @Test
    public void testCheckNewSamplesOnSpaceLevel()
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode("T1");
        prepareForAssertSampleTypeExists(sampleType.getCode(), sampleType("ALPHA", "BETA"));
        context.checking(new Expectations()
            {
                {
                    one(projectDAO).tryFindProject(DATABASE_INSTANCE_CODE, "S1", "P1");
                    ProjectPE project = new ProjectPE();
                    project.setCode("P1");
                    SpacePE space = new SpacePE();
                    space.setCode("S1");
                    project.setSpace(space);
                    will(returnValue(project));

                    one(experimentDAO).tryFindByCodeAndProject(project, "E1");
                    will(returnValue(new ExperimentPE()));

                    one(spaceDAO).tryFindSpaceByCodeAndDatabaseInstance("S1", databaseInstance);
                    will(returnValue(space));

                    one(sampleDAO).tryFindByCodeAndSpace("PLATE", space);
                    SamplePE sample = new SamplePE();
                    sample.setCode("PLATE");
                    will(returnValue(sample));
                }
            });

        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays.asList(
                sample("/S1/A", "/S1/P1/E1", "/S1/PLATE", "alpha:12"),
                sample("/S1/B", "/S1/P1/E1", "/S1/PLATE", "Beta:42")))));

        assertThat(checker.getErrors().size(), is(0));
    }

    @Test
    public void testCheckNewSamplesOnDatabaseInstanceLevel()
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode("T1");
        prepareForAssertSampleTypeExists(sampleType.getCode(), sampleType("ALPHA", "BETA"));
        context.checking(new Expectations()
            {
                {
                    one(projectDAO).tryFindProject(DATABASE_INSTANCE_CODE, "S1", "P1");
                    ProjectPE project = new ProjectPE();
                    project.setCode("P1");
                    SpacePE space = new SpacePE();
                    space.setCode("S1");
                    project.setSpace(space);
                    will(returnValue(project));

                    one(experimentDAO).tryFindByCodeAndProject(project, "E1");
                    will(returnValue(new ExperimentPE()));

                    one(sampleDAO).tryFindByCodeAndDatabaseInstance("PLATE", databaseInstance);
                    SamplePE sample = new SamplePE();
                    sample.setCode("PLATE");
                    will(returnValue(sample));
                }
            });

        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays.asList(
                sample("A", "/S1/P1/E1", "/PLATE", "alpha:12"),
                sample("B", "/S1/P1/E1", "/PLATE", "Beta:42")))));

        assertThat(checker.getErrors().size(), is(0));
    }

    @Test
    public void testCheckNewSamplesWithContainerAndContainedSamples()
    {
        SampleType containerType = new SampleType();
        containerType.setCode("PLATE");
        prepareForAssertSampleTypeExists(containerType.getCode(), sampleType("ALPHA"));
        SampleType containedType = new SampleType();
        containedType.setCode("WELL");
        prepareForAssertSampleTypeExists(containedType.getCode(), sampleType("BETA"));
        context.checking(new Expectations()
            {
                {
                    one(projectDAO).tryFindProject(DATABASE_INSTANCE_CODE, "S1", "P1");
                    ProjectPE project = new ProjectPE();
                    project.setCode("P1");
                    SpacePE space = new SpacePE();
                    space.setCode("S1");
                    project.setSpace(space);
                    will(returnValue(project));

                    one(experimentDAO).tryFindByCodeAndProject(project, "E1");
                    will(returnValue(new ExperimentPE()));
                }
            });

        NewSamplesWithTypes containers =
                new NewSamplesWithTypes(containerType, Arrays.asList(sample("PLATE", "/S1/P1/E1",
                        null, "alpha:12")));
        NewSamplesWithTypes contained =
                new NewSamplesWithTypes(containedType, Arrays.asList(
                        sample("A1", null, "/S1/PLATE", "beta:12"),
                        sample("A2", null, "/S1/PLATE", "beta:42")));
        checker.checkNewSamples(Arrays.asList(containers, contained));

        assertThat(checker.getErrors().size(), is(0));
    }

    @Test
    public void testCheckNewSamplesWithUnknownType()
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode("T1");
        prepareForAssertSampleTypeExists(sampleType.getCode(), null);

        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays
                .asList(sample("/S1/A", null, null, "alpha:12")))));

        assertThat(checker.getErrors(), containsExactly("Unknown sample type: T1"));
    }

    @Test
    public void testCheckNewSamplesWithUnknownExperiment()
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode("T1");
        prepareForAssertSampleTypeExists(sampleType.getCode(), sampleType("ALPHA"));
        context.checking(new Expectations()
            {
                {
                    one(projectDAO).tryFindProject(DATABASE_INSTANCE_CODE, "S1", "P1");
                    ProjectPE project = new ProjectPE();
                    project.setCode("P1");
                    SpacePE space = new SpacePE();
                    space.setCode("S1");
                    project.setSpace(space);
                    will(returnValue(project));

                    one(experimentDAO).tryFindByCodeAndProject(project, "E1");
                }
            });

        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays
                .asList(sample("A", "/S1/P1/E1", null, "alpha:12")))));

        assertThat(checker.getErrors(), containsExactly("Unknown experiment: /S1/P1/E1"));
    }

    @Test
    public void testCheckNewSamplesWithUnknownExperimentBecauseOfUnknownProject()
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode("T1");
        prepareForAssertSampleTypeExists(sampleType.getCode(), sampleType("ALPHA"));
        context.checking(new Expectations()
            {
                {
                    one(projectDAO).tryFindProject(DATABASE_INSTANCE_CODE, "S1", "P1");
                }
            });

        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays
                .asList(sample("A", "/S1/P1/E1", null, "alpha:12")))));

        assertThat(checker.getErrors(),
                containsExactly("Unknown experiment because of unknown project: /S1/P1/E1"));
    }

    @Test
    public void testCheckNewSamplesWithUnknownSpace()
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode("T1");
        prepareForAssertSampleTypeExists(sampleType.getCode(), sampleType("ALPHA"));
        context.checking(new Expectations()
            {
                {
                    one(spaceDAO).tryFindSpaceByCodeAndDatabaseInstance("S1", databaseInstance);
                }
            });

        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays
                .asList(sample("A", null, "/S1/PLATE", "alpha:12")))));

        assertThat(checker.getErrors(), containsExactly("Unknown space: /S1"));
    }

    @Test
    public void testCheckNewSamplesWithUnknownContainerSample()
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode("T1");
        prepareForAssertSampleTypeExists(sampleType.getCode(), sampleType("ALPHA"));
        context.checking(new Expectations()
            {
                {
                    one(sampleDAO).tryFindByCodeAndDatabaseInstance("PLATE", databaseInstance);
                }
            });

        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays
                .asList(sample("A", null, "/PLATE", "alpha:12")))));

        assertThat(checker.getErrors(), containsExactly("Unknown sample: /PLATE"));
    }

    @Test
    public void testCheckNewSamplesWithUnknownPropertyType()
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode("T1");
        prepareForAssertSampleTypeExists(sampleType.getCode(), sampleType());

        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays
                .asList(sample("/S1/A", null, null, "alpha:12")))));

        assertThat(checker.getErrors(),
                containsExactly("Sample type T1 has no property type ALPHA assigned."));
    }

    @Test
    public void multipleErrors()
    {
        final SampleType sampleType = new SampleType();
        sampleType.setCode("T1");

        context.checking(new Expectations()
            {
                {
                    allowing(sampleTypeDAO).tryFindSampleTypeByCode("T1");
                    will(returnValue(sampleType()));
                    allowing(sampleTypeDAO).tryFindSampleTypeByCode("ALPHA");
                    will(returnValue(sampleType("ALPHA")));
                    one(sampleDAO).tryFindByCodeAndDatabaseInstance("PLATE", databaseInstance);

                }
            });

        checker.checkNewSamples(Arrays.asList(new NewSamplesWithTypes(sampleType, Arrays
                .asList(sample("/S1/A", null, null, "alpha:12"), sample("A", null, "/PLATE",
                        "alpha:12")))));

        assertThat(checker.getErrors(),
                containsExactly("Sample type T1 has no property type ALPHA assigned.",
                        "Unknown sample: /PLATE"));
    }

    private NewMaterial material(String code, String... properties)
    {
        NewMaterial newMaterial = new NewMaterial();
        newMaterial.setCode(code);
        newMaterial.setProperties(createProperties(properties));
        return newMaterial;
    }

    private MaterialTypePE materialType(String... propertyTypes)
    {
        MaterialTypePE type = new MaterialTypePE();
        List<MaterialTypePropertyTypePE> list = new ArrayList<MaterialTypePropertyTypePE>();
        for (String propertyType : propertyTypes)
        {
            MaterialTypePropertyTypePE materialTypePropertyType = new MaterialTypePropertyTypePE();
            PropertyTypePE propertyTypePE = new PropertyTypePE();
            propertyTypePE.setCode(propertyType);
            materialTypePropertyType.setPropertyType(propertyTypePE);
            list.add(materialTypePropertyType);
        }
        type.setMaterialTypePropertyTypes(new HashSet<MaterialTypePropertyTypePE>(list));
        return type;
    }

    private void prepareForAssertMaterialTypeExists(final String materialTypeCode,
            final MaterialTypePE materialTypeOrNull)
    {
        context.checking(new Expectations()
            {
                {
                    one(materialTypeDAO).tryToFindEntityTypeByCode(materialTypeCode);
                    will(returnValue(materialTypeOrNull));
                }
            });
    }

    private NewSample sample(String sampleIdentifier, String experimentIdentifierOrNull,
            String containerIdentifierOrNull, String... properties)
    {
        NewSample newSample = new NewSample();
        newSample.setIdentifier(sampleIdentifier);
        newSample.setExperimentIdentifier(experimentIdentifierOrNull);
        newSample.setContainerIdentifier(containerIdentifierOrNull);
        newSample.setProperties(createProperties(properties));
        return newSample;
    }

    private SampleTypePE sampleType(String... propertyTypes)
    {
        SampleTypePE type = new SampleTypePE();
        List<SampleTypePropertyTypePE> list = new ArrayList<SampleTypePropertyTypePE>();
        for (String propertyType : propertyTypes)
        {
            SampleTypePropertyTypePE sampleTypePropertyType = new SampleTypePropertyTypePE();
            PropertyTypePE propertyTypePE = new PropertyTypePE();
            propertyTypePE.setCode(propertyType);
            sampleTypePropertyType.setPropertyType(propertyTypePE);
            list.add(sampleTypePropertyType);
        }
        type.setSampleTypePropertyTypes(new HashSet<SampleTypePropertyTypePE>(list));
        return type;
    }

    private void prepareForAssertSampleTypeExists(final String sampleTypeCode,
            final SampleTypePE sampleTypeOrNull)
    {
        context.checking(new Expectations()
            {
                {
                    one(sampleTypeDAO).tryFindSampleTypeByCode(sampleTypeCode);
                    will(returnValue(sampleTypeOrNull));
                }
            });
    }

    private IEntityProperty[] createProperties(String... properties)
    {
        IEntityProperty[] entityProperties = new IEntityProperty[properties.length];
        for (int i = 0; i < properties.length; i++)
        {
            String[] keyAndValue = properties[i].split(":");
            EntityProperty property = new EntityProperty();
            PropertyType propertyType = new PropertyType();
            propertyType.setCode(keyAndValue[0]);
            property.setPropertyType(propertyType);
            property.setValue(keyAndValue[1]);
            entityProperties[i] = property;
        }
        return entityProperties;
    }

    private <T> Matcher<Collection<T>> containsExactly(T... t)
    {
        return new CollectionContainsExactlyMatcher<T>(t);
    }

    private static class CollectionContainsExactlyMatcher<T> extends TypeSafeMatcher<Collection<T>>
    {

        private List<T> expected;

        public CollectionContainsExactlyMatcher(T... expected)
        {
            this.expected = Arrays.asList(expected);
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("A collection containing exactly items " + expected.toString());
        }

        @Override
        public boolean matchesSafely(Collection<T> collection)
        {
            return collection.containsAll(expected) && expected.containsAll(collection);
        }

    }

}
