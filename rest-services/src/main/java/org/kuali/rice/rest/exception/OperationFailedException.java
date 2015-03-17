package org.kuali.rice.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
  * Exception thrown from Rest controllers to return a Bad Request response.
  *
  * @author Kuali Rice Team (rice.collab@kuali.org)
  */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class OperationFailedException extends RuntimeException {
    public OperationFailedException() {
    }

    public OperationFailedException(String message) {
        super(message);
    }
}