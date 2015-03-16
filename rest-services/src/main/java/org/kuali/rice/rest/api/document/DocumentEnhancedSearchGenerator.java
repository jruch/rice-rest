package org.kuali.rice.rest.api.document;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.uif.RemotableAttributeField;
import org.kuali.rice.kew.api.document.attribute.DocumentAttribute;
import org.kuali.rice.kew.api.document.search.DocumentSearchCriteria;
import org.kuali.rice.kew.api.document.search.DocumentSearchResult;
import org.kuali.rice.kew.api.document.search.DocumentSearchResults;
import org.kuali.rice.kew.docsearch.QueryComponent;
import org.kuali.rice.kew.impl.document.search.DocumentSearchGeneratorImpl;
import org.kuali.rice.kew.util.PerformanceLogger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Brian on 3/11/15.
 */
public class DocumentEnhancedSearchGenerator extends DocumentSearchGeneratorImpl {
    public static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DocumentEnhancedSearchGenerator.class);

    @SuppressWarnings("deprecation")
    @Override
    public String generateSearchSql(DocumentSearchCriteria criteria, List<RemotableAttributeField> searchFields) {

        String docTypeTableAlias = "DOC1";
        String docHeaderTableAlias = "DOC_HDR";

        String sqlPrefix = "Select * from (";
        String sqlSuffix = ") FINAL_SEARCH order by FINAL_SEARCH.CRTE_DT desc";

        if (criteria.getStartAtIndex() != null && criteria.getMaxResults() != null) {
            if (this.getDbPlatform().toString().contains("MySQL")) {
                // Limit query for MySQL
                sqlSuffix = ") FINAL_SEARCH order by FINAL_SEARCH.CRTE_DT desc LIMIT " + criteria.getStartAtIndex() + ", " + criteria.getMaxResults();
            } else if (this.getDbPlatform().toString().contains("Oracle")) {
                // row num query for Oracle
                sqlPrefix = "Select * from (select SUBQ.*, ROWNUM rn from (" + sqlPrefix;
                sqlSuffix = sqlSuffix + ") SUBQ where rownum < " + criteria.getStartAtIndex() + criteria.getMaxResults() + ") where rn >= " + criteria.getStartAtIndex();
            }
        }

        // the DISTINCT here is important as it filters out duplicate rows which could occur as the result of doc search extension values...
        StringBuilder selectSQL = new StringBuilder("select DISTINCT(" + docHeaderTableAlias + ".DOC_HDR_ID), "
                + StringUtils.join(new String[]{
                docHeaderTableAlias + ".INITR_PRNCPL_ID",
                docHeaderTableAlias + ".DOC_HDR_STAT_CD",
                docHeaderTableAlias + ".CRTE_DT",
                docHeaderTableAlias + ".TTL",
                docHeaderTableAlias + ".APP_DOC_STAT",
                docHeaderTableAlias + ".STAT_MDFN_DT",
                docHeaderTableAlias + ".APRV_DT",
                docHeaderTableAlias + ".FNL_DT",
                docHeaderTableAlias + ".APP_DOC_ID",
                docHeaderTableAlias + ".RTE_PRNCPL_ID",
                docHeaderTableAlias + ".APP_DOC_STAT_MDFN_DT",
                docTypeTableAlias + ".DOC_TYP_NM",
                docTypeTableAlias + ".LBL",
                docTypeTableAlias + ".DOC_HDLR_URL",
                docTypeTableAlias + ".ACTV_IND"
        }, ", "));
        StringBuilder fromSQL = new StringBuilder(" from KREW_DOC_TYP_T " + docTypeTableAlias + " ");
        StringBuilder fromSQLForDocHeaderTable = new StringBuilder(", KREW_DOC_HDR_T " + docHeaderTableAlias + " ");

        StringBuilder whereSQL = new StringBuilder();
        whereSQL.append(getDocumentIdSql(criteria.getDocumentId(), getGeneratedPredicatePrefix(whereSQL.length()), docHeaderTableAlias));
        // if principalId criteria exists ignore deprecated principalName search term
        String principalInitiatorIdSql = getInitiatorIdSql(criteria.getInitiatorPrincipalId(), getGeneratedPredicatePrefix(whereSQL.length()));
        if (StringUtils.isNotBlank(principalInitiatorIdSql)) {
            whereSQL.append(principalInitiatorIdSql);
        } else {
            whereSQL.append(getInitiatorSql(criteria.getInitiatorPrincipalName(), getGeneratedPredicatePrefix(whereSQL.length())));
        }
        whereSQL.append(getAppDocIdSql(criteria.getApplicationDocumentId(), getGeneratedPredicatePrefix(whereSQL.length())));
        whereSQL.append(getDateCreatedSql(criteria.getDateCreatedFrom(), criteria.getDateCreatedTo(), getGeneratedPredicatePrefix(whereSQL.length())));
        whereSQL.append(getDateLastModifiedSql(criteria.getDateLastModifiedFrom(), criteria.getDateLastModifiedTo(), getGeneratedPredicatePrefix(whereSQL.length())));
        whereSQL.append(getDateApprovedSql(criteria.getDateApprovedFrom(), criteria.getDateApprovedTo(), getGeneratedPredicatePrefix(whereSQL.length())));
        whereSQL.append(getDateFinalizedSql(criteria.getDateFinalizedFrom(), criteria.getDateFinalizedTo(), getGeneratedPredicatePrefix(whereSQL.length())));

        // flags for the table being added to the FROM class of the sql
        String principalViewerSql = getViewerSql(criteria.getViewerPrincipalName(), getGeneratedPredicatePrefix(whereSQL.length()));
        String principalViewerIdSql = getViewerIdSql(criteria.getViewerPrincipalId(), getGeneratedPredicatePrefix(whereSQL.length()));
        // if principalId criteria exists ignore deprecated principalName search term
        if (StringUtils.isNotBlank(principalViewerIdSql)) {
            principalViewerSql = "";
        }
        String groupViewerSql = getGroupViewerSql(criteria.getGroupViewerId(), getGeneratedPredicatePrefix(whereSQL.length()));
        if (StringUtils.isNotBlank(principalViewerSql) || StringUtils.isNotBlank(groupViewerSql) || StringUtils.isNotBlank(principalViewerIdSql)) {
            whereSQL.append(principalViewerSql);
            whereSQL.append(principalViewerIdSql);
            whereSQL.append(groupViewerSql);
            fromSQL.append(", KREW_ACTN_RQST_T ");
        }

        String principalApproverSql = getApproverSql(criteria.getApproverPrincipalName(), getGeneratedPredicatePrefix(whereSQL.length()));
        String principalApproverIdSql = getApproverIdSql(criteria.getApproverPrincipalId(), getGeneratedPredicatePrefix(whereSQL.length()));
        // if principalId criteria exists ignore deprecated principalName search term
        if (StringUtils.isNotBlank(principalApproverIdSql)) {
            principalApproverSql = "";
        }
        if (StringUtils.isNotBlank(principalApproverSql) || StringUtils.isNotBlank(principalApproverIdSql)) {
            whereSQL.append(principalApproverSql);
            whereSQL.append(principalApproverIdSql);
            fromSQL.append(", KREW_ACTN_TKN_T ");
        }


        String docRouteNodeSql = getDocRouteNodeSql(criteria.getDocumentTypeName(), criteria.getRouteNodeName(), criteria.getRouteNodeLookupLogic(), getGeneratedPredicatePrefix(whereSQL.length()));
        if (StringUtils.isNotBlank(docRouteNodeSql)) {
            whereSQL.append(docRouteNodeSql);
            fromSQL.append(", KREW_RTE_NODE_INSTN_T ");
            fromSQL.append(", KREW_RTE_NODE_T ");
        }

        if (!criteria.getDocumentAttributeValues().isEmpty()) {
            QueryComponent queryComponent = getSearchableAttributeSql(criteria.getDocumentAttributeValues(), searchFields, getGeneratedPredicatePrefix(
                    whereSQL.length()));
            selectSQL.append(queryComponent.getSelectSql());
            fromSQL.append(queryComponent.getFromSql());
            whereSQL.append(queryComponent.getWhereSql());
        }

        whereSQL.append(getDocTypeFullNameWhereSql(criteria, getGeneratedPredicatePrefix(whereSQL.length())));
        whereSQL.append(getDocTitleSql(criteria.getTitle(), getGeneratedPredicatePrefix(whereSQL.length())));
        whereSQL.append(getDocumentStatusSql(criteria.getDocumentStatuses(), criteria.getDocumentStatusCategories(), getGeneratedPredicatePrefix(whereSQL.length())));
        whereSQL.append(getGeneratedPredicatePrefix(whereSQL.length())).append(" DOC_HDR.DOC_TYP_ID = DOC1.DOC_TYP_ID ");
        fromSQL.append(fromSQLForDocHeaderTable);

        // App Doc Status Value and Transition clauses
        String statusTransitionWhereClause = getStatusTransitionDateSql(criteria.getDateApplicationDocumentStatusChangedFrom(), criteria.getDateApplicationDocumentStatusChangedTo(), getGeneratedPredicatePrefix(whereSQL.length()));

        List<String> applicationDocumentStatuses = criteria.getApplicationDocumentStatuses();
        // deal with legacy usage of applicationDocumentStatus (which is deprecated)
        if (!StringUtils.isBlank(criteria.getApplicationDocumentStatus())) {
            if (!criteria.getApplicationDocumentStatuses().contains(criteria.getApplicationDocumentStatus())) {
                applicationDocumentStatuses = new ArrayList<String>(criteria.getApplicationDocumentStatuses());
                applicationDocumentStatuses.add(criteria.getApplicationDocumentStatus());
            }
        }

        whereSQL.append(getAppDocStatusesSql(applicationDocumentStatuses, getGeneratedPredicatePrefix(
                whereSQL.length()), statusTransitionWhereClause.length()));
        if (statusTransitionWhereClause.length() > 0) {
            whereSQL.append(statusTransitionWhereClause);
            whereSQL.append(getGeneratedPredicatePrefix(whereSQL.length())).append(" DOC_HDR.DOC_HDR_ID = STAT_TRAN.DOC_HDR_ID ");
            fromSQL.append(", KREW_APP_DOC_STAT_TRAN_T STAT_TRAN ");
        }

        String finalizedSql = sqlPrefix + " " + selectSQL.toString() + " " + fromSQL.toString() + " " + whereSQL.toString() + " " + sqlSuffix;

        LOG.info("*********** SEARCH SQL ***************");
        LOG.info(finalizedSql);
        LOG.info("**************************************");
        return finalizedSql;
    }

    @Override
    public DocumentSearchResults.Builder processResultSet(DocumentSearchCriteria criteria, boolean criteriaModified, Statement searchAttributeStatement, ResultSet resultSet, int maxResultCap, int fetchLimit) throws SQLException {
        DocumentSearchCriteria.Builder criteriaBuilder = DocumentSearchCriteria.Builder.create(criteria);
        DocumentSearchResults.Builder results = DocumentSearchResults.Builder.create(criteriaBuilder);
        results.setCriteriaModified(criteriaModified);

        List<DocumentSearchResult.Builder> resultList = new ArrayList<DocumentSearchResult.Builder>();
        results.setSearchResults(resultList);
        Map<String, DocumentSearchResult.Builder> resultMap = new HashMap<String, DocumentSearchResult.Builder>();

        int iteration = 0;
        boolean resultSetHasNext = resultSet.next();

        PerformanceLogger perfLog = new PerformanceLogger();

        while (resultSetHasNext && resultMap.size() < maxResultCap && iteration < fetchLimit) {
            DocumentSearchResult.Builder resultBuilder = processRow(criteria, searchAttributeStatement, resultSet);
            String documentId = resultBuilder.getDocument().getDocumentId();
            if (!resultMap.containsKey(documentId)) {
                resultList.add(resultBuilder);
                resultMap.put(documentId, resultBuilder);
            } else {
                // handle duplicate rows with different search data
                DocumentSearchResult.Builder previousEntry = resultMap.get(documentId);
                handleMultipleDocumentRows(previousEntry, resultBuilder);
            }

            iteration++;
            resultSetHasNext = resultSet.next();
        }

        perfLog.log("Time to read doc search results.", true);
        // if we have threshold+1 results, then we have more results than we are going to display
        results.setOverThreshold(resultSetHasNext);

        LOG.debug("Processed " + resultMap.size() + " document search result rows.");
        return results;
    }

    /**
     * Handles multiple document rows by collapsing them into the list of document attributes on the existing row.
     * The two rows must represent the same document.
     *
     * @param existingRow the existing row to combine the new row into
     * @param newRow      the new row from which to combine document attributes with the existing row
     */
    private void handleMultipleDocumentRows(DocumentSearchResult.Builder existingRow, DocumentSearchResult.Builder newRow) {
        for (DocumentAttribute.AbstractBuilder<?> newDocumentAttribute : newRow.getDocumentAttributes()) {
            existingRow.getDocumentAttributes().add(newDocumentAttribute);
        }
    }


}
