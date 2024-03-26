/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.afsserver.http;

import java.util.Arrays;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class HttpResponse {
    public static final String CONTENT_TYPE_TEXT = "text/plain";
    public static final String CONTENT_TYPE_BINARY_DATA = "application/octet-stream";
    public static final String CONTENT_TYPE_JSON = "application/json";

    private static final int MAX_TO_STRING_BODY_LENGTH = 1000;

    private final boolean error;
    private final String contentType;
    private final byte[] body;

    @Override
    public String toString()
    {
        final String bodyStr;

        if (body.length <= MAX_TO_STRING_BODY_LENGTH)
        {
            bodyStr = Arrays.toString(body);
        } else
        {
            final String tempBodyStr = Arrays.toString(Arrays.copyOf(body, MAX_TO_STRING_BODY_LENGTH));
            bodyStr = String.format("[%s...]", tempBodyStr.substring(1, tempBodyStr.length() - 1));
        }

        final StringBuilder sb = new StringBuilder("HttpResponse(");
        sb.append("error=").append(error);
        sb.append(", contentType='").append(contentType).append('\'');
        sb.append(", body=").append(bodyStr); // Cropping output to prevent logging too much data
        sb.append(')');
        return sb.toString();
    }


}
