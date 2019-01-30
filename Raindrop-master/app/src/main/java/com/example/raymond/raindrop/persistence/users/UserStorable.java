package com.example.raymond.raindrop.persistence.users;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Raymond on 10/22/2017.
 * Used as the storable to send and receive data from the users database in DynamoDB
 */

@DynamoDBTable(tableName = "users")
public class UserStorable {
    private String email;
    private int karma;

    public UserStorable() {

    }

    public UserStorable(String email, int karma) {
        this.email = email;
        this.karma = karma;
    }

    @DynamoDBHashKey(attributeName = "email")
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @DynamoDBAttribute(attributeName = "karma")
    public int getKarma() { return karma; }
    public void setKarma(int karma) {
        this.karma = karma;
    }



    public Map<String, AttributeValue> generateMapItem() {
        Map<String, AttributeValue> map = new HashMap<String, AttributeValue>();

        map.put("email", new AttributeValue(email));
        map.put("karma", new AttributeValue().withN(Integer.toString(karma)));

        return map;
    }

    @Override
    public String toString() {
        return "UserStorable{" +
                "email='" + email + '\'' +
                ", karma=" + karma +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserStorable that = (UserStorable) o;

        if (karma != that.karma) return false;
        return email != null ? email.equals(that.email) : that.email == null;
    }

    @Override
    public int hashCode() {
        int result = email != null ? email.hashCode() : 0;
        result = 31 * result + karma;
        return result;
    }
}
