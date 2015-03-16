package org.kuali.rice.rest.api.document;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.uif.RemotableAttributeField;
import org.kuali.rice.coreservice.framework.CoreFrameworkServiceLocator;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.document.search.DocumentSearchCriteria;
import org.kuali.rice.kew.api.document.search.DocumentSearchResults;
import org.kuali.rice.kew.docsearch.dao.impl.DocumentSearchDAOJdbcImpl;
import org.kuali.rice.kew.impl.document.search.DocumentSearchGenerator;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.util.PerformanceLogger;
import org.kuali.rice.krad.util.KRADConstants;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Created by Brian on 3/11/15.
 */
@Service
public class DocumentEnhancedSearchDAO extends DocumentSearchDAOJdbcImpl {
    public static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DocumentEnhancedSearchDAO.class);

    public DataSource getDataSource() {
        return new TransactionAwareDataSourceProxy(KEWServiceLocator.getDataSource());
    }

    @Override
    public DocumentSearchResults.Builder findDocuments(final DocumentSearchGenerator documentSearchGenerator, final DocumentSearchCriteria criteria, final boolean criteriaModified, final List<RemotableAttributeField> searchFields) {
        final int maxResultCap = getMaxResultCap(criteria);
        try {
            final JdbcTemplate template = new JdbcTemplate(getDataSource());

            return template.execute(new ConnectionCallback<DocumentSearchResults.Builder>() {

                public DocumentSearchResults.Builder doInConnection(final Connection con) throws SQLException {
                    final Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    try {
                        final int fetchIterationLimit = getFetchMoreIterationLimit();
                        final int fetchLimit = fetchIterationLimit * maxResultCap;
                        statement.setFetchSize(maxResultCap);
                        statement.setMaxRows(maxResultCap);

                        PerformanceLogger perfLog = new PerformanceLogger();
                        String sql = documentSearchGenerator.generateSearchSql(criteria, searchFields);
                        perfLog.log("Time to generate search sql from documentSearchGenerator class: " + documentSearchGenerator
                                .getClass().getName(), true);
                        LOG.info("Executing document search with statement max rows: " + statement.getMaxRows());
                        LOG.info("Executing document search with statement fetch size: " + statement.getFetchSize());
                        perfLog = new PerformanceLogger();
                        final ResultSet rs = statement.executeQuery(sql);
                        try {
                            perfLog.log("Time to execute doc search database query.", true);
                            final Statement searchAttributeStatement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                            try {
                                return documentSearchGenerator.processResultSet(criteria, criteriaModified, searchAttributeStatement, rs, maxResultCap, fetchLimit);
                            } finally {
                                try {
                                    searchAttributeStatement.close();
                                } catch (SQLException e) {
                                    LOG.warn("Could not close search attribute statement.");
                                }
                            }
                        } finally {
                            try {
                                rs.close();
                            } catch (SQLException e) {
                                LOG.warn("Could not close result set.");
                            }
                        }
                    } finally {
                        try {
                            statement.close();
                        } catch (SQLException e) {
                            LOG.warn("Could not close statement.");
                        }
                    }
                }
            });

        } catch (DataAccessException dae) {
            String errorMsg = "DataAccessException: " + dae.getMessage();
            LOG.error("getList() " + errorMsg, dae);
            throw new RuntimeException(errorMsg, dae);
        } catch (Exception e) {
            String errorMsg = "LookupException: " + e.getMessage();
            LOG.error("getList() " + errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

}
