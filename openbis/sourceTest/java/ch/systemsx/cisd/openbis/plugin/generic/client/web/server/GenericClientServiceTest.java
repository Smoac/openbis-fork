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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.core.IsAnything;
import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.springframework.web.multipart.MultipartFile;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractClientServiceTest;
import ch.systemsx.cisd.openbis.generic.client.web.server.UploadedFilesBean;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * Test cases for corresponding {@link GenericClientService} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = GenericClientService.class)
public final class GenericClientServiceTest extends AbstractClientServiceTest
{
    private MultipartFile multipartFile;

    private IGenericServer genericServer;

    private GenericClientService genericClientService;

    private final static NewSample createNewSample(final String sampleIdentifier,
            final String sampleTypeCode, final IEntityProperty[] properties, final String parent,
            final String container)
    {
        final NewSample newSample = new NewSample();
        newSample.setIdentifier(sampleIdentifier);
        final SampleType sampleType = createSampleType(sampleTypeCode);
        newSample.setSampleType(sampleType);
        newSample.setProperties(properties);
        newSample.setParentIdentifier(parent);
        newSample.setContainerIdentifier(container);
        return newSample;
    }

    private final static SampleType createSampleType(final String sampleTypeCode)
    {
        final SampleType sampleType = new SampleType();
        sampleType.setCode(sampleTypeCode);
        return sampleType;
    }

    private final static IEntityProperty createSampleProperty(final String propertyTypeCode,
            final String value)
    {
        final IEntityProperty sampleProperty = new EntityProperty();
        sampleProperty.setValue(value);
        final PropertyType propertyType = new PropertyType();
        propertyType.setCode(propertyTypeCode);
        sampleProperty.setPropertyType(propertyType);
        return sampleProperty;
    }

    //
    // AbstractClientServiceTest
    //

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        genericServer = context.mock(IGenericServer.class);
        multipartFile = context.mock(MultipartFile.class);
        genericClientService = new GenericClientService(genericServer, requestContextProvider);
    }

    @Test
    public final void testRegisterSample()
    {
        final String sessionKey = "some-session-key";
        final NewSample newSample =
                createNewSample("/group1/sample1", "MASTER_PLATE", IEntityProperty.EMPTY_ARRAY,
                        null, null);
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);
                    allowing(httpSession).getAttribute(sessionKey);
                    will(returnValue(new UploadedFilesBean()));
                    one(httpSession).removeAttribute(sessionKey);
                    one(genericServer).registerSample(with(SESSION_TOKEN), getTranslatedSample(),
                            anyAttachmentCollection());
                    will(new CustomAction("check sample")
                        {
                            @SuppressWarnings("unchecked")
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                final NewSample sample = (NewSample) invocation.getParameter(1);
                                assertEquals("MASTER_PLATE", sample.getSampleType().getCode());
                                assertEquals("/group1/sample1", sample.getIdentifier());
                                final Collection<NewAttachment> attachments =
                                        (Collection<NewAttachment>) invocation.getParameter(2);
                                assertEquals(0, attachments.size());
                                return null;
                            }
                        });
                }

                private final NewSample getTranslatedSample()
                {
                    return with(any(NewSample.class));
                }

                @SuppressWarnings("unchecked")
                private final Collection<NewAttachment> anyAttachmentCollection()
                {
                    return with(any(Collection.class));
                }
            });
        genericClientService.registerSample(sessionKey, newSample);
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSamples() throws IOException
    {
        final UploadedFilesBean uploadedFilesBean = new UploadedFilesBean();
        final String sessionKey = "uploaded-files";
        final NewSample newSample = new NewSample();
        newSample.setIdentifier("MP1");
        newSample.setContainerIdentifier("MP2");
        newSample.setParentIdentifier("MP3");
        newSample.setProperties(new IEntityProperty[]
            { createSampleProperty("prop1", "RED"), createSampleProperty("prop2", "1") });
        final SampleType sampleType = createSampleType("MASTER_PLATE");
        final String fileName = "originalFileName.txt";

        final List<NewSamplesWithTypes> samplesWithType = new ArrayList<NewSamplesWithTypes>();
        List<NewSample> newSamples = new ArrayList<NewSample>();
        newSamples.add(newSample);
        samplesWithType.add(new NewSamplesWithTypes(sampleType, newSamples));
        context.checking(new Expectations()
            {
                {
                    prepareGetHttpSession(this);
                    prepareGetSessionToken(this);

                    allowing(httpSession).getAttribute(sessionKey);
                    will(returnValue(uploadedFilesBean));

                    allowing(httpSession).removeAttribute(sessionKey);

                    exactly(2).of(multipartFile).getOriginalFilename();
                    will(returnValue(fileName));

                    one(multipartFile).transferTo(with(any(File.class)));
                    will(new CustomAction("copy content")
                        {
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                final File target = (File) invocation.getParameter(0);
                                FileUtilities
                                        .writeToFile(target,
                                                "identifier\tcontainer\tparent\tprop1\tprop2\nMP1\tMP2\tMP3\tRED\t1");
                                return null;
                            }
                        });

                    one(genericServer).registerSamples(with(equal(SESSION_TOKEN)),
                            with(newSampleWithTypesList()));
                    will(new CustomAction("check sample")
                        {

                            @SuppressWarnings("unchecked")
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                final List<NewSamplesWithTypes> samplesSecions =
                                        (List<NewSamplesWithTypes>) invocation.getParameter(1);
                                assertEquals(1, samplesSecions.size());
                                final NewSamplesWithTypes samples = samplesSecions.get(0);
                                // Do not compare sampleType, as the registration code doesn't set
                                // the database instance.
                                assertEquals(sampleType.getCode(), samples.getSampleType()
                                        .getCode());
                                assertEquals(1, samples.getNewSamples().size());
                                final NewSample sample = samples.getNewSamples().get(0);
                                assertEquals("MP1", sample.getIdentifier());
                                assertEquals("MP2", sample.getContainerIdentifier());
                                assertEquals("MP3", sample.getParentIdentifier());
                                assertEquals(2, sample.getProperties().length);
                                final IEntityProperty prop1 = sample.getProperties()[0];
                                final IEntityProperty prop2 = sample.getProperties()[1];
                                assertEquals("RED", prop1.getValue());
                                assertEquals("1", prop2.getValue());
                                return null;
                            }
                        });
                }
            });
        uploadedFilesBean.addMultipartFile(multipartFile);
        final List<BatchRegistrationResult> result =
                genericClientService.registerSamples(sampleType, sessionKey, null);
        assertEquals(1, result.size());
        final BatchRegistrationResult batchRegistrationResult = result.get(0);
        assertEquals(fileName, batchRegistrationResult.getFileName());
        assertEquals("1 sample(s) found and registered.", batchRegistrationResult.getMessage());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterExperiment()
    {
        final NewExperiment newExperiment =
                createNewExperiment("/group1/project1/exp1", "SIRNA_HCS",
                        IEntityProperty.EMPTY_ARRAY);
        final String attachmentSessionKey = "attachment-session-key";
        final String sampleSessionKey = "sample-session-key";
        context.checking(new Expectations()
            {
                {
                    prepareGetSessionToken(this);
                    allowing(httpSession).getAttribute(attachmentSessionKey);
                    // TODO 2009-01-20, IA: Add test for attachment handling
                    will(returnValue(new UploadedFilesBean()));
                    one(httpSession).removeAttribute(attachmentSessionKey);
                    one(genericServer).registerExperiment(with(SESSION_TOKEN),
                            getTranslatedExperiment(), anyAttachmentCollection());
                    will(new CustomAction("check experiment")
                        {
                            @SuppressWarnings("unchecked")
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                final NewExperiment experiment =
                                        (NewExperiment) invocation.getParameter(1);
                                assertEquals("SIRNA_HCS", experiment.getExperimentTypeCode());
                                assertEquals("/group1/project1/exp1", experiment.getIdentifier());
                                final Collection<NewAttachment> attachments =
                                        (Collection<NewAttachment>) invocation.getParameter(2);
                                assertEquals(0, attachments.size());
                                return null;
                            }
                        });
                }

                private final NewExperiment getTranslatedExperiment()
                {
                    return with(any(NewExperiment.class));
                }

                @SuppressWarnings("unchecked")
                private final Collection<NewAttachment> anyAttachmentCollection()
                {
                    return with(any(Collection.class));
                }

            });
        genericClientService.registerExperiment(attachmentSessionKey, sampleSessionKey,
                newExperiment);
        context.assertIsSatisfied();
    }

    //
    // Helper classes
    //

    private NewExperiment createNewExperiment(String identifier, String type,
            IEntityProperty[] properties)
    {
        final NewExperiment newExperiment = new NewExperiment();
        newExperiment.setIdentifier(identifier);
        newExperiment.setExperimentTypeCode(type);
        newExperiment.setProperties(properties);
        return newExperiment;
    }

    private IsAnything<List<NewSamplesWithTypes>> newSampleWithTypesList()
    {
        return new IsAnything<List<NewSamplesWithTypes>>();
    }

    /**
     * A {@link BaseMatcher} extension for checking the list of {@link NewSample NewSamples}.
     * 
     * @author Christian Ribeaud
     */
    @SuppressWarnings("unused")
    private final static class NewSampleListMatcher extends BaseMatcher<List<NewSample>>
    {
        private final List<NewSample> newSamples;

        NewSampleListMatcher(final List<NewSample> newSamples)
        {
            this.newSamples = newSamples;
        }

        private final boolean equals(final IEntityProperty[] properties,
                final IEntityProperty[] sampleProperties)
        {
            int i = -1;
            for (final IEntityProperty sampleProperty : sampleProperties)
            {
                if (StringUtils.equals(sampleProperty.getValue(), properties[++i].getValue()) == false
                        || StringUtils.equals(getPropertyTypeCode(sampleProperty),
                                getPropertyTypeCode(properties[i])) == false)
                {
                    return false;
                }
            }
            return true;
        }

        private final static String getPropertyTypeCode(final IEntityProperty sampleProperty)
        {
            return sampleProperty.getPropertyType().getCode();
        }

        private final boolean equals(final NewSample newSample, final NewSample thatNewSample)
        {
            return ObjectUtils.equals(newSample, thatNewSample)
                    && StringUtils.equals(newSample.getContainerIdentifier(), thatNewSample
                            .getContainerIdentifier())
                    && StringUtils.equals(newSample.getParentIdentifier(), thatNewSample
                            .getParentIdentifier())
                    && equals(newSample.getProperties(), thatNewSample.getProperties());
        }

        //
        // BaseMatcher
        //

        public final void describeTo(final Description description)
        {
            description.appendValue(newSamples);
        }

        @SuppressWarnings("unchecked")
        public final boolean matches(final Object item)
        {
            final List<NewSample> thisNewSamples = (List<NewSample>) item;
            if (thisNewSamples.size() == newSamples.size() == false)
            {
                return false;
            }
            int i = 0;
            for (final NewSample newSample : thisNewSamples)
            {
                final NewSample thatNewSample = newSamples.get(i++);
                if (equals(newSample, thatNewSample) == false)
                {
                    return false;
                }
            }
            return true;
        }
    }
}
