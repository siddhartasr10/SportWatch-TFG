import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';

import { map, firstValueFrom, catchError } from 'rxjs';

import { MatTabsModule } from '@angular/material/tabs';
import { MatButton, MatButtonModule } from '@angular/material/button';

import { ExtUser } from '../../shared/interfaces/User';
import { StreamingInfo } from '../../shared/interfaces/StreamingInfo';
import { FollowersStreamers, SuscribersStreamers } from '../../shared/interfaces/FollowsSuscribes';

import { UserService } from '../../shared/services/user-service/user-service.service';
import { FetchService } from '../../shared/services/fetch-service/fetch-service.service';
import { AuthService } from '../../shared/services/auth-service/auth-service.service';

import { VideoComponent } from '../../shared/components/video/video.component';
import { LoggedHeaderComponent } from '../../shared/components/logged-header/logged-header.component';

@Component({
  selector: 'app-profile',
  imports: [LoggedHeaderComponent, VideoComponent, MatTabsModule, MatButtonModule, CommonModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent {
    // profile means the one in the path
    profileUsername! : string;
    // if null it doesnt exist.
    profileUser! : ExtUser;
    profileUserVideos! : StreamingInfo[];
    profileUserStreams! : StreamingInfo[];
    // Array of the user ids of the followers.
    profileUserFollowers! : number[];
    // Array of the user ids of the suscribers.
    profileUserSuscribers! : number[];

    // the actual logged user username
    actualUsername! : string;
    actualUserFollowsProfileUser!: boolean;
    actualUserSuscribedProfileUser!: boolean;

    // Seguir | Siguiendo
    followBtnMsg : String = "";
    // Unirme | Eres miembro
    suscribeBtnMsg : String = "";




    constructor(private userService : UserService, private fetchService : FetchService, private authService : AuthService, private route : ActivatedRoute, private router : Router) {
        route.params.subscribe((params : Params) => this.profileUsername = params['username']);

        this.updateFollowerCount();
        this.updateSuscriberCount();
    }

    async ngOnInit() : Promise<void>{
        this.profileUser = await firstValueFrom(this.userService.getUserByUsername(this.profileUsername));

        if (this.profileUser === null) this.router.navigate(["feed"]);

        this.fetchService.fetchAllStreams().pipe(
            map((streams : StreamingInfo[]) => streams.filter(stream => stream.authorId == this.profileUser?.user_id)))
            .subscribe((streams : StreamingInfo[]) => {
                this.profileUserVideos = streams.filter(stream => !stream.isLive);
                this.profileUserStreams = streams.filter(stream => stream.isLive);
            });
        // I need to confirm the name of the current user, cannot trust the token to follow someone, (I cannot verify the signature on the frontend, as the keys are on the backend)
        // In the api call the JWT signature gets verified.
        // Awesome func of rxjs
        let json : {[msg:string] : string} = await firstValueFrom(this.authService.checkUser());
        this.actualUsername = json["msg"];

        this.userService.checkFollows(this.actualUsername, this.profileUsername).subscribe((doesFollow) => {
            this.actualUserFollowsProfileUser = doesFollow;

            (doesFollow) ? this.followBtnMsg = "Siguiendo" : this.followBtnMsg = "Seguir";
        });

        this.userService.checkSuscribed(this.actualUsername, this.profileUsername).subscribe((isSuscribed) => {
            this.actualUserSuscribedProfileUser = isSuscribed;

            (isSuscribed) ? this.suscribeBtnMsg = "Eres miembro" : this.suscribeBtnMsg = "Unirme";
        });



    }

    onFollowClick(matBtn : MatButton) : void {
        const btn = matBtn._elementRef.nativeElement as HTMLButtonElement;
        if (this.actualUserFollowsProfileUser) {
            this.userService.unfollowUser(this.actualUsername, this.profileUsername).subscribe({
                next:  () => this.updateFollowerCount(),
                error: () => undefined,
            });
        }
        else {
            this.userService.followUser(this.actualUsername, this.profileUsername).subscribe({
                next:  () => this.updateFollowerCount(),
                error: () => undefined,
            });
        }

        this.actualUserFollowsProfileUser = !this.actualUserFollowsProfileUser;
        (this.actualUserFollowsProfileUser)  ? this.followBtnMsg = "Siguiendo" : this.followBtnMsg = "Seguir";


    }

    updateFollowerCount() : void {
        this.userService.getFollowersOfUsername(this.profileUsername).pipe(map((followersStreamers: FollowersStreamers[]) => followersStreamers.map((followerStreamer: FollowersStreamers) => followerStreamer.follower_id)))
            .subscribe((userIds : number[]) => this.profileUserFollowers = userIds);
    }


    async onSuscribeClick(matBtn : MatButton) : Promise<void> {
        const btn = matBtn._elementRef.nativeElement as HTMLButtonElement;
        if (this.actualUserSuscribedProfileUser) {
            this.userService.unsuscribeUser(this.actualUsername, this.profileUsername).subscribe(() => this.updateSuscriberCount());
        }
        else {
            this.userService.suscribeUser(this.actualUsername, this.profileUsername).subscribe(() => this.updateSuscriberCount());
        }

        this.actualUserSuscribedProfileUser = !this.actualUserSuscribedProfileUser;
        (this.actualUserSuscribedProfileUser)  ? this.suscribeBtnMsg = "Eres miembro" : this.suscribeBtnMsg = "Unirme";


    }

    updateSuscriberCount() : void {
        this.userService.getSuscribersOfUsername(this.profileUsername).pipe(map((suscribersStreamers: SuscribersStreamers[]) => suscribersStreamers.map((suscriberStreamer: SuscribersStreamers) => suscriberStreamer.suscriber_id)))
            .subscribe((userIds : number[]) => this.profileUserSuscribers = userIds);
    }
}
