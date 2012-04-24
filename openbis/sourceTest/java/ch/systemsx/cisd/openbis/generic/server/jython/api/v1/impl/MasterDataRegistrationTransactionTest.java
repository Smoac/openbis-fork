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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl;

import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.EntityKind;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IDataSetType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IExperimentType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IFileFormatType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMaterialType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IPropertyAssignment;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IPropertyType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.ISampleType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.MaterialTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleTypeBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class MasterDataRegistrationTransactionTest extends AssertJUnit
{
    private static final String KNOWN_PROPERTY = "DESCRIPTION";

    private static final String SEESION_TOKEN = "seesion-token";

    private static final String KNOWN = "KNOWN";

    private static final String UNKNOWN = "UNKNOWN";

    private Mockery context;

    private ICommonServer server;

    private MasterDataRegistrationTransaction transaction;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        server = context.mock(ICommonServer.class);
        transaction =
                new MasterDataRegistrationTransaction(EncapsulatedCommonServer.create(server,
                        SEESION_TOKEN));
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testGetOrCreateExperimentTypeOfKnownType()
    {
        prepareListExperimentTypes();

        IExperimentType type = transaction.getOrCreateNewExperimentType(KNOWN);
        type.setDescription("description");

        assertEquals(KNOWN, type.getCode());
        assertEquals(null, type.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetOrCreateExperimentTypeOfUnknownType()
    {
        prepareListExperimentTypes();

        IExperimentType type = transaction.getOrCreateNewExperimentType(UNKNOWN);
        type.setDescription("description");

        assertEquals(UNKNOWN, type.getCode());
        assertEquals("description", type.getDescription());
        context.assertIsSatisfied();
    }

    private void prepareListExperimentTypes()
    {
        context.checking(new Expectations()
            {
                {
                    ExperimentType type =
                            new ExperimentTypeBuilder().code(KNOWN).getExperimentType();
                    one(server).listExperimentTypes(SEESION_TOKEN);
                    will(returnValue(Arrays.asList(type)));
                }
            });
    }

    @Test
    public void testGetOrCreateSampleTypeOfKnownType()
    {
        prepareListSampleTypes();

        ISampleType type = transaction.getOrCreateNewSampleType(KNOWN);
        type.setDescription("description");

        assertEquals(KNOWN, type.getCode());
        assertEquals(null, type.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetOrCreateSampleTypeOfUnknownType()
    {
        prepareListSampleTypes();

        ISampleType type = transaction.getOrCreateNewSampleType(UNKNOWN);
        type.setDescription("description");

        assertEquals(UNKNOWN, type.getCode());
        assertEquals("description", type.getDescription());
        context.assertIsSatisfied();
    }

    private void prepareListSampleTypes()
    {
        context.checking(new Expectations()
            {
                {
                    SampleType type = new SampleTypeBuilder().code(KNOWN).getSampleType();
                    one(server).listSampleTypes(SEESION_TOKEN);
                    will(returnValue(Arrays.asList(type)));
                }
            });
    }

    @Test
    public void testGetOrCreateDataSetTypeOfKnownType()
    {
        prepareListDataSetTypes();

        IDataSetType type = transaction.getOrCreateNewDataSetType(KNOWN);
        type.setDescription("description");

        assertEquals(KNOWN, type.getCode());
        assertEquals(null, type.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetOrCreateDataSetTypeOfUnknownType()
    {
        prepareListDataSetTypes();

        IDataSetType type = transaction.getOrCreateNewDataSetType(UNKNOWN);
        type.setDescription("description");

        assertEquals(UNKNOWN, type.getCode());
        assertEquals("description", type.getDescription());
        context.assertIsSatisfied();
    }

    private void prepareListDataSetTypes()
    {
        context.checking(new Expectations()
            {
                {

                    DataSetType type = new DataSetTypeBuilder().code(KNOWN).getDataSetType();
                    one(server).listDataSetTypes(SEESION_TOKEN);
                    will(returnValue(Arrays.asList(type)));
                }
            });
    }

    @Test
    public void testGetOrCreateMaterialTypeOfKnownType()
    {
        prepareListMaterialTypes();

        IMaterialType type = transaction.getOrCreateNewMaterialType(KNOWN);
        type.setDescription("description");

        assertEquals(KNOWN, type.getCode());
        assertEquals(null, type.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetOrCreateMaterialTypeOfUnknownType()
    {
        prepareListMaterialTypes();

        IMaterialType type = transaction.getOrCreateNewMaterialType(UNKNOWN);
        type.setDescription("description");

        assertEquals(UNKNOWN, type.getCode());
        assertEquals("description", type.getDescription());
        context.assertIsSatisfied();
    }

    private void prepareListMaterialTypes()
    {
        context.checking(new Expectations()
            {
                {

                    MaterialType type = new MaterialTypeBuilder().code(KNOWN).getMaterialType();
                    one(server).listMaterialTypes(SEESION_TOKEN);
                    will(returnValue(Arrays.asList(type)));
                }
            });
    }

    @Test
    public void testGetOrCreateFileFormatTypeOfKnownType()
    {
        prepareListFileFormatTypes();

        IFileFormatType type = transaction.getOrCreateNewFileFormatType(KNOWN);
        type.setDescription("description");

        assertEquals(KNOWN, type.getCode());
        assertEquals(null, type.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetOrCreateFileFormatTypeOfUnknownType()
    {
        prepareListFileFormatTypes();

        IFileFormatType type = transaction.getOrCreateNewFileFormatType(UNKNOWN);
        type.setDescription("description");

        assertEquals(UNKNOWN, type.getCode());
        assertEquals("description", type.getDescription());
        context.assertIsSatisfied();
    }

    private void prepareListFileFormatTypes()
    {
        context.checking(new Expectations()
            {
                {
                    FileFormatType type = new FileFormatType(KNOWN);
                    one(server).listFileFormatTypes(SEESION_TOKEN);
                    will(returnValue(Arrays.asList(type)));
                }
            });
    }

    @Test
    public void testGetOrCreatePropertyTypeOfKnownType()
    {
        prepareListPropertyTypes();

        IPropertyType type = transaction.getOrCreateNewPropertyType(KNOWN, DataType.INTEGER);
        type.setDescription("description");

        assertEquals(KNOWN, type.getCode());
        assertEquals(DataType.VARCHAR, type.getDataType());
        assertEquals(null, type.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetOrCreatePropertyTypeOfUnknownType()
    {
        prepareListPropertyTypes();

        IPropertyType type = transaction.getOrCreateNewPropertyType(UNKNOWN, DataType.INTEGER);
        type.setDescription("description");

        assertEquals(UNKNOWN, type.getCode());
        assertEquals(DataType.INTEGER, type.getDataType());
        assertEquals("description", type.getDescription());
        context.assertIsSatisfied();
    }

    private void prepareListPropertyTypes()
    {
        context.checking(new Expectations()
            {
                {
                    PropertyType type = new PropertyType();
                    type.setCode(KNOWN);
                    type.setDataType(new ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType(
                            DataTypeCode.VARCHAR));
                    one(server).listPropertyTypes(SEESION_TOKEN, false);
                    will(returnValue(Arrays.asList(type)));
                }
            });
    }

    @Test
    public void testGetOrCreateKnownVocabulary()
    {
        prepareListVocabularys();

        IVocabulary vocabulary = transaction.getOrCreateNewVocabulary(KNOWN);
        vocabulary.setDescription("description");

        assertEquals(KNOWN, vocabulary.getCode());
        assertEquals(null, vocabulary.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetOrCreateUnknownVocabulary()
    {
        prepareListVocabularys();

        IVocabulary vocabulary = transaction.getOrCreateNewVocabulary(UNKNOWN);
        vocabulary.setDescription("description");

        assertEquals(UNKNOWN, vocabulary.getCode());
        assertEquals("description", vocabulary.getDescription());
        context.assertIsSatisfied();
    }

    private void prepareListVocabularys()
    {
        context.checking(new Expectations()
            {
                {
                    Vocabulary vocabulary = new Vocabulary();
                    vocabulary.setCode(KNOWN);
                    one(server).listVocabularies(SEESION_TOKEN, true, false);
                    will(returnValue(Arrays.asList(vocabulary)));
                }
            });
    }

    @Test
    public void testAssignPropertyTypeWithExistingAssigment()
    {
        prepareListAssigments();

        IPropertyAssignment assigment =
                transaction.assignPropertyType(new SampleTypeImmutable(KNOWN),
                        new PropertyTypeImmutable(KNOWN_PROPERTY, DataType.VARCHAR));
        assigment.setSection("top");

        assertEquals(KNOWN, assigment.getEntityTypeCode());
        assertEquals(KNOWN_PROPERTY, assigment.getPropertyTypeCode());
        assertEquals(EntityKind.SAMPLE, assigment.getEntityKind());
        assertEquals(null, assigment.getSection());
        context.assertIsSatisfied();
    }

    @Test
    public void testAssignPropertyType()
    {
        prepareListAssigments();

        IPropertyAssignment assigment =
                transaction.assignPropertyType(new SampleTypeImmutable(UNKNOWN),
                        new PropertyTypeImmutable(KNOWN_PROPERTY, DataType.VARCHAR));
        assigment.setSection("top");

        assertEquals(UNKNOWN, assigment.getEntityTypeCode());
        assertEquals(KNOWN_PROPERTY, assigment.getPropertyTypeCode());
        assertEquals(EntityKind.SAMPLE, assigment.getEntityKind());
        assertEquals("top", assigment.getSection());
        context.assertIsSatisfied();
    }

    private void prepareListAssigments()
    {
        context.checking(new Expectations()
            {
                {
                    List<SampleTypePropertyType> assigments =
                            new SampleTypeBuilder()
                                    .code(KNOWN)
                                    .propertyType(KNOWN_PROPERTY, "Description",
                                            DataTypeCode.VARCHAR).getSampleType()
                                    .getAssignedPropertyTypes();
                    one(server).listEntityTypePropertyTypes(SEESION_TOKEN);
                    will(returnValue(assigments));
                }
            });
    }

}
