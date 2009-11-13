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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.Map;

import com.extjs.gxt.ui.client.data.BaseModel;

/**
 * A Subclass of the GXT BaseModel that, by default, turns off nested value support. 
 * 
 * NestedValues check for key strings with '.' in then treat them as paths for traversing multiple
 * HashMaps. 
 * 
 * We don't need this, and, in fact, it causes problems for us, since we occasionally use '.' as
 * part of a normal column name for a table without intending the nested-value semantics; thus we
 * turn it off.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class CISDBaseModel extends BaseModel
{

    private static final long serialVersionUID = 1L;

    /**
     *
     *
     */
    public CISDBaseModel()
    {
        super();
        setAllowNestedValues(false);
    }

    /**
     * @param properties
     */
    public CISDBaseModel(Map<String, Object> properties)
    {
        super(properties);
        setAllowNestedValues(false);
    }

}
