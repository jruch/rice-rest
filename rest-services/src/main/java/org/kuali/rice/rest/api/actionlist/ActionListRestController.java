package org.kuali.rice.rest.api.actionlist;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.displaytag.properties.SortOrderEnum;
import org.displaytag.util.LookupUtil;
import org.kuali.rice.core.api.delegation.DelegationType;
import org.kuali.rice.kew.actionitem.ActionItem;
import org.kuali.rice.kew.actionitem.ActionItemComparator;
import org.kuali.rice.kew.actionlist.ActionListFilter;
import com.wordnik.swagger.annotations.*;
import org.kuali.rice.kew.actionlist.service.ActionListService;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.rest.RiceRestConstants;
import org.kuali.rice.rest.api.paging.RicePagedResources;
import org.kuali.rice.rest.utils.RiceRestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * Rest resource for Document Search.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@Controller
@RequestMapping(value = RiceRestConstants.API_URL + "/actionlist", produces = MediaType.APPLICATION_JSON)
@Api(value = "/actionlist", description = "Action list operations")
public class ActionListRestController {

    private ActionItemResourceAssembler actionItemResourceAssembler = new ActionItemResourceAssembler();
    private static final String ACTION_LIST_DEFAULT_SORT = "routeHeader.createDate";

    @ResponseBody
    @RequestMapping(value = "/{principalId}", method = RequestMethod.GET)
    @ApiOperation(
            httpMethod = "GET",
            value = "Get action list by principal ID",
            notes = "List action items for principal ID using paging.  " +
                    "Filters can also be applied.",
            response = RicePagedResources.class
    )
    public ResponseEntity<RicePagedResources<ActionItemResource>> getDocuments(
                                                                               @ApiParam(value = "Principal Id of user to retrieve documents for", required = true) @PathVariable String principalId,
                                                                               @ApiParam(value = "Starting index of results to fetch") @RequestParam(value = "startIndex", defaultValue = "0", required = false) int startIndex,
                                                                               @ApiParam(value = "Max limit of items returned") @RequestParam(value = "limit", defaultValue = RiceRestConstants.DOCUMENT_SEARCH_DEFAULT_LIMIT, required = false) int limit,
                                                                               @ApiParam(value = "Filters to apply in the format: filter=&lt;name&gt;::&lt;value&gt;|&lt;name&gt;::&lt;value&gt;.<br />Filters defined in org.kuali.rice.kew.actionlist.ActionListFilter.  "
                                                                                    + "<br />Available filters:"
                                                                                    + "<br />Document"
                                                                                    + "<br />documentTitle"
                                                                                    + "<br />excludeDocumentTitle (boolean used in conjunction with documentTitle filter)"
                                                                                    + "<br />docRouteStatus"
                                                                                    + "<br />excludeRouteStatus (boolean used in conjunction with docRouteStatus filter)"
                                                                                    + "<br />actionRequestCd (action requested code)"
                                                                                    + "<br />excludeActionRequestCd (boolean used in conjunction with actionRequestedCd filter)"
                                                                                    + "<br />groupId"
                                                                                    + "<br />groupName"
                                                                                    + "<br />excludeGroupId (boolean used in conjunction with groupId filter)"
                                                                                    + "<br />documentType"
                                                                                    + "<br />excludeDocumentType (boolean used in conjunction with groupId filter)"
                                                                                    + "<br />createDateFrom (yyyy-mm-dd)"
                                                                                    + "<br />createDateTo (yyyy-mm-dd)"
                                                                                    + "<br />excludeCreateDate (boolean used in conjunction with createDateFrom filter)"
                                                                                    + "<br />lastAssignedDateFrom (yyyy-mm-dd)"
                                                                                    + "<br />lastAssignedDateTo (yyyy-mm-dd)"
                                                                                    + "<br />excludeLastAssignedDate (boolean used in conjunction with lastAssignedDateFrom filter)"
                                                                                    + "<br />delegatorId"
                                                                                    + "<br />primaryDelegateId"
                                                                                    + "<br />excludeDelegatorId  (boolean used in conjunction with delegatorId filter)"
                                                                                    + "<br />delegationType"
                                                                                    + "<br />excludeDelegationType (boolean used in conjunction with delegatorType filter)"
                                                                               ) @RequestParam(value = "filter", required = false) String filter,
                                                                               @ApiParam(value = "Sort Criteria") @RequestParam(value = "sortCriterion", required = false) String sortCriterion,
                                                                               @ApiParam(value = "Sort Direction") @RequestParam(value = "sortDirection", required = false) String dir
    ) {


        ActionListService actionListService = KEWServiceLocator.getActionListService();

        //setup filter
        Map<String, String> criteria = RiceRestUtils.translateFilterToMap(filter);
        final ObjectMapper mapper = new ObjectMapper();
        ActionListFilter actionListFilter = mapper.convertValue(criteria, ActionListFilter.class);

        //defaults
        if(actionListFilter.getDocRouteStatus() ==  null) { actionListFilter.setDocRouteStatus("All"); };
        if(actionListFilter.getActionRequestCd() ==  null) { actionListFilter.setActionRequestCd("All"); };
        if(actionListFilter.getGroupIdString() ==  null) { actionListFilter.setGroupIdString("No Filtering"); };

        //get action items
        List<ActionItem> results = new ArrayList<ActionItem>(actionListService.getActionList(principalId, actionListFilter));

        //sort action items if needed
        if (sortCriterion != null) {
            SortOrderEnum sortOrder = SortOrderEnum.ASCENDING;
            if (dir != null && "desc".equals(dir)) {
                sortOrder = SortOrderEnum.DESCENDING;
            }
            sortActionList(results, sortCriterion, sortOrder);
        }

        //return only items requested
        List<ActionItem> pagedResults = results.subList(Math.max(0, startIndex), Math.min(results.size(),
                startIndex + limit));

        // parameters for self url
        ObjectMapper objectMapper = new ObjectMapper();

        @SuppressWarnings("unchecked")
        Map<String, Object> filterMap = objectMapper.convertValue(actionListFilter, Map.class);
        Map<String,String> queryParams =new HashMap<String,String>();
        for (Map.Entry<String, Object> entry : filterMap.entrySet()) {
            if(entry.getValue() != null) {

                // if boolean, don't add if false, as this is the defaulted value
                if(entry.getValue() instanceof Boolean) {
                    if((Boolean)entry.getValue()) {
                        queryParams.put(entry.getKey(), (String) entry.getValue().toString());
                    }
                }
                else {
                    queryParams.put(entry.getKey(), (String) entry.getValue().toString());
                }
            }
        }
        queryParams.put("principalId", principalId);
        if(dir != null) { queryParams.put("sortOrder", dir); }
        if(sortCriterion != null) { queryParams.put("sortCriterion", sortCriterion); }

        RicePagedResources<ActionItemResource> result = new RicePagedResources<ActionItemResource>(startIndex,
                limit, actionItemResourceAssembler.toResources(pagedResults), queryParams);

        return new ResponseEntity<RicePagedResources<ActionItemResource>>(result, HttpStatus.OK);

    }

    private void sortActionList(List<ActionItem> actionList, String sortName, SortOrderEnum sortOrder) {
        if (StringUtils.isEmpty(sortName)) {
            return;
        }

        Comparator<ActionItem> comparator = new ActionItemComparator(sortName);
        if (SortOrderEnum.DESCENDING.equals(sortOrder)) {
            comparator = ComparatorUtils.reversedComparator(comparator);
        }

        Collections.sort(actionList, comparator);
    }

    private static class ActionItemComparator implements Comparator<ActionItem> {

        private static final String ACTION_LIST_DEFAULT_SORT = "routeHeader.createDate";

        private final String sortName;

        public ActionItemComparator(String sortName) {
            if (StringUtils.isEmpty(sortName)) {
                sortName = ACTION_LIST_DEFAULT_SORT;
            }
            this.sortName = sortName;
        }

        @Override
        public int compare(ActionItem actionItem1, ActionItem actionItem2) {
            try {
                // invoke the power of the lookup functionality provided by the display tag library, this LookupUtil method allows for us
                // to evaulate nested bean properties (like workgroup.groupNameId.nameId) in a null-safe manner.  For example, in the
                // example if workgroup evaluated to NULL then LookupUtil.getProperty would return null rather than blowing an exception
                Object property1 = LookupUtil.getProperty(actionItem1, sortName);
                Object property2 = LookupUtil.getProperty(actionItem2, sortName);
                if (property1 == null && property2 == null) {
                    return 0;
                } else if (property1 == null) {
                    return -1;
                } else if (property2 == null) {
                    return 1;
                }
                if (property1 instanceof Comparable) {
                    return ((Comparable)property1).compareTo(property2);
                }
                return property1.toString().compareTo(property2.toString());
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new RuntimeException("Could not sort for the given sort name: " + sortName, e);
            }
        }
    }

}
