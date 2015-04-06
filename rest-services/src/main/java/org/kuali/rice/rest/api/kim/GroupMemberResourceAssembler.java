package org.kuali.rice.rest.api.kim;

import org.kuali.rice.kim.api.group.Group;
import org.kuali.rice.kim.api.group.GroupMember;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

/**
 * Assembler for building GroupResource from Group
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class GroupMemberResourceAssembler extends ResourceAssemblerSupport<GroupMember, GroupMemberResource> {

    public GroupMemberResourceAssembler() {
        super(KimGroupRestController.class, GroupMemberResource.class);
    }

    /**
     * Creates the HAL links for the Group resource and creates the resource.
     *
     * @param groupMember the original group
     * @return the created resource with links
     */
    public GroupMemberResource toResource(GroupMember groupMember) {
        // adds a link with rel self pointing to itself
        GroupMemberResource resource = createResourceWithId(groupMember.getMemberId(), groupMember);
        Link link = ControllerLinkBuilder.linkTo(KimGroupRestController.class).slash(groupMember.getGroupId()).slash("members").slash(groupMember.getMemberId()).withSelfRel();
        resource.removeLinks();
        resource.add(link);
        return resource;
    }

    @Override
    protected GroupMemberResource instantiateResource(GroupMember groupMember) {
        return GroupMemberResource.fromGroupMember(groupMember);
    }
}
