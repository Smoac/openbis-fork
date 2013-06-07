/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.server;

/**
 * Basic constants.
 *
 * @author Franz-Josef Elmer
 */
public class Constants
{
    public static final String REQUEST_KEY = "_REQUEST_";
    
    public static final String GET_PARAMETER_DESCRIPTIONS_REQUEST = "getParameterDescriptions";
    
    public static final String EXCEPTION_COLUMN = "_EXCEPTION_";
    
    public static final String STACK_TRACE_CLASS_COLUMN = "_STACK_TRACE_CLASS_";
    
    public static final String STACK_TRACE_METHOD_NAME_COLUMN = "_STACK_TRACE_METHOD_NAME_";
    
    public static final String STACK_TRACE_FILE_NAME_COLUMN = "_STACK_TRACE_FILE_NAME_";
    
    public static final String STACK_TRACE_LINE_NUMBER_COLUMN = "_STACK_TRACE_LINE_NUMBER_";
    
    public static final String PARAMETER_DESCRIPTION_NAME_COLUMN = "name";
    
    public static final String PARAMETER_DESCRIPTION_TYPE_COLUMN = "type";
    
    public static final String FILE_NAME_COLUMN = "filename";
}
