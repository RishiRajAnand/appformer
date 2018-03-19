/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.kie.workbench.drools.client.perspectives;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.structure.client.editors.repository.clone.CloneRepositoryPresenter;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.security.OrganizationalUnitAction;
import org.guvnor.structure.security.RepositoryAction;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.kie.workbench.common.widgets.client.handlers.NewResourcePresenter;
import org.kie.workbench.common.workbench.client.PerspectiveIds;
import org.kie.workbench.drools.client.resources.i18n.AppConstants;
import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.panels.impl.MultiListWorkbenchPanelPresenter;
import org.uberfire.client.workbench.panels.impl.SimpleWorkbenchPanelPresenter;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.CompassPosition;
import org.uberfire.workbench.model.PanelDefinition;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PanelDefinitionImpl;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

/**
 * A Perspective for Administrators
 */
@ApplicationScoped
@WorkbenchPerspective(identifier = PerspectiveIds.ADMINISTRATION)
public class AdministrationPerspective {

    private AppConstants constants = AppConstants.INSTANCE;

    @Inject
    private NewResourcePresenter newResourcePresenter;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private SyncBeanManager iocManager;

    @Inject
    private CloneRepositoryPresenter cloneRepositoryPresenter;

    private Command newRepoCommand = null;
    private Command cloneRepoCommand = null;

    @PostConstruct
    public void init() {
        buildCommands();
    }

    @Perspective
    public PerspectiveDefinition getPerspective() {
        final PerspectiveDefinition perspective = new PerspectiveDefinitionImpl( MultiListWorkbenchPanelPresenter.class.getName() );
        perspective.setName( constants.administration() );

        perspective.getRoot().addPart( new PartDefinitionImpl( new DefaultPlaceRequest( "RepositoriesEditor" ) ) );

        final PanelDefinition west = new PanelDefinitionImpl( SimpleWorkbenchPanelPresenter.class.getName() );
        west.setWidth( 300 );
        west.setMinWidth( 200 );
        west.addPart( new PartDefinitionImpl( new DefaultPlaceRequest( "FileExplorer" ) ) );

        perspective.getRoot().insertChild( CompassPosition.WEST, west );

        return perspective;
    }

    @WorkbenchMenu
    public Menus getMenus() {
        return MenuFactory
                .newTopLevelMenu( constants.MenuOrganizationalUnits() )
                .withPermission( OrganizationalUnit.RESOURCE_TYPE, OrganizationalUnitAction.READ )
                .menus()
                .menu( constants.MenuManageOrganizationalUnits() )
                .respondsWith( () -> placeManager.goTo( "org.kie.workbench.common.screens.organizationalunit.manager.OrganizationalUnitManager" ) )
                .endMenu()
                .endMenus()
                .endMenu()
                .newTopLevelMenu( constants.repositories() )
                .menus()
                .menu( constants.listRepositories() )
                .withPermission( Repository.RESOURCE_TYPE, RepositoryAction.READ )
                .respondsWith( () -> placeManager.goTo( "RepositoriesEditor" ) )
                .endMenu()
                .menu( constants.cloneRepository() )
                .withPermission( Repository.RESOURCE_TYPE, RepositoryAction.CREATE )
                .respondsWith( cloneRepoCommand )
                .endMenu()
                .menu( constants.newRepository() )
                .withPermission( Repository.RESOURCE_TYPE, RepositoryAction.CREATE )
                .respondsWith( newRepoCommand )
                .endMenu()
                .endMenus()
                .endMenu().build();
    }

    Command getNewRepoCommand() {
        return newRepoCommand;
    }

    Command getCloneRepoCommand() {
        return cloneRepoCommand;
    }

    private void buildCommands() {
        this.cloneRepoCommand = () -> {
                cloneRepositoryPresenter.showForm();
        };
    }
}