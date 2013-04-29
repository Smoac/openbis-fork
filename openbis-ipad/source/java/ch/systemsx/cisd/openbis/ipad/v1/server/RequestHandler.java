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

package ch.systemsx.cisd.openbis.ipad.v1.server;

/**
 * Abstract superclass for the handlers for concrete requests like ROOT.
 * <p>
 * This superclass defines behavior common to all requests.
 * <p>
 * Subclasses need to implement the method optional_headers(), which returns a list of the optional
 * headers they fill out.
 * <p>
 * Subclasses should implement retrieve_data to get the data they provide.
 * <p>
 * Subclasses should implement add_data_rows. In this method, they should call add_row. The method
 * add_row takes a dictionary as an argument. The keys of the dictionary match the headers in the
 * result columns. The dictionary should include data for the required columns and optional ones
 * they fill.
 * 
 * @author cramakri
 */
public class RequestHandler
{

}
