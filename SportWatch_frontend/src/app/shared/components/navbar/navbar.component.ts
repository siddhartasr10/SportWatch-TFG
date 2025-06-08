import { Component } from '@angular/core';
import { ActivatedRoute, RouterLink, UrlSegment } from '@angular/router';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {
    private nonLoggedPaths : string[] = ["welcome", "login", "register"];
    logoRedirects : string = "";

    constructor(private route : ActivatedRoute) {
        route.url.subscribe((url : UrlSegment[]) => {
            this.logoRedirects =
                (this.nonLoggedPaths.some(path => path === url[0].path))
                ? "/welcome"
                : "/feed"
        });
    }

}
