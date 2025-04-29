import { Routes } from '@angular/router';
import { LandingComponent } from './landing/landing.component';

export const routes: Routes = [
    { path: 'welcome', component: LandingComponent },
    { path: '', redirectTo: '/welcome', pathMatch: 'full'},
    // keep the wildcard as last route.
    { path: '**', redirectTo: '/welcome'},
];
