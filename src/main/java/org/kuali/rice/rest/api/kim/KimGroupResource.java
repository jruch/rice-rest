package org.kuali.rice.rest.api.kim;

import com.wordnik.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;

/**
 * Rest resource for kim groups.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@RestController
@RequestMapping(value = "/api/v1/kimgroups", produces = MediaType.APPLICATION_JSON)
@Api(value = "/kimgroups", description = "Operations on kimgroups")
public class KimGroupResource {

}
