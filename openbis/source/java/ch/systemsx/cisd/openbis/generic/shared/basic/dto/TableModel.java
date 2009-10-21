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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A table with a list of rows and columns specification. Each column has header and type.
 * 
 * @author Tomasz Pylak
 */
public class TableModel implements IsSerializable, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private List<TableModelRow> rows;

    private List<TableModelColumnHeader> header;

    public TableModel(List<TableModelColumnHeader> header, List<TableModelRow> rows)
    {
        this.rows = rows;
        this.header = header;
        validate();
    }

    private void validate()
    {
        int columnsNo = header.size();
        for (TableModelRow row : rows)
        {
            assert row.getValues().size() == columnsNo : "row has a different number of columns than the table header";
        }
    }

    public List<TableModelRow> getRows()
    {
        return rows;
    }

    public List<TableModelColumnHeader> getHeader()
    {
        return header;
    }

    // GWT only
    @SuppressWarnings("unused")
    private TableModel()
    {
    }

    // GWT only
    @SuppressWarnings("unused")
    private void setRows(List<TableModelRow> rows)
    {
        this.rows = rows;
    }

    // GWT only
    @SuppressWarnings("unused")
    private void setHeader(List<TableModelColumnHeader> header)
    {
        this.header = header;
    }

    public static class TableModelColumnHeader implements IsSerializable, Serializable
    {
        private static final long serialVersionUID = ServiceVersionHolder.VERSION;

        private String title;

        // allows to fetch the value for this column from the row content
        private int index;

        public TableModelColumnHeader(String title, int index)
        {
            this.title = title;
            this.index = index;
        }

        public String getTitle()
        {
            return title;
        }

        public int getIndex()
        {
            return index;
        }

        // GWT only
        @SuppressWarnings("unused")
        private TableModelColumnHeader()
        {
        }

        // GWT only
        @SuppressWarnings("unused")
        private void setTitle(String title)
        {
            this.title = title;
        }

        // GWT only
        @SuppressWarnings("unused")
        private void setIndex(int index)
        {
            this.index = index;
        }
    }
}
