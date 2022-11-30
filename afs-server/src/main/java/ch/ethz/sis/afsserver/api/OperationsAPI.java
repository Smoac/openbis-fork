/*
 * Copyright 2022 ETH Zürich, SIS
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

package ch.ethz.sis.afsserver.api;

import java.io.File;
import java.util.List;

import lombok.NonNull;

public interface OperationsAPI
{

    @NonNull
    List<File> list(@NonNull String sourceOwner, @NonNull String source, boolean recursively)
            throws Exception;

    @NonNull
    byte[] read(@NonNull String sourceOwner, @NonNull String source, @NonNull long offset,
            @NonNull int limit) throws Exception;

    @NonNull
    boolean write(@NonNull String sourceOwner, @NonNull String source, @NonNull long offset,
            @NonNull byte[] data, @NonNull byte[] md5Hash) throws Exception;

    @NonNull
    boolean delete(@NonNull String sourceOwner, @NonNull String source) throws Exception;

    @NonNull
    boolean copy(@NonNull String sourceOwner, @NonNull String source, @NonNull String targetOwner,
            @NonNull String target) throws Exception;

    @NonNull
    boolean move(@NonNull String sourceOwner, @NonNull String source, @NonNull String targetOwner,
            @NonNull String target) throws Exception;

}
