package org.kuali.rice.rest.api.kim;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * Resource to return Ids along with href to access the referenced resource
 */
@ApiModel(value = "Resource for refernced Ids")
public class IdRefResource {
    String Id;
    String href;

    @ApiModelProperty(value="Referenced resource Id")
    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    @ApiModelProperty(value = "URI to GET referenced resource")
    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
