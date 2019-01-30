package com.example.raymond.raindrop.persistence.reports;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Joe on 11/13/2017.
 */

@DynamoDBTable(tableName = "reports")
public class ReportStorable {
    String reportId;
    String postId;
    String email;
    String description;


    public ReportStorable() {

    }

    public ReportStorable(String reportId, String postId, String email, String description) {
        this.reportId = reportId;
        this.postId = postId;
        this.email = email;
        this.description = description;
    }

    @DynamoDBAttribute(attributeName = "postId")
    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    @DynamoDBHashKey(attributeName = "reportId")
    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    @DynamoDBAttribute(attributeName = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @DynamoDBAttribute(attributeName = "email")
    public String getEmail() {

        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String, AttributeValue> generateMapItem() {
        Map<String, AttributeValue> map = new HashMap<String, AttributeValue>();

        map.put("reportId", new AttributeValue(reportId));
        map.put("postId", new AttributeValue(postId));
        map.put("email", new AttributeValue(email));
        map.put("description", new AttributeValue(description));

        return map;
    }

    @Override
    public String toString() {
        return "ReportStorable{" +
                "reportId='" + reportId + '\'' +
                ", postId='" + postId + '\'' +
                ", email='" + email + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReportStorable that = (ReportStorable) o;

        if (!reportId.equals(that.reportId)) return false;
        if (postId != null ? !postId.equals(that.postId) : that.postId != null) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;
        return description != null ? description.equals(that.description) : that.description == null;
    }

    @Override
    public int hashCode() {
        int result = reportId.hashCode();
        result = 31 * result + (postId != null ? postId.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

}
