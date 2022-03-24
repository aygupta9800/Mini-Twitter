package edu.sjsu.cmpe275.aop.tweet;

import java.security.AccessControlException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

public class TweetStatsServiceImpl implements TweetStatsService {
    /***
     * Following is a dummy implementation.
     * You are expected to provide an actual implementation based on the requirements.
     */
	
//	We will keep Data structure he to store are tweets etc
	
	public int longestTweet = 0;
//	user: messageID, message
	public HashMap<String, HashMap<UUID, String>> tweetsArray = new HashMap<>();
//	followee: set(followers array)
	public HashMap<String, HashSet<String>> followArray = new HashMap<>();
	
// 	blocked-user: set(user who blocked user )
	public HashMap<String, HashSet<String>> blockArray = new HashMap<>();
// messageID: set(users)
	public HashMap<UUID, HashSet<String>> likeArray = new HashMap<>();
//  user : [tweets shared with him]
	public HashMap<String, HashSet<UUID>> sharedTweets = new HashMap<>();
	public HashMap<UUID, Integer> msgShareCountMap = new HashMap<>();
	
//  ReplyMap <msgId: <all replies msgId>
	public HashMap<UUID, List<UUID>> replyMap = new HashMap<>();
//  Relpy count <user: replycount>
	public HashMap<String, Integer> replyCount = new HashMap<>();
	public HashMap<UUID, HashSet<UUID>> firstTweets = new HashMap<>();
	
//	Giving null longest thread so keeping it in global for now
	long maxThreadLen = 0;
	UUID longestThreadMsg = null;
	
	
	// function to find user who tweeted the msg
	public String getUserUsingMsgId(UUID msgId) {
//		Looping on the tweetHashmap
//		System.out.println("TWEETSARRAY"+ tweetsArray);
		for(Entry<String, HashMap<UUID, String>> userTweetList: tweetsArray.entrySet()) {
			for(Entry<UUID,String> userMessage : userTweetList.getValue().entrySet()) {
				
				
				if (userMessage.getKey() == msgId) {
					return userTweetList.getKey();
				}
			}
		}
		return null;
	}
	
	public void addTweetToTweetList(String tweetBy, String msg, UUID msgUuid) {
		HashMap<UUID, String> hashmap;
//		check if the tweetsArray is empty
		if (tweetsArray.isEmpty()) {
			hashmap = new HashMap<UUID, String>();
			hashmap.put(msgUuid, msg);
		} else {
			hashmap = tweetsArray.get(tweetBy);
//			If no tweet by the user till now
			if (hashmap == null) {
				hashmap = new HashMap<UUID, String>();
			} 
			hashmap.put(msgUuid, msg);
		}
		tweetsArray.put(tweetBy, hashmap);
	}
	
	// 
	public void updateLongestTweet(String tweet) {
		int tweetLen = tweet.length();
		if (tweetLen > longestTweet) {
			longestTweet = tweetLen;
//			System.out.println("====Longest tweet Len Till Now=="+ longestTweet);
		}
	}
	
	public boolean isTweetValid(UUID messageID) {
		for(Entry<String, HashMap<UUID, String>> userTweetsMap: tweetsArray.entrySet()) {
			for(Entry<UUID, String> userTweets: userTweetsMap.getValue().entrySet()) {
				if (userTweets.getKey() == messageID) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isTweetSharedWithUser(UUID msgId, String user) {
		HashSet<UUID> sharedMsgSet = sharedTweets.get(user);
		if (sharedMsgSet != null && sharedMsgSet.contains(msgId)) {
			return true;
		}
//		for(Entry)
		return false;
	}
	
	public boolean isBlocked(String user, String followee) {
		HashSet<String> blockedByUsers = blockArray.get(user);
		if (blockedByUsers != null && followee!= null && blockedByUsers.contains(followee)) {
			return true;
		}
		
		return false;
	}
	
	public boolean isLikedByUser(UUID msgId, String user) {
		for(Entry<UUID, HashSet<String>> e : likeArray.entrySet()) {
			if(e.getKey()==msgId) {
				return e.getValue().contains(user);
			}
			
		}
		return false;
	}
	
	public void updateShareCount(UUID messageId) {
		msgShareCountMap.put(messageId, msgShareCountMap.getOrDefault(messageId, 0) + 1);
	}
	
	public void shareTweet(String user, UUID msgId) {
//		TODO: Need to test and confirm for replies
//		System.out.println("===FollowerArray"+ followArray);
//		System.out.println("===++SHARED&&&Tweets"+ sharedTweets);
		HashSet<String> followerSet = followArray.get(user);
//		System.out.println("==============FollowerSEt==="+ followerSet);
		// Adding msg to user sharedMsg list as well
		// TODO: Check if we need to add themselves
		
//		We need to iterate over the hashset so we need to implement iterator
		if (followerSet != null) {
			Iterator<String> it = followerSet.iterator();
			while(it.hasNext()) {
				String follower = it.next();
//				HashSet<String> blockedByUsers = blockArray.get(follower);
				if (!isBlocked(follower, user)) {
//				if(blockedByUsers== null || !blockedByUsers.contains(user)) {
					HashSet<UUID> msgSet = sharedTweets.get(follower);
					if (msgSet == null) {
						msgSet = new HashSet<UUID>();
						msgSet.add(msgId);
//						System.out.println("====================XXXXXXXXXFollower"+ follower+ "Message set"+ msgSet);
						sharedTweets.put(follower, msgSet);
					} else {
						msgSet.add(msgId);
					}
//					System.out.println("===============XXXXXXX=SHARED TWEETS===="+ sharedTweets);
					// For sharing msg with every followers
					updateShareCount(msgId);
					
				}
						
			}
		}
//		System.out.println("======sharedTweets"+ sharedTweets);
	}
	
	
	// For longest thread we need to keep one more Datastructure which 
	public void addReplyToFirstMsg(UUID originalMessage, UUID replyMsgId, String apiType) {
		// If the first tweet of the tweet-reply chain
		if (apiType.equals("first")) {
			firstTweets.put(originalMessage, new HashSet<UUID>());
		} else {
			HashSet<UUID> msgSet = firstTweets.get(apiType);
			if (msgSet != null) {
				msgSet.add(replyMsgId);
			} else {
//				System.out.println("=======msgSet is null "+ originalMessage+ replyMsgId+ apiType);
				// TODO: WHy Msg Set is coming to be Null
				msgSet = new HashSet<UUID>();
				msgSet.add(replyMsgId);
				firstTweets.put(originalMessage, msgSet);
			}
		}
//		System.out.println("========PARAMS============"+ originalMessage + "{{{"+ replyMsgId+ "}}}}"+ apiType);
//		System.out.println("=====*****FIRST TWEETS******====="+ firstTweets);
	}
	
	
	// Functionality to add tweet
	
	

	@Override
	public void resetStatsAndSystem() {
		longestTweet = 0;
		msgShareCountMap.clear();
		tweetsArray.clear();;
		followArray.clear();
		blockArray.clear();
		likeArray.clear();
		sharedTweets.clear();
		replyMap.clear();
		replyCount.clear();
		firstTweets.clear();
		longestThreadMsg=null;
		maxThreadLen=0;
	}
    
	@Override
	public int getLengthOfLongestTweet() {
		// TODO Auto-generated method stub
		return longestTweet;
	}

	@Override
	public String getMostFollowedUser() {
		int maxFollower = 0;
		String user = null;
		for(Entry<String, HashSet<String>> userFollowers : followArray.entrySet()) {
			if (userFollowers.getValue().size() > maxFollower) {
				user = userFollowers.getKey();
				maxFollower = userFollowers.getValue().size();
			} else if(userFollowers.getValue().size() == maxFollower) {
				int comparedResult = userFollowers.getKey().compareTo(user);
		        if (comparedResult < 0) {
		            user = userFollowers.getKey();
		        } 
			// TODO: If have same followers
			}
		}
		return user;
	}

	@Override
	public UUID getMostPopularMessage() {
		// Looping through our msgsharedCount HashMap to 
		UUID mostPopularMsg = null;
		long maxSharedCount = 0;
		for(Entry<UUID, Integer> msg: msgShareCountMap.entrySet()) {
			UUID msgKey = msg.getKey();
			long msgCount = msg.getValue();
			if (msgCount > maxSharedCount) {
				mostPopularMsg = msgKey;
				maxSharedCount = msgCount;
			} else if(msgCount == maxSharedCount) {
				int comparedResult = msgKey.compareTo(mostPopularMsg);
		        if (comparedResult < 0) {
		            mostPopularMsg = msgKey;
		        } 
			}
		}
//		System.out.println("====TweetList"+ tweetsArray);
		return mostPopularMsg;
	}
	
	@Override
	public String getMostProductiveReplier() {
		// We have Kept ReplyCount hashMap to keep track of which user has replied at what time.
		// Just iterate over hashmap and return max count user
		long maxCount = 0;
		String replier = null;
		for(Entry<String, Integer> entry : replyCount.entrySet()) {
			if (entry.getValue() > maxCount) {
				maxCount = entry.getValue();
				replier = entry.getKey();
			} else if (entry.getValue() == maxCount) {
//					Check alphabetic order if both are of same length
				int comparedResult = entry.getKey().compareTo(replier);
		        if (comparedResult < 0) {
		            replier = entry.getKey();
		        }
			}
		}
				
		return replier;
	}

	@Override
	public UUID getMostLikedMessage() {
//		Loop over our liked array hashset to find like count of every msg.
		int maxLike = 0;
		UUID msgID = null;
		for (Entry<UUID, HashSet<String>> msgLikes: likeArray.entrySet()) {
			if (msgLikes.getValue().size() > maxLike) {
				maxLike = msgLikes.getValue().size();
				msgID = msgLikes.getKey();
			}
		}
		return msgID;
	}

	@Override
	public String getMostUnpopularFollower() {
//		 Iterate over block array hashset to make a new  hashmap which maps user to its block count
		int maxBlock = 0;
		String blockedUser = null;
		HashMap<String, Integer> blockCountMap = new HashMap<>();
		for(Entry<String, HashSet<String>> blockedByUsers : blockArray.entrySet()) {
			int blockLen = blockedByUsers.getValue().size();
			String blockVal = blockedByUsers.getKey();
			if (blockLen > maxBlock) {
				maxBlock = blockLen;
				blockedUser = blockVal;
			} else if (blockLen == maxBlock) {
//				Check alphabetic order if both are of same length
				int comparedResult = blockVal.compareTo(blockedUser);
		        if (comparedResult < 0) {
		            blockedUser = blockVal;
		        }
			}
		}
		return blockedUser;
	}
	
	public void depthFirstSearch(UUID msgId, long currentDepth) {
		
//		System.out.println("====msgId"+ msgId + "====longestThreadMsg=="+ longestThreadMsg + "===maxThreadLen"+ maxThreadLen+ "currentDepth"+ currentDepth);
		if (currentDepth > maxThreadLen) {
			maxThreadLen = currentDepth;
			longestThreadMsg = msgId;
		}
		HashSet<UUID> msgReplySet = firstTweets.get(msgId);
//		System.out.println("======msgReplySet"+ msgReplySet);
		if (msgReplySet == null || msgReplySet.isEmpty()) {
			// We reach the leaf node
			return;
		}
//		long maxDepth = 0;
//		We need to iterate over hashset using iterator and recursively call dfs
		Iterator<UUID> it = msgReplySet.iterator();
		while(it.hasNext()) {
			UUID replyMsgId = it.next();
			currentDepth += 1;
			depthFirstSearch(replyMsgId, currentDepth);
			if (currentDepth > maxThreadLen) {
				maxThreadLen = currentDepth;
				longestThreadMsg = replyMsgId;
//				System.out.println("======currentFDepth"+ currentDepth+ "replyMsgId"+replyMsgId);
			} else if(currentDepth == maxThreadLen) {
//				Check alphabetic order if both are of same length
				int comparedResult = replyMsgId.compareTo(longestThreadMsg);
		        if (comparedResult < 0) {
		            longestThreadMsg = replyMsgId;
//		            System.out.println("====longest message coming"+ longestThreadMsg);
		        }
			}				
		}
		
//		return 1 + maxDepth;
	}

	@Override
	public UUID getLongestMessageThread() {
//		System.out.println("++++FirstTweetsHashmap+++"+ firstTweets);
//		long maxThreadLen = 0;
//		UUID longestThreadMsg = null;
		// If firstTweets is empty then no thread
		if (firstTweets.isEmpty()) {
			return longestThreadMsg;
		}
		// our current structure is kind of graph and we want to find longest thread  i.e maximum depth so we need to use 
		// the depth first search traversal on our firstTweets Hashmap messages.
//		System.out.println("=====FirstTweets"+ firstTweets);
		for (Entry<UUID, HashSet<UUID>> e: firstTweets.entrySet()) {
			HashSet<UUID> replyMsgSet = e.getValue();
			for (UUID replyMsgId: replyMsgSet) {
//				System.out.println("====Reply Msg ID===="+replyMsgId);
//				We dont need to keep visit set here as reply chain will be unique and an reply can only reach through
//				its previous tweet and no other tweet
//				visitSet
				if (replyMsgId == null) {
					continue;
				}
				depthFirstSearch(replyMsgId, 1);
			}
		}
//		System.out.println("=============maxThreadLen"+ maxThreadLen+"==Message:" +longestThreadMsg);
		
		return longestThreadMsg;
	}

}



