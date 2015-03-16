package org.kuali.rice.rest.api.document;


import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import org.joda.time.DateTime;
import org.kuali.rice.kew.api.document.Document;
import org.kuali.rice.kew.api.document.DocumentContract;
import org.kuali.rice.kew.api.document.DocumentStatus;
import org.springframework.hateoas.ResourceSupport;

import java.util.Map;

/**
 * Document resource object representing a Rice Document.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@ApiModel(value = "Rice Document")
public class DocumentResource extends ResourceSupport implements DocumentContract {

    private Document document;

    public DocumentResource(Document document) {
        this.document = document;
    }

    @ApiModelProperty(value = "Document id", required=true)
    public String getDocumentId() {
        return document.getDocumentId();
    }

    @ApiModelProperty(value = "Document status")
    public DocumentStatus getStatus() {
        return document.getStatus();
    }

    @ApiModelProperty(value = "Date this document was created")
    public DateTime getDateCreated() {
        return document.getDateCreated();
    }

    @ApiModelProperty(value = "Date this document was modified")
    public DateTime getDateLastModified() {
        return document.getDateLastModified();
    }

    @ApiModelProperty(value = "Date this document was approved")
    public DateTime getDateApproved() {
        return document.getDateApproved();
    }

    @ApiModelProperty(value = "Date this document was finalized")
    public DateTime getDateFinalized() {
        return document.getDateFinalized();
    }

    @ApiModelProperty(value = "The title of this document")
    public String getTitle() {
        return document.getTitle();
    }

    @ApiModelProperty(value = "Application document id")
    public String getApplicationDocumentId() {
        return document.getApplicationDocumentId();
    }

    @ApiModelProperty(value = "Date this document was created")
    public String getInitiatorPrincipalId() {
        return document.getInitiatorPrincipalId();
    }

    @ApiModelProperty(value = "Routed by principal id")
    public String getRoutedByPrincipalId() {
        return document.getRoutedByPrincipalId() ;
    }

    @ApiModelProperty(value = "Document type name")
    public String getDocumentTypeName() {
        return document.getDocumentTypeName();
    }

    @ApiModelProperty(value = "Document type id")
    public String getDocumentTypeId() {
        return document.getDocumentTypeId();
    }

    @ApiModelProperty(value = "Document handler url")
    public String getDocumentHandlerUrl() {
        return document.getDocumentHandlerUrl();
    }

    @ApiModelProperty(value = "Application document status date")
    public String getApplicationDocumentStatus() {
        return document.getApplicationDocumentStatus();
    }

    @ApiModelProperty(value = "Application status date")
    public DateTime getApplicationDocumentStatusDate() {
        return document.getApplicationDocumentStatusDate() ;
    }

    @ApiModelProperty(value = "Additional document variables")
    public Map<String, String> getVariables() {
        return document.getVariables();
    }
}
