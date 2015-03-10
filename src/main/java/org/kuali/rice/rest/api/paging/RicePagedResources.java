package org.kuali.rice.rest.api.paging;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import org.kuali.rice.rest.api.document.DocumentSearchRestController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import java.util.*;

/**
 * RicePagedResources provides the links to navigate to additional pages and the information about he page of items
 * it returns.  These links are constructed based on the page attributes passed in.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@ApiModel(value = "Paged resources representing a page of items, with HAL nav links")
public class RicePagedResources<T> extends Resources<T> {

    @ApiModelProperty(value = "Metadata about the page returned")
    private PageMetadata pageMetadata;

    public RicePagedResources(int startIndex, int limit, Collection<T> items, Map<String, String> queryParams) {
        this(startIndex, limit, items, queryParams, 0);
    }

    public RicePagedResources(int startIndex, int limit, Collection<T> items, Map<String, String> queryParams, int itemsOmitted){
        super(items, new ArrayList<Link>());
        queryParams.put("limit", Integer.toString(limit));

        // Self ref
        queryParams.put("startIndex", Integer.toString(startIndex));
        super.add(buildPageLink(queryParams, Link.REL_SELF));

        // Next ref
        if (items.size() + itemsOmitted == limit) {
            int nextIndex = startIndex + limit;

            queryParams.put("startIndex", Integer.toString(nextIndex));
            super.add(buildPageLink(queryParams, Link.REL_NEXT));
        }

        // Previous ref
        if (startIndex != 0) {
            int prevIndex = startIndex - limit;
            if (prevIndex < 0) {
                prevIndex = 0;
            }

            queryParams.put("startIndex", Integer.toString(prevIndex));
            super.add(buildPageLink(queryParams, Link.REL_PREVIOUS));
        }

        // First ref
        queryParams.put("startIndex", "0");
        super.add(buildPageLink(queryParams, Link.REL_FIRST));


        this.pageMetadata = new PageMetadata(startIndex, limit, items.size());

    }


    private Link buildPageLink(Map<String, String> queryParams, String relation) {
        UriComponentsBuilder builder = createBuilder();

        for (String key: queryParams.keySet()) {
            builder = builder.queryParam(key, queryParams.get(key));
        }

        String uri = builder.build().toUriString();
        Link link = new Link(uri, relation);

        return link;
    }

    private ServletUriComponentsBuilder createBuilder() {
        return ServletUriComponentsBuilder.fromCurrentRequestUri();
      }

    public void setItemsOmitted(int itemsOmitted) {
        this.pageMetadata.setItemsOmitted(itemsOmitted);
    }

    public PageMetadata getPageMetadata() {
        return pageMetadata;
    }

    public void setPageMetadata(PageMetadata pageMetadata) {
        this.pageMetadata = pageMetadata;
    }

    public static class PageMetadata {

        private int startIndex;
        private int limit;
        private int itemsReturned;
        private int itemsOmitted;


        public PageMetadata(int startIndex, int limit, int itemsReturned) {
            this.limit = limit;
            this.startIndex = startIndex;
            this.itemsReturned = itemsReturned;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public void setStartIndex(int startIndex) {
            this.startIndex = startIndex;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public int getItemsReturned() {
            return itemsReturned;
        }

        public void setItemsReturned(int itemsReturned) {
            this.itemsReturned = itemsReturned;
        }

        public int getItemsOmitted() {
            return itemsOmitted;
        }

        public void setItemsOmitted(int itemsOmitted) {
            this.itemsOmitted = itemsOmitted;
        }
    }
}
