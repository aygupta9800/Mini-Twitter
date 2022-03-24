package edu.sjsu.cmpe275.aop.tweet;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.UUID;

public class TweetServiceImpl implements TweetService {

    /***
     * Following is a dummy implementation, which the correctness of your submission cannot depend on. 
     * You can tweak the implementation to suit your need, but this file is NOT part of the submission.
     */
	
	@Override
    public UUID tweet(String user, String message) throws IllegalArgumentException, IOException {
    	System.out.printf("User %s tweeted message: %s\n", user, message);
    	return UUID.randomUUID();
    }

	@Override
	public UUID reply(String user, UUID originalMessage, String message) throws IOException, IOException {
    	System.out.printf("User %s tweeted replied to message %s with message: %s\n", user, originalMessage, message);
//		return null;
		return UUID.randomUUID();
	}

	@Override
    public void follow(String follower, String followee) throws IOException {
       	System.out.printf("User %s followed user %s \n", follower, followee);
    }

	@Override
	public void block(String user, String follower) throws IOException {
       	System.out.printf("User %s blocked user %s \n", user, follower);		
	}

	@Override
	public void like(String user, UUID messageId) throws AccessControlException, IllegalArgumentException, IOException {
		System.out.printf("User %s liked message with ID %s \n", user, messageId);				
	}
}
