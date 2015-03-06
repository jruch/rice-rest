package org.kuali.rice.rest.api.actionlist;

import com.wordnik.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;

/**
 * Rest resource for ActionList.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@RestController
@RequestMapping(value = "/api/v1/actionlist", produces = MediaType.APPLICATION_JSON)
@Api(value = "/actionlist", description = "Operations on actionlist")
public class ActionListResource {
}
