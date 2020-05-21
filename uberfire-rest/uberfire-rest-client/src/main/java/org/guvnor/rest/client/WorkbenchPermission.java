package org.guvnor.rest.client;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class WorkbenchPermission {

    private Boolean editDataObject;
    private Boolean plannerAvailable;
    private Boolean editGlobalPreferences;
    private Boolean editProfilePreferences;
    private Boolean accessDataTransfer;
    private Boolean jarDownload;
    private Boolean editGuidedDecisionTableColumns;

    public WorkbenchPermission() {
    }

    public WorkbenchPermission(@MapsTo("editDataObject") Boolean editDataObject, @MapsTo("plannerAvailable") Boolean plannerAvailable,
                               @MapsTo("editGlobalPreferences") Boolean editGlobalPreferences, @MapsTo("editProfilePreferences") Boolean editProfilePreferences,
                               @MapsTo("accessDataTransfer") Boolean accessDataTransfer, @MapsTo("jarDownload") Boolean jarDownload,
                               @MapsTo("editGuidedDecisionTableColumns") Boolean editGuidedDecisionTableColumns) {
        this.editDataObject = editDataObject;
        this.plannerAvailable = plannerAvailable;
        this.editGlobalPreferences = editGlobalPreferences;
        this.editProfilePreferences = editProfilePreferences;
        this.accessDataTransfer = accessDataTransfer;
        this.jarDownload = jarDownload;
        this.editGuidedDecisionTableColumns = editGuidedDecisionTableColumns;
    }

    public Boolean getEditDataObject() {
        return editDataObject;
    }

    public Boolean getPlannerAvailable() {
        return plannerAvailable;
    }

    public Boolean getEditGlobalPreferences() {
        return editGlobalPreferences;
    }

    public Boolean getEditProfilePreferences() {
        return editProfilePreferences;
    }

    public Boolean getAccessDataTransfer() {
        return accessDataTransfer;
    }

    public Boolean getJarDownload() {
        return jarDownload;
    }

    public Boolean getEditGuidedDecisionTableColumns() {
        return editGuidedDecisionTableColumns;
    }
}
