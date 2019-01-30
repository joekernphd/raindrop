package com.example.raymond.raindrop.persistence.posts;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.AttributeTransformer;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Raymond on 11/5/2017.
 */

@DynamoDBTable(tableName = "posts")
public class PostStorable {
    String postId;
    String email;
    int karma;
    int report;
    double latitude;
    double longitude;
    long expires;
    String media;
    Set<String> upVoters;
    Set<String> downVoters;


    public PostStorable() {

    }

    public PostStorable(String postId) {
        this.postId = postId;
    }

    public PostStorable(String postId, String email, int karma, int report, double latitude, double longitude, long expires, String media) {
        this.postId = postId;
        this.email = email;
        this.karma = karma;
        this.report = report;
        this.latitude = latitude;
        this.longitude = longitude;
        this.expires = expires;
        this.media = media;
        this.upVoters = new HashSet<String>();
        this.upVoters.add("joe");
        this.downVoters = new HashSet<String>();
        this.downVoters.add("joe");
    }

    public PostStorable(String postId, String email, int karma, int report, double latitude, double longitude, long expires, String media, Set<String> upVoters, Set<String> downVoters) {
        this.postId = postId;
        this.email = email;
        this.karma = karma;
        this.report = report;
        this.latitude = latitude;
        this.longitude = longitude;
        this.expires = expires;
        this.media = media;
        this.upVoters = upVoters;
        this.downVoters = downVoters;
    }

    @DynamoDBHashKey(attributeName = "postId")
    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    @DynamoDBAttribute(attributeName = "email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @DynamoDBAttribute(attributeName = "karma")
    public int getKarma() {
        return karma;
    }

    public void setKarma(int karma) {
        this.karma = karma;
    }

    @DynamoDBAttribute(attributeName = "report")
    public int getReport() {
        return report;
    }

    public void setReport(int report) {
        this.report = report;
    }

    @DynamoDBAttribute(attributeName = "latitude")
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @DynamoDBAttribute(attributeName = "longitude")
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @DynamoDBAttribute(attributeName = "expires")
    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    @DynamoDBAttribute(attributeName = "media")
    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    @DynamoDBAttribute(attributeName = "upVoters")
    public Set<String> getUpVoters() { return upVoters; }

    public void setUpVoters(Set<String> upVoters) { this.upVoters = upVoters; }

    @DynamoDBAttribute(attributeName = "downVoters")
    public Set<String> getDownVoters() { return downVoters; }

    public void setDownVoters(Set<String> downVoters) { this.downVoters = downVoters; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PostStorable that = (PostStorable) o;

        if (karma != that.karma) return false;
        if (report != that.report) return false;
        if (Double.compare(that.latitude, latitude) != 0) return false;
        if (Double.compare(that.longitude, longitude) != 0) return false;
        if (expires != that.expires) return false;
        if (!postId.equals(that.postId)) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;
        if (media != null ? !media.equals(that.media) : that.media != null) return false;
        if (upVoters != null ? !upVoters.equals(that.upVoters) : that.upVoters != null)
            return false;
        return downVoters != null ? downVoters.equals(that.downVoters) : that.downVoters == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = postId.hashCode();
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + karma;
        result = 31 * result + report;
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (expires ^ (expires >>> 32));
        result = 31 * result + (media != null ? media.hashCode() : 0);
        result = 31 * result + (upVoters != null ? upVoters.hashCode() : 0);
        result = 31 * result + (downVoters != null ? downVoters.hashCode() : 0);
        return result;
    }

    public Map<String, AttributeValue> generateMapItem() {
        Map<String, AttributeValue> map = new HashMap<String, AttributeValue>();

        map.put("postId", new AttributeValue(postId));
        map.put("email", new AttributeValue(email));
        map.put("karma", new AttributeValue().withN(Integer.toString(karma)));
        map.put("report", new AttributeValue().withN(Integer.toString(report)));
        map.put("latitude", new AttributeValue().withN(Double.toString(latitude)));
        map.put("longitude", new AttributeValue().withN(Double.toString(longitude)));
        map.put("expires", new AttributeValue().withN(Long.toString(expires)));
        map.put("media", new AttributeValue(media));

        return map;
    }

    @Override
    public String toString() {
        return "PostStorable{" +
                "postId='" + postId + '\'' +
                ", email='" + email + '\'' +
                ", karma=" + karma +
                ", report=" + report +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", expires=" + expires +
                ", media='" + media + '\'' +
                '}';
    }

}
