package org.kuali.rice.rest.api.kim;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.apache.commons.beanutils.BeanUtils;
import org.joda.time.DateTime;
import org.kuali.rice.core.api.membership.MemberType;
import org.kuali.rice.kim.api.group.GroupMember;
import org.kuali.rice.kim.api.group.GroupMemberContract;
import org.kuali.rice.rest.exception.OperationFailedException;

/**
 * Resource for GroupMember
 */
public class GroupMemberResource  implements  GroupMemberContract{

    private String id;

    private String groupId;

    private String memberId;

    private String typeCode;

    private DateTime activeFromDate;

    private DateTime activeToDate;

    private Long versionNumber;

    private String objectId;

    private boolean active;


    public String getTypeCode() {
        return typeCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getMemberId() {
        return memberId;
    }

    @Override
    public MemberType getType() {
        return MemberType.fromCode( typeCode );
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public DateTime getActiveFromDate() {
        return activeFromDate;
    }

    public void setActiveFromDate(DateTime activeFromDate) {
        this.activeFromDate = activeFromDate;
    }

    public DateTime getActiveToDate() {
        return activeToDate;
    }

    public void setActiveToDate(DateTime activeToDate) {
        this.activeToDate = activeToDate;
    }

    public Long getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Long versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    /**
     * Rest does not have a concept of POJO with business logic.
     * Added here just to implement GroupContract interface
     */
    public boolean isActive(DateTime dateTime) {
        throw new UnsupportedOperationException("Not supported in Rest");
    }

    public void setType(MemberType type) {
        typeCode = type.getCode();
    }

    public static GroupMemberResource fromGroupMember(GroupMember member) {
        GroupMemberResource gmr = new GroupMemberResource();
        try {
            BeanUtils.copyProperties(gmr, member);
            gmr.setActive( member.isActive() );
        } catch (Exception e) {
            throw new OperationFailedException(e.getMessage());
        }

        return gmr;
    }
}
