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

package ch.systemsx.cisd.openbis.generic.client.web.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.ColumnModelEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IUpdater;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ColumnSetting;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DisplaySettingsManagerTest extends AssertJUnit
{
    private final class ColumnModelMatcher extends BaseMatcher<ColumnModel>
    {
        private final ColumnConfig[] columnConfigs;
        
        private String message;

        ColumnModelMatcher(ColumnConfig... columnConfigs)
        {
            this.columnConfigs = columnConfigs;
        }
        
        public void describeTo(Description description)
        {
            description.appendText(message);
        }

        public boolean matches(Object item)
        {
            if (item instanceof ColumnModel == false)
            {
                return false;
            }
            ColumnModel columnModel = (ColumnModel) item;
            if (columnConfigs.length != columnModel.getColumnCount())
            {
                message =
                        columnConfigs.length + " columns expected instead of "
                                + columnModel.getColumnCount();
                return false;
            }
            for (int i = 0; i < columnConfigs.length; i++)
            {
                String prefix = "[" + i + "]: ";
                ColumnConfig columnConfig = columnConfigs[i];
                if (columnConfig.getId().equals(columnModel.getColumnId(i)) == false)
                {
                    message =
                            prefix + "ID " + columnConfig.getId() + " expected instead of "
                                    + columnModel.getColumnId(i);
                    return false;
                }
                if (columnConfig.isHidden() != columnModel.isHidden(i))
                {
                    message =
                            prefix + "Hidden flag " + columnConfig.isHidden()
                                    + " expected instead of " + columnModel.isHidden(i);
                    return false;
                }
                if (columnConfig.getWidth() != columnModel.getColumnWidth(i))
                {
                    message =
                        prefix + "Width " + columnConfig.getWidth()
                        + " expected instead of " + columnModel.getColumnWidth(i);
                    return false;
                }
            }
            return true;
        }
    }

    private static final String DISPLAY_TYPE_ID = "id1";
    
    private Mockery context;
    private IUpdater updater;
    private IGrid<BeanModel> grid;
    private ListStore<BeanModel> listStore;
    private DisplaySettingsManager manager;
    private DisplaySettings displaySettings;

    private ColumnModelEvent event;

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        updater = context.mock(IUpdater.class);
        grid = context.mock(IGrid.class);
        listStore = new ListStore<BeanModel>();
        displaySettings = new DisplaySettings();
        manager = new DisplaySettingsManager(displaySettings, updater);
        event = new ColumnModelEvent(null);
    }
    
    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testPrepareForUnknownDisplayTypeID()
    {
        final ColumnModel columnModel = new ColumnModel(new ArrayList<ColumnConfig>());
        context.checking(new Expectations()
            {
                {
                    one(grid).getColumnModel();
                    will(returnValue(columnModel));
                }
            });
        
        manager.prepareGrid(DISPLAY_TYPE_ID, grid);
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testPrepareForUnchangedColumnSettings()
    {
        ColumnConfig c1 = createColumnConfig("c1", false, 42);
        ColumnConfig c2 = createColumnConfig("c2", true, 4711);
        final ColumnModel columnModel = new ColumnModel(Arrays.asList(c1, c2));
        List<ColumnSetting> settings = Arrays.asList(createColumnSetting(c1), createColumnSetting(c2));
        displaySettings.getColumnSettings().put(DISPLAY_TYPE_ID, settings);
        context.checking(new Expectations()
            {
                {
                    exactly(2).of(grid).getColumnModel();
                    will(returnValue(columnModel));
                }
            });
        
        manager.prepareGrid(DISPLAY_TYPE_ID, grid);
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testPrepareForChangedHiddenFlag()
    {
        ColumnConfig c1 = createColumnConfig("c1", false, 42);
        final ColumnConfig c2 = createColumnConfig("c2", true, 4711);
        final ColumnModel columnModel = new ColumnModel(Arrays.asList(c1, c2));
        final ColumnSetting c1Setting = createColumnSetting(c1);
        c1Setting.setHidden(true);
        List<ColumnSetting> settings = Arrays.asList(c1Setting, createColumnSetting(c2));
        displaySettings.getColumnSettings().put(DISPLAY_TYPE_ID, settings);
        context.checking(new Expectations()
            {
                {
                    exactly(2).of(grid).getColumnModel();
                    will(returnValue(columnModel));
                    
                    one(grid).getStore();
                    will(returnValue(listStore));
                    
                    one(grid).reconfigure(with(equal(listStore)),
                            with(new ColumnModelMatcher(createColumnConfig(c1Setting), c2)));
                }
            });
        
        manager.prepareGrid(DISPLAY_TYPE_ID, grid);
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testPrepareForChangedColumnOrder()
    {
        final ColumnConfig c1 = createColumnConfig("c1", false, 42);
        final ColumnConfig c2 = createColumnConfig("c2", true, 4711);
        final ColumnModel columnModel = new ColumnModel(Arrays.asList(c1, c2));
        ColumnSetting c1Setting = createColumnSetting(c1);
        ColumnSetting c2Setting = createColumnSetting(c2);
        List<ColumnSetting> settings = Arrays.asList(c2Setting, c1Setting);
        displaySettings.getColumnSettings().put(DISPLAY_TYPE_ID, settings);
        context.checking(new Expectations()
            {
                {
                    exactly(2).of(grid).getColumnModel();
                    will(returnValue(columnModel));

                    one(grid).getStore();
                    will(returnValue(listStore));

                    one(grid).reconfigure(with(equal(listStore)),
                            with(new ColumnModelMatcher(c2, c1)));
                }
            });
        
        manager.prepareGrid(DISPLAY_TYPE_ID, grid);
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testPrepareForColumnNoLongerExist()
    {
        final ColumnConfig c1 = createColumnConfig("c1", false, 42);
        final ColumnConfig c2 = createColumnConfig("c2", true, 4711);
        final ColumnModel columnModel = new ColumnModel(Arrays.asList(c2));
        ColumnSetting c1Setting = createColumnSetting(c1);
        ColumnSetting c2Setting = createColumnSetting(c2);
        List<ColumnSetting> settings = Arrays.asList(c1Setting, c2Setting);
        displaySettings.getColumnSettings().put(DISPLAY_TYPE_ID, settings);
        context.checking(new Expectations()
            {
                {
                    exactly(2).of(grid).getColumnModel();
                    will(returnValue(columnModel));

                    one(grid).getStore();
                    will(returnValue(listStore));

                    one(grid).reconfigure(with(equal(listStore)), with(new ColumnModelMatcher(c2)));
                }
            });
        
        manager.prepareGrid(DISPLAY_TYPE_ID, grid);
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testPrepareForNewColumn()
    {
        final ColumnConfig c1 = createColumnConfig("c1", false, 42);
        final ColumnConfig c2 = createColumnConfig("c2", true, 4711);
        final ColumnModel columnModel = new ColumnModel(Arrays.asList(c1, c2));
        ColumnSetting c2Setting = createColumnSetting(c2);
        List<ColumnSetting> settings = Arrays.asList(c2Setting);
        displaySettings.getColumnSettings().put(DISPLAY_TYPE_ID, settings);
        context.checking(new Expectations()
            {
                {
                    exactly(2).of(grid).getColumnModel();
                    will(returnValue(columnModel));

                    one(grid).getStore();
                    will(returnValue(listStore));

                    one(grid).reconfigure(with(equal(listStore)),
                            with(new ColumnModelMatcher(c2, c1)));
                }
            });
        
        manager.prepareGrid(DISPLAY_TYPE_ID, grid);
        
        context.assertIsSatisfied();
    }
    
    
    @Test
    public void testPrepareForChangedColumnWidth()
    {
        ColumnConfig c1 = createColumnConfig("c1", false, 42);
        final ColumnConfig c2 = createColumnConfig("c2", true, 4711);
        final ColumnModel columnModel = new ColumnModel(Arrays.asList(c1, c2));
        final ColumnSetting c1Setting = createColumnSetting(c1);
        c1Setting.setWidth(24);
        List<ColumnSetting> settings = Arrays.asList(c1Setting, createColumnSetting(c2));
        displaySettings.getColumnSettings().put(DISPLAY_TYPE_ID, settings);
        context.checking(new Expectations()
            {
                {
                    exactly(2).of(grid).getColumnModel();
                    will(returnValue(columnModel));

                    one(grid).getStore();
                    will(returnValue(listStore));

                    one(grid).reconfigure(with(equal(listStore)),
                            with(new ColumnModelMatcher(createColumnConfig(c1Setting), c2)));
                }
            });

        manager.prepareGrid(DISPLAY_TYPE_ID, grid);
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testUpdateAfterHiddenChangedEvent()
    {
        testUpdateAfterEvent(Events.HiddenChange);
    }
    
    @Test
    public void testUpdateAfterWidthChangedEvent()
    {
        testUpdateAfterEvent(Events.WidthChange);
    }
    
    private void testUpdateAfterEvent(int eventType)
    {
        ColumnConfig column1 = createColumnConfig("c1", false, 42);
        ColumnConfig column2 = createColumnConfig("c2", true, 4711);
        final ColumnModel columnModel = new ColumnModel(Arrays.asList(column1, column2));
        context.checking(new Expectations()
            {
                {
                    allowing(grid).getColumnModel();
                    will(returnValue(columnModel));

                    one(updater).update();
                }
            });
        
        manager.prepareGrid(DISPLAY_TYPE_ID, grid);
        columnModel.fireEvent(eventType, event);
        
        List<ColumnSetting> columnSettings = displaySettings.getColumnSettings().get(DISPLAY_TYPE_ID);
        assertEquals(2, columnSettings.size());
        assertEquals(false, columnSettings.get(0).isHidden());
        assertEquals(42, columnSettings.get(0).getWidth());
        assertEquals("c1", columnSettings.get(0).getColumnID());
        assertEquals(true, columnSettings.get(1).isHidden());
        assertEquals(4711, columnSettings.get(1).getWidth());
        assertEquals("c2", columnSettings.get(1).getColumnID());
        
        context.assertIsSatisfied();
    }
    
    private ColumnConfig createColumnConfig(ColumnSetting columnSetting)
    {
        return createColumnConfig(columnSetting.getColumnID(), columnSetting.isHidden(),
                columnSetting.getWidth());
    }
    
    private ColumnConfig createColumnConfig(String id, boolean hidden, int width)
    {
        ColumnConfig columnConfig = new ColumnConfig(id, "<" + id + ">", width);
        columnConfig.setHidden(hidden);
        return columnConfig;
    }
    
    private ColumnSetting createColumnSetting(ColumnConfig columnConfig)
    {
        ColumnSetting columnSetting = new ColumnSetting();
        columnSetting.setColumnID(columnConfig.getId());
        columnSetting.setHidden(columnConfig.isHidden());
        columnSetting.setWidth(columnConfig.getWidth());
        return columnSetting;
    }
}
