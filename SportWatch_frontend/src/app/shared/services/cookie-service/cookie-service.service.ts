import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class CookieService {
    private cookies! : string[];
    constructor() {
        this.updateCookies();
    }

    updateCookies() {
        this.cookies = document.cookie.split("; ");
    }

    getAuthToken() {
        console.log(this.cookies.filter(cookiePair => cookiePair.startsWith("authToken"))[0].split("=")[1]);

    }

    getUser() : string {
        return document.cookie.split("; ").filter(cookiePair => cookiePair.startsWith("user"))[0]?.split("=")[1];
    }
}
