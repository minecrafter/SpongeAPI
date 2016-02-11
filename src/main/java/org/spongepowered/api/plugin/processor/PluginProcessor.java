/*
 * This file is part of SpongeAPI, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.api.plugin.processor;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.StandardLocation.CLASS_OUTPUT;

import net.minecrell.plugin.meta.PluginMetadata;
import org.spongepowered.api.plugin.Plugin;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;

@SupportedAnnotationTypes({
        "org.spongepowered.api.plugin.Plugin",
        "org.spongepowered.api.plugin.Dependency"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public final class PluginProcessor extends AbstractProcessor {

    private static final PluginMetadataFormat format = new McModInfoFormat();

    private final Map<String, PluginElement> plugins = new HashMap<>();
    private final Set<String> duplicates = new HashSet<>();

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        // TODO: Read options
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            if (!roundEnv.errorRaised()) {
                finish();
            }

            return true;
        }

        if (!contains(annotations, Plugin.class)) {
            return false;
        }

        for (Element e : roundEnv.getElementsAnnotatedWith(Plugin.class)) {
            if (e.getKind() != ElementKind.CLASS) {
                getMessager().printMessage(ERROR, "Invalid element of type " + e.getKind() + " annotated with @Plugin", e);
                continue;
            }

            final TypeElement pluginElement = (TypeElement) e;
            AnnotationWrapper<Plugin> annotation = AnnotationWrapper.get(pluginElement, Plugin.class);

            final String id = annotation.get().id();
            if (id.isEmpty()) {
                getMessager().printMessage(ERROR, "Plugin ID cannot be empty", pluginElement, annotation.getMirror(), annotation.getValue("id"));
                continue;
            }

            PluginElement plugin = new PluginElement(id, pluginElement, annotation);

            // Check for conflicting plugin IDs
            if (this.duplicates.contains(id) || this.plugins.containsKey(id)) {
                PluginElement otherPlugin = this.plugins.remove(id);
                if (otherPlugin != null) {
                    reportDuplicatePlugin(id, otherPlugin);
                    this.duplicates.add(id);
                }

                reportDuplicatePlugin(id, plugin);
                continue;
            }

            this.plugins.put(id, plugin);
            plugin.apply(getMessager());
        }

        return true;
    }

    private void finish() {
        List<PluginMetadata> meta = this.plugins.values().stream()
                .map(PluginElement::getMetadata)
                .collect(Collectors.toList());

        final String name = format.getFilename();

        try {
            FileObject obj = this.processingEnv.getFiler().createResource(CLASS_OUTPUT, "", name);

            getMessager().printMessage(NOTE, "Writing plugin metadata to " + obj.toUri());
            try (BufferedWriter writer = new BufferedWriter(obj.openWriter())) {
                format.write(writer, meta);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write plugin metadata to " + name, e);
        }
    }

    private void reportDuplicatePlugin(String id, PluginElement plugin) {
        getMessager().printMessage(ERROR, "Duplicate plugin ID: " + id, plugin.getElement(), plugin.getAnnotation().getMirror(),
                plugin.getAnnotation().getValue("id"));
    }

    private Messager getMessager() {
        return this.processingEnv.getMessager();
    }

    private static boolean contains(Collection<? extends TypeElement> elements, Class<?> clazz) {
        if (elements.isEmpty()) {
            return false;
        }

        final String name = clazz.getName();
        for (TypeElement element : elements) {
            if (element.getQualifiedName().contentEquals(name)) {
                return true;
            }
        }

        return false;
    }

}
