import { Routes } from '@angular/router';
import { LandingComponent } from './views/landing/landing.component';
import { LoginComponent } from './views/login/login.component';
import { RegisterComponent } from './views/register/register.component';
import { FeedComponent } from './views/feed/feed.component';
import { ProfileComponent } from './views/profile/profile.component';
import { VideoIdComponent } from './views/video-id/video-id.component';


export const routes: Routes = [
    { path: 'welcome', component: LandingComponent },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'feed', component: FeedComponent },
    { path: 'profile/:username', component: ProfileComponent },
    { path: 'video/:id', component: VideoIdComponent },
    { path: '', redirectTo: '/welcome', pathMatch: 'full'},
    // NOTE: keep the wildcard as last route.
    { path: '**', redirectTo: '/welcome'},
];
