import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-logged-header',
  imports: [NavbarComponent, RouterLink],
  templateUrl: './logged-header.component.html',
  styleUrl: './logged-header.component.css'
})
export class LoggedHeaderComponent {
    constructor(private router : Router) {}

    // TODO: haz un cookie service para esto. Esta forma es fea.
    user : string = document.cookie.split("; ").filter(cookiePair => cookiePair.startsWith("user"))[0].split("=")[1];

    handleSearch(search : string) : void {
        this.router.navigateByUrl(`/feed?s=${encodeURI(search)}`);
    }

}
