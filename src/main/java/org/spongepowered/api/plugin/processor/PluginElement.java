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

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.WARNING;

import net.minecrell.plugin.meta.PluginMetadata;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;

final class PluginElement {

    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9-_]+");

    private final TypeElement element;
    private final AnnotationWrapper<Plugin> annotation;
    private final PluginMetadata metadata;

    PluginElement(String id, TypeElement element, AnnotationWrapper<Plugin> annotation) {
        this.element = checkNotNull(element, "element");
        this.annotation = checkNotNull(annotation, "annotation");
        this.metadata = new PluginMetadata(id);
    }

    TypeElement getElement() {
        return this.element;
    }

    AnnotationWrapper<Plugin> getAnnotation() {
        return this.annotation;
    }

    PluginMetadata getMetadata() {
        return this.metadata;
    }

    void apply(Messager messager) {
        String value = this.annotation.get().id();
        if (!ID_PATTERN.matcher(value).matches()) {
            messager.printMessage(ERROR, "Plugin ID should match pattern " + ID_PATTERN.pattern(), this.element, this.annotation.getMirror(),
                    this.annotation.getValue("id"));
        }

        value = this.annotation.get().description();
        if (value.isEmpty()) {
            messager.printMessage(WARNING, "Missing plugin name", this.element, this.annotation.getMirror());
        } else {
            this.metadata.setName(value);
        }

        value = this.annotation.get().version();
        if (value.isEmpty()) {
            messager.printMessage(WARNING, "Missing plugin version", this.element, this.annotation.getMirror());
        } else {
            this.metadata.setVersion(value);
        }

        value = this.annotation.get().description();
        if (value.isEmpty()) {
            messager.printMessage(WARNING, "Missing plugin description", this.element, this.annotation.getMirror());
        } else {
            this.metadata.setDescription(value);
        }

        value = this.annotation.get().url();
        if (!value.isEmpty()) {
            if (!isLikelyValidUrl(value)) {
                messager.printMessage(ERROR, "Invalid URL: " + value, this.element, this.annotation.getMirror(), this.annotation.getValue("url"));
                return;
            }

            this.metadata.setUrl(value);
        }

        String[] authors = this.annotation.get().authors();
        if (authors.length > 0) {
            for (String author : authors) {
                if (author.isEmpty()) {
                    messager.printMessage(ERROR, "Empty author is not allowed", this.element, this.annotation.getMirror(),
                            this.annotation.getValue("authors"));
                    continue;
                }

                this.metadata.addAuthor(author);
            }
        }

        Dependency[] dependencies = this.annotation.get().dependencies();
        if (dependencies.length > 0) {
            for (Dependency dependency : dependencies) {
                final String id = dependency.id();
                if (id.isEmpty()) {
                    messager.printMessage(ERROR, "Dependency ID should not be empty", this.element, this.annotation.getMirror(),
                            this.annotation.getValue("dependencies"));
                }

                // TODO: Load order
                this.metadata.loadAfter(new PluginMetadata.Dependency(id, dependency.version()), dependency.required());
            }
        }
    }

    private static boolean isLikelyValidUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException ignored) {
            return false;
        }
    }

}
