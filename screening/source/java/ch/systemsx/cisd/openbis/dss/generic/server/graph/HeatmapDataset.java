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

package ch.systemsx.cisd.openbis.dss.generic.server.graph;

import org.jfree.data.DomainOrder;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class HeatmapDataset implements XYZDataset
{
    private final DefaultXYZDataset wrappedDataset;

    private Range range;

    public HeatmapDataset(DefaultXYZDataset dataset)
    {
        wrappedDataset = dataset;
    }

    /**
     * The range of the z values
     */
    public Range getRange()
    {
        return range;
    }

    /**
     * Set the range of the z-values
     */
    public void setRange(Range range)
    {
        this.range = range;
    }

    // XYZDataset
    public Number getZ(int series, int item)
    {
        return wrappedDataset.getZ(series, item);
    }

    public double getZValue(int series, int item)
    {
        return wrappedDataset.getZValue(series, item);
    }

    public DomainOrder getDomainOrder()
    {
        return wrappedDataset.getDomainOrder();
    }

    public int getItemCount(int series)
    {
        return wrappedDataset.getItemCount(series);
    }

    public Number getX(int series, int item)
    {
        return wrappedDataset.getX(series, item);
    }

    public double getXValue(int series, int item)
    {
        return wrappedDataset.getXValue(series, item);
    }

    public Number getY(int series, int item)
    {
        return wrappedDataset.getY(series, item);
    }

    public double getYValue(int series, int item)
    {
        return wrappedDataset.getYValue(series, item);
    }

    public int getSeriesCount()
    {
        return wrappedDataset.getSeriesCount();
    }

    // The JFreeChart interface does not use generics
    @SuppressWarnings("unchecked")
    public Comparable getSeriesKey(int series)
    {
        return wrappedDataset.getSeriesKey(series);
    }

    @SuppressWarnings("unchecked")
    public int indexOf(Comparable seriesKey)
    {
        return wrappedDataset.indexOf(seriesKey);
    }

    public void addChangeListener(DatasetChangeListener arg0)
    {
        wrappedDataset.addChangeListener(arg0);
    }

    public DatasetGroup getGroup()
    {
        return wrappedDataset.getGroup();
    }

    public void removeChangeListener(DatasetChangeListener arg0)
    {
        wrappedDataset.removeChangeListener(arg0);
    }

    public void setGroup(DatasetGroup arg0)
    {
        wrappedDataset.setGroup(arg0);
    }
}
