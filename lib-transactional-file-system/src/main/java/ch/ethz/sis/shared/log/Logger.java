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
package ch.ethz.sis.shared.log;

public interface Logger
{
    //
    // Trace API - Used for debugging, not intended for production usage
    //
    boolean isTraceEnabled();

    void traceAccess(String message, Object... args);

    void traceAccess(String message, Throwable ex, Object... args);

    <R> R traceExit(R arg);

    //
    // Catching API - Used to record errors
    //
    boolean isErrorEnabled();

    void catching(Throwable ex);

    <T extends Throwable> T throwing(T ex);

    //
    // INFO API - Used to record important system events
    //

    boolean isInfoEnabled();

    void info(String message, Object... args);

    void info(String message, Throwable ex, Object... args);

}