import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { ExtUser } from '../../interfaces/User';
import { FollowersStreamers, SuscribersStreamers } from '../../interfaces/FollowsSuscribes';
import { BaseCsrfService } from '../base-csrf-service/base-csrf-service.service';


@Injectable({
  providedIn: 'root'
})
export class UserService extends BaseCsrfService {

    constructor(http : HttpClient) {
        super(http);
    }

    getUserByUsername(username : string) : Observable<ExtUser> {
        return this.http.get<ExtUser>(`${this.apiUrl}/user/${username}`, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json',
        });

    }

    getFollowersOfUsername(username : string) : Observable<FollowersStreamers[]> {
        return this.http.get<FollowersStreamers[]>(`${this.apiUrl}/followers/${username}`, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json',
        });

    }

    getSuscribersOfUsername(username : string) : Observable<SuscribersStreamers[]> {
        return this.http.get<SuscribersStreamers[]>(`${this.apiUrl}/suscribers/${username}`, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json',
        });

    }

    getUsernameFollows(username : string) : Observable<FollowersStreamers[]> {
        return this.http.get<FollowersStreamers[]>(`${this.apiUrl}/follows/${username}`, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json',
        });

    }

    getUsernameSuscribed(username : string) : Observable<SuscribersStreamers[]> {
        return this.http.get<SuscribersStreamers[]>(`${this.apiUrl}/suscribed/${username}`, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json',
        });

    }

}
