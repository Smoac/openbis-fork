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
        assert text != null : "Given text can not be null.";
        final Element element = DOM.createElement("i");
        DOM.setInnerText(element, text);
        return DOM.toString(element);
    }

    /**
     * Looks in given <var>text</var> for given <var>tagName</var> and extracts its value (by
     * removing the tag).
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

    /**
     * Creates an anchor for an <var>email</var>.
     * 
     * @param innerText if blank, then given <var>email</var> is taken as inner text.
     */
    public final static String createEmailAnchor(final String email, final String innerText)
    {
        assert email != null : "Undefined email.";
        final Element anchor = DOMUtils.createBasicAnchorElement();
        DOM.setElementAttribute(anchor, "href", "mailto:" + email);
        DOM.setElementAttribute(anchor, "title", email);
        DOM.setInnerText(anchor, innerText == null ? email : innerText);
        return DOM.toString(anchor);
    }

    /**
     * Creates an anchor for an <var>tooltip</var>.
     * 
     * @param innerText if blank, then given <var>tooltip</var> is taken as inner text.
     */
    public final static String createAnchorWithTooltip(final String tooltip, final String innerText)
    {
        assert tooltip != null : "Undefined tooltip.";
        final Element anchor = DOMUtils.createBasicAnchorElement();
        DOM.setElementAttribute(anchor, "title", tooltip);
        DOM.setInnerText(anchor, innerText == null ? tooltip : innerText);
        return DOM.toString(anchor);
    }

    /** Creates a basic anchor element with <code>cifex-a</code> as style class. */
    public final static Element createBasicAnchorElement()
    {
        final Element anchor = DOM.createAnchor();
        DOM.setElementAttribute(anchor, "class", "cifex-a");
        return anchor;
    }

    /** Creates an anchor with given <var>value</var>. */
    public final static String createAnchor(final String value)
    {
        return createAnchor(null, value, null, null, null, false);
    }

    /** Creates an anchor with given <var>value</var> and given style <var>id</var>. */
    public final static String createAnchor(final String value, final String id)
    {
        assert value != null : "Undefined value.";
        return createAnchor(null, value, null, null, id, false);
    }

    /** Creates an anchor with given <var>value</var> and given style <var>id</var>. */
    public final static String createAnchor(final String title, final String value,
            final String id, final boolean html)
    {
        assert value != null : "Undefined value.";
        return createAnchor(title, value, null, null, id, html);
    }

    /**
     * Creates an anchor with given <var>value</var>, given <var>href</var>, <var>target</var>
     * and given <var>id</var>.
     * 
     * @param title
     */
    public final static String createAnchor(final String title, final String value, final String href,
            final String target, final String id, final boolean html)
    {
        assert value != null : "Undefined value.";
        final Element anchor = createBasicAnchorElement();
        DOM.setElementAttribute(anchor, "href", href == null ? "javascript:return void;" : href);
        if (title != null)
        {
            DOM.setElementAttribute(anchor, "title", title);
        } else if (href == null)
        {
            DOM.setElementAttribute(anchor, "title", value);
        } else
        {
            DOM.setElementAttribute(anchor, "title", href);
        }
        if (target != null)
        {
            DOM.setElementAttribute(anchor, "target", target);
        }
        if (id != null)
        {
            DOM.setElementAttribute(anchor, "id", id);
        }
        if (html)
        {
            DOM.setInnerHTML(anchor, value);
        } else
        {
            DOM.setInnerText(anchor, value);
        }
        return DOM.toString(anchor);
    }
}
