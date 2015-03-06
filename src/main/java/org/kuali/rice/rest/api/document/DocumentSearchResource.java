package org.kuali.rice.rest.api.document;

import com.wordnik.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;

/**
 * Rest resource for Document Search.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@RestController
@RequestMapping(value = "/api/v1/documents", produces = MediaType.APPLICATION_JSON)
@Api(value = "/documents", description = "Document search operations")
public class DocumentSearchResource {
}
