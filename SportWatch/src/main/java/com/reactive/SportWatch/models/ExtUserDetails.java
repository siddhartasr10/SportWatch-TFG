package com.reactive.SportWatch.models;

import java.sql.Timestamp;

import org.springframework.security.core.userdetails.UserDetails;

public interface ExtUserDetails extends UserDetails {

	/**
	 * Returns the streamer id or null email saved to verify the user. Cannot return <code>null</code>.
	 * @return the streamer id or null email.
	 */
    String getEmail();

	/**
	 * Returns the time at which the user was created. Cannot return <code>null</code>.
	 * @return the time at which the user was created.
	 */
    Timestamp getCreated_at();

	/**
	 * Returns the streamer id or null saved to identify if the user can upload videos. Can return <code> null </code>
	 * @return the streamer id or <code> null </code>
	 */
    int getStreamerId();

}
