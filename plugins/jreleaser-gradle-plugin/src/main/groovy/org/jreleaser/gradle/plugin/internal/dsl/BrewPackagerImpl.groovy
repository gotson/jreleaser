/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileStatic
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.jreleaser.gradle.plugin.dsl.BrewPackager
import org.jreleaser.model.Brew

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class BrewPackagerImpl extends AbstractPackagerTool implements BrewPackager {
    final MapProperty<String, String> dependencies

    @Inject
    BrewPackagerImpl(ObjectFactory objects) {
        super(objects)
        dependencies = objects.mapProperty(String, String).convention(Providers.notDefined())
    }

    @Override
    protected String toolName() { 'brew' }

    @Override
    void addDependency(String key, String value) {
        if (isNotBlank(key) && isNotBlank(value)) {
            dependencies.put(key.trim(), value.trim())
        }
    }

    @Override
    void addDependency(String key) {
        if (isNotBlank(key)) {
            dependencies.put(key.trim(), '')
        }
    }

    @Override
    boolean isSet() {
        super.isSet() || dependencies.present
    }

    Brew toModel() {
        Brew tool = new Brew()
        fillToolProperties(tool)
        if (tap.isSet()) tool.tap = tap.toHomebrewTap()
        if (dependencies.present) tool.dependencies.putAll(dependencies.get())
        tool
    }
}
