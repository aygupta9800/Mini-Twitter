package edu.sjsu.cmpe275.aop.tweet.aspect;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import edu.sjsu.cmpe275.aop.tweet.TweetStatsServiceImpl;

@Aspect
@Order(3)
public class ValidationAspect {
    /***
     * Following is a dummy implementation of this aspect.
     * You are expected to provide an actual implementation based on the requirements, including adding/removing advices as needed.
     */
	@Autowired TweetStatsServiceImpl statsObj;
	
	
	@Pointcut("execution (public * edu.sjsu.cmpe275.aop.tweet.TweetService.tweet(..))")
	private void tweetApiPointCut() {};
	
	@Pointcut("execution (public * edu.sjsu.cmpe275.aop.tweet.TweetService.follow(..))")
	private void followApiPointCut() {}
	
	@Pointcut("execution (public * edu.sjsu.cmpe275.aop.tweet.TweetService.block(..))")
	private void blockApiPointCut() {}
	
	@Pointcut("execution (public * edu.sjsu.cmpe275.aop.tweet.TweetService.like(..))")
	private void likeApiPointCut() {}
	
	@Pointcut("execution(public int edu.sjsu.cmpe275.aop.tweet.TweetService.reply(..))")
	public void replyApiPointCut() {}

	@Before("execution(public int edu.sjsu.cmpe275.aop.tweet.TweetService.retweet(..))")
	public void dummyBeforeAdvice(JoinPoint joinPoint) {
		System.out.printf("Permission check before the executuion of the metohd %s\n", joinPoint.getSignature().getName());
	}
	
	
	// validation check before tweet functionality
	@Before("tweetApiPointCut()")
	public void validateTweet(JoinPoint joinPoint) {
		String tweetBy = (String) joinPoint.getArgs()[0];
		String msg = (String) joinPoint.getArgs()[1];
		if (tweetBy == null || tweetBy == "") {
			throw new IllegalArgumentException("User who tweets cannot have null value");
		}
		if (msg == null || msg == "") {
			throw new IllegalArgumentException("Message cannot be null");
		}
		if (msg.length()> 140) {
			throw new IllegalArgumentException("Length of the Tweet cannot be greater than 140 characters");
		}
	}
	
	@Before("replyApiPointCut()")
	public void validateReplyApi(JoinPoint jp) throws Throwable {
		// 
//		throw new IOException("==Reply==network==failure");
		Object [] args = jp.getArgs();
		String user = (String) args[0];
		UUID originalMessage = (UUID) args[1];
		String message = (String) args[2];
		
//		System.out.println("******Reply validate params*****"+ user+originalMessage+ message);
		
		if (user == null || originalMessage == null || message == null || user == "" || message == "") {
			throw new IllegalArgumentException("User or originalMessage or reply msg cant be null or empty");
		}
		
		if (message.length() > 140) {
			throw new IllegalArgumentException("Message Length must not be more than 140 characters");
		}
		String orgMsgUser = statsObj.getUserUsingMsgId(originalMessage);
		if(orgMsgUser.equals(user)) {
			throw new IllegalArgumentException("User cannot reply to itself");
		}
		if(statsObj.isTweetValid(originalMessage)) {
			throw new IllegalArgumentException("Original tweet doesnt exist");
		}
	}
		
	
	
	@Before("followApiPointCut()")
	public void validateFollowApi(JoinPoint joinPoint) throws Throwable {
		Object [] args = joinPoint.getArgs();
		String follower = (String) args[0];
		String followee = (String) args[1];
//		System.out.println("*********Before Follow User******"+ follower + followee);
		if (follower == null || followee == null || follower == "" || followee == "") {
			throw new IllegalArgumentException("Follower or Followee cant be null or empty");
		}
		if (follower.equals(followee)) {
			throw new IllegalArgumentException("User cannot follow himself");
		}
		
	}
	
	
	@Before("blockApiPointCut()")
	public void validateBlockApi(JoinPoint jp) throws Throwable {
		Object [] args = jp.getArgs();
		String user = (String) args[0];
		String follower = (String) args[1];
//		System.out.println("******Before Block User"+ user + follower);
		if (user == null || follower == null || user=="" || follower =="") {
			throw new IllegalArgumentException("User or Follower cant be null or empty");
		}
		if (user.equals(follower)) {
			throw new IllegalArgumentException("User cannot block himself");
		}
				
	}
	

	@Before("likeApiPointCut()")
	public void validateLikeApi(JoinPoint jp) throws Throwable {
		Object [] args = jp.getArgs();
		String user = (String) args[0];
		UUID msgId = (UUID) args[1];
		if (user == null || msgId == null || user == "") {
			throw new IllegalArgumentException("User or MessageId cant be null or empty");
		}
	}
	
		
//		TODO: check if original message is present
		
		
//      TODO: user cannot reply to its own tweet.

	
	
}
