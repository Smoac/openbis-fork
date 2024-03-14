package ch.ethz.sis.openbis.systemtests.common;

import ch.ethz.sis.openbis.generic.OpenBIS;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;

public class IntegrationTestOpenBIS extends OpenBIS
{

    public IntegrationTestOpenBIS()
    {
        super(TestInstanceHostUtils.getOpenBISUrl() + TestInstanceHostUtils.getOpenBISPath(),
                TestInstanceHostUtils.getDSSUrl() + TestInstanceHostUtils.getDSSPath(),
                TestInstanceHostUtils.getAFSUrl() + TestInstanceHostUtils.getAFSPath());
    }
}
