import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';


@Injectable({providedIn: 'root'})
export class AuthService {
    // I really hope angular starts automatically taking the cookie and setting it as header
    // when i move to prod, bc this is ugly
    private xsrfToken : string = "";
    constructor(private http: HttpClient) {
        this.addCsrfTokenToCookies().subscribe({
            next: () => this.xsrfToken = document.cookie.split("; ").filter(cookiePair => cookiePair.startsWith("XSRF"))[0].split("=")[1]
        });
    }

    private readonly apiUrl : string = "http://localhost:4200/api";

    // Gets CsrfToken and sets it on the existing cookies.
    // withCredentials only needed if different origin (im using proxy so no need), but just in case
    // I don't need to add csrf token to header cause angular does this for me
    addCsrfTokenToCookies() : Observable<Object> {
        return this.http.get(`${this.apiUrl}/csrf-token`, {withCredentials: true, responseType: 'text' });
    }

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
            headers: new HttpHeaders({ 'Content-Type': 'application/x-www-form-urlencoded', 'X-XSRF-TOKEN': this.xsrfToken }),
            withCredentials: true,
            responseType: 'json',
        });
    }

    // responseType text cause i return no json and observe the response to be able to read http status code.
    login(username: string, password: string) : Observable<Object> {
        let params : HttpParams = new HttpParams().appendAll({'username': username, 'password': password});

        return this.http.post(`${this.apiUrl}/login`, params.toString(), {
            headers: new HttpHeaders({ 'Content-Type': 'application/x-www-form-urlencoded', 'X-XSRF-TOKEN': this.xsrfToken }),
            withCredentials: true,
            responseType: 'json',
        });
;
    }

    // Component will have to catch errors in observable catch: () => and handle redirect with this.router.navigate().
        // Network errors (e.g., offline, server unreachable)
        // 4xx and 5xx HTTP responses
        // Timeout or parsing errors (less common unless manually configured)


}
