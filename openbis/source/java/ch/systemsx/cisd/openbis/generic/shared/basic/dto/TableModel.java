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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A table with a list of rows and columns specification. Each column has header and type.
 * 
 * @author Tomasz Pylak
 */
// TODO 2009-07-02, Tomasz Pylak: implement me. This stub holds just one string.
public class TableModel implements IsSerializable, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String content;

    public TableModel(String content)
    {
        this.content = content;
    }

    public String getContent()
    {
        return content;
    }

    // GWT only
    @SuppressWarnings("unused")
    private TableModel()
    {
    }

    // GWT only
    @SuppressWarnings("unused")
    private void setContent(String content)
    {
        this.content = content;
    }

}
