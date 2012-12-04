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

package ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.lemnik.eodsql.DataIterator;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IEntityPropertiesEnricher;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocationNode;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses =
    { IDatasetListingQuery.class })
public class DatasetListerFastTest extends AssertJUnit
{
    private static final String DSS_URL = "dss-url";

    private static final String DSS_CODE = "DSS";

    private Mockery context;

    private IDatasetListingQuery query;

    private DatasetLister datasetLister;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        query = context.mock(IDatasetListingQuery.class);
        IEntityPropertiesEnricher propertiesEnricher =
                context.mock(IEntityPropertiesEnricher.class);
        datasetLister =
                new DatasetLister(42L, new DatabaseInstance(), query, propertiesEnricher, null,
                        null, null);
    }

    @AfterMethod
    public void afterMethod(Method m)
    {
        try
        {
            context.assertIsSatisfied();
        } catch (Throwable t)
        {
            // assert expectations were met, including the name of the failed method
            throw new Error(m.getName() + "() : ", t);
        }
    }

    @Test
    public void testListLocationsByDatasetCode()
    {
        context.checking(new Expectations()
            {
                {
                    one(query).listLocationsByDatasetCode("ds-1");
                    will(returnValue(new WrappingDataIterator<DatasetLocationNodeRecord>(Arrays
                            .<DatasetLocationNodeRecord> asList(
                                    location(2L, "ds-c1", "a/b/c/1", 1L),
                                    location(3L, "ds-c2", "a/b/c/2", 1L),
                                    location(1L, "ds-1", null, null)))));
                }
            });

        IDatasetLocationNode location = datasetLister.listLocationsByDatasetCode("ds-1");

        assertEquals("ds-1", location.getLocation().getDataSetCode());
        assertEquals(null, location.getLocation().getDataSetLocation());
        assertEquals(DSS_CODE, location.getLocation().getDataStoreCode());
        assertEquals(DSS_URL, location.getLocation().getDataStoreUrl());
        assertEquals(true, location.isContainer());
        List<IDatasetLocationNode> components =
                new ArrayList<IDatasetLocationNode>(location.getComponents());
        Collections.sort(components, new Comparator<IDatasetLocationNode>()
            {
                @Override
                public int compare(IDatasetLocationNode n1, IDatasetLocationNode n2)
                {
                    return n1.getLocation().getDataSetCode()
                            .compareTo(n2.getLocation().getDataSetCode());
                }
            });
        assertEquals("ds-c1", components.get(0).getLocation().getDataSetCode());
        assertEquals("a/b/c/1", components.get(0).getLocation().getDataSetLocation());
        assertEquals("ds-c2", components.get(1).getLocation().getDataSetCode());
        assertEquals("a/b/c/2", components.get(1).getLocation().getDataSetLocation());
    }

    private DatasetLocationNodeRecord location(long id, String code, String location,
            Long containerID)
    {
        DatasetLocationNodeRecord record = new DatasetLocationNodeRecord();
        record.id = id;
        record.code = code;
        record.data_store_code = DSS_CODE;
        record.data_store_url = DSS_URL;
        record.location = location;
        record.ctnr_id = containerID;
        return record;
    }

    private static final class WrappingDataIterator<E> implements DataIterator<E>
    {
        private final Collection<E> collection;

        private final Iterator<E> iterator;

        public WrappingDataIterator(Collection<E> collection)
        {
            this.collection = collection;
            iterator = collection.iterator();
        }

        @Override
        public boolean hasNext()
        {
            return iterator.hasNext();
        }

        @Override
        public E next()
        {
            return iterator.next();
        }

        @Override
        public void remove()
        {
            iterator.remove();
        }

        @Override
        public Iterator<E> iterator()
        {
            return collection.iterator();
        }

        @Override
        public void close()
        {
        }

        @Override
        public boolean isClosed()
        {
            return false;
        }
    }

}
