package edu.sjsu.cmpe275.aop.tweet.aspect;


import java.io.IOException;
import java.util.Random;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.aspectj.lang.annotation.Around;

@Aspect
@Order(1)
public class RetryAspect {
    /***
     * Following is a dummy implementation of this aspect.
     * You are expected to provide an actual implementation based on the requirements, including adding/removing advices as needed.
     * @throws Throwable 
     */

	@Around("execution(public int edu.sjsu.cmpe275.aop.tweet.TweetService.*tweet(..))")
	public int dummyAdviceOne(ProceedingJoinPoint joinPoint) throws Throwable {
//		System.out.printf("Prior to the executuion of the metohd %s\n", joinPoint.getSignature().getName());
		Integer result = null;
		try {
			result = (Integer) joinPoint.proceed();
//			System.out.printf("Finished the executuion of the metohd %s with result %s\n", joinPoint.getSignature().getName(), result);
		} catch (Throwable e) {
			e.printStackTrace();
			System.out.printf("Aborted the executuion of the metohd %s\n", joinPoint.getSignature().getName());
			throw e;
		}
		return result.intValue();
	}
	
	@Around("execution(public * edu.sjsu.cmpe275.aop.tweet.TweetService.*(..))")
	public Object networkFailureAdvice(ProceedingJoinPoint jp) throws Throwable {
//		System.out.printf("Prior to the executuion of the metohd %s\n", jp.getSignature().getName());
		Object result = null;
		int triesCount = 4;
		while(true) {
			try {
				result = jp.proceed();
				System.out.printf("Inside While Prior to the executuion of the metohd %s\n", jp.getSignature().getName());
//				TO test the advice
//				Random rand = new Random();
//				int r = rand.nextInt(500);
//				if(r % 5 != 0) {
//					throw new IOException();
//				}
				return result;
					
			} catch(IOException e) {
				e.printStackTrace();
				triesCount -= 1;
				if(triesCount > 0) {
					System.out.printf("Network Error, reconnect count left="+ triesCount);
				} else {
					System.out.printf("Aborted the executuion of the metohd %s\n", jp.getSignature().getName());
					throw e;
				}
			}
		}
						
	}

}
