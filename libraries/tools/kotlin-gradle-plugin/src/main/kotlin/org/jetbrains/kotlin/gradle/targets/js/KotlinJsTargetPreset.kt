/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("PackageDirectoryMismatch") // Old package for compatibility
package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.Project
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.reflect.Instantiator
import org.jetbrains.kotlin.gradle.plugin.Kotlin2JsSourceSetProcessor
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetProcessor
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetConfigurator
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsTargetConfigurator
import org.jetbrains.kotlin.gradle.tasks.KotlinTasksProvider

class KotlinJsTargetPreset(
    project: Project,
    instantiator: Instantiator,
    fileResolver: FileResolver,
    kotlinPluginVersion: String
) : KotlinOnlyTargetPreset<KotlinJsCompilation>(
    project,
    instantiator,
    fileResolver,
    kotlinPluginVersion
) {
    override fun createKotlinTargetConfigurator() = KotlinJsTargetConfigurator(kotlinPluginVersion)

    override fun getName(): String = PRESET_NAME

    override fun createCompilationFactory(forTarget: KotlinOnlyTarget<KotlinJsCompilation>) =
        KotlinJsCompilationFactory(project, forTarget)

    override val platformType: KotlinPlatformType
        get() = KotlinPlatformType.js

    companion object {
        const val PRESET_NAME = "js"
    }
}