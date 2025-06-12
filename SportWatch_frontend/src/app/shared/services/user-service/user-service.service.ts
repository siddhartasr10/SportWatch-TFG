import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { ExtUser } from '../../interfaces/User';
import { FollowersStreamers, SuscribersStreamers } from '../../interfaces/FollowsSuscribes';
import { BaseCsrfService } from '../base-csrf-service/base-csrf-service.service';


@Injectable({
  providedIn: 'root'
})
export class UserService  {
    protected readonly apiUrl : string = 'http://localhost:4200/api';

    constructor(private http : HttpClient, private csrf: BaseCsrfService) {}

  /**
   * Fetches extended user details for the given username.
   * @param username The username of the user to retrieve.
   * @returns Observable containing user details.
   */
    getUserByUsername(username : string) : Observable<ExtUser> {
        return this.http.get<ExtUser>(`${this.apiUrl}/user/${username}`, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.csrf.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json',
        });

    }

    getUserById(id : number) : Observable<{username: string, password: string}> {
        return this.http.get<{username:string, password: string}>(`${this.apiUrl}/user/id/${id}`, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.csrf.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json',
        });

    }

  /**
   * Retrieves a list of followers for the given username.
   * @param username The username whose followers are to be retrieved.
   * @returns Observable containing an array of followers.
   */
    getFollowersOfUsername(username : string) : Observable<FollowersStreamers[]> {
        return this.http.get<FollowersStreamers[]>(`${this.apiUrl}/followers/${username}`, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.csrf.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json',
        });

    }

  /**
   * Retrieves a list of suscribers for the given username.
   * @param username The username whose suscribers are to be retrieved.
   * @returns Observable containing an array of suscribers.
   */
    getSuscribersOfUsername(username : string) : Observable<SuscribersStreamers[]> {
        return this.http.get<SuscribersStreamers[]>(`${this.apiUrl}/suscribers/${username}`, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.csrf.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json',
        });

    }

  /**
   * Retrieves a list of users the given username is following.
   * @param username The username whose followings are to be retrieved.
   * @returns Observable containing an array of followed users.
   */
    getUsernameFollows(username : string) : Observable<FollowersStreamers[]> {
        return this.http.get<FollowersStreamers[]>(`${this.apiUrl}/follows/${username}`, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.csrf.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json',
        });

    }

  /**
   * Retrieves a list of streamers the user is suscribed to.
   * @param username The username whose suscriptions are to be retrieved.
   * @returns Observable containing an array of suscribed streamers.
   */
    getUsernameSuscribed(username : string) : Observable<SuscribersStreamers[]> {
        return this.http.get<SuscribersStreamers[]>(`${this.apiUrl}/suscribed/${username}`, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.csrf.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json',
        });

    }

    /**
    * Follows a streamer on behalf of a user.
    * @param follower the username of the follower
    * @param streamer the username of the streamer
    */
    followUser(follower: string, streamer: string): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/follow/${follower}/${streamer}`, null, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.csrf.getXsrfToken() }),
            withCredentials: true
        });
    }

    /**
    * Subscribes a user to a streamer.
    * @param suscriber the username of the suscriber
    * @param streamer the username of the streamer
    */
    suscribeUser(suscriber: string, streamer: string): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/suscribe/${suscriber}/${streamer}`, null, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.csrf.getXsrfToken() }),
            withCredentials: true
        });
    }

    /**
    * Unfollows a streamer on behalf of a user.
    * @param follower the username of the follower
    * @param streamer the username of the streamer
    */
    unfollowUser(follower: string, streamer: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/unfollow/${follower}/${streamer}`, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.csrf.getXsrfToken() }),
            withCredentials: true
        });
    }

    /**
    * Unsubscribes a user from a streamer.
    * @param suscriber the username of the suscriber
    * @param streamer the username of the streamer
    */
    unsuscribeUser(suscriber: string, streamer: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/unsuscribe/${suscriber}/${streamer}`, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.csrf.getXsrfToken() }),
            withCredentials: true
        });
    }

    /**
    * Checks if a user follows a streamer.
    * @param follower the username of the follower
    * @param streamer the username of the streamer
    * @returns Observable<boolean>
    */
    checkFollows(follower: string, streamer: string): Observable<boolean> {
        return this.http.get<boolean>(`${this.apiUrl}/follows/${follower}/${streamer}`, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.csrf.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json'
        });
    }

    /**
    * Checks if a user is subscribed to a streamer.
    * @param suscriber the username of the suscriber
    * @param streamer the username of the streamer
    * @returns Observable<boolean>
    */
    checkSuscribed(suscriber: string, streamer: string): Observable<boolean> {
        return this.http.get<boolean>(`${this.apiUrl}/suscribes/${suscriber}/${streamer}`, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.csrf.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json'
        });
    }

    updateDescription(description : string): Observable<{[msg: string]: number}> {
        return this.http.put<{[msg: string]: number}>(`${this.apiUrl}/description`, {"description": description}, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.csrf.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json'
        });

    }

}
