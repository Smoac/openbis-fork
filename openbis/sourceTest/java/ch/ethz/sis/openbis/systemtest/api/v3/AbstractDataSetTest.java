package ch.ethz.sis.openbis.systemtest.api.v3;

import static ch.systemsx.cisd.common.test.AssertionUtil.assertCollectionContainsOnly;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;

public class AbstractDataSetTest extends AbstractTest
{

    protected static void assertIdentifiers(Collection<DataSet> dataSets, String... expectedCodesIdentifiers)
    {
        Set<String> actualSet = new HashSet<String>();
        for (DataSet dataSet : dataSets)
        {
            actualSet.add(dataSet.getPermId().getPermId());
        }

        assertCollectionContainsOnly(actualSet, expectedCodesIdentifiers);
    }

}
