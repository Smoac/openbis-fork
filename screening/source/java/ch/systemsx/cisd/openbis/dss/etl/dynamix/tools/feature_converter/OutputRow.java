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

package ch.systemsx.cisd.openbis.dss.etl.dynamix.tools.feature_converter;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.annotation.BeanProperty;

/**
 * @author Izabela Adamczyk
 */
final class OutputRow extends Features
{

    private static final String CATEGORY = "category";

    private static final String WELL_NAME = "WellName";

    private String wellName;

    private String category;

    public String getWellName()
    {
        return wellName;
    }

    @BeanProperty(label = WELL_NAME)
    public void setWellName(String wellName)
    {
        this.wellName = wellName;
    }

    public String getCategory()
    {
        return category;
    }

    @BeanProperty(label = CATEGORY)
    public void setCategory(String category)
    {
        this.category = category;
    }

    /** NOTE: Order strictly connected with {@link #getColumns()} */
    public static List<String> getHeaderColumns()
    {
        ArrayList<String> list = new ArrayList<String>();
        list.add(WELL_NAME);
        list.add(Features.asHeader(CATEGORY, "Category"));
        list.addAll(Features/* super class */.getHeaderColumns());
        return list;
    }

    /** NOTE: Order strictly connected with {@link #getHeaderColumns()} */
    @Override
    public List<String> getColumns()
    {
        ArrayList<String> list = new ArrayList<String>();
        list.add(getWellName());
        list.add(getCategory());
        list.addAll(super.getColumns());
        return list;
    }

}