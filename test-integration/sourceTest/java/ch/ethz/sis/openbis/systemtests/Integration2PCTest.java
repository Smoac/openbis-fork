package ch.ethz.sis.openbis.systemtests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.OpenBIS;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.systemtests.common.AbstractIntegrationTest;
import ch.ethz.sis.openbis.systemtests.common.IntegrationTestOpenBIS;
import ch.systemsx.cisd.common.test.AssertionUtil;

public class Integration2PCTest extends AbstractIntegrationTest
{

    @Test
    public void test()
    {
        OpenBIS openBIS = new IntegrationTestOpenBIS();
        openBIS.setInteractiveSessionKey(TEST_INTERACTIVE_SESSION_KEY);

        openBIS.login("test", "admin");

        openBIS.beginTransaction();

        byte[] bytes = "Hello World!".getBytes(StandardCharsets.UTF_8);
        openBIS.getAfsServerFacade().write("another-sample", "anotherdir/anotherfile", 0L, bytes, calculateMD5(bytes));

        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode("2PT_TEST");

        SpacePermId spaceId = openBIS.createSpaces(List.of(spaceCreation)).get(0);

        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setSpaceId(spaceId);
        projectCreation.setCode("2PT_TEST");

        ProjectPermId projectId = openBIS.createProjects(List.of(projectCreation)).get(0);

        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setProjectId(projectId);
        experimentCreation.setCode("2PT_TEST");

        try
        {
            openBIS.createExperiments(List.of(experimentCreation));
            fail();
        }catch(Exception e){
            assertEquals(e.getMessage(), "Operation 'createExperiments' failed.");
            AssertionUtil.assertContains("Type id cannot be null", e.getCause().getMessage());
        }

        ExperimentCreation experimentCreation2 = new ExperimentCreation();
        experimentCreation2.setTypeId(new EntityTypePermId("UNKNOWN"));
        experimentCreation2.setProjectId(projectId);
        experimentCreation2.setCode("2PT_TEST");

        openBIS.createExperiments(List.of(experimentCreation2));

        openBIS.commitTransaction();
    }

    public static byte[] calculateMD5(byte[] data)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data);
            return md.digest();
        } catch (Exception e)
        {
            throw new RuntimeException("Checksum calculation failed", e);
        }
    }

}
