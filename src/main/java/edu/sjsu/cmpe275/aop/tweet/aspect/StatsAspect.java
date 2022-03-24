package edu.sjsu.cmpe275.aop.tweet.aspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import edu.sjsu.cmpe275.aop.tweet.TweetStatsService;
import edu.sjsu.cmpe275.aop.tweet.TweetStatsServiceImpl;

@Aspect
@Order(2)
public class StatsAspect {
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
	
	@Pointcut("execution(public * edu.sjsu.cmpe275.aop.tweet.TweetService.reply(..))")
	public void replyApiPointCut() {}
	
//	@Pointcut("execution (public * edu.sjsu.cmpe275.aop.tweet.TweetStatsService.getMostFollowedUser(..))")
//	private void mostFollowedUserPointCut() {};
	
	@After("execution(public * edu.sjsu.cmpe275.aop.tweet.TweetService.*(..))")
	public void dummyAfterAdvice(JoinPoint joinPoint) {
		System.out.printf("After the executuion of the metohd %s\n", joinPoint.getSignature().getName());
		//stats.resetStats();
	}
	
	@Before("execution(public void edu.sjsu.cmpe275.aop.tweet.TweetService.follow(..))")
	public void dummyBeforeAdvice(JoinPoint joinPoint) {
		System.out.printf("Before the executuion of the metohd %s\n", joinPoint.getSignature().getName());
	}
	
	// To save the result of the tweetApi in our Data structure after successful completion
	@AfterReturning(pointcut="tweetApiPointCut()", returning ="msgUuid")
	public void storeTweet(JoinPoint joinPoint, UUID msgUuid) {
		String tweetBy = (String) joinPoint.getArgs()[0];
		String msg = (String) joinPoint.getArgs()[1];
//		System.out.println("**** store tweet method ****"+ tweetBy+ msg);
		statsObj.addTweetToTweetList(tweetBy, msg, msgUuid);
		statsObj.updateLongestTweet(msg);
		statsObj.shareTweet(tweetBy, msgUuid);
		// Adding tweet to firstTweets as first message
		statsObj.addReplyToFirstMsg(msgUuid, null, "first");
//		System.out.println("=============STATS Tweet" + statsObj.tweetsArray);
	}
	
	@AfterReturning(pointcut="replyApiPointCut()", returning="replyMsgId")
	public void afterReply(JoinPoint joinPoint, UUID replyMsgId) throws Throwable  {
		if (replyMsgId== null) {
			replyMsgId = UUID.randomUUID();
		}
//		Take user , originalMessage, message
		String user = (String) joinPoint.getArgs()[0];
		UUID originalMessage = (UUID) joinPoint.getArgs()[1];
		String message = (String) joinPoint.getArgs()[2];
//		System.out.println("====Calling "+replyMsgId+ user + message);
		List <UUID> replyList;
//		TODO: Do we need to check empty replyMap case here
		if (statsObj.replyMap.containsKey(originalMessage)) {
			replyList = statsObj.replyMap.get(originalMessage);
		} else {
			replyList = new ArrayList<>();
		}
		replyList.add(replyMsgId);
		statsObj.replyMap.put(originalMessage, replyList);
		statsObj.updateLongestTweet(message);
		// Adding reply count of user to find most productive replier
		statsObj.replyCount.put(user, statsObj.replyCount.getOrDefault(user, 0) + 1);
//		TODO: check this method
		String orgMsgUser = statsObj.getUserUsingMsgId(originalMessage);
		// TODO: if replier is blocked by the orgUser then he shouldnt be allowed to reply
		// we need to share the reply with orgUser + followers of replier.
		// Only share with original user if its not blocked by replier
		if (!statsObj.isBlocked(orgMsgUser, user)) {
			HashSet<UUID> orgUserMsgSet = statsObj.sharedTweets.get(orgMsgUser);
			if(orgUserMsgSet == null) {
				orgUserMsgSet = new HashSet<>();
				orgUserMsgSet.add(replyMsgId);
//				System.out.println("===========ORGMSGUSER"+ orgMsgUser+ ":org msg set:"+orgUserMsgSet);
				statsObj.sharedTweets.put(orgMsgUser, orgUserMsgSet);
				statsObj.updateShareCount(replyMsgId);
//				System.out.println("============SHARED-TweetsAdding-1Reply"+ statsObj.sharedTweets);
			} else {
				orgUserMsgSet.add(replyMsgId);
//				System.out.println("============2Reply");
			}	
		}
//		System.out.println("============3Reply, user:"+ user+ "replyMsgId:"+ replyMsgId);
		statsObj.shareTweet(user, replyMsgId);
//		System.out.println("============4Reply");
		statsObj.addTweetToTweetList(user, message, replyMsgId);
		// TODO: ADD Reply to FirstTweets
		statsObj.addReplyToFirstMsg(originalMessage, replyMsgId, "reply");
		
//		System.out.println("==== After SUccessFul Reply"+ user+ originalMessage+ message+ replyMsgId );
//		System.out.println("=====ShareTweetListAfterReply"+ statsObj.sharedTweets);
//		System.out.println("======TweetArrayAfterReply" + statsObj.tweetsArray);
//		System.out.println("=====FIRST TWEETS====="+ statsObj.firstTweets);
//		System.out.println("==============================");
		
	}
	
	// Follow User Functionality
//	TODO: change it to after returning
	@AfterReturning("followApiPointCut()")
	public void followUser(JoinPoint joinPoint) throws Throwable{
		Object [] args = joinPoint.getArgs();
		String follower = (String) args[0];
		String followee = (String) args[1];
		HashSet<String> userFollowSet;
//		TODO: Block Logic
		if (statsObj.followArray.isEmpty()) {
			userFollowSet = new HashSet<>();
		} else {
			userFollowSet = statsObj.followArray.get(followee);
			if (userFollowSet == null) {
				userFollowSet = new HashSet<String>();
			}
		}
		userFollowSet.add(follower);
		statsObj.followArray.put(followee, userFollowSet);
//		System.out.println("*********afterfollowUserApi******"+ statsObj.followArray);
		
	}
	
	// Block User Functionality
	@AfterReturning("blockApiPointCut()")
	public void blockFollower(JoinPoint joinPoint) throws Throwable{
		String user = (String) joinPoint.getArgs()[0];
		String follower = (String) joinPoint.getArgs()[1];
		HashSet<String> userBlockSet;
		if (statsObj.blockArray.isEmpty()) {
			userBlockSet = new HashSet<>();
		} else {
			userBlockSet = statsObj.blockArray.get(follower);
			if (userBlockSet == null) {
				userBlockSet = new HashSet<String>();
			}
		}
		userBlockSet.add(user);
		statsObj.blockArray.put(follower, userBlockSet);
//		System.out.println("********block Array blocked: [those who blocked]*******"+ statsObj.blockArray);
	}
	
	@AfterReturning("likeApiPointCut()")
	public void likeMsg(JoinPoint jp) throws Throwable {
		Object [] args = jp.getArgs();
		String user = (String) args[0];
		UUID msgId = (UUID) args[1];
		String msgUser = statsObj.getUserUsingMsgId(msgId);
		HashSet<String> userLikedArray;
//		same user cant like its own message
		if (msgUser == user) {
			return ;
		}
		if (statsObj.likeArray.isEmpty()) {
			userLikedArray = new HashSet<>();
		} else {
			userLikedArray = statsObj.likeArray.get(msgId);
			if (userLikedArray == null) {
				userLikedArray = new HashSet<String>();
			}
		}
		
		userLikedArray.add(user);
		statsObj.likeArray.put(msgId, userLikedArray);
//		System.out.println("******LikeArray******"+statsObj.likeArray);
		
	}
		
}
