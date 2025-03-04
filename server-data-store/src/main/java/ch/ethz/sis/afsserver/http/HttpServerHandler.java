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

import io.netty.handler.codec.http.HttpMethod;

import java.util.List;
import java.util.Map;

public interface HttpServerHandler {

    /*
     * The path that indicates the webserver that should use this handler
     */
    public String getPath();

    /*
     * The handler
     */
    public HttpResponse process(HttpMethod method, Map<String, List<String>> uriParameters, byte[] requestBody);
}
