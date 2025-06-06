import { Component, output, Output, OutputEmitterRef, WritableSignal, signal, OnInit, OnDestroy } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { NavbarComponent } from '../navbar/navbar.component';
import { AuthService } from '../../services/auth-service/auth-service.service';
import { CookieService } from '../../services/cookie-service/cookie-service.service';

@Component({
  selector: 'app-logged-header',
  imports: [NavbarComponent, RouterLink],
  templateUrl: './logged-header.component.html',
  styleUrl: './logged-header.component.css'
})
export class LoggedHeaderComponent {
    constructor(private router : Router, private authService : AuthService, private cookieService : CookieService) {
        this.user = this.cookieService.getUser();
    }

    user : string;

    // No le pongo 'search' porque (search)="" search es un evento experimental de chrome ;(
    // Output va de componente hijo -> padre Input de padre -> hijo.
    searchEv: OutputEmitterRef<string> = output<string>();
    profileModalState : WritableSignal<boolean> = signal(false);

    previousScrollY : number  = 0;
    currentIntervalId : number = 0;

    handleSearch(search : string) {
        this.router.navigate(["/feed"], {queryParams: {"s": search}});
        this.searchEv.emit(search);
    }

    updateProfileModal() {
        this.profileModalState.update(state => !state);
        if (!this.profileModalState()) return clearInterval(this.currentIntervalId);

        this.previousScrollY = window.scrollY;
        this.currentIntervalId = window.setInterval(() => {
            if (window.scrollY > this.previousScrollY)
                window.scrollBy(0, this.previousScrollY - window.scrollY);

            if (window.scrollY < this.previousScrollY)
                window.scrollBy(0, this.previousScrollY - window.scrollY);
        }, 400);

    }

    handleLogout() {
        this.authService.addCsrfTokenToCookies().subscribe({
            next: (res) => this.authService.logout().subscribe({complete: () => this.router.navigate(['login'])}),

        });
        // this.authService.logout().subscribe({
        //     next: () => this.router.navigate(['login']),
        // });
    }

    ngOnDestroy() {
        clearInterval(this.currentIntervalId);
    }
}
