package org.kuali.rice.rest.api.kim;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.criteria.Predicate;
import org.kuali.rice.core.api.criteria.PredicateUtils;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.core.api.membership.MemberType;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.group.*;
import org.kuali.rice.kim.api.identity.principal.Principal;
import org.kuali.rice.kim.api.identity.principal.PrincipalQueryResults;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kim.impl.KIMPropertyConstants;
import org.kuali.rice.rest.RiceRestConstants;
import org.kuali.rice.rest.api.paging.RicePagedResources;
import org.kuali.rice.rest.exception.BadRequestException;
import org.kuali.rice.rest.exception.NotFoundException;
import org.kuali.rice.rest.exception.OperationFailedException;
import org.kuali.rice.rest.utils.RiceRestUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.sql.Timestamp;
import java.util.*;

import static org.kuali.rice.core.api.criteria.PredicateFactory.*;

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

    private GroupResourceAssembler groupResourceAssembler = new GroupResourceAssembler();

    private GroupMemberResourceAssembler groupMemberResourceAssembler = new GroupMemberResourceAssembler();

    @ApiOperation(
            httpMethod = "GET",
            value = "Returns a kim group given the groupId",
            response = GroupResource.class
    )
    @RequestMapping(value="/{groupId}", method = RequestMethod.GET)
    public ResponseEntity<GroupResource>  getGroup(@ApiParam(value = "Id of the group", required = true) @PathVariable("groupId") String id) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException();
        }
        Group group =  groupService.getGroup(id);
        if(group == null){
            throw new NotFoundException();
        }

        return new ResponseEntity<GroupResource>(groupResourceAssembler.toResource(group), HttpStatus.OK);
    }

    @ApiOperation(
            httpMethod = "POST",
            value = "Creates a new group using the given Group.",
            response = GroupResource.class
    )
    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody ResponseEntity<GroupResource> createGroup(@ApiParam(value = "The group to be created", required = true) @RequestBody GroupResource groupResource){
        if(null == groupResource)  {
            throw new BadRequestException();
        }
        Group newGroup =  groupService.createGroup( GroupResource.toGroup(groupResource) );

        if(null == newGroup) {
            throw new OperationFailedException("Could not create group!");
        }

        return new ResponseEntity<GroupResource>(groupResourceAssembler.toResource(newGroup), HttpStatus.CREATED);
    }

    @ApiOperation(
            httpMethod = "PUT",
            value = "Updates an existing group using the given Group",
            notes = "This will attempt to update an existing Group.The passed in Group" +
                    "must have it's Id set and be a valid group that already exists.If the passed in groupId and the group.id " +
                    "values are different then this method will inactivate the old group and create a new group with the same " +
                    "members with the passed in groups properties.",
            response = GroupResource.class
    )
    @RequestMapping(value = "/{groupId}", method = RequestMethod.PUT)
    @ResponseBody ResponseEntity<GroupResource> updateGroup(@ApiParam(value = "Id of the group to be updated", required = true) @PathVariable("groupId") String groupId,
                                    @ApiParam(value = "Group object to use for update", required = true) @RequestBody GroupResource groupResource) {
        if (StringUtils.isBlank(groupId) || groupResource == null) {
            throw new BadRequestException();
        }

        Group updatedGroup = null;

        if(groupId.equals( groupResource.getId()) ) {
            updatedGroup = groupService.updateGroup( GroupResource.toGroup(groupResource) );
        } else {
            updatedGroup = groupService.updateGroup(groupId, GroupResource.toGroup(groupResource) );
        }

        if(null == updatedGroup) {
            throw new OperationFailedException("Failed to update group: " + groupId);
        }

        return new ResponseEntity<GroupResource>(groupResourceAssembler.toResource(updatedGroup), HttpStatus.OK);
    }

    @ApiOperation(
            httpMethod = "GET",
            value = "Get all the groups for a given principal, principal and namespaceCode or namespaceCode and groupName" ,
            notes = "This will include all groups directly assigned as well as those inferred by the fact that they are" +
                    " members of higher level groups.",
            response = GroupResource.class
    )
    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<Iterable<GroupResource>> retrieveGroups(@ApiParam(value = "The id of the Principal") @QueryParam("principalId") String principalId,
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
            groupResources.add(groupResourceAssembler.toResource(group));
        }

        return new ResponseEntity<Iterable<GroupResource>> (groupResources, HttpStatus.OK);
    }

    @ApiOperation(
            httpMethod = "GET",
            value = "Get all the group ids for a given principal or principal and namespaceCode",
            notes = "This will include all groups directly assigned as well as those inferred by the fact that they are members of higher level groups.",
            response = Link.class
    )
    @RequestMapping(value="/group-refs", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<Iterable<Link>> retrieveGroupRefs(@ApiParam(value = "The id of the Principal") @QueryParam("principalId") String principalId,
                                                            @ApiParam(value = "The namespace code of the desired Groups to return") @QueryParam("namespaceCode") String namespaceCode) {

        List<String> groupIds = new ArrayList<String>();

        if (StringUtils.isNotBlank(principalId) && StringUtils.isBlank(namespaceCode)) {
            groupIds =  groupService.getGroupIdsByPrincipalId(principalId);
        } else if (StringUtils.isNotBlank(principalId) && StringUtils.isNotBlank(namespaceCode)) {
            groupIds = groupService.getGroupIdsByPrincipalIdAndNamespaceCode(principalId, namespaceCode);
        } else {
            throw new BadRequestException("Legal query params are: just principalId, principalId and namespacecode");
        }

        List<Link> groupRefs = new ArrayList<Link>();
        if(null != groupIds) {
             for(String id : groupIds) {
                Link link = ControllerLinkBuilder.linkTo(KimGroupRestController.class).slash(id).withSelfRel();
                groupRefs.add(link);
            }
        }

        return new ResponseEntity<Iterable<Link>> (groupRefs, HttpStatus.OK);
    }

    @ApiOperation(
            httpMethod = "GET",
            value = "Get a group member given a groupId and memberId",
            response = GroupMemberResource.class
    )
    @RequestMapping(value="/{groupId}/members/{memberId}", method = RequestMethod.GET)
    public ResponseEntity<List<GroupMemberResource>> getGroupMember(@ApiParam(value = "The id of the group", required=true) @PathVariable("groupId") String groupId,
                                      @ApiParam(value = "The member id of the member", required=true) @PathVariable("memberId") String memberId) {

        if(StringUtils.isBlank(groupId) || StringUtils.isBlank(memberId))  {
            throw new BadRequestException();
        }

        List<GroupMember> members = groupService.getMembersOfGroup(groupId);

        List<GroupMemberResource> memberResources = new ArrayList<GroupMemberResource>();
        for(GroupMember member : members) {
            if(memberId.equals(member.getMemberId())) {
                memberResources.add ( groupMemberResourceAssembler.toResource(member));
            }
        }

        return new ResponseEntity<List<GroupMemberResource>> (memberResources, HttpStatus.OK);
    }

    @ApiOperation(
            httpMethod = "GET",
            value = "Gets a list of group members that belong to the group",
            response = GroupMemberResource.class
    )
    @RequestMapping(value="/{groupId}/members", method = RequestMethod.GET)
    public ResponseEntity<Iterable<GroupMemberResource>> getMembersForGroup(@ApiParam(value = "The id of the group", required=true) @PathVariable("groupId") String groupId) {
        if(StringUtils.isBlank(groupId)) {
            throw new BadRequestException();
        }

        List<GroupMember> members = groupService.getMembersOfGroup(groupId);

        List<GroupMemberResource> memberResources = new ArrayList<GroupMemberResource>();
        for(GroupMember member : members) {
            GroupMemberResource gmr = groupMemberResourceAssembler.toResource(member);
            memberResources.add(gmr);
        }

        return new ResponseEntity<Iterable<GroupMemberResource>> (memberResources, HttpStatus.OK);
    }

    @ApiOperation(
            httpMethod = "GET",
            value = "Gets a list of group member ids of type PRINCIPAL given a group id",
            notes = "If directMembersOnly is true the list will contain only direct member principal ids otherwise it will contain all member principal ids." +
                    "By default it is false",
            response = Link.class
    )
    @RequestMapping(value="/{groupId}/member-refs/principal", method = RequestMethod.GET)
    public ResponseEntity<Iterable<Link>> getDirectPrincipalMemberRefsForGroup(@ApiParam(value = "The id of the group", required=true) @PathVariable("groupId") String groupId,
                                                                 @ApiParam(value = "Flag for direct members") @QueryParam("directMembersOnly") boolean directMembersOnly) {
        List<String> memberIds = null;

        if(directMembersOnly) {
            memberIds = groupService.getDirectMemberPrincipalIds(groupId);
        } else {
            memberIds = groupService.getMemberPrincipalIds(groupId);
        }

        List<Link> memberRefs = new ArrayList<Link>();
        if(null != memberIds) {
            for(String id : memberIds) {
                Link link = ControllerLinkBuilder.linkTo(KimGroupRestController.class).slash(groupId).slash("members").slash(id).withSelfRel();
                memberRefs.add( link );
            }
        }

        return new ResponseEntity<Iterable<Link>> (memberRefs, HttpStatus.OK);
    }

    @ApiOperation(
            httpMethod = "GET",
            value = "Gets a list of group member ids of type GROUP given a group id",
            notes = "If directMembersOnly is true the list will contain only direct member group ids otherwise it will contain all member group ids." +
                    "By default it is false",
            response = Link.class
    )
    @RequestMapping(value="/{groupId}/member-refs/group", method = RequestMethod.GET)
    public ResponseEntity<Iterable<Link>> getGroupMemberRefsForGroup(@ApiParam(value = "The id of the group", required=true) @PathVariable("groupId") String groupId,
                                                       @ApiParam(value = "Flag for direct members") @QueryParam("directMembersOnly") boolean directMembersOnly) {

        List<String> memberIds = null;

        if(directMembersOnly) {
            memberIds = groupService.getDirectMemberGroupIds(groupId);
        } else {
            memberIds = groupService.getMemberGroupIds(groupId);
        }

        List<Link> memberRefs = new ArrayList<Link>();
        if(null != memberIds) {
            for(String id : memberIds) {
                Link link = ControllerLinkBuilder.linkTo(KimGroupRestController.class).slash(groupId).slash("members").slash(id).withSelfRel();
                memberRefs.add( link );
            }
        }

        return new ResponseEntity<Iterable<Link>> (memberRefs, HttpStatus.OK);
    }

    @ApiOperation(
            httpMethod = "PUT",
            value = "Adds a new member of type PRINCIPAL to the group",
            notes = "The member should be an existing principal" ,
            response = Link.class
    )
    @RequestMapping(value = "/{groupId}/members/principal/{memberId}", method = RequestMethod.PUT)
    @ResponseBody
    ResponseEntity<Link> addPrincipalToGroup(@ApiParam(value = "The id of the group", required=true) @PathVariable("groupId") String groupId,
                                               @ApiParam(value = "The member id of the member", required=true) @PathVariable("memberId") String memberId) {
        if (StringUtils.isBlank(groupId) || StringUtils.isBlank(memberId)) {
            throw new BadRequestException();
        }

        boolean status =  groupService.addPrincipalToGroup(memberId, groupId);

        if(!status) {
            throw new OperationFailedException("Could not add principal " + memberId + " to group " + groupId);
        }

        Link link = ControllerLinkBuilder.linkTo(KimGroupRestController.class).slash(groupId).slash("members").withSelfRel();

        return new ResponseEntity<Link>(link, HttpStatus.OK);
    }

    @ApiOperation(
            httpMethod = "PUT",
            value = "Adds a new member of type GROUP to the group",
            notes = "The group should be an existing group" ,
            response = Link.class
    )
    @RequestMapping(value = "/{groupId}/members/group/{memberId}", method = RequestMethod.PUT)
    @ResponseBody
    ResponseEntity<Link> addGroupToGroup(@ApiParam(value = "The id of the group", required=true) @PathVariable("groupId") String groupId,
                                           @ApiParam(value = "The member id of the member", required=true) @PathVariable("memberId") String memberId) {
        if (StringUtils.isBlank(groupId) || StringUtils.isBlank(memberId)) {
            throw new BadRequestException();
        }

        boolean status =  groupService.addGroupToGroup(memberId, groupId);

        if(!status) {
            throw new OperationFailedException("Could not add group " + memberId + " to group " + groupId);
        }

        Link link = ControllerLinkBuilder.linkTo(KimGroupRestController.class).slash(groupId).slash("members").withSelfRel();

        return new ResponseEntity<Link>(link, HttpStatus.OK);
    }

    @ApiOperation(
            httpMethod = "POST",
            value = "Add a new member to the group using a given group member",
            response = GroupMemberResource.class
    )
    @RequestMapping(value = "/{groupId}/members", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    ResponseEntity<GroupMemberResource> createGroupMember(@ApiParam(value = "The id of the group", required=true) @PathVariable("groupId") String groupId,
                                  @ApiParam(value = "The group member to use", required=true) @RequestBody GroupMemberResource groupMemberResource) {
        if (StringUtils.isBlank(groupId) || groupMemberResource == null) {
            throw new BadRequestException();
        }

        groupService.createGroupMember( GroupMemberResource.toGroupMember(groupMemberResource) );

        /**
         *  When the GroupMember is saved, BO saves it as SQL time this causes the time to be changed to local timezone.
         *  The input from JSON is in JODA time with timezone that always converts the time to GMT. So even if the two
         *  times are the same, they are represented in different timezones and the equals operator fails. The right way
         *  to compare time should be with millisecs. See KULRICE-14225 for more details
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

        GroupMemberResource resource = groupMemberResourceAssembler.toResource(newMember);

        return new ResponseEntity<GroupMemberResource>(resource, HttpStatus.CREATED);
    }

    @ApiOperation(
            httpMethod = "PUT",
            value = "Updates an existing group using the given GroupMember ",
            notes = "The passed in GroupMember must have it's Id set and be a valid groupMember that already exists",
            response = GroupMemberResource.class
    )
    @RequestMapping(value = "/{groupId}/members/{memberId}", method = RequestMethod.PUT)
    @ResponseBody
    GroupMemberResource updateGroupMember(@ApiParam(value = "The id of the group", required=true) @PathVariable("groupId") String groupId,
                                          @ApiParam(value = "The member id of the member", required=true) @PathVariable("memberId") String memberId,
                                          @ApiParam(value = "The group member to update", required=true) @RequestBody GroupMemberResource groupMemberResource) {
        if (StringUtils.isBlank(groupId) || StringUtils.isBlank(memberId) || groupMemberResource == null) {
            throw new BadRequestException();
        }

        GroupMember member =  groupService.updateGroupMember( GroupMemberResource.toGroupMember(groupMemberResource) );

        return GroupMemberResource.fromGroupMember(member);

    }

    @ApiOperation(
            httpMethod = "DELETE",
            value = "Deletes an existing member from a Group depending on whether it is of type PRINCIPAL or GROUP",
            response = Link.class
    )
    @RequestMapping(value = "/{groupId}/members/{memberId}", method = RequestMethod.DELETE)
    public ResponseEntity<Link> removeMemberFromGroup(@ApiParam(value = "The id of the group", required=true) @PathVariable("groupId") String groupId,
                                                      @ApiParam(value = "The member id of the member", required=true) @PathVariable("memberId") String memberId) {
        if (StringUtils.isBlank(groupId) || StringUtils.isBlank(memberId)) {
            throw new BadRequestException();
        }

        ResponseEntity<List<GroupMemberResource>> members = getGroupMember(groupId, memberId);
        if(members.getBody() == null || members.getBody().size() > 0) {
            throw new NotFoundException("No members found with member Id: " + memberId + " for group: " + groupId);
        }

        GroupMemberResource member = members.getBody().get(0);

        boolean status = false;

        if(MemberType.PRINCIPAL.equals(member.getType())) {
            status = groupService.removePrincipalFromGroup(memberId, groupId);
        }  else  if(MemberType.GROUP.equals(member.getType())) {
            status = groupService.removeGroupFromGroup(memberId, groupId);
        }

        if(!status) {
            throw new OperationFailedException("Could not remove member " + memberId + " from group " + groupId);
        }

        Link link = ControllerLinkBuilder.linkTo(KimGroupRestController.class).slash(groupId).slash("members").withSelfRel();
        return new ResponseEntity<Link>(link, HttpStatus.OK);
    }

    @ApiOperation(
            httpMethod = "DELETE",
            value = "Removes all members from the group with the given groupId.",
            response = Link.class
    )
    @RequestMapping(value = "/{groupId}/members", method = RequestMethod.DELETE)
    public ResponseEntity<Link> removeAllMemberFromGroup(@ApiParam(value = "The id of the group", required=true) @PathVariable("groupId") String groupId ) {
        if (StringUtils.isBlank(groupId)) {
            throw new BadRequestException();
        }

        groupService.removeAllMembers(groupId);

        Link link = ControllerLinkBuilder.linkTo(KimGroupRestController.class).slash(groupId).slash("members").withSelfRel();

        return new ResponseEntity<Link>(link, HttpStatus.OK);
    }

    @ApiOperation(
            httpMethod = "GET",
            value = "Lists groups using paging and limiting results",
            notes = "Filters can be applied to limit results",
            response = RicePagedResources.class
    )
    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public @ResponseBody
    ResponseEntity<RicePagedResources<GroupResource>> findGroups(@ApiParam(value = "Starting index of results to fetch") @RequestParam(value = "startIndex", defaultValue = "0", required = false) int startIndex,
                                                                 @ApiParam(value = "Max limit of items returned") @RequestParam(value = "limit", defaultValue = RiceRestConstants.MAX_RESULTS, required = false) int limit,
                                                                 @ApiParam(value = "Filters to apply in the format: filter=&lt;name&gt;::&lt;wildcardedValue&gt;|&lt;name&gt;::&lt;wildcardedValue&gt;") @RequestParam(value = "filter", required = false) String filter) {

        Map<String, String> criteria = RiceRestUtils.translateFilterToMap(filter);
        QueryByCriteria queryByCriteria = null;

        boolean validPrncplFoundIfPrncplCritPresent = true;
        Map<String, String> attribsMap = new HashMap<String, String>();
        if (!criteria.isEmpty()) {
            List<Predicate> predicates = new ArrayList<Predicate>();
            //principalId doesn't exist on 'Group'.  Lets do this predicate conversion separately
            if (StringUtils.isNotBlank(criteria.get(KimConstants.UniqueKeyConstants.PRINCIPAL_NAME))) {
                Predicate principalPred = like("principalName", criteria.get(KimConstants.UniqueKeyConstants.PRINCIPAL_NAME));
                QueryByCriteria principalCriteria = QueryByCriteria.Builder.fromPredicates(principalPred);

                PrincipalQueryResults principals = KimApiServiceLocator.getIdentityService()
                        .findPrincipals(principalCriteria);
                List<String> principalIds = new ArrayList<String>();
                for (Principal principal : principals.getResults()) {
                    principalIds.add(principal.getPrincipalId());
                }
                if (CollectionUtils.isNotEmpty(principalIds)) {
                    Timestamp currentTime = new Timestamp(Calendar.getInstance().getTimeInMillis());
                    predicates.add( and(
                            in("members.memberId", principalIds.toArray(
                                    new String[principalIds.size()])),
                            equal("members.typeCode", KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE.getCode()),
                            and(
                                    or(isNull("members.activeFromDateValue"), lessThanOrEqual("members.activeFromDateValue", currentTime)),
                                    or(isNull("members.activeToDateValue"), greaterThan("members.activeToDateValue", currentTime))
                            )
                    ));
                }else {
                    validPrncplFoundIfPrncplCritPresent = false;
                }

            }
            criteria.remove(KimConstants.UniqueKeyConstants.PRINCIPAL_NAME);

            if(!criteria.isEmpty()) {
                predicates.add(PredicateUtils.convertMapToPredicate(criteria));
            }

            QueryByCriteria.Builder builder = QueryByCriteria.Builder.create();
            builder.setStartAtIndex(startIndex);
            builder.setMaxResults(limit);

            queryByCriteria = builder.fromPredicates(and(predicates.toArray(new Predicate[predicates.size()])));


        }

        List<Group> groups = new ArrayList<Group>();

        if (validPrncplFoundIfPrncplCritPresent) {
            GroupQueryResults groupResults = KimApiServiceLocator.getGroupService().findGroups(queryByCriteria);

            // Really bad predicate to SQL query building causes 7776 groups with same Id to be returned when 1 is exptected (issue exists in GroupService.findGroups)
            Set<String> uniqueGroupIds = new HashSet<String>();
            for(Group group : groupResults.getResults()) {
                if(!uniqueGroupIds.contains( group.getId()) ) {
                    uniqueGroupIds.add(group.getId());
                    groups.add(group);
                }
            }
        }

        Map<String, String> queryParams = new HashMap<String, String>();

        if (StringUtils.isNotBlank(filter)) {
            queryParams.put("filter", filter);
        }


        RicePagedResources<GroupResource> result = new RicePagedResources<GroupResource>(startIndex,
                limit, groupResourceAssembler.toResources(groups), queryParams, 0);

        return new ResponseEntity<RicePagedResources<GroupResource>>(result, HttpStatus.OK);

    }

    @ApiOperation(
            httpMethod = "GET",
            value = "Lists group Members using paging and limiting results",
            notes = "Filters can be applied to limit results",
            response = RicePagedResources.class
    )
    @RequestMapping(value="/search/members", method = RequestMethod.GET)
    public ResponseEntity<RicePagedResources<GroupMemberResource>> findGroupMembers(@ApiParam(value = "Starting index of results to fetch") @RequestParam(value = "startIndex", defaultValue = "0", required = false) int startIndex,
                                                                                    @ApiParam(value = "Max limit of items returned") @RequestParam(value = "limit", defaultValue = RiceRestConstants.MAX_RESULTS, required = false) int limit,
                                                                                    @ApiParam(value = "Filters to apply in the format: filter=&lt;name&gt;::&lt;wildcardedValue&gt;|&lt;name&gt;::&lt;wildcardedValue&gt;") @RequestParam(value = "filter", required = false) String filter) {

        List<Predicate> predicates = new ArrayList<Predicate>();
        QueryByCriteria queryByCriteria = null;

        Map<String, String> criteria = RiceRestUtils.translateFilterToMap(filter);

        Timestamp currentTime = new Timestamp(Calendar.getInstance().getTimeInMillis());
        if(!criteria.isEmpty())    {
            if(criteria.containsKey("principalId") && criteria.containsKey("groupId")){
                predicates.add(
                        and(
                                equal(KIMPropertyConstants.GroupMember.MEMBER_ID, criteria.get("principalId")),
                                equal(KIMPropertyConstants.GroupMember.MEMBER_TYPE_CODE, KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE.getCode()),
                                equal(KIMPropertyConstants.GroupMember.GROUP_ID, criteria.get("groupId")),
                                and(
                                        or(isNull("members.activeFromDateValue"), lessThanOrEqual("members.activeFromDateValue", currentTime)),
                                        or(isNull("members.activeToDateValue"), greaterThan("members.activeToDateValue", currentTime))
                                )
                        )
                );
            }
            predicates.add(PredicateUtils.convertMapToPredicate(criteria));

            QueryByCriteria.Builder builder = QueryByCriteria.Builder.create();
            builder.setStartAtIndex(startIndex);
            builder.setMaxResults(limit);

            queryByCriteria = QueryByCriteria.Builder.fromPredicates(and(predicates.toArray(new Predicate[predicates.size()])));
        }

        List<GroupMember> groupMembers = new ArrayList<GroupMember>();
        GroupMemberQueryResults groupMemberResults = KimApiServiceLocator.getGroupService().findGroupMembers(queryByCriteria);
        groupMembers.addAll( groupMemberResults.getResults() );

        Map<String, String> queryParams = new HashMap<String, String>();

        if (StringUtils.isNotBlank(filter)) {
            queryParams.put("filter", filter);
        }


        RicePagedResources<GroupMemberResource> result = new RicePagedResources<GroupMemberResource>(startIndex,
                limit, groupMemberResourceAssembler.toResources(groupMembers), queryParams, 0);

        return new ResponseEntity<RicePagedResources<GroupMemberResource>>(result, HttpStatus.OK);
    }

}
