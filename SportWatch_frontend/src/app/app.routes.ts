import { Routes } from '@angular/router';
import { LandingComponent } from './views/landing/landing.component';
import { LoginComponent } from './views/login/login.component';
import { RegisterComponent } from './views/register/register.component';

export const routes: Routes = [
    { path: 'welcome', component: LandingComponent },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: '', redirectTo: '/welcome', pathMatch: 'full'},
    // keep the wildcard as last route.
    { path: '**', redirectTo: '/welcome'},
];
