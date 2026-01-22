/*
 * Copyright 2024-2025 Golem Cloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.golem.java.agentic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Exposes an agent method as an HTTP endpoint.
 *
 * <p>Example:
 * <pre>{@code
 * @Endpoint(method = "POST", path = "/items")
 * public Item createItem(String name) {
 *     return new Item(name);
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Endpoint {

    /**
     * The HTTP method (GET, POST, PUT, DELETE, etc.).
     *
     * @return The HTTP method
     */
    String method() default "GET";

    /**
     * The URL path for the endpoint. Defaults to the method name if not specified.
     *
     * @return The URL path
     */
    String path() default "";
}
