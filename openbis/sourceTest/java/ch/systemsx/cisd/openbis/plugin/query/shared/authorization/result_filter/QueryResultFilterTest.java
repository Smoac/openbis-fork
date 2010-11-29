/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.shared.authorization.result_filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.plugin.query.shared.authorization.IAuthorizationChecker;

/**
 * Test cases for {@link QueryResultFilter}.
 * 
 * @author Izabela Adamczyk
 */
@Friend(toClasses = QueryResultFilter.class)
public class QueryResultFilterTest extends AssertJUnit
{

    private static final String KEY2 = "key2";

    private static final String KEY1 = "key1";

    private Mockery context;

    private IGroupLoaderFactory loaderFactory;

    private IGroupLoader loader;

    private IAuthorizationChecker authorizationChecker;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        loaderFactory = context.mock(IGroupLoaderFactory.class);
        loader = context.mock(IGroupLoader.class);
        authorizationChecker = context.mock(IAuthorizationChecker.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    private QueryResultFilter createFilter()
    {
        return new QueryResultFilter(loaderFactory, authorizationChecker);
    }

    @Test
    public void testFilterEmptyResult() throws Exception
    {
        PersonPE person = new PersonPE();
        TableModel before =
                new TableModel(new ArrayList<TableModelColumnHeader>(),
                        new ArrayList<TableModelRow>());
        context.checking(new Expectations()
            {
                {
                    one(loaderFactory).create(EntityKind.EXPERIMENT);
                    will(returnValue(loader));

                    one(loaderFactory).create(EntityKind.SAMPLE);
                    will(returnValue(loader));

                    one(loaderFactory).create(EntityKind.DATA_SET);
                    will(returnValue(loader));

                    exactly(3).of(loader).loadGroups(new HashSet<String>());
                    will(returnValue(new HashMap<String, SpacePE>()));
                }
            });

        TableModel after = createFilter().filterResults(person, before);
        assertEquals(before.getRows().size(), after.getRows().size());
        context.assertIsSatisfied();
    }

    @Test
    public void testFilterNoMagicColumnResult() throws Exception
    {
        PersonPE person = new PersonPE();
        ArrayList<TableModelColumnHeader> header = new ArrayList<TableModelColumnHeader>();
        header.add(new TableModelColumnHeader("title", "t", 1));
        ArrayList<TableModelRow> rows = new ArrayList<TableModelRow>();
        ArrayList<ISerializableComparable> values = new ArrayList<ISerializableComparable>();
        values.add(new StringTableCell("value"));
        rows.add(new TableModelRow(values));
        rows.add(new TableModelRow(values));
        TableModel before = new TableModel(header, rows);
        context.checking(new Expectations()
            {
                {
                    one(loaderFactory).create(EntityKind.EXPERIMENT);
                    will(returnValue(loader));

                    one(loaderFactory).create(EntityKind.SAMPLE);
                    will(returnValue(loader));

                    one(loaderFactory).create(EntityKind.DATA_SET);
                    will(returnValue(loader));

                    exactly(3).of(loader).loadGroups(new HashSet<String>());
                    will(returnValue(new HashMap<String, SpacePE>()));
                }
            });

        TableModel after = createFilter().filterResults(person, before);
        assertEquals(2, after.getRows().size());
        assertEquals(before.getRows().size(), after.getRows().size());
        context.assertIsSatisfied();
    }

    @Test
    public void testFilterOneMagicColumnResult() throws Exception
    {
        final PersonPE person = new PersonPE();
        ArrayList<TableModelColumnHeader> header = new ArrayList<TableModelColumnHeader>();
        TableModelColumnHeader magicHeader = new TableModelColumnHeader("Sample", "s", 1);
        magicHeader.setEntityKind(EntityKind.SAMPLE);
        header.add(magicHeader);
        ArrayList<TableModelRow> rows = new ArrayList<TableModelRow>();
        ArrayList<ISerializableComparable> values1 = new ArrayList<ISerializableComparable>();
        values1.add(new StringTableCell(KEY1));
        rows.add(new TableModelRow(values1));
        ArrayList<ISerializableComparable> values2 = new ArrayList<ISerializableComparable>();
        values2.add(new StringTableCell(KEY2));
        rows.add(new TableModelRow(values2));
        TableModel before = new TableModel(header, rows);
        context.checking(new Expectations()
            {
                {
                    one(loaderFactory).create(EntityKind.EXPERIMENT);
                    will(returnValue(loader));

                    one(loaderFactory).create(EntityKind.SAMPLE);
                    will(returnValue(loader));

                    one(loaderFactory).create(EntityKind.DATA_SET);
                    will(returnValue(loader));

                    HashSet<String> keys = new HashSet<String>();
                    keys.add(KEY1);
                    keys.add(KEY2);

                    one(loader).loadGroups(keys);
                    HashMap<String, SpacePE> map = new HashMap<String, SpacePE>();
                    SpacePE group = new SpacePE();
                    map.put(KEY1, group);
                    will(returnValue(map));

                    exactly(2).of(loader).loadGroups(new HashSet<String>());
                    will(returnValue(new HashMap<String, SpacePE>()));

                    one(authorizationChecker).isAuthorized(person, group, RoleWithHierarchy.SPACE_OBSERVER);
                    will(returnValue(true));

                    one(authorizationChecker).isAuthorized(person, null, RoleWithHierarchy.SPACE_OBSERVER);
                    will(returnValue(false));
                }
            });

        TableModel after = createFilter().filterResults(person, before);
        assertEquals(1, after.getRows().size());
        context.assertIsSatisfied();
    }
}
