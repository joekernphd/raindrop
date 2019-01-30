package com.example.raymond.raindrop.persistence.reports;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.example.raymond.raindrop.persistence.CredentialsForAWS;
import com.example.raymond.raindrop.persistence.users.UserStorable;

/**
 * Created by Joe on 11/13/2017.
 */

public class ReportRepository {
    private static final String TABLE_NAME = "reports";
    private final AmazonDynamoDBClient ddbClient;
    private final DynamoDBMapper mapper;

    public ReportRepository() {
        ddbClient = new AmazonDynamoDBClient(CredentialsForAWS.getAWSCredentials());
        mapper = new DynamoDBMapper(ddbClient);
    }

    public ReportStorable read(ReportStorable storable) {
        return mapper.load(ReportStorable.class, storable.getEmail());
    }

    public ReportStorable read(String reportId) {
        return mapper.load(ReportStorable.class, reportId);
    }

    public void write(ReportStorable storable) {
        mapper.save(storable);
    }

    public static String getTableName() {
        return TABLE_NAME;
    }
}
