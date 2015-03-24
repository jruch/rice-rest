package org.kuali.rice.rest.api.kim;

import org.kuali.rice.kew.api.document.Document;
import org.kuali.rice.kim.api.group.Group;
import org.kuali.rice.rest.api.document.DocumentResource;
import org.kuali.rice.rest.api.document.DocumentSearchRestController;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

/**
 * Assembler for building GroupResource from Group
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class GroupResourceAssembler extends ResourceAssemblerSupport<Group, GroupResource> {

    public GroupResourceAssembler() {
        super(KimGroupRestController.class, GroupResource.class);
    }

    /**
     * Creates the HAL links for the Group resource and creates the resource.
     *
     * @param group the original group
     * @return the created resource with links
     */
    public GroupResource toResource(Group group) {
        // adds a link with rel self pointing to itself
        GroupResource resource = createResourceWithId(group.getId(), group);
        return resource;
    }

    @Override
    protected GroupResource instantiateResource(Group group) {
        return GroupResource.fromGroup(group);
    }
}
