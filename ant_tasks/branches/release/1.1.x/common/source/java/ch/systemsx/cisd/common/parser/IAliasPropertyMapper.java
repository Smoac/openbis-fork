/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.parser;

/**
 * A <code>IPropertyMapper</code> extension that allows you to define an alias for a given property name.
 * 
 * @author Christian Ribeaud
 */
public interface IAliasPropertyMapper extends IPropertyMapper
{
    /** Sets an alias for given <code>propertyname</code>. */
    public void setAlias(String aliasName, String propertyName);
}
