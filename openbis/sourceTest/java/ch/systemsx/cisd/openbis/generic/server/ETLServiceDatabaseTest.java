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

package ch.systemsx.cisd.openbis.generic.server;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * @author pkupczyk
 */
@Test(groups =
    { "db", "dataset" })
public class ETLServiceDatabaseTest extends AbstractDAOTest
{
    @Autowired
    private IETLLIMSService service;

    private String sessionToken;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        sessionToken = service.tryToAuthenticate("test", "password").getSessionToken();
    }

    @Test
    public void testListExperimentsWithBasicFetchOptions()
    {
        List<ExperimentIdentifier> identifiers =
                Collections.singletonList(new ExperimentIdentifier("CISD", "CISD", "NEMO", "EXP1"));

        List<Experiment> result =
                service.listExperiments(sessionToken, identifiers, new ExperimentFetchOptions());

        assertEquals(1, result.size());
        assertTrue(result.get(0).getFetchOptions().isSetOf(ExperimentFetchOption.BASIC));
    }

    @Test
    public void testListExperimentsWithAllFetchOptions()
    {
        List<ExperimentIdentifier> identifiers =
                Collections.singletonList(new ExperimentIdentifier("CISD", "CISD", "NEMO", "EXP1"));

        List<Experiment> result =
                service.listExperiments(sessionToken, identifiers, new ExperimentFetchOptions(
                        ExperimentFetchOption.values()));

        assertEquals(1, result.size());
        assertTrue(result.get(0).getFetchOptions().isSetOf(ExperimentFetchOption.values()));
    }

    @Test
    public void testListExperimentsForProjectsWithBasicFetchOptions()
    {
        List<ProjectIdentifier> identifiers =
                Collections.singletonList(new ProjectIdentifier("CISD", "CISD", "NOE"));

        List<Experiment> result =
                service.listExperimentsForProjects(sessionToken, identifiers,
                        new ExperimentFetchOptions());

        assertEquals(1, result.size());
        assertTrue(result.get(0).getFetchOptions().isSetOf(ExperimentFetchOption.BASIC));
    }

    @Test
    public void testListExperimentsForProjectsWithAllFetchOptions()
    {
        List<ProjectIdentifier> identifiers =
                Collections.singletonList(new ProjectIdentifier("CISD", "CISD", "NOE"));

        List<Experiment> result =
                service.listExperimentsForProjects(sessionToken, identifiers,
                        new ExperimentFetchOptions(ExperimentFetchOption.values()));

        assertEquals(1, result.size());
        assertTrue(result.get(0).getFetchOptions().isSetOf(ExperimentFetchOption.values()));
    }

    @Test
    public void testPerformEntityOperationsUpdateSample()
    {
        // Find the samples to add
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(
                MatchClauseAttribute.CODE, "3VCP7"));
        List<Sample> samplesToUpdate = service.searchForSamples(sessionToken, searchCriteria);
        assertEquals(1, samplesToUpdate.size());
        Sample sampleToUpdate = samplesToUpdate.get(0);

        // Update the comment
        String newComment = "This is a new comment. This is not the old comment.";
        String oldComment = EntityHelper.tryFindPropertyValue(sampleToUpdate, "COMMENT");
        assertFalse(newComment.equals(oldComment));
        EntityHelper.createOrUpdateProperty(sampleToUpdate, "COMMENT", newComment);

        // Update the parents
        SearchCriteria parentSearchCriteria = new SearchCriteria();
        parentSearchCriteria.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(
                MatchClauseAttribute.CODE, "3V-126"));
        List<Sample> parentsToAdd = service.searchForSamples(sessionToken, parentSearchCriteria);
        assertEquals(1, parentsToAdd.size());
        Sample parentToAdd = parentsToAdd.get(0);
        assertEquals(1, sampleToUpdate.getParents().size());
        sampleToUpdate.addParent(parentToAdd);

        performSampleUpdate(sampleToUpdate);

        // Now retrieve the sample again and check that the properties were updated.
        List<Sample> updatedSamples = service.searchForSamples(sessionToken, searchCriteria);
        assertEquals(1, updatedSamples.size());
        Sample updatedSample = updatedSamples.get(0);
        assertTrue("The modification date should have been updated", updatedSample
                .getModificationDate().compareTo(sampleToUpdate.getModificationDate()) > 0);
        assertEquals(newComment, EntityHelper.tryFindPropertyValue(updatedSample, "COMMENT"));
        assertEquals(2, updatedSample.getParents().size());
    }

    private void performSampleUpdate(Sample sampleToUpdate)
    {
        TechId registrationid = new TechId(service.drawANewUniqueID(sessionToken));
        List<NewSpace> spaceRegistrations = Collections.emptyList();
        List<NewProject> projectRegistrations = Collections.emptyList();
        List<NewExperiment> experimentRegistrations = Collections.emptyList();

        SampleUpdatesDTO sampleUpdate = convertToSampleUpdateDTO(sampleToUpdate);
        List<SampleUpdatesDTO> sampleUpdates = Arrays.asList(sampleUpdate);
        List<NewSample> sampleRegistrations = Collections.emptyList();
        Map<String, List<NewMaterial>> materialRegistrations = Collections.emptyMap();
        List<? extends NewExternalData> dataSetRegistrations = Collections.emptyList();
        List<DataSetBatchUpdatesDTO> dataSetUpdates = Collections.emptyList();
        AtomicEntityOperationDetails details =
                new AtomicEntityOperationDetails(registrationid, null, spaceRegistrations,
                        projectRegistrations, experimentRegistrations, sampleUpdates,
                        sampleRegistrations, materialRegistrations, dataSetRegistrations,
                        dataSetUpdates);
        service.performEntityOperations(sessionToken, details);
    }

    private SampleUpdatesDTO convertToSampleUpdateDTO(Sample sample)
    {
        List<NewAttachment> attachments = Collections.emptyList();
        String containerIdentifier =
                (sample.getContainer() != null) ? sample.getContainer().getIdentifier() : null;
        Set<Sample> sampleParents = sample.getParents();
        String[] parentCodes = new String[sampleParents.size()];
        int i = 0;
        for (Sample parent : sampleParents)
        {
            parentCodes[i++] = parent.getCode();
        }
        SampleUpdatesDTO sampleUpdate =
                new SampleUpdatesDTO(TechId.create(sample), // db id
                        sample.getProperties(), // List<IEntityProperty>
                        sample.getExperiment() == null ? null
                                : ExperimentIdentifierFactory.parse(sample.getExperiment()
                                        .getIdentifier()), // ExperimentIdentifier
                        attachments, // Collection<NewAttachment>
                        sample.getModificationDate(), // Sample version
                        SampleIdentifierFactory.parse(sample.getIdentifier()), // Sample Identifier
                        containerIdentifier, // Container Identifier
                        parentCodes // Parent Identifiers
                );
        return sampleUpdate;
    }

}
