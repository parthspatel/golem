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

package cloud.golem.sdk.agentic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a Golem agent.
 *
 * <p>Classes annotated with {@code @Agent} will be registered with the Golem runtime
 * and can be instantiated and invoked through the Golem API.
 *
 * <p>Example:
 * <pre>{@code
 * @Agent(name = "my-counter")
 * public class CounterAgent extends BaseAgent {
 *     private int count = 0;
 *
 *     public int increment() {
 *         count++;
 *         return count;
 *     }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Agent {

    /**
     * The name of the agent. If not specified, the class name will be used.
     *
     * @return The agent name
     */
    String name() default "";

    /**
     * A description of the agent.
     *
     * @return The agent description
     */
    String description() default "";
}
