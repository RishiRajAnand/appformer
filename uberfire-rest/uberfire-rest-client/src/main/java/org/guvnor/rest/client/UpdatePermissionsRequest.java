/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
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

package org.guvnor.rest.client;

import java.util.List;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class UpdatePermissionsRequest {
    Permission project;
    Permission spaces;
    Permission editor;
    Permission pages;

    public UpdatePermissionsRequest(){};

    public UpdatePermissionsRequest(@MapsTo("project") Permission project, @MapsTo("spaces") Permission spaces,@MapsTo("editor") Permission editor,@MapsTo("pages") Permission pages) {
        this.project = project;
        this.spaces = spaces;
        this.editor = editor;
        this.pages = pages;
    }

    public Permission getProject() {
        return project;
    }

    public void setProject(Permission project) {
        this.project = project;
    }

    public Permission getSpaces() {
        return spaces;
    }

    public void setSpaces(Permission spaces) {
        this.spaces = spaces;
    }

    public Permission getEditor() {
        return editor;
    }

    public void setEditor(Permission editor) {
        this.editor = editor;
    }

    public Permission getPages() {
        return pages;
    }

    public void setPages(Permission pages) {
        this.pages = pages;
    }
}

