package org.kuali.rice.rest.api.actionlist;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import org.joda.time.DateTime;
import org.kuali.rice.core.api.delegation.DelegationType;
import org.kuali.rice.kew.actionitem.ActionItem;
import org.springframework.hateoas.ResourceSupport;


/**
 * Document resource object representing a Rice Document.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@ApiModel(value = "Rice Action Item")
public class ActionItemResource extends ResourceSupport {

    private ActionItem actionItem;

    public ActionItemResource(ActionItem actionItem) {
        this.actionItem = actionItem;
    }

    @ApiModelProperty(value = "Action item id", required=true)
    public String getActionItemId() { return actionItem.getId(); }

    @ApiModelProperty(value = "Date and time this action item was assigned")
    public DateTime getDateTimeAssigned() { return actionItem.getDateTimeAssigned(); }

    @ApiModelProperty(value = "")
    public String getActionRequestCd() { return actionItem.getActionRequestCd(); }

    @ApiModelProperty(value = "")
    public String getActionRequestId() {
        return actionItem.getActionRequestId();
    }

    @ApiModelProperty(value = "Document id")
    public String getDocumentId() {
        return actionItem.getDocumentId();
    }

    @ApiModelProperty(value = "Document title")
    public String getDocTitle() {
        return actionItem.getDocTitle();
    }

    @ApiModelProperty(value = "Document label")
    public String getDocLabel() { return actionItem.getDocLabel(); }

    @ApiModelProperty(value = "Url to document handler")
    public String getDocHandlerURL() { return actionItem.getDocHandlerURL(); }

    @ApiModelProperty(value = "Document name")
    public String getDocName() { return actionItem.getDocName(); }

    @ApiModelProperty(value = "Responsibility Id")
    public String getResponsibilityId() { return actionItem.getResponsibilityId(); }

    @ApiModelProperty(value = "Responsibility Id")
    public String getRoleName() { return actionItem.getRoleName(); }

    @ApiModelProperty(value = "Delegation Type")
    public DelegationType getDelegationType() { return actionItem.getDelegationType(); }

    @ApiModelProperty(value = "Group Id")
    public String getGroupId() { return actionItem.getGroupId(); }

    @ApiModelProperty(value = "Principal Id")
    public String getPrincipalId() { return actionItem.getPrincipalId(); }

    @ApiModelProperty(value = "Delegate Group Id")
    public String getDelegatorGroupId() { return actionItem.getDelegatorGroupId(); }

    @ApiModelProperty(value = "Delegate Principal Id")
    public String getDelegatorPrincipalId() { return actionItem.getDelegatorPrincipalId(); }

}
