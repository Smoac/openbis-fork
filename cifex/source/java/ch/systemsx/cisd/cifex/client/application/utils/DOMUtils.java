/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.client.application.utils;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

/**
 * Useful DOM utility methods.
 * 
 * @author Christian Ribeaud
 */
public final class DOMUtils
{
    private DOMUtils()
    {
        // Can not be instantiated.
    }

    /** A <code>HTML</code> break (<code>&lt;br&gt;</code>). */
    public static final String BR = DOM.toString(DOM.createElement("br"));

    /** Surrounds given <var>text</var> with italic HTML tags. */
    public static final String renderItalic(final String text)
    {
        final Element element = DOM.createElement("i");
        DOM.setInnerText(element, text);
        return DOM.toString(element);
    }

    /**
     * Looks in given <var>text</var> for given <var>tagName</var> and extracts its value (by removing the tag).
     */
    public final static String getElementValue(final String tagName, final String text)
    {
        assert tagName != null : "Tag name can not be null.";
        assert text != null : "Given text can not be null.";
        final String startTag = "<" + tagName + ">";
        final String endTag = "</" + tagName + ">";
        final int endIndex = text.indexOf(endTag);
        if (text.indexOf(startTag) < 0 || endIndex < 0)
        {
            return text;
        }
        return text.substring(startTag.length(), endIndex);
    }
}
