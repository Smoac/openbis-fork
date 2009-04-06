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

package ch.systemsx.cisd.openbis.generic.shared.basic;

/**
 * Common constants to be used both on client and server side for field validation.
 * 
 * @author Piotr Buczek
 */
public class CommonValidationConstants
{

    // A complete regexp for hyperlink may be very complex like this one:
    // http://internet.ls-la.net/folklore/url-regexpr.html
    // We should rather use sth simple for "safe" hyperlink - with no <>()[]
    /** a simple regular expression for "safe" hyperlinks */
    public static final String HYPERLINK_REGEXP = "[^<>()\\[\\]]*";
}
