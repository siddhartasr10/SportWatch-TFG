package com.reactive.SportWatch.models;

import org.springframework.security.core.userdetails.UserDetails;

public interface ExtUserDetails extends UserDetails {

	/**
	 * Returns the email saved to verify the user. Cannot return <code>null</code>.
	 * @return the email.
	 */
    String getEmail();

}
