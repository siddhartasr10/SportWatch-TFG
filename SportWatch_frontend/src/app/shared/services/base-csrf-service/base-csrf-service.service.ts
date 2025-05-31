import { Injectable } from '@angular/core';

@Injectable({providedIn: 'root'})
export class BaseCsrfService {

    private xsrfToken : string = "";
    constructor(protected http: HttpClient) {
        this.addCsrfTokenToCookies().subscribe({
            next: () => this.xsrfToken = document.cookie.split("; ").filter(cookiePair => cookiePair.startsWith("XSRF"))[0].split("=")[1]
        });

    // Gets CsrfToken and sets it on the existing cookies.
    // withCredentials only needed if different origin (im using proxy so no need), but just in case I don't need to add csrf token to header cause angular does this for me
    addCsrfTokenToCookies() : Observable<Object> {
        return this.http.get(`${this.apiUrl}/csrf-token`, {withCredentials: true, responseType: 'text' });
    }
}
