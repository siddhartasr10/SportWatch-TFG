import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({providedIn: 'root'})
export class BaseCsrfService {

    protected readonly apiUrl : string = 'http://localhost:4200/api';
    constructor(protected http: HttpClient) {
        this.addCsrfTokenToCookies();
    }

    // Gets CsrfToken and sets it on the existing cookies.
    // withCredentials only needed if different origin (im using proxy so no need), but just in case I don't need to add csrf token to header cause angular does this for me
    public addCsrfTokenToCookies() : Observable<Object> {
        return this.http.get(`${this.apiUrl}/csrf-token`, {withCredentials: true, responseType: 'text' });
    }

    getXsrfToken() : string {
        return document.cookie.split("; ").filter(cookiePair => cookiePair.startsWith("XSRF"))[0].split("=")[1];
    }
}
