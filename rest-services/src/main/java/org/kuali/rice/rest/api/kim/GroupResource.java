package org.kuali.rice.rest.api.kim;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import org.kuali.rice.kim.api.group.Group;
import org.kuali.rice.kim.api.group.GroupContract;
import org.kuali.rice.rest.exception.OperationFailedException;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.ResourceSupport;

import java.util.Map;

/**
 * Resource for Group
 */
@ApiModel(value = "KIM Group Resource")
public class GroupResource extends ResourceSupport {

    private String groupId;

    private String namespaceCode;

    private String name;

    private String description;

    private String kimTypeId;

    private Map<String, String> attributes;

    private Long versionNumber;

    private String objectId;

    private boolean active;

    @ApiModelProperty(value = "Namespace code")
    public String getNamespaceCode() {
        return namespaceCode;
    }

    @ApiModelProperty(value = "Name of the croup")
    public String getName() {
        return name;
    }

    @ApiModelProperty(value = "Group description")
    public String getDescription() {
        return description;
    }

    @ApiModelProperty(value = "Group's Kim Type")
    public String getKimTypeId() {
        return kimTypeId;
    }

    @ApiModelProperty(value = "Additional group attributes")
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @ApiModelProperty(value = "group system database")
    public String getObjectId() {
        return objectId;
    }

    @ApiModelProperty(value = "Group Id", required=true)
    public String getGroupId() {
        return groupId;
    }

    @ApiModelProperty(value = "Indicates if group is active")
    public boolean isActive() {
        return active;
    }

    @ApiModelProperty(value = "System property indicating database version")
    public Long getVersionNumber() {
        return versionNumber;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setNamespaceCode(String namespaceCode) {
        this.namespaceCode = namespaceCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setKimTypeId(String kimTypeId) {
        this.kimTypeId = kimTypeId;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void setVersionNumber(Long versionNumber) {
        this.versionNumber = versionNumber;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public static GroupResource fromGroup(Group group) {
        GroupResource grp = new GroupResource();
        try {
            BeanUtils.copyProperties(group, grp, new String[]{"id"});
            grp.setGroupId(group.getId());
        } catch (Exception e) {
            throw new OperationFailedException(e.getMessage());
        }

        return grp;
    }

    public static Group toGroup(GroupResource groupResource) {
        Group.Builder grp = Group.Builder.create(groupResource.getNamespaceCode(), groupResource.getName(), groupResource.getKimTypeId());
        try {
            BeanUtils.copyProperties(groupResource, grp, new String[]{"id", "groupId"});
            grp.setId(groupResource.getGroupId());
        } catch (Exception e) {
            throw new OperationFailedException(e.getMessage());
        }

        return grp.build();
    }
}

