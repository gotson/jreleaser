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
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.jreleaser.gradle.plugin.dsl.Artifact
import org.jreleaser.gradle.plugin.dsl.Glob
import org.jreleaser.gradle.plugin.dsl.Java
import org.jreleaser.gradle.plugin.dsl.Jlink
import org.jreleaser.model.Active

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class JlinkImpl extends AbstractAssembler implements Jlink {
    String name
    final Property<String> imageName
    final Property<String> moduleName
    final ListProperty<String> args
    final SetProperty<String> moduleNames
    final JavaImpl java

    private final ArtifactImpl jdk
    private final ArtifactImpl mainJar
    private final NamedDomainObjectContainer<GlobImpl> jars
    final NamedDomainObjectContainer<ArtifactImpl> targetJdks
    
    @Inject
    JlinkImpl(ObjectFactory objects) {
        super(objects)

        imageName = objects.property(String).convention(Providers.notDefined())
        moduleName = objects.property(String).convention(Providers.notDefined())
        args = objects.listProperty(String).convention(Providers.notDefined())
        moduleNames = objects.setProperty(String).convention(Providers.notDefined())
        java = objects.newInstance(JavaImpl, objects)
        jdk = objects.newInstance(ArtifactImpl, objects)
        mainJar = objects.newInstance(ArtifactImpl, objects)

        targetJdks = objects.domainObjectContainer(ArtifactImpl, new NamedDomainObjectFactory<ArtifactImpl>() {
            @Override
            ArtifactImpl create(String name) {
                ArtifactImpl artifact = objects.newInstance(ArtifactImpl, objects)
                artifact.name = name
                artifact
            }
        })

        jars = objects.domainObjectContainer(GlobImpl, new NamedDomainObjectFactory<GlobImpl>() {
            @Override
            GlobImpl create(String name) {
                GlobImpl glob = objects.newInstance(GlobImpl, objects)
                glob.name = name
                glob
            }
        })
    }

    void setName(String name) {
        this.name = name
    }

    @Override
    void addArg(String arg) {
        if (isNotBlank(arg)) {
            args.add(arg.trim())
        }
    }

    @Override
    void jdk(Action<? super Artifact> action) {
        action.execute(jdk)
    }

    @Override
    void mainJar(Action<? super Artifact> action) {
        action.execute(mainJar)
    }

    @Override
    void targetJdk(Action<? super Artifact> action) {
        ArtifactImpl artifact = targetJdks.maybeCreate("targetJdk-${targetJdks.size()}".toString())
        action.execute(artifact)
    }

    @Override
    void java(Action<? super Java> action) {
        action.execute(java)
    }

    @Override
    void jars(Action<? super Glob> action) {
        GlobImpl glob = jars.maybeCreate("jars-${jars.size()}".toString())
        action.execute(glob)
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    org.jreleaser.model.Jlink toModel() {
        org.jreleaser.model.Jlink jlink = new org.jreleaser.model.Jlink()
        jlink.name = name
        fillProperties(jlink)
        jlink.args = (List<String>) args.getOrElse([])
        jlink.jdk = jdk.toModel()
        jlink.mainJar = mainJar.toModel()
        jlink.java = java.toModel()
        if(imageName.present) jlink.imageName = imageName.get()
        if(moduleName.present) jlink.moduleName = moduleName.get()
        jlink.moduleNames = (Set<String>) moduleNames.getOrElse([] as Set)
        for (ArtifactImpl artifact : targetJdks) {
            jlink.addTargetJdk(artifact.toModel())
        }
        for (GlobImpl glob : jars) {
            jlink.addJar(glob.toModel())
        }
        jlink
    }
}