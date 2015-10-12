package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;

import javax.annotation.Resource;

import org.apache.log4j.Level;
import org.springframework.beans.factory.InitializingBean;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.shared.Constants;

public class DAOFactoryTest extends AbstractDAOTest
{
    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    private BufferedAppender logRecorder;

    @BeforeMethod
    public void setUp()
    {
        logRecorder = new BufferedAppender(null, Level.INFO, ".*\\." + DAOFactory.class.getSimpleName());
    }

    @Test
    public void testEnableAndDisableProjectSamples() throws Exception
    {
        configurer.getResolvedProps().setProperty(Constants.PROJECT_SAMPLES_ENABLED_KEY, "true");
        configurer.getResolvedProps().setProperty(Constants.PROJECT_SAMPLES_ENABLED_KEY, "true");
        ((InitializingBean) daoFactory).afterPropertiesSet();
        assertEquals("", logRecorder.getLogContent());

        logRecorder.resetLogContent();
        configurer.getResolvedProps().setProperty(Constants.PROJECT_SAMPLES_ENABLED_KEY, "false");
        ((InitializingBean) daoFactory).afterPropertiesSet();
        assertEquals("It is not possible to disable project samples feature. The system still considers project-samples-enabled=true.",
                logRecorder.getLogContent());

        logRecorder.resetLogContent();
        configurer.getResolvedProps().setProperty(Constants.PROJECT_SAMPLES_ENABLED_KEY, "false");
        ((InitializingBean) daoFactory).afterPropertiesSet();
        assertEquals("It is not possible to disable project samples feature. The system still considers project-samples-enabled=true.",
                logRecorder.getLogContent());

        logRecorder.resetLogContent();
        configurer.getResolvedProps().setProperty(Constants.PROJECT_SAMPLES_ENABLED_KEY, "true");
        ((InitializingBean) daoFactory).afterPropertiesSet();
        assertEquals("", logRecorder.getLogContent());
    }

}
