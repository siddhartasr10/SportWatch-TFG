import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseCsrfService } from '../base-csrf-service/base-csrf-service.service';


@Injectable({providedIn: 'root'})
export class AuthService {
    protected readonly apiUrl : string = 'http://localhost:4200/api';
    // I really hope angular starts automatically taking the cookie and setting it as header
    // when i move to prod, bc this is ugly (it should do it btw but angular doesnt want to.)
    constructor(private http : HttpClient, private csrf: BaseCsrfService) {}

    // Server only recognizes x-url-form-encoded so data must be passed by requestParams.
    register(username: string, password: string, email: string) : Observable<Object> {
        // this or urlencode(params) are xss safe ways to do it.
        let params : HttpParams = new HttpParams().appendAll({
            'username': username,
            'password': password,
            'email': email
        });

        // let params = `username=${username}&password=${password}&email=${email}`;

        return this.http.post(`${this.apiUrl}/register`, params, {
            headers: new HttpHeaders({ 'Content-Type': 'application/x-www-form-urlencoded', 'X-XSRF-TOKEN': this.csrf.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json',
        });
    }

    // responseType text cause i return no json and observe the response to be able to read http status code.
    login(username: string, password: string) : Observable<Object> {
        let params : HttpParams = new HttpParams().appendAll({'username': username, 'password': password});

        return this.http.post(`${this.apiUrl}/login`, params.toString(), {
            headers: new HttpHeaders({ 'Content-Type': 'application/x-www-form-urlencoded', 'X-XSRF-TOKEN': this.csrf.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json',
        });
;
    }

    logout() : Observable<Object> {
        return this.http.post(`${this.apiUrl}/logout`, "", {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.csrf.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json',
        });

    }

    // msg has the username.
    checkUser() : Observable<{[msg: string] : string}> {
        return this.http.get<{[msg: string] : string}>(`${this.apiUrl}/check-user`, {
            headers: new HttpHeaders({ 'X-XSRF-TOKEN': this.csrf.getXsrfToken() }),
            withCredentials: true,
            responseType: 'json',
        });
    }

    // Component will have to catch errors in observable catch: () => and handle redirect with this.router.navigate().
        // Network errors (e.g., offline, server unreachable)
        // 4xx and 5xx HTTP responses
        // Timeout or parsing errors (less common unless manually configured)


}
