/*
 * Copyright 2013 ETH Zuerich, CISD
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
package ch.ethz.sis.openbis.systemtest.api.v3;

import static ch.systemsx.cisd.common.test.AssertionUtil.assertCollectionContainsOnly;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import ch.ethz.sis.openbis.generic.shared.api.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.AttachmentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IParentChildrenHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.Space;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.IObjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.NotFetchedException;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.systemtest.SystemTestCase;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Jakub Straszewski
 */
public class AbstractTest extends SystemTestCase
{

    protected static final String NOT_EXISTING_USER = "notexistinguser";

    protected static final String TEST_SPACE_USER = "test_space";

    protected static final String TEST_USER = "test";

    protected static final String TEST_POWER_USER_CISD = "test_role";

    protected static final String TEST_GROUP_OBSERVER = "observer";

    protected static final String PASSWORD = "password";

    private BufferedAppender logRecorder;

    @Autowired
    protected IApplicationServerApi v3api;

    @Autowired
    protected IGeneralInformationService generalInformationService;

    @BeforeClass
    public void beforeClass()
    {
        Logger.getLogger("OPERATION.Resources").setLevel(Level.DEBUG);
        Logger.getLogger("OPERATION.AbstractCachingTranslator").setLevel(Level.DEBUG);
    }

    @AfterClass
    public void afterClass()
    {
        Logger.getLogger("OPERATION.Resources").setLevel(Level.INFO);
        Logger.getLogger("OPERATION.AbstractCachingTranslator").setLevel(Level.INFO);
    }

    @BeforeMethod
    public void beforeMethod(Method method)
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.DEBUG);
        System.out.println(">>>>>>>>> BEFORE METHOD: " + method.getName());
    }

    @AfterMethod
    public void afterMethod(Method method)
    {
        logRecorder.reset();
        System.out.println("<<<<<<<<< AFTER METHOD: " + method.getName());
    }

    protected void assertTypeNotFetched(final Experiment experiment)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    experiment.getType();
                }
            });
    }

    protected void assertTypeNotFetched(final DataSet dataSet)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    dataSet.getType();
                }
            });
    }

    protected void assertSpaceNotFetched(final Sample sample)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    sample.getSpace();
                }
            });
    }

    protected void assertSpaceNotFetched(final Person person)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    person.getSpace();
                }
            });
    }

    protected void assertProjectNotFetched(final Experiment experiment)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    experiment.getProject();
                }
            });
    }

    protected void assertProjectsNotFetched(final Space space)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    space.getProjects();
                }
            });
    }

    protected void assertTagsNotFetched(final Experiment experiment)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    experiment.getTags();
                }
            });
    }

    protected void assertTagsNotFetched(final Material material)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    material.getTags();
                }
            });
    }

    protected void assertTagsNotFetched(final DataSet dataSet)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    dataSet.getTags();
                }
            });
    }

    protected void assertExperimentNotFetched(final Sample sample)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    sample.getExperiment();
                }
            });
    }

    protected void assertExperimentNotFetched(final DataSet dataSet)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    dataSet.getExperiment();
                }
            });
    }

    protected void assertSampleNotFetched(final DataSet dataSet)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    dataSet.getSample();
                }
            });
    }

    protected void assertSamplesNotFetched(final Space space)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    space.getSamples();
                }
            });
    }

    protected void assertExternalDataNotFetched(final DataSet dataSet)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    dataSet.getExternalData();
                }
            });
    }

    protected void assertPropertiesNotFetched(final IPropertiesHolder propertiesHolder)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    propertiesHolder.getProperties();
                }
            });
    }

    protected void assertContainerNotFetched(final Sample sample)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    sample.getContainer();
                }
            });
    }

    protected void assertContainedNotFetched(final Sample sample)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    sample.getContained();
                }
            });
    }

    protected void assertParentsNotFetched(final IParentChildrenHolder<?> parentChildrenHolder)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    parentChildrenHolder.getParents();
                }
            });
    }

    protected void assertChildrenNotFetched(final IParentChildrenHolder<?> parentChildrenHolder)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    parentChildrenHolder.getChildren();
                }
            });
    }

    protected void assertContainersNotFetched(final DataSet dataSet)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    dataSet.getContainers();
                }
            });
    }

    protected void assertContainedNotFetched(final DataSet dataSet)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    dataSet.getContained();
                }
            });
    }

    protected void assertTagsNotFetched(final Sample sample)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    sample.getTags();
                }
            });
    }

    protected void assertRegistratorNotFetched(final Space space)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    space.getRegistrator();
                }
            });
    }

    protected void assertRegistratorNotFetched(final Sample sample)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    sample.getRegistrator();
                }
            });
    }

    protected void assertRegistratorNotFetched(final DataSet dataSet)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    dataSet.getRegistrator();
                }
            });
    }

    protected void assertModifierNotFetched(final Sample sample)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    sample.getModifier();
                }
            });
    }

    protected void assertModifierNotFetched(final DataSet dataSet)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    dataSet.getModifier();
                }
            });
    }

    protected void assertRegistratorNotFetched(final Experiment experiment)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    experiment.getRegistrator();
                }
            });
    }

    protected void assertModifierNotFetched(final Experiment experiment)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    experiment.getModifier();
                }
            });
    }

    protected void assertRegistratorNotFetched(final Person person)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    person.getRegistrator();
                }
            });
    }

    protected void assertPreviousAttachmentNotFetched(final Attachment att)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    att.getPreviousVersion();
                }
            });
    }

    protected void assertAttachmentContentNotFetched(final Attachment att)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    att.getContent();
                }
            });
    }

    protected void assertAttachmentsNotFetched(final Experiment exp)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    exp.getAttachments();
                }
            });
    }

    protected void assertAttachmentsNotFetched(final Sample sample)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    sample.getAttachments();
                }
            });
    }

    private void assertNotFetched(final IDelegatedAction action)
    {
        try
        {
            action.execute();
            fail("NotFetchedException expected");
        } catch (NotFetchedException e)
        {
            // ok
        }
    }

    protected void assertUserFailureException(IDelegatedAction action, String expectedMessage)
    {
        assertUserFailureException(action, expectedMessage, null);
    }

    protected void assertUserFailureException(IDelegatedAction action, String expectedMessage, String expectedContextDescription)
    {
        try
        {
            action.execute();
            fail("Expected an exception to be thrown");
        } catch (Exception e)
        {
            assertEquals(e.getClass(), UserFailureException.class);
            AssertionUtil.assertContains(expectedMessage, e.getMessage());
            if (expectedContextDescription != null)
            {
                AssertionUtil.assertContains("(Context: [" + expectedContextDescription + "])", e.getMessage());
            }
        }
    }

    protected void assertAuthorizationFailureException(IDelegatedAction action)
    {
        assertAuthorizationFailureException(action, null);
    }

    protected void assertAuthorizationFailureException(IDelegatedAction action, String expectedContextDescription)
    {
        try
        {
            action.execute();
            fail("Expected an exception to be thrown");
        } catch (Exception e)
        {
            if (false == e instanceof AuthorizationFailureException)
            {
                assertNotNull(e.getCause());
                assertEquals(e.getCause().getClass(), AuthorizationFailureException.class);
            }
            if (expectedContextDescription != null)
            {
                AssertionUtil.assertContains("(Context: [" + expectedContextDescription + "])", e.getMessage());
            }
        }
    }

    protected void assertUnauthorizedObjectAccessException(IDelegatedAction action, IObjectId expectedObjectId)
    {
        assertUnauthorizedObjectAccessException(action, expectedObjectId, null);
    }

    protected void assertUnauthorizedObjectAccessException(IDelegatedAction action, IObjectId expectedObjectId, String expectedContextDescription)
    {
        try
        {
            action.execute();
            fail("Expected an exception to be thrown");
        } catch (Exception e)
        {
            assertNotNull(e.getCause());
            assertEquals(e.getCause().getClass(), UnauthorizedObjectAccessException.class);
            assertEquals(((UnauthorizedObjectAccessException) e.getCause()).getObjectId(), expectedObjectId);
            if (expectedContextDescription != null)
            {
                AssertionUtil.assertContains("(Context: [" + expectedContextDescription + "])", e.getMessage());
            }
        }
    }

    protected void assertObjectNotFoundException(IDelegatedAction action, IObjectId expectedObjectId)
    {
        assertObjectNotFoundException(action, expectedObjectId, null);
    }

    protected void assertObjectNotFoundException(IDelegatedAction action, IObjectId expectedObjectId, String expectedContextDescription)
    {
        try
        {
            action.execute();
            fail("Expected an exception to be thrown");
        } catch (Exception e)
        {
            assertNotNull(e.getCause());
            assertEquals(e.getCause().getClass(), ObjectNotFoundException.class);
            assertEquals(((ObjectNotFoundException) e.getCause()).getObjectId(), expectedObjectId);
            if (expectedContextDescription != null)
            {
                AssertionUtil.assertContains("(Context: [" + expectedContextDescription + "])", e.getMessage());
            }
        }
    }

    protected void assertContainSameObjects(Collection<?> c1, Collection<?> c2, int expectedSameObjectCount)
    {
        int count = 0;
        for (Object o1 : c1)
        {
            for (Object o2 : c2)
            {
                if (o1 == o2)
                {
                    count++;
                }
            }
        }
        assertEquals(count, expectedSameObjectCount);
    }

    protected void assertTags(Collection<Tag> tags, String... expectedTagPermIds)
    {
        Set<String> actualPermIds = new HashSet<String>();
        for (Tag tag : tags)
        {
            actualPermIds.add(tag.getPermId().getPermId());
        }
        assertCollectionContainsOnly(actualPermIds, expectedTagPermIds);
    }

    protected Map<String, Attachment> assertAttachments(Collection<Attachment> attachments, AttachmentCreation... expectedAttachments)
    {
        if (expectedAttachments == null || expectedAttachments.length == 0)
        {
            assertEquals(attachments.size(), 0);
            return Collections.emptyMap();
        } else
        {
            Map<String, AttachmentCreation> expectedMap = new HashMap<String, AttachmentCreation>();
            for (AttachmentCreation expected : expectedAttachments)
            {
                expectedMap.put(expected.getFileName(), expected);
            }

            Map<String, Attachment> actualMap = new HashMap<String, Attachment>();
            for (Attachment actual : attachments)
            {
                actualMap.put(actual.getFileName(), actual);
            }

            AssertionUtil.assertCollectionContainsOnly(actualMap.keySet(), expectedMap.keySet().toArray(new String[] {}));

            for (Attachment actual : attachments)
            {
                AttachmentCreation expected = expectedMap.get(actual.getFileName());
                assertEquals(actual.getFileName(), expected.getFileName());
                assertEquals(actual.getTitle(), expected.getTitle());
                assertEquals(actual.getDescription(), expected.getDescription());
                assertEquals(actual.getContent(), expected.getContent());
            }

            return actualMap;
        }
    }

    protected void assertEqualsDate(Date actualDate, String expectedDate)
    {
        assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(actualDate), expectedDate);
    }

    protected static void assertProjectIdentifiers(Collection<Project> projects, String... expectedIdentifiers)
    {
        Set<String> actualSet = new HashSet<String>();
        for (Project project : projects)
        {
            actualSet.add(project.getIdentifier().getIdentifier());
        }

        assertCollectionContainsOnly(actualSet, expectedIdentifiers);
    }

    protected static void assertExperimentIdentifiers(Collection<Experiment> experiments, String... expectedIdentifiers)
    {
        Set<String> actualSet = new HashSet<String>();
        for (Experiment experiment : experiments)
        {
            actualSet.add(experiment.getIdentifier().getIdentifier());
        }

        assertCollectionContainsOnly(actualSet, expectedIdentifiers);
    }

    protected static void assertSampleIdentifier(Sample sample, String expectedIdentifier)
    {
        assertEquals(sample.getIdentifier().getIdentifier(), expectedIdentifier);
    }

    protected static void assertSampleIdentifiers(Collection<Sample> samples, String... expectedIdentifiers)
    {
        Set<String> actualSet = new HashSet<String>();
        for (Sample sample : samples)
        {
            actualSet.add(sample.getIdentifier().getIdentifier());
        }

        assertCollectionContainsOnly(actualSet, expectedIdentifiers);
    }

}
