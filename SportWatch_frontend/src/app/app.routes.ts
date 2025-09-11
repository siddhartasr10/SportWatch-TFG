import { Routes } from '@angular/router';
import { LandingComponent } from './views/landing/landing.component';
import { LoginComponent } from './views/login/login.component';
import { RegisterComponent } from './views/register/register.component';
import { FeedComponent } from './views/feed/feed.component';
import { ProfileComponent } from './views/profile/profile.component';
import { VideoIdComponent } from './views/video-id/video-id.component';
import { LoggedGuard } from './shared/guards/logged-guard/logged-guard.guard';
import { AlreadyLoggedGuard } from './shared/guards/already-logged-guard/already-logged-guard.guard';


export const routes: Routes = [
    { path: 'welcome', component: LandingComponent },
    { path: 'login', component: LoginComponent, canActivate: [AlreadyLoggedGuard] },
    { path: 'register', component: RegisterComponent, canActivate: [AlreadyLoggedGuard] },
    { path: 'feed', component: FeedComponent, canActivate: [LoggedGuard] },
    { path: 'profile/:username', component: ProfileComponent, canActivate: [LoggedGuard]},
    { path: 'video/:id', component: VideoIdComponent, canActivate: [LoggedGuard]},
    { path: '', redirectTo: '/welcome', pathMatch: 'full'},
    // NOTE: keep the wildcard as last route.
    { path: '**', redirectTo: '/welcome'},
];
