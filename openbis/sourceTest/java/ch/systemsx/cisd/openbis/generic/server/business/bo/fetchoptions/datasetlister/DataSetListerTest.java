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

package ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.datasetlister;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.api.v1.ResourceNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.EntityListingTestUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;

/**
 * Test cases for {@link IDataSetListingQuery}.
 * 
 * @author Piotr Kupczyk
 */
@Test(groups =
    { "db", "dataset" })
public class DataSetListerTest extends AbstractDAOTest
{

    @Resource(name = ResourceNames.GENERAL_INFORMATION_SERVICE_SERVER)
    IGeneralInformationService service;

    private IDataSetLister lister;

    private String sessionToken;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        sessionToken = service.tryToAuthenticateForAllServices("test", "password");
        lister =
                new DataSetLister(EntityListingTestUtils.createQuery(daoFactory,
                        IDataSetListingQuery.class));
    }

    @Test
    public void testGetDataSetMetaDataForEmptyDataSetCodesShouldReturnEmptyList()
    {
        List<String> codes = new ArrayList<String>();
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();

        List<DataSet> result = lister.getDataSetMetaData(codes, fetchOptions);

        assertEqualToDataSetMetaDataWithCodes(codes, fetchOptions, result);
    }

    @Test
    public void testGetDataSetMetaDataForExistingDataSetCodeShouldReturnDataSet()
    {
        List<String> codes = Collections.singletonList("20081105092159188-3");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();

        List<DataSet> result = lister.getDataSetMetaData(codes, fetchOptions);

        assertEqualToDataSetMetaDataWithCodes(codes, fetchOptions, result);
    }

    @Test
    public void testGetDataSetMetaDataForExistingDataSetCodesShouldReturnDataSets()
    {
        List<String> codes = new ArrayList<String>();
        codes.add("20081105092159188-3");
        codes.add("20081105092159111-1");
        codes.add("20081105092259000-19");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();

        List<DataSet> result = lister.getDataSetMetaData(codes, fetchOptions);

        assertEqualToDataSetMetaDataWithCodes(codes, fetchOptions, result);
    }

    @Test
    public void testGetDataSetMetaDataForExistingDataSetCodesWithParentsShouldReturnDataSetsWithParents()
    {
        List<String> codes = new ArrayList<String>();
        codes.add("20081105092159188-3");
        codes.add("20081105092159111-1");
        codes.add("20081105092259000-19");
        codes.add("20081105092259000-20");
        codes.add("20081105092259000-21");
        DataSetFetchOptions fetchOptions =
                new DataSetFetchOptions(DataSetFetchOption.BASIC, DataSetFetchOption.PARENTS);

        List<DataSet> result = lister.getDataSetMetaData(codes, fetchOptions);

        assertEqualToDataSetMetaDataWithCodes(codes, fetchOptions, result);
    }

    @Test
    public void testGetDataSetMetaDataForExistingDataSetCodesWithChildrenShouldReturnDataSetsWithChildren()
    {
        List<String> codes = new ArrayList<String>();
        codes.add("20081105092159188-3");
        codes.add("20081105092159111-1");
        codes.add("20081105092259000-19");
        codes.add("20081105092259000-20");
        codes.add("20081105092259000-21");
        DataSetFetchOptions fetchOptions =
                new DataSetFetchOptions(DataSetFetchOption.BASIC, DataSetFetchOption.CHILDREN);

        List<DataSet> result = lister.getDataSetMetaData(codes, fetchOptions);

        assertEqualToDataSetMetaDataWithCodes(codes, fetchOptions, result);
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testGetDataSetMetaDataForExistingAndNotExistingDataSetCodesShouldThrowException()
    {
        List<String> codes = new ArrayList<String>();
        codes.add("20081105092159188-3");
        codes.add("UNKNOWN-DATASET-CODE");
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();

        lister.getDataSetMetaData(codes, fetchOptions);
    }

    private static void sortDataSetsByCode(List<DataSet> dataSets)
    {
        Collections.sort(dataSets, new Comparator<DataSet>()
            {
                public int compare(DataSet o1, DataSet o2)
                {
                    return o1.getCode().compareTo(o2.getCode());
                }
            });
    }

    private void assertEqualToDataSetMetaDataWithCodes(List<String> expectedDataSetsCodes,
            DataSetFetchOptions expectedFetchOptions, List<DataSet> actualDataSets)
    {
        assertEquals(expectedDataSetsCodes.size(), actualDataSets.size());

        List<DataSet> expectedDataSets =
                service.getDataSetMetaData(sessionToken, expectedDataSetsCodes);

        sortDataSetsByCode(expectedDataSets);
        sortDataSetsByCode(actualDataSets);

        Iterator<DataSet> expectedIterator = expectedDataSets.iterator();
        Iterator<DataSet> actualIterator = actualDataSets.iterator();

        // results should be returned in the same order
        while (expectedIterator.hasNext() && actualIterator.hasNext())
        {
            DataSet expectedDataSet = expectedIterator.next();
            DataSet actualDataSet = actualIterator.next();
            assertNotNull(expectedDataSet);
            assertNotNull(actualDataSet);
            assertEqualsToDataSet(expectedDataSet, expectedFetchOptions, actualDataSet);
        }
    }

    private void assertEqualsToDataSet(DataSet expected, DataSetFetchOptions expectedFetchOptions,
            DataSet actual)
    {
        assertEquals(expected.getCode(), actual.getCode());
        assertEquals(expected.getExperimentIdentifier(), actual.getExperimentIdentifier());
        assertEquals(expected.getSampleIdentifierOrNull(), actual.getSampleIdentifierOrNull());
        assertEquals(expected.getDataSetTypeCode(), actual.getDataSetTypeCode());
        assertEquals(expected.isContainerDataSet(), actual.isContainerDataSet());
        assertEqualsToRegistrationDetails(expected.getRegistrationDetails(),
                expected.getRegistrationDetails());

        if (expectedFetchOptions.isSupersetOf(DataSetFetchOption.PARENTS))
        {
            assertCollections(expected.getParentCodes(), actual.getParentCodes());
        }

        if (expectedFetchOptions.isSupersetOf(DataSetFetchOption.CHILDREN))
        {
            assertCollections(expected.getChildrenCodes(), actual.getChildrenCodes());
        }

        assertEquals(expectedFetchOptions, actual.getFetchOptions());
    }

    private static <T extends Comparable<T>> void assertCollections(Collection<T> col1,
            Collection<T> col2)
    {
        ArrayList<T> a1 = new ArrayList<T>(col1);
        ArrayList<T> a2 = new ArrayList<T>(col2);

        Collections.sort(a1);
        Collections.sort(a2);

        assertEquals(a1, a2);
    }

    private void assertEqualsToRegistrationDetails(EntityRegistrationDetails expected,
            EntityRegistrationDetails actual)
    {
        assertFalse(expected == null ^ actual == null);

        if (expected != null && actual != null)
        {
            assertEquals(expected.getUserId(), actual.getUserId());
            assertEquals(expected.getUserFirstName(), actual.getUserFirstName());
            assertEquals(expected.getUserLastName(), actual.getUserLastName());
            assertEquals(expected.getUserEmail(), actual.getUserEmail());
            assertEquals(expected.getRegistrationDate(), actual.getRegistrationDate());
        }

    }
}