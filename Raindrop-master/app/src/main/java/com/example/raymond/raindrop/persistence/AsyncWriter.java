package com.example.raymond.raindrop.persistence;

import android.support.annotation.NonNull;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.example.raymond.raindrop.persistence.posts.PostRepository;
import com.example.raymond.raindrop.persistence.reports.ReportRepository;
import com.example.raymond.raindrop.persistence.users.UserRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Raymond on 11/15/2017.
 */

public final class AsyncWriter {
    private static final AmazonDynamoDBAsyncClient asyncClient = new AmazonDynamoDBAsyncClient(CredentialsForAWS.getAWSCredentials());
    private static final Set<String> legalTableNames = legalTableNames();

    private AsyncWriter() {
        throw new AssertionError("Dont instantiate this!");
    }

    public static void write(String tableName, Map<String, AttributeValue> storableMap) {
        if(legalTableNames.contains(tableName)) {
            asyncClient.putItemAsync(new PutItemRequest(tableName, storableMap));
        } else {
            throw new AssertionError("Not a legal table name!");
        }
    }

    private static Set<String> legalTableNames() {
        Set<String> legalTableNames = new HashSet<String>();
        legalTableNames.add(UserRepository.getTableName());
        legalTableNames.add(PostRepository.getTableName());
        legalTableNames.add(ReportRepository.getTableName());

        return legalTableNames;
    }
}
