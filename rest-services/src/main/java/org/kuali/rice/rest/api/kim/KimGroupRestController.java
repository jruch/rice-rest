package org.kuali.rice.rest.api.kim;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.membership.MemberType;
import org.kuali.rice.kim.api.group.Group;
import org.kuali.rice.kim.api.group.GroupMember;
import org.kuali.rice.kim.api.group.GroupService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.rest.exception.BadRequestException;
import org.kuali.rice.rest.exception.NotFoundException;
import org.kuali.rice.rest.exception.OperationFailedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Rest resource for kim groups.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@Controller
@RequestMapping(value = "/api/v1/kimgroups", produces = MediaType.APPLICATION_JSON)
@Api(value = "/kimgroups", description = "Operations on kimgroups")
public class KimGroupRestController {

    private static final String RESOURCE_REQUEST_MAPPING = "/api/v1/kimgroups";

    private GroupService groupService = KimApiServiceLocator.getGroupService();

    @ApiOperation(
            httpMethod = "GET",
            value = "Returns a kim group given the groupId",
            response = Group.class
    )
    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public @ResponseBody GroupResource getGroup(@ApiParam(value = "Id of the group", required = true) @PathVariable("id") String id) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException();
        }
        Group group =  groupService.getGroup(id);
        if(group == null){
            throw new NotFoundException();
        }

        return GroupResource.fromGroup(group);
    }

    @ApiOperation(
            httpMethod = "POST",
            value = "Creates a new group using the given Group.",
            response = Group.class
    )
    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody GroupResource createGroup(@ApiParam(value = "The group to be created", required = true) @RequestBody GroupResource groupResource){
        if(null == groupResource)  {
            throw new BadRequestException();
        }
        Group newGroup =  groupService.createGroup( Group.Builder.create(groupResource).build() );

        if(null == newGroup) {
            throw new OperationFailedException("Could not create group!");
        }

        return GroupResource.fromGroup( newGroup);
    }

    @ApiOperation(
            httpMethod = "PUT",
            value = "Updates an existing group using the given Group",
            notes = "This will attempt to update an existing Group.The passed in Group" +
                    "must have it's Id set and be a valid group that already exists.If the passed in groupId and the group.id " +
                    "values are different then this method will inactivate the old group and create a new group with the same " +
                    "members with the passed in groups properties.",
            response = Group.class
    )
    @RequestMapping(value = "/{groupId}", method = RequestMethod.PUT)
    @ResponseBody GroupResource updateGroup(@ApiParam(value = "Id of the group to be updated", required = true) @PathVariable("groupId") String groupId,
                                    @ApiParam(value = "Group object to use for update", required = true) @RequestBody GroupResource groupResource) {
        if (StringUtils.isBlank(groupId) || groupResource == null) {
            throw new BadRequestException();
        }

        Group updatedGroup = null;

        if(groupId.equals( groupResource.getId()) ) {
            updatedGroup = groupService.updateGroup( Group.Builder.create(groupResource).build() );
        } else {
            updatedGroup = groupService.updateGroup(groupId, Group.Builder.create(groupResource).build() );
        }

        if(null == updatedGroup) {
            throw new OperationFailedException("Failed to update group: " + groupId);
        }

        return GroupResource.fromGroup( updatedGroup );
    }

    @ApiOperation(
            httpMethod = "GET",
            value = "Get all the groups for a given principal, principal and namespaceCode or namespaceCode and groupName" ,
            notes = "This will include all groups directly assigned as well as those inferred by the fact that they are" +
                    " members of higher level groups.",
            response = Iterable.class
    )
    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody Iterable<GroupResource> retrieveGroups(@ApiParam(value = "The id of the Principal") @QueryParam("principalId") String principalId,
                                                        @ApiParam(value = "The namespace code of the desired Groups to return") @QueryParam("namespaceCode") String namespaceCode,
                                                        @ApiParam(value = "String that matches the desired Group's name") @QueryParam("groupName") String groupName) {
        List<Group> groupList = null;

        if (StringUtils.isNotBlank(principalId) && StringUtils.isBlank(namespaceCode)) {
            groupList =  groupService.getGroupsByPrincipalId(principalId);
        } else if (StringUtils.isNotBlank(principalId) && StringUtils.isNotBlank(namespaceCode)) {
            groupList =  groupService.getGroupsByPrincipalIdAndNamespaceCode(principalId, namespaceCode);
        } else if (StringUtils.isNotBlank(namespaceCode) && StringUtils.isNotBlank(groupName)) {
            Group group = groupService.getGroupByNamespaceCodeAndName(namespaceCode, groupName);
            groupList = new ArrayList<Group>();
            groupList.add(group);
        } else {
            throw new BadRequestException("Legal query params are: just principalId, principalId and namespacecode or namespacecode and groupName");
        }



        List<GroupResource> groupResources = new ArrayList<GroupResource>();
        for(Group group : groupList) {
            groupResources.add ( GroupResource.fromGroup( group ) );
        }

        return groupResources;
    }

    @ApiOperation(
            httpMethod = "GET",
            value = "Get all the group ids for a given principal or principal and namespaceCode",
            notes = "This will include all groups directly assigned as well as those inferred by the fact that they are members of higher level groups.",
            response = Iterable.class
    )
    @RequestMapping(value="/group-refs", method = RequestMethod.GET)
    public @ResponseBody Iterable<IdRefResource> retrieveGroupRefs(@ApiParam(value = "The id of the Principal") @QueryParam("principalId") String principalId,
                                                            @ApiParam(value = "The namespace code of the desired Groups to return") @QueryParam("namespaceCode") String namespaceCode) {

        List<String> groupIds = new ArrayList<String>();

        if (StringUtils.isNotBlank(principalId) && StringUtils.isBlank(namespaceCode)) {
            groupIds =  groupService.getGroupIdsByPrincipalId(principalId);
        } else if (StringUtils.isNotBlank(principalId) && StringUtils.isNotBlank(namespaceCode)) {
            groupIds = groupService.getGroupIdsByPrincipalIdAndNamespaceCode(principalId, namespaceCode);
        } else {
            throw new BadRequestException("Legal query params are: just principalId, principalId and namespacecode");
        }

        List<IdRefResource> groupRefs = new ArrayList<IdRefResource>();
        if(null != groupIds) {
             for(String id : groupIds) {
                IdRefResource idRef = new IdRefResource();

                String location = ServletUriComponentsBuilder.fromCurrentServletMapping().path(RESOURCE_REQUEST_MAPPING + "/{id}").buildAndExpand(id).toUriString();
                idRef.setHref(location);
                idRef.setId(id);
                groupRefs.add(idRef);
            }
        }

        return groupRefs;
    }

    @ApiOperation(
            httpMethod = "GET",
            value = "Get a group member given a groupId and memberId",
            response = GroupMemberResource.class
    )
    @RequestMapping(value="/{groupId}/members/{memberId}", method = RequestMethod.GET)
    public List<GroupMemberResource> getGroupMember(@ApiParam(value = "The id of the group", required=true) @PathVariable("groupId") String groupId,
                                      @ApiParam(value = "The member id of the member", required=true) @PathVariable("memberId") String memberId) {

        if(StringUtils.isBlank(groupId) || StringUtils.isBlank(memberId))  {
            throw new BadRequestException();
        }

        List<GroupMember> members = groupService.getMembersOfGroup(groupId);

        List<GroupMemberResource> memberResources = new ArrayList<GroupMemberResource>();
        for(GroupMember member : members) {
            if(memberId.equals(member.getMemberId())) {
                memberResources.add ( GroupMemberResource.fromGroupMember(member ));
            }
        }

        return memberResources;
    }

    @ApiOperation(
            httpMethod = "GET",
            value = "Gets a list of group members that belong to the group",
            response = Iterable.class
    )
    @RequestMapping(value="/{groupId}/members", method = RequestMethod.GET)
    public Iterable<GroupMemberResource> getMembersForGroup(@ApiParam(value = "The id of the group", required=true) @PathVariable("groupId") String groupId) {
        if(StringUtils.isBlank(groupId)) {
            throw new BadRequestException();
        }

        List<GroupMember> members = groupService.getMembersOfGroup(groupId);

        List<GroupMemberResource> memberResources = new ArrayList<GroupMemberResource>();
        for(GroupMember member : members) {
            GroupMemberResource gmr = new GroupMemberResource();
            try {
                BeanUtils.copyProperties(gmr, member);
            } catch (Exception e) {
                throw new OperationFailedException(e.getMessage());
            }
            memberResources.add(gmr);
        }

        return memberResources;
    }

    @ApiOperation(
            httpMethod = "GET",
            value = "Gets a list of group member ids of type PRINCIPAL given a group id",
            notes = "If directMembersOnly is true the list will contain only direct member principal ids otherwise it will contain all member principal ids." +
                    "By default it is false",
            response = Iterable.class
    )
    @RequestMapping(value="/{groupId}/member-refs/principal", method = RequestMethod.GET)
    public Iterable<IdRefResource> getDirectPrincipalMemberRefsForGroup(@ApiParam(value = "The id of the group", required=true) @PathVariable("groupId") String groupId,
                                                                 @ApiParam(value = "Flag for direct members") @QueryParam("directMembersOnly") boolean directMembersOnly) {
        List<String> memberIds = null;

        if(directMembersOnly) {
            memberIds = groupService.getDirectMemberPrincipalIds(groupId);
        } else {
            memberIds = groupService.getMemberPrincipalIds(groupId);
        }

        List<IdRefResource> memberRefs = new ArrayList<IdRefResource>();
        if(null != memberIds) {
            for(String id : memberIds) {
                IdRefResource idRef = new IdRefResource();
                String location = ServletUriComponentsBuilder.fromCurrentServletMapping().path(RESOURCE_REQUEST_MAPPING + "/{groupId}/members/{memberId}").buildAndExpand(groupId, id).toUriString() ;
                idRef.setId( id );
                idRef.setHref(location);
                memberRefs.add( idRef );
            }
        }

        return memberRefs;
    }

    @ApiOperation(
            httpMethod = "GET",
            value = "Gets a list of group member ids of type GROUP given a group id",
            notes = "If directMembersOnly is true the list will contain only direct member group ids otherwise it will contain all member group ids." +
                    "By default it is false",
            response = Iterable.class
    )
    @RequestMapping(value="/{groupId}/member-refs/group", method = RequestMethod.GET)
    public Iterable<IdRefResource> getGroupMemberRefsForGroup(@ApiParam(value = "The id of the group", required=true) @PathVariable("groupId") String groupId,
                                                       @ApiParam(value = "Flag for direct members") @QueryParam("directMembersOnly") boolean directMembersOnly) {

        List<String> memberIds = null;

        if(directMembersOnly) {
            memberIds = groupService.getDirectMemberGroupIds(groupId);
        } else {
            memberIds = groupService.getMemberGroupIds(groupId);
        }

        List<IdRefResource> memberRefs = new ArrayList<IdRefResource>();
        if(null != memberIds) {
            for(String id : memberIds) {
                IdRefResource idRef = new IdRefResource();
                String location = ServletUriComponentsBuilder.fromCurrentServletMapping().path(RESOURCE_REQUEST_MAPPING + "/{groupId}/members/{memberId}").buildAndExpand(groupId, id).toUriString() ;
                idRef.setId( id );
                idRef.setHref(location);
                memberRefs.add( idRef );
            }
        }

        return memberRefs;
    }

    @ApiOperation(
            httpMethod = "PUT",
            value = "Adds a new member of type PRINCIPAL to the group",
            notes = "The member should be an existing principal" ,
            response = ResponseEntity.class
    )
    @RequestMapping(value = "/{groupId}/members/principal/{memberId}", method = RequestMethod.PUT)
    @ResponseBody
    ResponseEntity<String> addPrincipalToGroup(@ApiParam(value = "The id of the group", required=true) @PathVariable("groupId") String groupId,
                                               @ApiParam(value = "The member id of the member", required=true) @PathVariable("memberId") String memberId) {
        if (StringUtils.isBlank(groupId) || StringUtils.isBlank(memberId)) {
            throw new BadRequestException();
        }

        boolean status =  groupService.addPrincipalToGroup(memberId, groupId);

        if(!status) {
            throw new OperationFailedException("Could not add principal " + memberId + " to group " + groupId);
        }


        UriComponents location = ServletUriComponentsBuilder.fromCurrentServletMapping().path(RESOURCE_REQUEST_MAPPING + "/{groupId}/members").buildAndExpand(groupId);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location.toUri());
        return new ResponseEntity<String>("", responseHeaders, HttpStatus.OK);
    }

    @ApiOperation(
            httpMethod = "PUT",
            value = "Adds a new member of type GROUP to the group",
            notes = "The group should be an existing group" ,
            response = ResponseEntity.class
    )
    @RequestMapping(value = "/{groupId}/members/group/{memberId}", method = RequestMethod.PUT)
    @ResponseBody
    ResponseEntity<String> addGroupToGroup(@ApiParam(value = "The id of the group", required=true) @PathVariable("groupId") String groupId,
                                           @ApiParam(value = "The member id of the member", required=true) @PathVariable("memberId") String memberId) {
        if (StringUtils.isBlank(groupId) || StringUtils.isBlank(memberId)) {
            throw new BadRequestException();
        }

        boolean status =  groupService.addGroupToGroup(memberId, groupId);

        if(!status) {
            throw new OperationFailedException("Could not add group " + memberId + " to group " + groupId);
        }


        UriComponents location = ServletUriComponentsBuilder.fromCurrentServletMapping().path(RESOURCE_REQUEST_MAPPING + "/{groupId}/members").buildAndExpand(groupId);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location.toUri());
        return new ResponseEntity<String>("", responseHeaders, HttpStatus.OK);
    }

    @ApiOperation(
            httpMethod = "POST",
            value = "Add a new member to the group using a given group member",
            response = GroupMemberResource.class
    )
    @RequestMapping(value = "/{groupId}/members", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    GroupMemberResource createGroupMember(@ApiParam(value = "The id of the group", required=true) @PathVariable("groupId") String groupId,
                                  @ApiParam(value = "The group member to use") @RequestBody GroupMemberResource groupMemberResource) {
        if (StringUtils.isBlank(groupId) || groupMemberResource == null) {
            throw new BadRequestException();
        }

        groupService.createGroupMember( GroupMember.Builder.create(groupMemberResource).build() );

        /**
         *  Right now there is bug in groupServiceImpl which compares JodaTime with SQL time using .equals operator.
         *  See KULRICE-XXXX for more details
         */

        List<GroupMember> groupMembers = groupService.getMembersOfGroup(groupId);
        GroupMember newMember = null;
        for(GroupMember member : groupMembers) {
            if (member.getMemberId().equals(groupMemberResource.getMemberId())
                    && member.getType().equals(groupMemberResource.getType())
                    && member.getActiveFromDate().getMillis() == groupMemberResource.getActiveFromDate().getMillis()
                    && member.getActiveToDate().getMillis() == groupMemberResource.getActiveToDate().getMillis()) {
                newMember = member;
                break;
            }
        }

        if(null == newMember) {
            throw new OperationFailedException("Could not find the newly created GroupMember!");
        }

        return GroupMemberResource.fromGroupMember(newMember);
    }

    @ApiOperation(
            httpMethod = "PUT",
            value = "Add a new member to the group using a given group member",
            response = GroupMemberResource.class
    )
    @RequestMapping(value = "/{groupId}/members/{memberId}", method = RequestMethod.PUT)
    @ResponseBody
    GroupMemberResource updateGroupMember(@PathVariable("groupId") String groupId, @PathVariable("memberId") String memberId, @RequestBody GroupMemberResource groupMemberResource) {
        if (StringUtils.isBlank(groupId) || StringUtils.isBlank(memberId) || groupMemberResource == null) {
            throw new BadRequestException();
        }

        GroupMember member =  groupService.updateGroupMember( GroupMember.Builder.create(groupMemberResource).build() );

        return GroupMemberResource.fromGroupMember(member);

    }

    @RequestMapping(value = "/{groupId}/members/{memberId}", method = RequestMethod.DELETE)
    public ResponseEntity<String> removeMemberFromGroup(@PathVariable("groupId") String groupId, @PathVariable("memberId") String memberId) {
        if (StringUtils.isBlank(groupId) || StringUtils.isBlank(memberId)) {
            throw new BadRequestException();
        }

        List<GroupMemberResource> members = getGroupMember(groupId, memberId);
        if(members == null || members.size() > 0) {
            throw new NotFoundException("No members found with member Id: " + memberId + " for group: " + groupId);
        }

        GroupMemberResource member = members.get(0);

        boolean status = false;

        if(MemberType.PRINCIPAL.equals(member.getType())) {
            status = groupService.removePrincipalFromGroup(memberId, groupId);
        }  else  if(MemberType.GROUP.equals(member.getType())) {
            status = groupService.removeGroupFromGroup(memberId, groupId);
        }

        if(!status) {
            throw new OperationFailedException("Could not remove member " + memberId + " from group " + groupId);
        }

        UriComponents location = ServletUriComponentsBuilder.fromCurrentServletMapping().path(RESOURCE_REQUEST_MAPPING + "/{groupId}/members").buildAndExpand(groupId);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location.toUri());
        return new ResponseEntity<String>("", responseHeaders, HttpStatus.OK);
    }


    @RequestMapping(value = "/{groupId}/members", method = RequestMethod.DELETE)
    public ResponseEntity<String> removeAllMemberFromGroup(@PathVariable("groupId") String groupId ) {
        if (StringUtils.isBlank(groupId)) {
            throw new BadRequestException();
        }

        groupService.removeAllMembers(groupId);

        UriComponents location = ServletUriComponentsBuilder.fromCurrentServletMapping().path(RESOURCE_REQUEST_MAPPING + "/{groupId}/members").buildAndExpand(groupId);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location.toUri());
        return new ResponseEntity<String>("", responseHeaders, HttpStatus.OK);
    }
}
