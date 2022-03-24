package edu.sjsu.cmpe275.aop.tweet.aspect;

import java.security.AccessControlException;
import java.util.HashSet;
import java.util.UUID;

import org.aopalliance.intercept.Joinpoint;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import edu.sjsu.cmpe275.aop.tweet.TweetStatsServiceImpl;

@Aspect
@Order(0)
public class AccessControlAspect {
    /***
     * Following is a dummy implementation of this aspect.
     * You are expected to provide an actual implementation based on the requirements, including adding/removing advices as needed.
     * @throws Throwable 
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

	@Before("execution(public int edu.sjsu.cmpe275.aop.tweet.TweetService.retweet(..))")
	public void dummyBeforeAdvice(JoinPoint joinPoint) {
		System.out.printf("Permission check before the executuion of the metohd %s\n", joinPoint.getSignature().getName());
	}

	@Around("execution(public int edu.sjsu.cmpe275.aop.tweet.TweetService.*(..))")
	public int dummyAdviceOne(ProceedingJoinPoint joinPoint) throws Throwable {
		System.out.printf("Prior to the executuion of the metohd %s\n", joinPoint.getSignature().getName());
		Integer result = null;
		try {
			result = (Integer) joinPoint.proceed();
			System.out.printf("Finished the executuion of the metohd %s with result %s\n", joinPoint.getSignature().getName(), result);
		} catch (Throwable e) {
			e.printStackTrace();
			System.out.printf("Aborted the executuion of the metohd %s\n", joinPoint.getSignature().getName());
			throw e;
		}
		return result.intValue();
	}
	
	@Before("tweetApiPointCut()")
	public void tweetAccessCheck(JoinPoint jp) throws Throwable {
		
	}
	
	@Before("replyApiPointCut()")
	public void replyAccessCheck(JoinPoint jp) throws Throwable {
		Object [] args = jp.getArgs();
		String user = (String) args[0];
		UUID originalMessage = (UUID) args[1];
		String message = (String) args[2];
//		if(statsObj.isTweetValid(originalMessage)) {
//			throw new AccessControlException("Original tweet doesnt exist");
//		}
		String originalUser = statsObj.getUserUsingMsgId(originalMessage);
//		System.out.println("=====Original User=="+ originalUser+ "===user"+ user+ "Orignalmessage"+ originalMessage+ "replymessage"+ message);
		if(!statsObj.isTweetSharedWithUser(originalMessage, user)) {
			throw new AccessControlException("Original Message was not shared with the user so he cant reply");
		}
//		System.out.println("=====Original User=="+ originalUser+ "===user"+ user+ "Orignalmessage"+ originalMessage+ "replymessage"+ message);
		if (statsObj.isBlocked(originalUser, user)) {
			throw new AccessControlException("Replier has blocked orginal message user so cant reply to him");
		}
//		if(statsObj)
		// Get Original User
//		String orgMsgUser = statsObj.getUserUsingMsgId(originalMessage);
//		if(orgMsgUser.equals(user)) {
//			throw new AccessControlException("User cannot reply to itself");
//		}
		// TODO: Check if Original message even exist or not
//		if(statsObj.isBlocked(orgMsgUser, user)) {
//			throw new AccessControlException("current user has blocked the org message user so he cant reply");
//		}
		// If original message 
	}
	
	@Before("blockApiPointCut()")
	public void blockAccessCheck(JoinPoint jp) throws Throwable {
		// Nothing For now
	}
	
	@Before("likeApiPointCut()")
	public void likeAccessCheck(JoinPoint jp) throws Throwable {
		Object [] args = jp.getArgs();
		String user = (String) args[0];
		UUID messageId = (UUID) args[1];
		String msgUser = statsObj.getUserUsingMsgId(messageId);
		Boolean isShared = statsObj.isTweetSharedWithUser(messageId, user);
		Boolean isLikedByUser = statsObj.isLikedByUser(messageId, user);
//		Boolean isBlockedByMsgUser = statsObj.isBlocked(user, msgUser);
		Boolean tweetValid = statsObj.isTweetValid(messageId);
		Boolean sameUser = user.equals(msgUser);
		if (!isShared || !tweetValid || isLikedByUser || sameUser) {
//			System.out.println("===ACCESS ERROR IN LIKe"+ user+ "{{"+ messageId+ "}}"+ msgUser+ isShared+isLikedByUser+tweetValid+sameUser);
			throw new AccessControlException("User does not have access to like the message");
		}
		
	}
	
	@Before("followApiPointCut()")
	public void followAccessCheck(JoinPoint jp) throws Throwable {
//		String follower = (String) jp.getArgs()[0];
//		String followee = (String) jp.getArgs()[1];
		// TODO: No check For Now
		
	}
	

}
