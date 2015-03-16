package org.kuali.rice.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown from Rest controllers to return an Unauthorized response.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(){}

    public UnauthorizedException(String message){
        super(message);
    }
}
