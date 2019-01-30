package com.example.raymond.raindrop.persistence.posts;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.example.raymond.raindrop.logic.LatLongDistance;
import com.example.raymond.raindrop.persistence.CredentialsForAWS;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raymond on 11/5/2017.
 */

public class PostRepository {
    private static final String TABLE_NAME = "posts";

    private final AmazonDynamoDBClient ddbClient;
    private final DynamoDBMapper mapper;

    public PostRepository() {
        ddbClient = new AmazonDynamoDBClient(CredentialsForAWS.getAWSCredentials());
        mapper = new DynamoDBMapper(ddbClient);
    }

    public PostStorable read(PostStorable storable) {
        return mapper.load(PostStorable.class, storable.getPostId());
    }

    public PostStorable read(String postId) {
        return mapper.load(PostStorable.class, postId);
    }

    public void write(PostStorable storable) {
        mapper.save(storable);
    }

    public List<PostStorable> scanAllEntries() {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

        return mapper.scan(PostStorable.class, scanExpression);
    }

    public List<PostStorable> scanCloseEntries(double latitude, double longitude) {
        List<PostStorable> postStorables = scanAllEntries();
        List<PostStorable> nearbyPosts = new ArrayList<PostStorable>();
        for(PostStorable p : postStorables) {
            if(LatLongDistance.isClose(latitude, longitude, p.getLatitude(), p.getLongitude())) {
                nearbyPosts.add(p);
            }
        }

        return nearbyPosts;
    }

    public List<PostStorable> scanFarEntries(double latitude, double longitude) {
        List<PostStorable> postStorables = scanAllEntries();
        List<PostStorable> farPosts = new ArrayList<PostStorable>();
        for(PostStorable p : postStorables) {
            if(LatLongDistance.isCloseEnough(latitude, longitude, p.getLatitude(), p.getLongitude())) {
                farPosts.add(p);
            }
        }

        return farPosts;
    }

    public static String getTableName() {
        return TABLE_NAME;
    }
}
