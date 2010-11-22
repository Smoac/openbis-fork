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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.DynamicPropertyEvaluationOperation;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentWithContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewBasicExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperimentsWithType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTypeTranslator;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * Test cases for corresponding {@link GenericServer} class.
 * 
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = GenericServer.class)
public final class GenericServerTest extends AbstractServerTestCase
{
    private static final String EXPERIMENT_IDENTIFIER1 = "/SPACE/PROJECT/EXP1";

    private static final String EXPERIMENT_IDENTIFIER2 = "/SPACE/PROJECT/EXP2";

    private static final String EXPERIMENT_TYPE = "EXP_TYPE";

    private static final String PROJECT_1 = "PROJECT-1";

    private static final String GROUP_1 = "GROUP-1";

    private static final String DATABASE_1 = "DATABASE-1";

    private IGenericBusinessObjectFactory genericBusinessObjectFactory;

    private ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin;

    private IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin;

    private ICommonServer commonServer;

    private final IGenericServer createServer()
    {
        GenericServer genericServer =
                new GenericServer(sessionManager, daoFactory, genericBusinessObjectFactory,
                        sampleTypeSlaveServerPlugin, dataSetTypeSlaveServerPlugin);
        genericServer.commonServer = commonServer;
        return genericServer;
    }

    private static final ExperimentTypePE createExperimentType(String code)
    {
        ExperimentTypePE experimentTypePE = new ExperimentTypePE();
        experimentTypePE.setCode(code);
        DatabaseInstancePE databaseInstance = new DatabaseInstancePE();
        databaseInstance.setCode("DB");
        experimentTypePE.setDatabaseInstance(databaseInstance);
        return experimentTypePE;
    }

    private static final ArrayList<NewBasicExperiment> createNewExperiments(
            NewBasicExperiment... basicExperiments)
    {
        return new ArrayList<NewBasicExperiment>(Arrays.asList(basicExperiments));
    }

    private final NewSample createNewSample(final String identifier)
    {
        final NewSample newSample = new NewSample();
        newSample.setIdentifier(identifier);
        return newSample;
    }

    private final NewMaterial createNewMaterial(final String code)
    {
        final NewMaterial material = new NewMaterial();
        material.setCode(code);
        return material;
    }

    //
    // AbstractServerTestCase
    //

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        genericBusinessObjectFactory = context.mock(IGenericBusinessObjectFactory.class);
        sampleTypeSlaveServerPlugin = context.mock(ISampleTypeSlaveServerPlugin.class);
        dataSetTypeSlaveServerPlugin = context.mock(IDataSetTypeSlaveServerPlugin.class);
        commonServer = context.mock(ICommonServer.class);
    }

    public final void testRegisterSample()
    {
        prepareGetSession();
        final NewSample newSample = new NewSample();
        context.checking(new Expectations()
            {
                {
                    one(genericBusinessObjectFactory).createSampleBO(SESSION);
                    will(returnValue(sampleBO));

                    one(sampleBO).define(newSample);
                    exactly(2).of(sampleBO).save();

                    final SamplePE sample = new SamplePE();
                    final Long id = 1L;
                    sample.setId(id);
                    allowing(sampleBO).getSample();
                    will((returnValue(sample)));

                    final DynamicPropertyEvaluationOperation operation =
                            DynamicPropertyEvaluationOperation.evaluate(SamplePE.class,
                                    Arrays.asList(id));
                    one(evaluator).scheduleUpdate(with(operation));
                }
            });
        createServer().registerSample(SESSION_TOKEN, newSample,
                Collections.<NewAttachment> emptyList());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetExperimentFileAttachment() throws Exception
    {
        prepareGetSession();
        final TechId experimentId = CommonTestUtils.TECH_ID;
        final AttachmentPE attachmentPE = CommonTestUtils.createAttachment();
        context.checking(new Expectations()
            {
                {
                    one(genericBusinessObjectFactory).createExperimentBO(SESSION);
                    will(returnValue(experimentBO));

                    one(experimentBO).loadDataByTechId(experimentId);

                    one(experimentBO).getExperimentFileAttachment(attachmentPE.getFileName(),
                            attachmentPE.getVersion());
                    will(returnValue(attachmentPE));

                }
            });
        final AttachmentWithContent attachment =
                createServer().getExperimentFileAttachment(SESSION_TOKEN, experimentId,
                        attachmentPE.getFileName(), attachmentPE.getVersion());
        assertEquals(attachmentPE.getFileName(), attachment.getFileName());
        assertEquals(attachmentPE.getAttachmentContent().getValue(), attachment.getContent());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSamplesWithoutExpectations()
    {
        prepareGetSession();
        final IGenericServer server = createServer();
        // Null values
        boolean fail = true;
        try
        {
            server.registerSamples(null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        // Empty collection
        server.registerSamples(SESSION_TOKEN, Collections.<NewSamplesWithTypes> emptyList());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSamplesWithDuplicatedNewSamples()
    {
        prepareGetSession();
        final IGenericServer server = createServer();
        final List<NewSample> newSamples = new ArrayList<NewSample>();
        newSamples.add(createNewSample("same"));
        newSamples.add(createNewSample("same"));
        List<NewSamplesWithTypes> samplesWithTypes = new ArrayList<NewSamplesWithTypes>();
        samplesWithTypes.add(new NewSamplesWithTypes(new SampleType(), newSamples));
        try
        {
            server.registerSamples(SESSION_TOKEN, samplesWithTypes);
            fail(String.format("'%s' expected.", UserFailureException.class));
        } catch (final UserFailureException ex)
        {
            // Nothing to do here.
        }
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterOrUpdateSamplesWithoutUpdate()
    {
        prepareGetSession();
        final SampleTypePE sampleTypePE = CommonTestUtils.createSampleType();
        final SampleType sampleType = new SampleType();
        sampleType.setCode(sampleTypePE.getCode());
        final List<NewSample> newSamples = new ArrayList<NewSample>();
        newSamples.add(createNewSample("one"));
        newSamples.add(createNewSample("two"));
        List<NewSamplesWithTypes> samplesWithTypes = new ArrayList<NewSamplesWithTypes>();
        samplesWithTypes.add(new NewSamplesWithTypes(sampleType, newSamples));
        context.checking(new Expectations()
            {
                {
                    one(sampleTypeDAO).tryFindSampleTypeByCode(sampleTypePE.getCode());
                    will(returnValue(sampleTypePE));

                    one(sampleTypeSlaveServerPlugin).registerSamples(SESSION, newSamples);
                }
            });
        createServer().registerOrUpdateSamples(SESSION_TOKEN, samplesWithTypes);
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSamples()
    {
        prepareGetSession();
        final SampleTypePE sampleTypePE = CommonTestUtils.createSampleType();
        final SampleType sampleType = new SampleType();
        sampleType.setCode(sampleTypePE.getCode());
        final List<NewSample> newSamples = new ArrayList<NewSample>();
        newSamples.add(createNewSample("one"));
        newSamples.add(createNewSample("two"));
        List<NewSamplesWithTypes> samplesWithTypes = new ArrayList<NewSamplesWithTypes>();
        samplesWithTypes.add(new NewSamplesWithTypes(sampleType, newSamples));
        context.checking(new Expectations()
            {
                {
                    one(sampleTypeDAO).tryFindSampleTypeByCode(sampleTypePE.getCode());
                    will(returnValue(sampleTypePE));

                    one(sampleTypeSlaveServerPlugin).registerSamples(SESSION, newSamples);
                }
            });
        createServer().registerSamples(SESSION_TOKEN, samplesWithTypes);
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterExperimentWithoutSamples()
    {
        prepareGetSession();
        final NewExperiment newExperiment = new NewExperiment();
        context.checking(new Expectations()
            {
                {
                    one(genericBusinessObjectFactory).createExperimentBO(SESSION);
                    will(returnValue(experimentBO));

                    one(experimentBO).define(newExperiment);
                    exactly(2).of(experimentBO).save();

                    final ExperimentPE experiment = new ExperimentPE();
                    final Long id = 1L;
                    experiment.setId(id);
                    allowing(experimentBO).getExperiment();
                    will((returnValue(experiment)));

                    final DynamicPropertyEvaluationOperation operation =
                            DynamicPropertyEvaluationOperation.evaluate(ExperimentPE.class,
                                    Arrays.asList(id));
                    one(evaluator).scheduleUpdate(with(operation));
                }
            });
        createServer().registerExperiment(SESSION_TOKEN, newExperiment,
                Collections.<NewAttachment> emptyList());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterExperimentWithSamples()
    {
        prepareGetSession();
        final String experimentTypeCode = "EXP-TYPE1";
        final String experimentCode = "EXP1";
        final String groupCode = "CISD";
        String sample1Code = "SAMPLE1";
        final String sample1 = createSampleIdentifier(groupCode, sample1Code);
        final String sampleIdentifier2 = "SAMPLE2";
        final SampleIdentifier sampleIdentifier2WithGroup =
                SampleIdentifierFactory.parse(sampleIdentifier2);
        sampleIdentifier2WithGroup.getSpaceLevel().setSpaceCode(groupCode);
        final SampleIdentifier sampleIdentifier1 = SampleIdentifierFactory.parse(sample1);
        final String[] samples =
            { sample1, sampleIdentifier2 };
        final ExperimentPE experimentPE =
                createExperiment(experimentTypeCode, experimentCode, groupCode);
        final Long id = 1L;
        experimentPE.setId(id);
        final NewExperiment newExperiment =
                createNewExperiment(experimentTypeCode, experimentCode, groupCode, samples);
        context.checking(new Expectations()
            {
                {
                    one(genericBusinessObjectFactory).createExperimentBO(SESSION);
                    will(returnValue(experimentBO));

                    one(experimentBO).define(newExperiment);
                    exactly(2).of(experimentBO).save();

                    allowing(experimentBO).getExperiment();
                    will(returnValue(experimentPE));

                    one(genericBusinessObjectFactory).createSampleBO(SESSION);
                    will(returnValue(sampleBO));
                    one(sampleBO).loadBySampleIdentifier(sampleIdentifier1);
                    one(sampleBO).setExperiment(experimentPE);

                    one(genericBusinessObjectFactory).createSampleBO(SESSION);
                    will(returnValue(sampleBO));
                    one(sampleBO).loadBySampleIdentifier(sampleIdentifier2WithGroup);
                    one(sampleBO).setExperiment(experimentPE);

                    final DynamicPropertyEvaluationOperation operation =
                            DynamicPropertyEvaluationOperation.evaluate(ExperimentPE.class,
                                    Arrays.asList(id));
                    one(evaluator).scheduleUpdate(with(operation));
                }
            });
        createServer().registerExperiment(SESSION_TOKEN, newExperiment,
                Collections.<NewAttachment> emptyList());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterExperimentWithSampleFromWronGroup()
    {
        prepareGetSession();
        final String experimentTypeCode = "EXP-TYPE1";
        final String experimentCode = "EXP1";
        final String groupCode = "CISD";
        String sample1Code = "SAMPLE1";
        final String sample1 = createSampleIdentifier("NOT_" + groupCode, sample1Code);
        final String[] samples =
            { sample1 };
        final ExperimentPE experimentPE =
                createExperiment(experimentTypeCode, experimentCode, groupCode);
        final NewExperiment newExperiment =
                createNewExperiment(experimentTypeCode, experimentCode, groupCode, samples);
        context.checking(new Expectations()
            {
                {
                    one(genericBusinessObjectFactory).createExperimentBO(SESSION);
                    will(returnValue(experimentBO));

                    one(experimentBO).define(newExperiment);
                    exactly(2).of(experimentBO).save();
                    one(experimentBO).getExperiment();
                    will(returnValue(experimentPE));

                }
            });
        boolean exceptionThrown = false;
        try
        {
            createServer().registerExperiment(SESSION_TOKEN, newExperiment,
                    Collections.<NewAttachment> emptyList());
        } catch (UserFailureException e)
        {
            exceptionThrown = true;
            assertTrue(e.getMessage().contains(
                    "Sample '/NOT_CISD/SAMPLE1' does not belong to the space 'CISD'"));
        }
        assertTrue(exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterMaterials()
    {
        prepareGetSession();
        final MaterialTypePE materialTypePE = CommonTestUtils.createMaterialType();
        final List<NewMaterial> newMaterials = new ArrayList<NewMaterial>();
        newMaterials.add(createNewMaterial("one"));
        newMaterials.add(createNewMaterial("two"));
        final String typeCode = materialTypePE.getCode();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getEntityTypeDAO(EntityKind.MATERIAL);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(typeCode);
                    will(returnValue(materialTypePE));

                    one(genericBusinessObjectFactory).createMaterialTable(SESSION);
                    will(returnValue(materialTable));

                    one(materialTable).add(newMaterials, materialTypePE);
                    one(materialTable).save();
                }
            });
        createServer().registerMaterials(SESSION_TOKEN, typeCode, newMaterials);
        context.assertIsSatisfied();
    }

    @Test
    public final void testUpdateMaterials()
    {
        prepareGetSession();
        final MaterialTypePE materialTypePE = CommonTestUtils.createMaterialType();
        final NewMaterial m1 = createNewMaterial("M1");
        final NewMaterial m2 = createNewMaterial("M2");
        final String typeCode = materialTypePE.getCode();
        prepareMaterialUpdate(materialTypePE, false, m1, m2);

        int updateCount =
                createServer().updateMaterials(SESSION_TOKEN, typeCode, Arrays.asList(m1, m2),
                        false);

        assertEquals(2, updateCount);
        context.assertIsSatisfied();
    }

    @Test
    public final void testUpdateMaterialsFailedBecauseOfDuplicates()
    {
        prepareGetSession();
        final MaterialTypePE materialTypePE = CommonTestUtils.createMaterialType();
        final NewMaterial m1 = createNewMaterial("M1");
        final NewMaterial m2 = createNewMaterial("M1");
        final String typeCode = materialTypePE.getCode();

        try
        {
            createServer().updateMaterials(SESSION_TOKEN, typeCode, Arrays.asList(m1, m2), false);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Following material(s) '[M1]' are duplicated.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public final void testUpdateMaterialsIgnoreUnregistered()
    {
        prepareGetSession();
        final MaterialTypePE materialTypePE = CommonTestUtils.createMaterialType();
        final NewMaterial m1 = createNewMaterial("M1");
        final NewMaterial m2 = createNewMaterial("M2");
        final String typeCode = materialTypePE.getCode();
        prepareMaterialUpdate(materialTypePE, false, m1);

        int updateCount =
                createServer()
                        .updateMaterials(SESSION_TOKEN, typeCode, Arrays.asList(m1, m2), true);

        assertEquals(1, updateCount);
        context.assertIsSatisfied();
    }

    @Test
    public final void testUpdateMaterialsFailForUnregistered()
    {
        prepareGetSession();
        final MaterialTypePE materialTypePE = CommonTestUtils.createMaterialType();
        final NewMaterial m1 = createNewMaterial("M1");
        final NewMaterial m2 = createNewMaterial("M2");
        final String typeCode = materialTypePE.getCode();
        prepareMaterialUpdate(materialTypePE, true, m1);

        try
        {
            createServer().updateMaterials(SESSION_TOKEN, typeCode, Arrays.asList(m1, m2), false);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Can not update unregistered material 'M2'. "
                    + "Please use checkbox for ignoring unregistered materials.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    protected void prepareMaterialUpdate(final MaterialTypePE materialTypePE,
            final boolean doNotUpdate, final NewMaterial... materialsToBeRegistered)
    {
        context.checking(new Expectations()
            {
                {
                    List<Material> existingMaterials = new ArrayList<Material>();
                    List<MaterialUpdateDTO> updates = new ArrayList<MaterialUpdateDTO>();
                    for (NewMaterial material : materialsToBeRegistered)
                    {
                        Material m = createMaterial(material);
                        existingMaterials.add(m);
                        updates.add(createUpdateDTO(m, material));
                    }
                    existingMaterials.add(createMaterial(createNewMaterial("A")));

                    one(commonServer).listMaterials(with(SESSION_TOKEN),
                            with(new BaseMatcher<ListMaterialCriteria>()
                                {
                                    public boolean matches(Object item)
                                    {
                                        assertTrue(item instanceof ListMaterialCriteria);
                                        MaterialType materialType =
                                                ((ListMaterialCriteria) item).getMaterialType();
                                        assertEquals(materialTypePE.getCode(),
                                                materialType.getCode());
                                        return true;
                                    }

                                    public void describeTo(Description description)
                                    {
                                        description.appendText(materialTypePE.getCode());
                                    }
                                }), with(false));
                    will(returnValue(existingMaterials));

                    one(daoFactory).getEntityTypeDAO(EntityKind.MATERIAL);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(materialTypePE.getCode());
                    will(returnValue(materialTypePE));

                    if (doNotUpdate == false)
                    {
                        one(genericBusinessObjectFactory).createMaterialTable(SESSION);
                        will(returnValue(materialTable));

                        one(materialTable).update(updates);
                        one(materialTable).save();
                    }
                }

                private Material createMaterial(NewMaterial newMaterial)
                {
                    Material material = new Material();
                    material.setCode(newMaterial.getCode());
                    material.setId((long) newMaterial.getCode().hashCode());
                    material.setMaterialType(MaterialTypeTranslator.translateSimple(CommonTestUtils
                            .createMaterialType()));
                    return material;
                }

                private MaterialUpdateDTO createUpdateDTO(Material existingMaterial,
                        NewMaterial material)
                {
                    return new MaterialUpdateDTO(new TechId(existingMaterial.getId()), Arrays
                            .asList(material.getProperties()), existingMaterial
                            .getModificationDate());
                }
            });
    }

    @Test
    public void testEditMaterialNothingChanged() throws Exception
    {
        final TechId materialId = CommonTestUtils.TECH_ID;
        final List<IEntityProperty> properties = new ArrayList<IEntityProperty>();
        prepareGetSession();
        final Date version = new Date(1);
        final MaterialPE material = new MaterialPE();
        Date newModificationDate = new Date(2);
        material.setModificationDate(newModificationDate);
        context.checking(new Expectations()
            {
                {
                    one(genericBusinessObjectFactory).createMaterialBO(SESSION);
                    will(returnValue(materialBO));

                    one(materialBO).update(new MaterialUpdateDTO(materialId, properties, version));
                    one(materialBO).save();
                    one(materialBO).getMaterial();
                    will(returnValue(material));
                }
            });
        assertEquals(newModificationDate,
                createServer().updateMaterial(SESSION_TOKEN, materialId, properties, version));
        context.assertIsSatisfied();
    }

    public void testEditSampleNothingChanged() throws Exception
    {
        final TechId sampleId = CommonTestUtils.TECH_ID;
        final List<IEntityProperty> properties = new ArrayList<IEntityProperty>();
        prepareGetSession();
        final Date version = new Date();
        final Collection<NewAttachment> attachments = Collections.<NewAttachment> emptyList();
        final Long id = 1L;
        final SamplePE sample = new SamplePE();
        sample.setId(id);
        Set<SampleRelationshipPE> newParents = new HashSet<SampleRelationshipPE>();
        sample.setParentRelationships(newParents);
        Date newModificationDate = new Date(2);
        sample.setModificationDate(newModificationDate);
        final SampleUpdatesDTO updates =
                new SampleUpdatesDTO(sampleId, properties, null, attachments, version, null, null,
                        null);
        context.checking(new Expectations()
            {
                {
                    one(genericBusinessObjectFactory).createSampleBO(SESSION);
                    will(returnValue(sampleBO));

                    one(sampleBO).update(updates);
                    one(sampleBO).save();
                    allowing(sampleBO).getSample();
                    will(returnValue(sample));

                    final DynamicPropertyEvaluationOperation operation =
                            DynamicPropertyEvaluationOperation.evaluate(SamplePE.class,
                                    Arrays.asList(id));
                    one(evaluator).scheduleUpdate(with(operation));
                }
            });
        SampleUpdateResult result = createServer().updateSample(SESSION_TOKEN, updates);
        assertEquals(newModificationDate, result.getModificationDate());
        assertEquals(newParents.size(), result.getParents().size());
        context.assertIsSatisfied();
    }

    @Test
    public void testEditExperimentNothingChanged() throws Exception
    {
        final TechId experimentId = CommonTestUtils.TECH_ID;
        final ProjectIdentifier newProjectIdentifier =
                new ProjectIdentifier(DATABASE_1, GROUP_1, PROJECT_1);
        final ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
        updates.setExperimentId(experimentId);
        updates.setProjectIdentifier(newProjectIdentifier);
        final ExperimentPE experiment = new ExperimentPE();
        final Long id = 1L;
        experiment.setId(id);
        Date newModificationDate = new Date(2);
        experiment.setModificationDate(newModificationDate);
        ArrayList<SamplePE> newSamples = new ArrayList<SamplePE>();
        experiment.setSamples(newSamples);
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(genericBusinessObjectFactory).createExperimentBO(SESSION);
                    will(returnValue(experimentBO));

                    one(experimentBO).update(updates);
                    one(experimentBO).save();
                    allowing(experimentBO).getExperiment();
                    will(returnValue(experiment));

                    final DynamicPropertyEvaluationOperation operation =
                            DynamicPropertyEvaluationOperation.evaluate(ExperimentPE.class,
                                    Arrays.asList(id));
                    one(evaluator).scheduleUpdate(with(operation));
                }
            });
        ExperimentUpdateResult result = createServer().updateExperiment(SESSION_TOKEN, updates);
        assertEquals(newModificationDate, result.getModificationDate());
        assertEquals(newSamples.size(), result.getSamples().size());
        context.assertIsSatisfied();
    }

    @Test
    public void testFailRegisterExperimentsWithNullParameter() throws Exception
    {
        boolean asserionError = false;
        try
        {
            createServer().registerExperiments(SESSION_TOKEN, null);
        } catch (AssertionError ex)
        {
            asserionError = true;
        }
        assertTrue(asserionError);
    }

    @Test
    public void testFailRegisterExperimentsWithNullType() throws Exception
    {
        boolean asserionError = false;
        try
        {
            createServer().registerExperiments(SESSION_TOKEN,
                    new NewExperimentsWithType(null, createNewExperiments()));
        } catch (AssertionError ex)
        {
            asserionError = true;
        }
        assertTrue(asserionError);
    }

    @Test
    public void testFailRegisterExperimentsWithNullCollection() throws Exception
    {
        boolean asserionError = false;
        try
        {
            createServer().registerExperiments(SESSION_TOKEN, new NewExperimentsWithType("", null));
        } catch (AssertionError ex)
        {
            asserionError = true;
        }
        assertTrue(asserionError);
    }

    @Test
    public void testRegisterExperimentEmptyCollection() throws Exception
    {
        prepareGetSession();
        createServer().registerExperiments(SESSION_TOKEN,
                new NewExperimentsWithType(EXPERIMENT_TYPE, createNewExperiments()));
    }

    @Test
    public void testRegisterExperiment() throws Exception
    {
        prepareGetSession();
        final ExperimentTypePE experimentTypePE = createExperimentType(EXPERIMENT_TYPE);
        final List<NewBasicExperiment> entities =
                createNewExperiments(new NewBasicExperiment(EXPERIMENT_IDENTIFIER1),
                        new NewBasicExperiment(EXPERIMENT_IDENTIFIER2));
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getEntityTypeDAO(EntityKind.EXPERIMENT);
                    will(returnValue(entityTypeDAO));
                    one(entityTypeDAO).tryToFindEntityTypeByCode(EXPERIMENT_TYPE);
                    will(returnValue(experimentTypePE));
                    one(genericBusinessObjectFactory).createExperimentTable(SESSION);
                    will(returnValue(experimentTable));
                    one(experimentTable).add(entities, experimentTypePE);
                    one(experimentTable).save();
                }
            });
        createServer().registerExperiments(SESSION_TOKEN,
                new NewExperimentsWithType(EXPERIMENT_TYPE, entities));
        context.assertIsSatisfied();
    }
}
