package org.kuali.rice.rest.api.kim;

import org.apache.commons.beanutils.BeanUtils;
import org.kuali.rice.kim.api.group.Group;
import org.kuali.rice.kim.api.group.GroupContract;
import org.kuali.rice.rest.exception.OperationFailedException;
import org.springframework.hateoas.ResourceSupport;

import java.util.Map;

/**
 * Resource for Group
 */
public class GroupResource implements GroupContract {

    private String id;

    private String namespaceCode;

    private String name;

    private String description;

    private String kimTypeId;

    private Map<String, String> attributes;

    private Long versionNumber;

    private String objectId;

    private boolean active;

    @Override
    public String getNamespaceCode() {
        return namespaceCode;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getKimTypeId() {
        return kimTypeId;
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public String getObjectId() {
        return objectId;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public Long getVersionNumber() {
        return versionNumber;
    }

    public void setId(String id) {
        this.id = id;
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
            BeanUtils.copyProperties(grp, group);
        } catch (Exception e) {
            throw new OperationFailedException(e.getMessage());
        }

        return grp;
    }
}

