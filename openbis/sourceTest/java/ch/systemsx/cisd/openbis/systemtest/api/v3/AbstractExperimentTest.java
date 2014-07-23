package ch.systemsx.cisd.openbis.systemtest.api.v3;

import static ch.systemsx.cisd.common.test.AssertionUtil.assertCollectionContainsOnly;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;

public class AbstractExperimentTest extends AbstractTest
{

    protected static void assertIdentifiers(Collection<Experiment> experiments, String... expectedIdentifiers)
    {
        Set<String> actualSet = new HashSet<String>();
        for (Experiment experiment : experiments)
        {
            actualSet.add(experiment.getIdentifier().getIdentifier());
        }

        assertCollectionContainsOnly(actualSet, expectedIdentifiers);
    }

}
