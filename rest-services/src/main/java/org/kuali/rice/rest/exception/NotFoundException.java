package org.kuali.rice.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown from Rest controllers to return a Not Found response.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
    public NotFoundException(){}
    
    public NotFoundException(String message){
        super(message);
    }
}
