package edu.sjsu.cmpe275.aop.tweet;

import java.util.UUID;

public interface TweetStatsService {
	// Please do NOT change this file. Refer to the handout for actual definitions.

	void resetStatsAndSystem();

	int getLengthOfLongestTweet();

	String getMostFollowedUser();

	UUID getMostPopularMessage();

	UUID getMostLikedMessage();

	String getMostProductiveReplier();

	String getMostUnpopularFollower();
	
	UUID getLongestMessageThread();
}