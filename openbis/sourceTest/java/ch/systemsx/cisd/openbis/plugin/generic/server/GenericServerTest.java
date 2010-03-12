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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentWithContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * Test cases for corresponding {@link GenericServer} class.
 * 
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = GenericServer.class)
public final class GenericServerTest extends AbstractServerTestCase
{
    private static final String PROJECT_1 = "PROJECT-1";

    private static final String GROUP_1 = "GROUP-1";

    private static final String DATABASE_1 = "DATABASE-1";

    private IGenericBusinessObjectFactory genericBusinessObjectFactory;

    private ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin;

    private IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin;

    private final IGenericServer createServer()
    {
        return new GenericServer(sessionManager, daoFactory, genericBusinessObjectFactory,
                sampleTypeSlaveServerPlugin, dataSetTypeSlaveServerPlugin);
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
    }

    @Test
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

                }
            });
        createServer().registerSample(SESSION_TOKEN, newSample,
                Collections.<NewAttachment> emptyList());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetExperimentInfo() throws Exception
    {
        prepareGetSession();
        final ExperimentIdentifier experimentIdentifier =
                CommonTestUtils.createExperimentIdentifier();
        final ExperimentPE experimentPE = CommonTestUtils.createExperiment(experimentIdentifier);
        context.checking(new Expectations()
            {
                {
                    one(genericBusinessObjectFactory).createExperimentBO(SESSION);
                    will(returnValue(experimentBO));

                    one(experimentBO).loadByExperimentIdentifier(experimentIdentifier);
                    one(experimentBO).enrichWithProperties();
                    one(experimentBO).enrichWithAttachments();

                    one(experimentBO).getExperiment();
                    will(returnValue(experimentPE));
                }
            });
        final Experiment experiment =
                createServer().getExperimentInfo(SESSION_TOKEN, experimentIdentifier);
        assertEquals(experimentPE.getCode(), experiment.getCode());
        assertEquals(experimentPE.getExperimentType().getCode(), experiment.getExperimentType()
                .getCode());
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

                    one(genericBusinessObjectFactory).createSampleBO(SESSION);
                    will(returnValue(sampleBO));
                    one(sampleBO).loadBySampleIdentifier(sampleIdentifier1);
                    one(sampleBO).setExperiment(experimentPE);

                    one(genericBusinessObjectFactory).createSampleBO(SESSION);
                    will(returnValue(sampleBO));
                    one(sampleBO).loadBySampleIdentifier(sampleIdentifier2WithGroup);
                    one(sampleBO).setExperiment(experimentPE);
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

                    one(materialBO).update(materialId, properties, version);
                    one(materialBO).save();
                    one(materialBO).getMaterial();
                    will(returnValue(material));
                }
            });
        assertEquals(newModificationDate, createServer().updateMaterial(SESSION_TOKEN, materialId,
                properties, version));
        context.assertIsSatisfied();
    }

    @Test
    public void testEditSampleNothingChanged() throws Exception
    {
        final TechId sampleId = CommonTestUtils.TECH_ID;
        final List<IEntityProperty> properties = new ArrayList<IEntityProperty>();
        prepareGetSession();
        final Date version = new Date();
        final Collection<NewAttachment> attachments = Collections.<NewAttachment> emptyList();
        final SamplePE sample = new SamplePE();
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
                    one(sampleBO).getSample();
                    will(returnValue(sample));
                }
            });
        assertEquals(newModificationDate, createServer().updateSample(SESSION_TOKEN, updates));
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
                    one(experimentBO).getExperiment();
                    will(returnValue(experiment));
                }
            });
        ExperimentUpdateResult result = createServer().updateExperiment(SESSION_TOKEN, updates);
        assertEquals(newModificationDate, result.getModificationDate());
        assertEquals(GenericServer.extractSampleCodes(newSamples).length,
                result.getSamples().length);
        context.assertIsSatisfied();
    }

}
