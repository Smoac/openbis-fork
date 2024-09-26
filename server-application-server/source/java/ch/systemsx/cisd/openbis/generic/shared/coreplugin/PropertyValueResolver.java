/*
 * Copyright ETH 2024 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.coreplugin;

import java.util.Properties;
import java.util.function.Function;

final class PropertyValueResolver {

    private static final String PLACEHOLDER_PREFIX = "${";
    private static final String PLACEHOLDER_SUFFIX = "}";
    private static final String PLACEHOLDER_SEPARATOR = ":";

    private final Properties properties;
    private final Function<String, String> getSystemPropertyMethod;
    private final Function<String, String> getEnvVariableMethod;

    public PropertyValueResolver(Properties properties) {
        this(properties, System::getProperty, System::getenv);
    }

    // Introduced for testability
    PropertyValueResolver(Properties properties, Function<String, String> getSystemPropertyMethod, Function<String, String> getEnvVariableMethod) {
        this.properties = properties;
        this.getSystemPropertyMethod = getSystemPropertyMethod;
        this.getEnvVariableMethod = getEnvVariableMethod;
    }

    public String resolvePropertyValue(final String propertyValue)
    {
        if(propertyValue != null && !propertyValue.isEmpty()) {
            String value = propertyValue.strip();
            if(value.startsWith(PLACEHOLDER_PREFIX) && value.endsWith(PLACEHOLDER_SUFFIX)) {
                value = value.substring(2, value.length()-1);
                String key;
                String defaultValue;
                if(value.contains(PLACEHOLDER_SEPARATOR)) {
                    key = value.substring(0, value.indexOf(PLACEHOLDER_SEPARATOR));
                    defaultValue = value.substring(value.indexOf(PLACEHOLDER_SEPARATOR)+1);
                } else {
                    key = value;
                    defaultValue = propertyValue;
                }
                return getValue(key, defaultValue);
            }
        }
        return propertyValue;
    }

    private String getValue(String key, String defaultValue) {
        String value = getSystemPropertyMethod.apply(key);
        if (value == null) {
            value = getEnvVariableMethod.apply(key);
        }
        if(value == null) {
            value = properties.getProperty(key);
        }
        if(value == null) {
            value = defaultValue;
        }
        return value;
    }



}
