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
package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static ch.systemsx.cisd.common.test.AssertionUtil.assertCollectionContainsOnly;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.create.AttachmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IAttachmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IDataSetsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IExperimentHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IExperimentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IMaterialsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModifierHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IOwnerHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IParentChildrenHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IProjectHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IProjectsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyAssignmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyTypeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ISampleHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ISamplesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ISpaceHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ITagsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.systemtest.asapi.v3.index.IndexOperation;
import ch.ethz.sis.openbis.systemtest.asapi.v3.index.IndexState;
import ch.ethz.sis.openbis.systemtest.asapi.v3.index.ReindexingState;
import ch.ethz.sis.openbis.systemtest.asapi.v3.index.RemoveFromIndexState;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Id;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.systemtest.SystemTestCase;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

import junit.framework.Assert;

/**
 * @author Jakub Straszewski
 */
public class AbstractTest extends SystemTestCase
{

    protected static final String SYSTEM_USER = "system";

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

    @Autowired
    protected IDAOFactory daoFactory;

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

    protected void assertSpaceNotFetched(final ISpaceHolder holder)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    holder.getSpace();
                }
            });
    }

    protected void assertProjectNotFetched(final IProjectHolder holder)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    holder.getProject();
                }
            });
    }

    protected void assertProjectsNotFetched(final IProjectsHolder holder)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    holder.getProjects();
                }
            });
    }

    protected void assertExperimentsNotFetched(final IExperimentsHolder holder)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    holder.getExperiments();
                }
            });
    }

    protected void assertTagsNotFetched(final ITagsHolder holder)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    holder.getTags();
                }
            });
    }

    protected void assertExperimentNotFetched(final IExperimentHolder holder)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    holder.getExperiment();
                }
            });
    }

    protected void assertSampleNotFetched(final ISampleHolder holder)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    holder.getSample();
                }
            });
    }

    protected void assertSamplesNotFetched(final ISamplesHolder holder)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    holder.getSamples();
                }
            });
    }

    protected void assertDataSetsNotFetched(final IDataSetsHolder holder)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    holder.getDataSets();
                }
            });
    }

    protected void assertMaterialsNotFetched(final IMaterialsHolder holder)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    holder.getMaterials();
                }
            });
    }

    protected void assertPhysicalDataNotFetched(final DataSet dataSet)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    dataSet.getPhysicalData();
                }
            });
    }

    protected void assertPropertyTypeNotFetched(final IPropertyTypeHolder propertyTypeHolder)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    propertyTypeHolder.getPropertyType();
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

    protected void assertPropertyAssignmentsNotFetched(final IPropertyAssignmentsHolder propertyAssignmentsHolder)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    propertyAssignmentsHolder.getPropertyAssignments();
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

    protected void assertComponentsNotFetched(final Sample sample)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    sample.getComponents();
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

    protected void assertComponentsNotFetched(final DataSet dataSet)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    dataSet.getComponents();
                }
            });
    }

    protected void assertOwnerNotFetched(final IOwnerHolder holder)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    holder.getOwner();
                }
            });
    }

    protected void assertRegistratorNotFetched(final IRegistratorHolder entity)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    entity.getRegistrator();
                }
            });
    }

    protected void assertModifierNotFetched(final IModifierHolder entity)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    entity.getModifier();
                }
            });
    }

    protected void assertLeaderNotFetched(final Project entity)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    entity.getLeader();
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

    protected void assertAttachmentsNotFetched(final IAttachmentsHolder exp)
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

    protected void assertHistoryNotFetched(final HistoryEntry history)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    history.getAuthor();
                }
            });
    }

    protected void assertVocabularyNotFetched(final VocabularyTerm term)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    term.getVocabulary();
                }
            });
    }

    protected void assertVocabularyNotFetched(final PropertyType propertyType)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    propertyType.getVocabulary();
                }
            });
    }

    protected void assertMaterialTypeNotFetched(final PropertyType propertyType)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    propertyType.getMaterialType();
                }
            });
    }

    protected void assertSummaryNotFetched(final OperationExecution execution)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    execution.getSummary();
                }
            });
    }

    protected void assertDetailsNotFetched(final OperationExecution execution)
    {
        assertNotFetched(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    execution.getDetails();
                }
            });
    }

    protected void assertNotFetched(final IDelegatedAction action)
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

    protected void assertRuntimeException(IDelegatedAction action, String expectedMessage)
    {
        assertRuntimeException(action, expectedMessage, null);
    }

    protected void assertRuntimeException(IDelegatedAction action, String expectedMessage, String expectedContextPattern)
    {
        try
        {
            action.execute();
            fail("Expected an exception to be thrown");
        } catch (Exception e)
        {
            assertEquals(e.getClass(), RuntimeException.class);
            AssertionUtil.assertContains(expectedMessage, e.getMessage());
            assertExceptionContext(e, expectedContextPattern);
        }
    }

    protected void assertUserFailureException(IDelegatedAction action, String expectedMessage)
    {
        assertUserFailureException(action, expectedMessage, null);
    }

    protected void assertUserFailureException(IDelegatedAction action, String expectedMessage, String expectedContextPattern)
    {
        try
        {
            action.execute();
            fail("Expected an exception to be thrown");
        } catch (Exception e)
        {
            assertEquals(e.getClass(), UserFailureException.class);
            AssertionUtil.assertContains(expectedMessage, e.getMessage());
            assertExceptionContext(e, expectedContextPattern);
        }
    }

    protected void assertAuthorizationFailureException(IDelegatedAction action)
    {
        assertAuthorizationFailureException(action, null);
    }

    protected void assertAuthorizationFailureException(IDelegatedAction action, String expectedContextPattern)
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
            assertExceptionContext(e, expectedContextPattern);
        }
    }

    protected void assertUnauthorizedObjectAccessException(IDelegatedAction action, IObjectId expectedObjectId)
    {
        assertUnauthorizedObjectAccessException(action, expectedObjectId, null);
    }

    protected void assertUnauthorizedObjectAccessException(IDelegatedAction action, IObjectId expectedObjectId, String expectedContextPattern)
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
            assertExceptionContext(e, expectedContextPattern);
        }
    }

    protected void assertObjectNotFoundException(IDelegatedAction action, IObjectId expectedObjectId)
    {
        assertObjectNotFoundException(action, expectedObjectId, null);
    }

    protected void assertObjectNotFoundException(IDelegatedAction action, IObjectId expectedObjectId, String expectedContextPattern)
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
            assertExceptionContext(e, expectedContextPattern);
        }
    }

    protected void assertExceptionContext(Exception e, String expectedContextPattern)
    {
        if (expectedContextPattern != null)
        {
            final String contextStart = "(Context: [";
            final String contextEnd = "])";

            int contextStartIndex = -1;
            int contextEndIndex = -1;

            if (e.getMessage() != null && e.getMessage().indexOf(contextStart) >= 0)
            {
                contextStartIndex = e.getMessage().indexOf(contextStart) + contextStart.length();
                contextEndIndex = e.getMessage().indexOf(contextEnd, contextStartIndex);
            }

            if (contextStartIndex >= 0 && contextEndIndex >= 0)
            {
                String expectedMultilineContextPattern = "(?s)" + expectedContextPattern;
                String actualContext = e.getMessage().substring(contextStartIndex, contextEndIndex);
                Assert.assertTrue("Actual context: " + actualContext + ", Expected context: " + expectedMultilineContextPattern,
                        actualContext.matches(expectedMultilineContextPattern));
            } else
            {
                Assert.fail("No context found in exception message: " + e.getMessage());
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
        assertEquals(createTimestampFormat().format(actualDate), expectedDate);
    }

    protected void assertEqualsDate(Date actualDate, Date expectedDate)
    {
        assertEquals(createTimestampFormat().format(actualDate), createTimestampFormat().format(expectedDate));
    }

    private SimpleDateFormat createTimestampFormat()
    {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    protected void assertToday(Date actualDate)
    {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals(format.format(actualDate), format.format(new Date()));
    }

    protected List<String> extractCodes(List<? extends ICodeHolder> codeHolders)
    {
        List<String> codes = new ArrayList<>();
        for (ICodeHolder codeHolder : codeHolders)
        {
            codes.add(codeHolder.getCode());
        }
        return codes;
    }

    protected List<String> extractVocabularyCodes(List<PropertyAssignment> propertyAssignments)
    {
        List<String> codes = new ArrayList<>();
        if (propertyAssignments != null)
        {
            for (PropertyAssignment propertyAssignment : propertyAssignments)
            {
                if (propertyAssignment.getPropertyType().getVocabulary() != null)
                {
                    codes.add(propertyAssignment.getPropertyType().getVocabulary().getCode());
                }
            }
        }
        return codes;
    }

    protected PropertyAssignment getPropertyAssignment(List<PropertyAssignment> propertyAssignments, String code)
    {
        List<String> codes = new ArrayList<>();
        for (PropertyAssignment propertyAssignment : propertyAssignments)
        {
            String propertyCode = propertyAssignment.getPropertyType().getCode();
            codes.add(propertyCode);
            if (propertyCode.equals(code))
            {
                return propertyAssignment;
            }
        }
        throw new AssertionError("No property '" + code + "' found in " + codes);
    }

    protected void assertOrder(List<PropertyAssignment> propertyAssignments, String... codes)
    {
        Set<String> codesSet = new LinkedHashSet<>(Arrays.asList(codes));
        List<String> propertyCodes = new ArrayList<>();
        for (PropertyAssignment assignment : propertyAssignments)
        {
            String code = assignment.getPropertyType().getCode();
            if (codesSet.contains(code))
            {
                propertyCodes.add(code);
            }
        }
        assertEquals(propertyCodes.toString(), codesSet.toString());
    }

    protected static void assertSpaceCodes(Collection<Space> spaces, String... expectedCodes)
    {
        Set<String> actualSet = new HashSet<String>();
        for (Space space : spaces)
        {
            actualSet.add(space.getCode());
        }

        assertCollectionContainsOnly(actualSet, expectedCodes);
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

    protected static void assertDataSetCodes(Collection<DataSet> dataSets, String... expectedCodes)
    {
        Set<String> actualSet = new HashSet<String>();
        for (DataSet dataSet : dataSets)
        {
            actualSet.add(dataSet.getCode());
        }

        assertCollectionContainsOnly(actualSet, expectedCodes);
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

    protected static void assertSampleIdentifiersInOrder(Collection<Sample> samples, String... expectedIdentifiers)
    {
        List<String> identifiers = new LinkedList<String>();

        for (Sample sample : samples)
        {
            identifiers.add(sample.getIdentifier().getIdentifier());
        }

        assertEquals(identifiers, Arrays.asList(expectedIdentifiers));
    }

    protected static void assertMaterialPermIds(Collection<Material> materials, MaterialPermId... expectedPermIds)
    {
        Set<MaterialPermId> actualSet = new HashSet<MaterialPermId>();
        for (Material material : materials)
        {
            actualSet.add(material.getPermId());
        }

        assertCollectionContainsOnly(actualSet, expectedPermIds);
    }

    protected static void assertVocabularyTermPermIds(Collection<VocabularyTerm> terms, VocabularyTermPermId... expectedPermIds)
    {
        Set<VocabularyTermPermId> actualSet = new HashSet<VocabularyTermPermId>();
        for (VocabularyTerm term : terms)
        {
            actualSet.add(term.getPermId());
        }

        assertCollectionContainsOnly(actualSet, expectedPermIds);
    }

    protected void assertExperimentsReindexed(ReindexingState previousState, String... permIds)
    {
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listByPermID(Arrays.asList(permIds));
        assertEquals(experiments.size(), permIds.length);
        assertIndexStateChange(previousState, new ReindexingState(), ExperimentPE.class.getName(), experiments.toArray(new ExperimentPE[] {}));
    }

    protected void assertSamplesReindexed(ReindexingState previousState, String... permIds)
    {
        List<SamplePE> samples = daoFactory.getSampleDAO().listByPermID(Arrays.asList(permIds));
        assertEquals(samples.size(), permIds.length);
        assertIndexStateChange(previousState, new ReindexingState(), SamplePE.class.getName(), samples.toArray(new SamplePE[] {}));
    }

    protected void assertDataSetsReindexed(ReindexingState previousState, String... permIds)
    {
        List<DataPE> dataSets = daoFactory.getDataDAO().listByCode(new HashSet<String>(Arrays.asList(permIds)));
        assertEquals(dataSets.size(), permIds.length);
        assertIndexStateChange(previousState, new ReindexingState(), DataPE.class.getName(), dataSets.toArray(new DataPE[] {}));
    }

    protected void assertMaterialsReindexed(ReindexingState previousState, MaterialPermId... permIds)
    {
        Collection<MaterialIdentifier> identifiers = new HashSet<MaterialIdentifier>();
        for (MaterialPermId permId : permIds)
        {
            identifiers.add(new MaterialIdentifier(permId.getCode(), permId.getTypeCode()));
        }
        List<MaterialPE> materials = daoFactory.getMaterialDAO().listMaterialsByMaterialIdentifier(identifiers);
        assertEquals(materials.size(), permIds.length);
        assertIndexStateChange(previousState, new ReindexingState(), MaterialPE.class.getName(), materials.toArray(new MaterialPE[] {}));
    }

    protected void assertExperimentsRemovedFromIndex(RemoveFromIndexState previousState, Long... ids)
    {
        assertIndexStateChange(previousState, new RemoveFromIndexState(), ExperimentPE.class.getName(), ids);
    }

    protected void assertSamplesRemovedFromIndex(RemoveFromIndexState previousState, Long... ids)
    {
        assertIndexStateChange(previousState, new RemoveFromIndexState(), SamplePE.class.getName(), ids);
    }

    protected void assertDataSetsRemovedFromIndex(RemoveFromIndexState previousState, Long... ids)
    {
        assertIndexStateChange(previousState, new RemoveFromIndexState(), DataPE.class.getName(), ids);
    }

    protected void assertMaterialsRemovedFromIndex(RemoveFromIndexState previousState, Long... ids)
    {
        assertIndexStateChange(previousState, new RemoveFromIndexState(), MaterialPE.class.getName(), ids);
    }

    protected <S extends IndexState> void assertIndexStateChange(S previousState, S currentState, String expectedClassName,
            Long... expectedIds)
    {
        Id[] idHolders = new Id[expectedIds.length];
        int index = 0;

        for (Long expectedId : expectedIds)
        {
            Id idHolder = new Id();
            idHolder.setId(expectedId);
            idHolders[index] = idHolder;
            index++;
        }

        assertIndexStateChange(previousState, currentState, expectedClassName, idHolders);
    }

    protected <S extends IndexState> void assertIndexStateChange(S previousState, S currentState, String expectedClassName,
            IIdHolder... expectedIdHolders)
    {
        Map<Object, IndexOperation> newOperations = new IdentityHashMap<Object, IndexOperation>();

        for (IndexOperation operation : currentState.getOperations())
        {
            newOperations.put(operation.getOriginalOperation(), operation);
        }
        for (IndexOperation operation : previousState.getOperations())
        {
            newOperations.remove(operation.getOriginalOperation());
        }

        Map<String, Set<Long>> classNameToIdsMap = new HashMap<String, Set<Long>>();

        for (IndexOperation newOperation : newOperations.values())
        {
            Set<Long> ids = classNameToIdsMap.get(newOperation.getClassName());
            if (ids == null)
            {
                ids = new HashSet<Long>();
                classNameToIdsMap.put(newOperation.getClassName(), ids);
            }
            ids.addAll(newOperation.getIds());
        }

        Set<Long> expectedIds = new HashSet<Long>();
        for (IIdHolder expectedIdHolder : expectedIdHolders)
        {
            expectedIds.add(expectedIdHolder.getId());
        }

        Set<Long> actualIds = classNameToIdsMap.get(expectedClassName);
        if (actualIds == null)
        {
            actualIds = Collections.emptySet();
        }

        assertCollectionContainsOnly(actualIds, expectedIds.toArray(new Long[] {}));
    }

    protected static String patternContains(String... parts)
    {
        StringBuilder pattern = new StringBuilder();
        pattern.append(".*");
        for (String part : parts)
        {
            pattern.append(Pattern.quote(part));
            pattern.append(".*");
        }
        return pattern.toString();
    }

    protected static String toDblQuotes(String str)
    {
        return str.replaceAll("'", "\"");
    }

}
