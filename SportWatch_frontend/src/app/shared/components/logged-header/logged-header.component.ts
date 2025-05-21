import { Component, output, Output, OutputEmitterRef, WritableSignal, signal, OnInit, OnDestroy } from '@angular/core';
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

    user : string = document.cookie.split("; ").filter(cookiePair => cookiePair.startsWith("user"))[0]?.split("=")[1];

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

    ngOnDestroy() {
        clearInterval(this.currentIntervalId);
    }
}
