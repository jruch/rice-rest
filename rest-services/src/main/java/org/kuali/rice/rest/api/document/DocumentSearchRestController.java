package org.kuali.rice.rest.api.document;

import com.wordnik.swagger.annotations.*;
import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.document.Document;
import org.kuali.rice.kew.api.document.search.DocumentSearchCriteria;
import org.kuali.rice.kew.api.document.search.DocumentSearchResults;
import org.kuali.rice.kew.impl.document.search.DocumentSearchCriteriaTranslator;
import org.kuali.rice.kew.impl.document.search.DocumentSearchCriteriaTranslatorImpl;
import org.kuali.rice.rest.RiceRestConstants;
import org.kuali.rice.rest.api.paging.RicePagedResources;
import org.kuali.rice.rest.exception.NotFoundException;
import org.kuali.rice.rest.utils.RiceRestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rest resource for Document Search.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@Controller
@RequestMapping(value = RiceRestConstants.API_URL + "/documents", produces = MediaType.APPLICATION_JSON)
@Api(value = "/documents", description = "Document search operations")
public class DocumentSearchRestController {

    private DocumentResourceAssembler documentResourceAssembler = new DocumentResourceAssembler();

    DocumentSearchCriteriaTranslator translator = new DocumentSearchCriteriaTranslatorImpl();

    @Autowired
    DocumentEnhancedSearchService documentEnhancedSearchService;

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(
            httpMethod = "GET",
            value = "List documents for principalId using paging",
            notes = "List documents for principalId using paging and limiting results.  " +
                    "Filters can also be applied (and can use wildcards/range)." + "" +
                    "<br/>Filters that can be applied:" +
                    "<br/>documentId" +
                    "<br/>title" +
                    "<br/>applicationDocumentId" +
                    "<br/>applicationDocumentStatus" +
                    "<br/>initiatorPrincipalName" +
                    "<br/>initiatorPrincipalId" +
                    "<br/>viewerPrincipalName" +
                    "<br/>viewerPrincipalId" +
                    "<br/>groupViewerId" +
                    "<br/>groupViewerName" +
                    "<br/>approverPrincipalName" +
                    "<br/>approverPrincipalId" +
                    "<br/>routeNodeName" +
                    "<br/>documentTypeName" +
                    "<br/> Date filters below support greater than, less than, and equal symbols for ranges (ie, &gt;=03/10/2010) : " +
                    "<br/>dateCreated (and explicit dateCreatedFrom and dateCreatedTo variations)"  +
                    "<br/>dateLastModified (and explicit dateLastModifiedFrom and dateLastModifiedTo variations)" +
                    "<br/>dateApproved (and explicit dateApprovedFrom and dateApprovedTo variations)" +
                    "<br/>dateFinalized (and explicit dateFinalizedFrom and dateFinalizedTo variations)"  +
                    "<br/>dateApplicationDocumentStatusChanged (and explicit dateApplicationDocumentStatusChangedFrom and dateApplicationDocumentStatusChangedTo variations)",
            response = RicePagedResources.class,
            authorizations = {@Authorization(value = "oauth2",
                    scopes = {
                            @AuthorizationScope(scope = "access", description = "Read documents")
                    }
            )}
    )
    public ResponseEntity<RicePagedResources<DocumentResource>> getDocuments(@ApiParam(value = "Principal Id of user to retrieve documents for") @RequestParam(value = "principalId", required = false) String principalId,
                                                                             @ApiParam(value = "Starting index of results to fetch") @RequestParam(value = "startIndex", defaultValue = "0", required = false) int startIndex,
                                                                             @ApiParam(value = "Max limit of items returned") @RequestParam(value = "limit", defaultValue = RiceRestConstants.DOCUMENT_SEARCH_DEFAULT_LIMIT, required = false) int limit,
                                                                             @ApiParam(value = "Filters to apply in the format: filter=&lt;name&gt;::&lt;wildcardedValue&gt;|&lt;name&gt;::&lt;wildcardedValue&gt;") @RequestParam(value = "filter", required = false) String filter) {

        Map<String, String> criteria = RiceRestUtils.translateFilterToMap(filter);

        if (!criteria.containsKey("isAdvancedSearch")) {
            criteria.put("isAdvancedSearch", "NO");
        }

        DocumentSearchCriteria serviceCriteria = translator.translateFieldsToCriteria(criteria);
        DocumentSearchCriteria.Builder builder = DocumentSearchCriteria.Builder.create(serviceCriteria);

        builder.setStartAtIndex(startIndex);
        builder.setMaxResults(limit);

        serviceCriteria = builder.build();

        DocumentSearchResults serviceResults = documentEnhancedSearchService.lookupDocuments(principalId, serviceCriteria);

        List<Document> results = new ArrayList<Document>();
        for (org.kuali.rice.kew.api.document.search.DocumentSearchResult serviceResult : serviceResults.getSearchResults()) {
            Document document = serviceResult.getDocument();

            results.add(document);
        }

        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("principalId", principalId);

        if (StringUtils.isNotBlank(filter)) {
            queryParams.put("filter", filter);
        }

        RicePagedResources<DocumentResource> result = new RicePagedResources<DocumentResource>(startIndex,
                limit, documentResourceAssembler.toResources(results), queryParams, serviceResults.getNumberOfSecurityFilteredResults());

        return new ResponseEntity<RicePagedResources<DocumentResource>>(result, HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ApiOperation(
            value = "Get a document by id",
            notes = "Get a document by id",
            response = DocumentResource.class,
            authorizations = {@Authorization(value = "oauth2",
                    scopes = {
                            @AuthorizationScope(scope = "access", description = "Read documents")
                    }
            )}
    )
    @ApiResponses({@ApiResponse(code = 404, response = String.class, message = "Not found")})
    public ResponseEntity<DocumentResource> getDocumentById(@ApiParam(value = "Principal Id of user to retrieve documents for") @RequestParam(value = "principalId", required = false) String principalId,
                                                            @ApiParam(value = "Id of the document", required = true) @PathVariable String id) {
        DocumentResource documentResource = null;
        if (StringUtils.isNotBlank(principalId)) {
            // go through getDocuments for security when principal is supplied
            ResponseEntity<RicePagedResources<DocumentResource>> results = getDocuments(principalId, 0, 1, "documentId::" + id);
            if (!results.getBody().getContent().isEmpty()) {
                documentResource = results.getBody().getContent().iterator().next();
            }
        } else {
            Document document = KewApiServiceLocator.getWorkflowDocumentService().getDocument(id);
            if (document != null) {
                documentResource = documentResourceAssembler.toResource(document);
            }
        }


        if (documentResource == null) {
            throw new NotFoundException();
        }

        return new ResponseEntity<DocumentResource>(documentResource, HttpStatus.OK);
    }


    public void setDocumentEnhancedSearchService(DocumentEnhancedSearchService documentEnhancedSearchService) {
        this.documentEnhancedSearchService = documentEnhancedSearchService;
    }

}
