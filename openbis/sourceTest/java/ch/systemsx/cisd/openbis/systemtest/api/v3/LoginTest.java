package ch.systemsx.cisd.openbis.systemtest.api.v3;

import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;

public class LoginTest extends AbstractTest
{

    @Test
    public void testLoginWithExistingUser()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        Assert.assertNotNull(sessionToken);

        List<Experiment> experimentFromCisdSpace =
                v3api.listExperiments(sessionToken, Collections.singletonList(new ExperimentPermId("200811050951882-1028")),
                        new ExperimentFetchOptions());

        Assert.assertEquals(1, experimentFromCisdSpace.size());

        List<Experiment> experimentFromTestSpace =
                v3api.listExperiments(sessionToken, Collections.singletonList(new ExperimentPermId("201206190940555-1032")),
                        new ExperimentFetchOptions());

        Assert.assertEquals(1, experimentFromTestSpace.size());
        v3api.logout(sessionToken);
    }

    @Test
    public void testLoginWithNotExistingUser()
    {
        String sessionToken = v3api.login(NOT_EXISTING_USER, NOT_EXISTING_USER_PASSWORD);
        Assert.assertNull(sessionToken);
    }

    @Test
    public void testLoginAsWithNotExistingUser()
    {
        String sessionToken = v3api.loginAs(NOT_EXISTING_USER, NOT_EXISTING_USER_PASSWORD, TEST_USER);
        Assert.assertNull(sessionToken);
    }

    @Test
    public void testLoginAsWithExistingUserAsNotExistingUser()
    {
        String sessionToken = v3api.loginAs(TEST_USER, TEST_USER_PASSWORD, NOT_EXISTING_USER);
        Assert.assertNull(sessionToken);
    }

    @Test
    public void testLoginAsWithInstanceAdminAsInstanceAdmin()
    {
        String sessionToken = v3api.loginAs(TEST_USER, TEST_USER_PASSWORD, TEST_USER);
        Assert.assertNotNull(sessionToken);

        List<Experiment> experimentFromCisdSpace =
                v3api.listExperiments(sessionToken, Collections.singletonList(new ExperimentPermId("200811050951882-1028")),
                        new ExperimentFetchOptions());

        Assert.assertEquals(1, experimentFromCisdSpace.size());

        List<Experiment> experimentFromTestSpace =
                v3api.listExperiments(sessionToken, Collections.singletonList(new ExperimentPermId("201206190940555-1032")),
                        new ExperimentFetchOptions());

        Assert.assertEquals(1, experimentFromTestSpace.size());
        v3api.logout(sessionToken);
    }

    @Test
    public void testLoginAsWithInstanceAdminAsSpaceAdmin()
    {
        String sessionToken = v3api.loginAs(TEST_USER, TEST_USER_PASSWORD, TEST_SPACE_USER);
        Assert.assertNotNull(sessionToken);

        List<Experiment> experimentFromCisdSpace =
                v3api.listExperiments(sessionToken, Collections.singletonList(new ExperimentPermId("200811050951882-1028")),
                        new ExperimentFetchOptions());

        Assert.assertEquals(0, experimentFromCisdSpace.size());

        List<Experiment> experimentFromTestSpace =
                v3api.listExperiments(sessionToken, Collections.singletonList(new ExperimentPermId("201206190940555-1032")),
                        new ExperimentFetchOptions());

        Assert.assertEquals(1, experimentFromTestSpace.size());
        v3api.logout(sessionToken);
    }

    @Test
    public void testLoginAsWithSpaceAdminAsInstanceAdmin()
    {
        String sessionToken = v3api.loginAs(TEST_SPACE_USER, TEST_SPACE_USER_PASSWORD, TEST_USER);
        Assert.assertNull(sessionToken);
    }
}
