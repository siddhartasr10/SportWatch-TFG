import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { LoggedHeaderComponent } from '../../shared/components/logged-header/logged-header.component';
import { ActivatedRoute, Params } from '@angular/router';

import { map } from 'rxjs';

import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';

import { ExtUser } from '../../shared/interfaces/User';
import { UserService } from '../../shared/services/user-service/user-service.service';
import { FetchService } from '../../shared/services/fetch-service/fetch-service.service';
import { StreamingInfo } from '../../shared/interfaces/StreamingInfo';
import { FollowersStreamers, SuscribersStreamers } from '../../shared/interfaces/FollowsSuscribes';
import { VideoComponent } from '../../shared/components/video/video.component';

@Component({
  selector: 'app-profile',
    imports: [LoggedHeaderComponent, VideoComponent, MatTabsModule, MatButtonModule, CommonModule],

  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent {
    profileUsername! : string;
    profileUser! : ExtUser;
    userVideos! : StreamingInfo[];
    userStreams! : StreamingInfo[];
    // Array of the user ids of the followers.
    userFollowers! : number[];
    // Array of the user ids of the suscribers.
    userSuscribers! : number[];

    constructor(private route : ActivatedRoute, private userService : UserService, private fetchService : FetchService) {
        route.params.subscribe((params : Params) => this.profileUsername = params['username']);
        this.userService.getUserByUsername(this.profileUsername).pipe(map((user) => {console.log(user); return user;})).subscribe((user : ExtUser) => this.profileUser = user);

        this.userService.getFollowersOfUsername(this.profileUsername).pipe(map((followersStreamers: FollowersStreamers[]) => followersStreamers.map((followerStreamer: FollowersStreamers) => followerStreamer.follower_id)))
            .subscribe((userIds : number[]) => this.userFollowers = userIds);

        this.userService.getSuscribersOfUsername(this.profileUsername).pipe(map((followersStreamers: SuscribersStreamers[]) => followersStreamers.map((followerStreamer: SuscribersStreamers) => followerStreamer.suscriber_id)))
            .subscribe((userIds : number[]) => this.userSuscribers = userIds);

        this.fetchService.fetchAllStreams().pipe(map((streams : StreamingInfo[]) => streams.filter(stream => stream.authorId === this.profileUser.user_id)))
            .subscribe((streams : StreamingInfo[]) => {
                this.userVideos = streams.filter(stream => !stream.isLive);
                this.userStreams = streams.filter(stream => stream.isLive);
            });
    }

    ngOnInit() : void {
    }
}
