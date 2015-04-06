package org.kuali.rice.rest.api.kim;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import org.joda.time.DateTime;
import org.kuali.rice.core.api.membership.MemberType;
import org.kuali.rice.kim.api.group.GroupMember;
import org.kuali.rice.kim.api.group.GroupMemberContract;
import org.kuali.rice.rest.exception.OperationFailedException;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.ResourceSupport;

/**
 * Resource for GroupMember
 */
@ApiModel(value = "KIM Group Member Resource")
public class GroupMemberResource extends ResourceSupport {

    @JsonProperty("id")
    private String recId;

    private String groupId;

    private String memberId;

    private String typeCode;

    private DateTime activeFromDate;

    private DateTime activeToDate;

    private Long versionNumber;

    private String objectId;

    private boolean active;

    @ApiModelProperty(value = "Type code", required = true)
    public String getTypeCode() {
        return typeCode;
    }

    @ApiModelProperty(value = "Member's parent group", required = true)
    public String getGroupId() {
        return groupId;
    }

    @ApiModelProperty(value = "Member's id same as principalId", required = true)
    public String getMemberId() {
        return memberId;
    }

    @ApiModelProperty(value = "Member type based on typeCode")
    public MemberType getType() {
        return MemberType.fromCode(typeCode);
    }

    @ApiModelProperty(value = "Active from date")
    public DateTime getActiveToDate() {
        return activeToDate;
    }

    @ApiModelProperty(value = "System property indicating database version")
    public Long getVersionNumber() {
        return versionNumber;
    }

    @ApiModelProperty(value = "System generated database Id")
    public String getObjectId() {
        return objectId;
    }

    @ApiModelProperty(value = "Indicates if the member is active")
    public boolean isActive() {
        return active;
    }

    @ApiModelProperty(value = "System Id of group member")
    public String getRecId() {
        return recId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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

    public void setActiveToDate(DateTime activeToDate) {
        this.activeToDate = activeToDate;
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

    public void setType(MemberType type) {
        typeCode = type.getCode();
    }

    public void setRecId(String recId) {
        this.recId = recId;
    }

    public static GroupMemberResource fromGroupMember(GroupMember member) {
        GroupMemberResource gmr = new GroupMemberResource();
        try {
            BeanUtils.copyProperties(member, gmr, new String[]{"id"});
            gmr.setActive(member.isActive());
            gmr.setRecId(member.getId());
        } catch (Exception e) {
            throw new OperationFailedException(e.getMessage());
        }

        return gmr;
    }

    public static GroupMember toGroupMember(GroupMemberResource memberResource) {
        GroupMember.Builder mbr = GroupMember.Builder.create(memberResource.getGroupId(), memberResource.getMemberId(), memberResource.getType());

        try {
            BeanUtils.copyProperties(memberResource, mbr, new String[]{"id", "recId"});
            mbr.setId(memberResource.getRecId());
        } catch (Exception e) {
            throw new OperationFailedException(e.getMessage());
        }

        return mbr.build();
    }
}
