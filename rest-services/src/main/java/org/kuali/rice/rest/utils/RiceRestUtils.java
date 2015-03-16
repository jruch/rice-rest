package org.kuali.rice.rest.utils;

import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for Rice rest functionality.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class RiceRestUtils {

    /**
     * Translate a filter to a map
     * @param filterParam
     * @return
     */
    public static Map<String, String> translateFilterToMap(String filterParam) {
        Map<String, String> translation = new HashMap<String, String>();
        if (filterParam == null) {
            return translation;
        }

        String[] filters = filterParam.split("\\|");


        for (String filter : filters) {
            String[] filterValues = filter.split("::");
            if (filterValues.length != 2) {
                continue;
            }
            translation.put(filterValues[0], filterValues[1]);
        }

        return translation;
    }

}
