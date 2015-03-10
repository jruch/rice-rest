package org.kuali.rice.rest.api.document;

import org.kuali.rice.kew.api.document.Document;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

/**
 * Assembler for created a DocumentResource from a Document.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class DocumentResourceAssembler extends ResourceAssemblerSupport<Document, DocumentResource> {

    public DocumentResourceAssembler() {
        super(DocumentSearchRestController.class, DocumentResource.class);
    }

    /**
     * Creates the HAL links for the document resource and creates the resource.
     *
     * @param document the original document
     * @return the created resource with links
     */
    public DocumentResource toResource(Document document) {
        // adds a link with rel self pointing to itself
        DocumentResource resource = createResourceWithId(document.getDocumentId(), document);
        return resource;
    }

    @Override
    protected DocumentResource instantiateResource(Document document) {
        return new DocumentResource(document);
    }
}
