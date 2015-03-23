package org.kuali.rice.rest.api.actionlist;

import org.kuali.rice.kew.actionitem.ActionItem;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

public class ActionItemResourceAssembler extends ResourceAssemblerSupport<ActionItem, ActionItemResource> {

    public ActionItemResourceAssembler() {
        super(ActionListRestController.class, ActionItemResource.class);
    }

    /**
     * Creates the HAL links for the document resource and creates the resource.
     *
     * @param actionItem the original action item
     * @return the created resource with links
     */
    public ActionItemResource toResource(ActionItem actionItem) {
        // adds a link with rel self pointing to itself
        ActionItemResource resource = createResourceWithId(actionItem.getActionRequestId(), actionItem);
        return resource;
    }

    @Override
    protected ActionItemResource instantiateResource(ActionItem actionItem) {
        return new ActionItemResource(actionItem);
    }
}