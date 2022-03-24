package edu.sjsu.cmpe275.aop.tweet;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.UUID;

public interface TweetService {
	// Please do NOT change this file. Refer to the handout for actual interface
	// definitions.

	/**
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @returns a unique message ID
	 */
	UUID tweet(String user, String message) throws IllegalArgumentException, IOException;

	/**
	 * @returns a unique ID for the replying message.
	 * @throws IllegalArgumentException * @throws IOException * @throws
	 *                                  AccessControlException
	 */
	UUID reply(String user, UUID originalMessage, String message) throws IOException, AccessControlException, IllegalArgumentException;

	/**
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	void follow(String follower, String followee)
			throws IllegalArgumentException, IOException;

	/**
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	void block(String user, String followee) throws  IllegalArgumentException, IOException;

	/**
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws AccessControlException
	 */
	void like(String user, UUID messageId) throws AccessControlException, IllegalArgumentException, IOException;

}
