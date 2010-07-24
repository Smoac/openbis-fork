/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess;

import java.sql.SQLException;
import java.util.List;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOWithoutContextTest;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Tests for {@link IScreeningQuery}.
 * 
 * @author Tomasz Pylak
 */
@ContextConfiguration(locations = "classpath:screening-applicationContext.xml")
// In 'commonContext.xml', our transaction manager is called 'transaction-manager' (by default
// Spring looks for 'transactionManager').
@TransactionConfiguration(transactionManager = "transaction-manager")
@Friend(toClasses =
    { IScreeningQuery.class, WellContent.class })
@Test(groups =
    { "db", "screening" })
public class ScreeningDAOTest extends AbstractDAOWithoutContextTest
{

    static
    {
        System.setProperty("script-folder", "../openbis/sourceTest");
        System.setProperty("mass-upload-folder", "../openbis/sourceTest/sql/postgresql");

    }

    private IScreeningQuery query;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        query = createQuery(daoFactory);
    }

    private static IScreeningQuery createQuery(IDAOFactory daoFactory)
    {
        return EntityListingTestUtils.createQuery(daoFactory, IScreeningQuery.class);
    }

    @Test
    public void testGetPlateLocationsForOneMaterial()
    {
        // it just tests if the sql is correct
        List<WellContent> locations =
                EntityListingTestUtils.asList(query.getPlateLocationsForMaterialId(1, 1));
        AssertJUnit.assertEquals(0, locations.size());
    }

    @Test
    public void testGetPlateLocationsForManyMaterials()
    {
        String[] materialTypeCodes = new String[]
            { ScreeningConstants.GENE_PLUGIN_TYPE_CODE };
        // it just tests if the sql is correct
        List<WellContent> locations =
                EntityListingTestUtils.asList(query.getPlateLocationsForMaterialCodes(1,
                        new String[]
                            { "grs", "abc" }, materialTypeCodes));
        AssertJUnit.assertEquals(0, locations.size());
    }

}
