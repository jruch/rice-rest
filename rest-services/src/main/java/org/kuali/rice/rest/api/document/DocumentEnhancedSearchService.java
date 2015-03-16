package org.kuali.rice.rest.api.document;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.MutableDateTime;
import org.kuali.rice.core.api.uif.RemotableAttributeField;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.document.search.DocumentSearchCriteria;
import org.kuali.rice.kew.api.document.search.DocumentSearchResult;
import org.kuali.rice.kew.api.document.search.DocumentSearchResults;
import org.kuali.rice.kew.docsearch.DocumentSearchCustomizationMediator;
import org.kuali.rice.kew.docsearch.service.DocumentSearchService;
import org.kuali.rice.kew.docsearch.service.impl.DocumentSearchServiceImpl;
import org.kuali.rice.kew.doctype.SecuritySession;
import org.kuali.rice.kew.doctype.bo.DocumentType;
import org.kuali.rice.kew.framework.document.search.DocumentSearchResultValue;
import org.kuali.rice.kew.framework.document.search.DocumentSearchResultValues;
import org.kuali.rice.kew.impl.document.search.DocumentSearchGenerator;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by Brian on 3/11/15.
 */
@Service
public class DocumentEnhancedSearchService extends DocumentSearchServiceImpl {


    @Autowired
    DocumentEnhancedSearchDAO documentEnhancedSearchDAO;

    public DocumentSearchResults lookupDocuments(String principalId, DocumentSearchCriteria criteria) {
        DocumentSearchGenerator docSearchGenerator = new DocumentEnhancedSearchGenerator();
        DocumentType documentType = KEWServiceLocator.getDocumentTypeService().findByNameCaseInsensitive(criteria.getDocumentTypeName());
        DocumentSearchCriteria.Builder criteriaBuilder = DocumentSearchCriteria.Builder.create(criteria);

        DocumentSearchCriteria builtCriteria = criteria;

        // copy over applicationDocumentStatuses if they came back empty -- version compatibility hack!
        // we could have called into an older client that didn't have the field and it got wiped, but we
        // still want doc search to work as advertised.
        if (!CollectionUtils.isEmpty(criteria.getApplicationDocumentStatuses())
                && CollectionUtils.isEmpty(builtCriteria.getApplicationDocumentStatuses())) {
            DocumentSearchCriteria.Builder patchedCriteria = DocumentSearchCriteria.Builder.create(builtCriteria);
            patchedCriteria.setApplicationDocumentStatuses(criteriaBuilder.getApplicationDocumentStatuses());
            builtCriteria = patchedCriteria.build();
        }

        builtCriteria = applyCriteriaDefaults(builtCriteria);
        boolean criteriaModified = !criteria.equals(builtCriteria);
        List<RemotableAttributeField> searchFields = determineSearchFields(documentType);
        DocumentSearchResults.Builder searchResults = documentEnhancedSearchDAO.findDocuments(docSearchGenerator, builtCriteria, criteriaModified, searchFields);
        if (documentType != null) {
            // Pass in the principalId as part of searchCriteria to result customizers
            //TODO: The right way  to do this should have been to update the API for document customizer

            DocumentSearchCriteria.Builder docSearchUserIdCriteriaBuilder = DocumentSearchCriteria.Builder.create(builtCriteria);
            docSearchUserIdCriteriaBuilder.setDocSearchUserId(principalId);
            DocumentSearchCriteria docSearchUserIdCriteria = docSearchUserIdCriteriaBuilder.build();

            DocumentSearchResultValues resultValues = getDocumentSearchCustomizationMediator().customizeResults(documentType, docSearchUserIdCriteria, searchResults.build());
            if (resultValues != null && CollectionUtils.isNotEmpty(resultValues.getResultValues())) {
                Map<String, DocumentSearchResultValue> resultValueMap = new HashMap<String, DocumentSearchResultValue>();
                for (DocumentSearchResultValue resultValue : resultValues.getResultValues()) {
                    resultValueMap.put(resultValue.getDocumentId(), resultValue);
                }
                for (DocumentSearchResult.Builder result : searchResults.getSearchResults()) {
                    DocumentSearchResultValue value = resultValueMap.get(result.getDocument().getDocumentId());
                    if (value != null) {
                        applyResultCustomization(result, value);
                    }
                }
            }
        }

        if (StringUtils.isNotBlank(principalId) && !searchResults.getSearchResults().isEmpty()) {
            DocumentSearchResults builtResults = searchResults.build();
            Set<String> authorizedDocumentIds = KEWServiceLocator.getDocumentSecurityService().documentSearchResultAuthorized(
                    principalId, builtResults, new SecuritySession(principalId));
            if (CollectionUtils.isNotEmpty(authorizedDocumentIds)) {
                int numFiltered = 0;
                List<DocumentSearchResult.Builder> finalResults = new ArrayList<DocumentSearchResult.Builder>();
                for (DocumentSearchResult.Builder result : searchResults.getSearchResults()) {
                    if (authorizedDocumentIds.contains(result.getDocument().getDocumentId())) {
                        finalResults.add(result);
                    } else {
                        numFiltered++;
                    }
                }
                searchResults.setSearchResults(finalResults);
                searchResults.setNumberOfSecurityFilteredResults(numFiltered);
            } else {
                searchResults.setNumberOfSecurityFilteredResults(searchResults.getSearchResults().size());
                searchResults.setSearchResults(Collections.<DocumentSearchResult.Builder>emptyList());
            }
        }

        return searchResults.build();
    }


    protected DocumentSearchCriteria applyCriteriaDefaults(DocumentSearchCriteria criteria) {
        DocumentSearchCriteria.Builder comparisonCriteria = createEmptyComparisonCriteria(criteria);
        boolean isCriteriaEmpty = criteria.equals(comparisonCriteria.build());
        boolean isTitleOnly = false;
        boolean isDocTypeOnly = false;
        if (!isCriteriaEmpty) {
            comparisonCriteria.setTitle(criteria.getTitle());
            isTitleOnly = criteria.equals(comparisonCriteria.build());
        }

        if (!isCriteriaEmpty && !isTitleOnly) {
            comparisonCriteria = createEmptyComparisonCriteria(criteria);
            comparisonCriteria.setDocumentTypeName(criteria.getDocumentTypeName());
            isDocTypeOnly = criteria.equals(comparisonCriteria.build());
        }

        if (isCriteriaEmpty || isTitleOnly || isDocTypeOnly) {
            DocumentSearchCriteria.Builder criteriaBuilder = DocumentSearchCriteria.Builder.create(criteria);
            Integer defaultCreateDateDaysAgoValue = null;
            if (isCriteriaEmpty || isDocTypeOnly) {
                // if they haven't set any criteria, default the from created date to today minus days from constant variable
                defaultCreateDateDaysAgoValue = KewApiConstants.DOCUMENT_SEARCH_NO_CRITERIA_CREATE_DATE_DAYS_AGO;
            } else if (isTitleOnly) {
                // If the document title is the only field which was entered, we want to set the "from" date to be X
                // days ago.  This will allow for a more efficient query.
                defaultCreateDateDaysAgoValue = KewApiConstants.DOCUMENT_SEARCH_DOC_TITLE_CREATE_DATE_DAYS_AGO;
            }

            if (defaultCreateDateDaysAgoValue != null) {
                // add a default create date
                MutableDateTime mutableDateTime = new MutableDateTime();
                mutableDateTime.addDays(defaultCreateDateDaysAgoValue.intValue());
                criteriaBuilder.setDateCreatedFrom(mutableDateTime.toDateTime());
            }
            criteria = criteriaBuilder.build();
        }
        return criteria;
    }

    protected DocumentSearchCustomizationMediator getDocumentSearchCustomizationMediator() {
        return KEWServiceLocator.getDocumentSearchCustomizationMediator();
    }

    public DocumentEnhancedSearchDAO getDocumentEnhancedSearchDAO() {
        return documentEnhancedSearchDAO;
    }

    public void setDocumentEnhancedSearchDAO(DocumentEnhancedSearchDAO documentEnhancedSearchDAO) {
        this.documentEnhancedSearchDAO = documentEnhancedSearchDAO;
    }
}
