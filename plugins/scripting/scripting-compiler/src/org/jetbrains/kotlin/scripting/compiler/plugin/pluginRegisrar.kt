/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.compiler.plugin

import com.intellij.mock.MockProject
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.extensions.ReplFactoryExtension
import org.jetbrains.kotlin.cli.common.extensions.ScriptEvaluationExtension
import org.jetbrains.kotlin.cli.common.extensions.ShellExtension
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.extensions.CollectAdditionalSourcesExtension
import org.jetbrains.kotlin.extensions.CompilerConfigurationExtension
import org.jetbrains.kotlin.extensions.ProjectExtensionDescriptor
import org.jetbrains.kotlin.resolve.extensions.ExtraImportsProviderExtension
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import org.jetbrains.kotlin.scripting.compiler.plugin.definitions.CliScriptDefinitionProvider
import org.jetbrains.kotlin.scripting.compiler.plugin.definitions.CliScriptDependenciesProvider
import org.jetbrains.kotlin.scripting.compiler.plugin.definitions.CliScriptReportSink
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinitionProvider
import org.jetbrains.kotlin.scripting.definitions.ScriptDependenciesProvider
import org.jetbrains.kotlin.scripting.extensions.*
import org.jetbrains.kotlin.scripting.resolve.ScriptReportSink

private fun <T> ProjectExtensionDescriptor<T>.registerExtensionIfRequired(project: MockProject, extension: T) {
    try {
        registerExtension(project, extension)
    } catch (ex: IllegalArgumentException) {
        // ignore
    }
}

class ScriptingCompilerConfigurationComponentRegistrar : ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        CompilerConfigurationExtension.registerExtension(project,
                                                         ScriptingCompilerConfigurationExtension(
                                                             project
                                                         )
        )
        CollectAdditionalSourcesExtension.registerExtension(project,
                                                            ScriptingCollectAdditionalSourcesExtension(
                                                                project
                                                            )
        )
        ScriptEvaluationExtension.registerExtensionIfRequired(project, JvmCliScriptEvaluationExtension())
        ShellExtension.registerExtensionIfRequired(project, JvmCliReplShellExtension())
        ReplFactoryExtension.registerExtensionIfRequired(project,
                                                         JvmStandardReplFactoryExtension()
        )

        val scriptDefinitionProvider = CliScriptDefinitionProvider()
        project.registerService(ScriptDefinitionProvider::class.java, scriptDefinitionProvider)
        project.registerService(
            ScriptDependenciesProvider::class.java,
            CliScriptDependenciesProvider(project)
        )
        SyntheticResolveExtension.registerExtension(project, ScriptingResolveExtension())
        ExtraImportsProviderExtension.registerExtension(project, ScriptExtraImportsProviderExtension())

        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)
        if (messageCollector != null) {
            project.registerService(
                ScriptReportSink::class.java,
                CliScriptReportSink(messageCollector)
            )
        }
    }
}