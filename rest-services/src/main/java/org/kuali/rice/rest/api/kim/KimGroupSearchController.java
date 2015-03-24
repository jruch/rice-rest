package org.kuali.rice.rest.api.kim;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiParam;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.kuali.rice.core.api.criteria.Predicate;
import org.kuali.rice.core.api.criteria.PredicateUtils;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.group.*;
import org.kuali.rice.kim.api.identity.principal.Principal;
import org.kuali.rice.kim.api.identity.principal.PrincipalQueryResults;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kim.impl.KIMPropertyConstants;
import org.kuali.rice.rest.RiceRestConstants;
import org.kuali.rice.rest.api.paging.RicePagedResources;
import org.kuali.rice.rest.utils.RiceRestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.ws.rs.core.MediaType;
import java.sql.Timestamp;
import java.util.*;

import static org.kuali.rice.core.api.criteria.PredicateFactory.*;
import static org.kuali.rice.core.api.criteria.PredicateFactory.and;
import static org.kuali.rice.core.api.criteria.PredicateFactory.greaterThan;

/**
 * Created by sona on 3/17/15.
 */
@Controller
@RequestMapping(value = "/api/v1/kimgroups/search", produces = MediaType.APPLICATION_JSON)
@Api(value = "/kimgroups/search", description = "Searches on kimgroups")
//{kimTypeId=, name=, description=, active=Y, principalName=, id=, namespaceCode=}
public class KimGroupSearchController {

    private GroupService groupService = KimApiServiceLocator.getGroupService();

    private GroupResourceAssembler groupResourceAssembler = new GroupResourceAssembler();

    private GroupMemberResourceAssembler groupMemberResourceAssembler = new GroupMemberResourceAssembler();

    @RequestMapping(method = RequestMethod.GET)
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

            predicates.add(PredicateUtils.convertMapToPredicate(criteria));

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
    @RequestMapping(value="/members", method = RequestMethod.GET)
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


    //    @Override
//    public List<String> findGroupIds(QueryByCriteria queryByCriteria) throws RiceIllegalArgumentException {
//        return null;
//    }
//

//
//    @Override
//
//
//
//    @Override
//    public List<Group> getGroups(Collection<String> strings) throws RiceIllegalArgumentException {
//        return null;
//    }
//
//    @Override
//    public boolean isMemberOfGroup(String s, String s2) throws RiceIllegalArgumentException {
//        return false;
//    }
//
//    @Override
//    public boolean isDirectMemberOfGroup(String s, String s2) throws RiceIllegalArgumentException {
//        return false;
//    }
//
//
//    @Override
//    public boolean isGroupMemberOfGroup(String s, String s2) throws RiceIllegalArgumentException {
//        return false;
//    }
//
//
//    @Override
//    public List<String> getParentGroupIds(String s) throws RiceIllegalArgumentException {
//        return null;
//    }
//
//    @Override
//    public List<String> getDirectParentGroupIds(String s) throws RiceIllegalArgumentException {
//        return null;
//    }
//
//    @Override
//    public Map<String, String> getAttributes(String s) throws RiceIllegalArgumentException {
//        return null;
//    }
//
//

}
