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

package ch.systemsx.cisd.cifex.client.application.ui;

import ch.systemsx.cisd.cifex.client.application.Constants;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.client.dto.File;

/**
 * @author Bernd Rinn
 */
public class CommentRenderer
{

    private static final int COMMENT_MAX_LENGTH = 20;

    /**
     * Nicely renders given <code>comment</code>.
     */
    public final static String createCommentAnchor(final File file)
    {
        final String comment = StringUtils.isBlank(file.getComment()) ? "-" : file.getComment();
        if (StringUtils.isBlank(file.getComment()))
        {
            return comment;
        } else
        {
            final String abbreviatedComment = StringUtils.abbreviate(comment, COMMENT_MAX_LENGTH);
            return DOMUtils.createAnchor(comment, abbreviatedComment, Constants.SHOW_COMMENT_ID);
        }
    }
}
