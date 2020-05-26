/*
 *
 *   Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.uberfire.ext.security.management.impl;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.uberfire.annotations.FallbackImplementation;
import org.uberfire.ext.security.management.api.RestWorkbenchEnties;

/**
 * Default implementation for {@link RestWorkbenchEnties}. To override it, just provide a default
 * CDI bean that implements {@link RestWorkbenchEnties}.
 */
@ApplicationScoped
@FallbackImplementation
public class DefaultRestWorkbenchEntities implements RestWorkbenchEnties{

    protected DefaultRestWorkbenchEntities() {
    }

    @Override
    public List<String> getAllEditorId() {
        return Arrays.asList();
    }

    @Override
    public List<String> getAllPerpective() {
        return Arrays.asList();
    }

    @Override
    public List<String> getAllWorkbenchFeatures() {
        return Arrays.asList();
    }
}
