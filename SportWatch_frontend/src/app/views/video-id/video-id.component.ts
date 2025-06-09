import { Component, ElementRef, ViewChild, WritableSignal, signal } from '@angular/core';
import { ActivatedRoute, Params, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom, map } from 'rxjs';
import  Hls from 'hls.js';

import { StreamingInfo } from '../../shared/interfaces/StreamingInfo';
import { FollowersStreamers, SuscribersStreamers } from '../../shared/interfaces/FollowsSuscribes';

import { FetchService } from '../../shared/services/fetch-service/fetch-service.service';
import { UserService } from '../../shared/services/user-service/user-service.service';

import { LoggedHeaderComponent } from '../../shared/components/logged-header/logged-header.component';
import { VideoComponent } from '../../shared/components/video/video.component';
import { AuthService } from '../../shared/services/auth-service/auth-service.service';
import { MatButton, MatButtonModule } from '@angular/material/button';
import { FechaService } from '../../shared/services/fecha-service/fecha-service.service';

@Component({
  selector: 'app-video-id',
    imports: [LoggedHeaderComponent, VideoComponent, CommonModule, RouterLink, MatButtonModule, MatButton],
  templateUrl: './video-id.component.html',
  styleUrl: './video-id.component.css'
})
export class VideoIdComponent {
    @ViewChild('videoPlayer') videoRef!: ElementRef<HTMLVideoElement>;
    mainVideo! : StreamingInfo | undefined;

    actualUsername! : string;
    actualUserFollowsProfileUser!: boolean;
    actualUserSuscribedProfileUser!: boolean;

    videos : WritableSignal<StreamingInfo[]> = signal([]);
    comments : WritableSignal<{username: string, text: string}[]> = signal([]);

    authorUserFollowers!: number[];
    authorUserSuscribers!: number[];

    // Seguir | Siguiendo
    followBtnMsg : String = "";
    // Unirme | Eres miembro
    suscribeBtnMsg : String = "";

    constructor(private fetchService: FetchService, private userService : UserService, private authService: AuthService,
                private route : ActivatedRoute, private router : Router, private fechaService : FechaService) {

        this.route.params.subscribe((params : Params) => {
            const id = params["id"] as string;
            // if this returns NaN its equal as false
            // console.log(Number(id));
            if (!Number(id))
                this.router.navigate(["/feed"]);

            this.fetchService.fetchStreamById(Number(id)).subscribe({
                next: (stream) => {
                    this.mainVideo = stream;
                    this.mainVideo!.createdAt = this.fechaService.getTimeAgo(stream.createdAt);
                    this.fetchService.fetchAllStreams().subscribe({
                        next: (videos: StreamingInfo[]) => this.videos.set(videos.filter((video) => video.streamId !== this.mainVideo!.streamId)),
                        error: (error: HttpErrorResponse) => console.log("Error ocurred loading videos: ", error),
                    });

                    this.authService.checkUser().pipe(map((data) => data["msg"]))
                        .subscribe((username) => {
                            this.actualUsername = username;

                            this.userService.checkFollows(this.actualUsername, this.mainVideo!.author).subscribe((doesFollow) => {
                                this.actualUserFollowsProfileUser = doesFollow;
                                // console.log("DoesFollow: ", doesFollow);

                                (doesFollow) ? this.followBtnMsg = "Siguiendo" : this.followBtnMsg = "Seguir";
                            });

                            this.userService.checkSuscribed(this.actualUsername, this.mainVideo!.author).subscribe((isSuscribed) => {
                                this.actualUserSuscribedProfileUser = isSuscribed;
                                // console.log("isMember: ", isSuscribed);
                                (isSuscribed) ? this.suscribeBtnMsg = "Eres miembro" : this.suscribeBtnMsg = "Unirme";
                            });
                        });

                    // it needs mainVideo to be returned, so videoPlayer is rendered and not null.
                    console.log("videoRef", this.videoRef);
                    setTimeout(() => this.loadHls(), 3000);
                },
                error: (err: HttpErrorResponse) => {
                    if (err.status === 404) {
                        this.mainVideo = undefined;
                        this.router.navigate(["/feed"]);
                    }
                }
            });

        });

    }
    async ngOnInit() : Promise<void> {
        this.actualUsername  = await firstValueFrom(this.authService.checkUser()).then((data) => data["msg"]);
        // console.log(this.actualUsername);
    }


    onFollowClick(matBtn : MatButton) : void {
        // const btn = matBtn._elementRef.nativeElement as HTMLButtonElement;
        if (this.actualUserFollowsProfileUser) {
            this.userService.unfollowUser(this.actualUsername, this.mainVideo!.author).subscribe({
                next:  () => this.updateFollowerCount(),
                error: () => undefined,
            });
        }
        else {
            this.userService.followUser(this.actualUsername, this.mainVideo!.author).subscribe({
                next:  () => this.updateFollowerCount(),
                error: () => undefined,
            })
        }

        this.actualUserFollowsProfileUser = !this.actualUserFollowsProfileUser;
        (this.actualUserFollowsProfileUser)  ? this.followBtnMsg = "Siguiendo" : this.followBtnMsg = "Seguir";


    }

    updateFollowerCount() : void {
        this.userService.getFollowersOfUsername(this.mainVideo!.author).pipe(map((followersStreamers: FollowersStreamers[]) => followersStreamers.map((followerStreamer: FollowersStreamers) => followerStreamer.follower_id)))
            .subscribe((userIds : number[]) => this.authorUserFollowers = userIds);
    }


    async onSuscribeClick(matBtn : MatButton) : Promise<void> {
        // const btn = matBtn._elementRef.nativeElement as HTMLButtonElement;
        if (this.actualUserSuscribedProfileUser) {
            this.userService.unsuscribeUser(this.actualUsername, this.mainVideo!.author).subscribe(() => this.updateSuscriberCount());
        }
        else {
            this.userService.suscribeUser(this.actualUsername, this.mainVideo!.author).subscribe(() => this.updateSuscriberCount());
        }

        this.actualUserSuscribedProfileUser = !this.actualUserSuscribedProfileUser;
        (this.actualUserSuscribedProfileUser)  ? this.suscribeBtnMsg = "Eres miembro" : this.suscribeBtnMsg = "Unirme";


    }

    updateSuscriberCount() : void {
        this.userService.getSuscribersOfUsername(this.mainVideo!.author).pipe(map((suscribersStreamers: SuscribersStreamers[]) => suscribersStreamers.map((suscriberStreamer: SuscribersStreamers) => suscriberStreamer.suscriber_id)))
            .subscribe((userIds : number[]) => this.authorUserSuscribers = userIds);
    }



    loadHls() {
        console.log("Videoref after: ", this.videoRef);
        const video = this.videoRef?.nativeElement;
        const videoSrc = this.mainVideo!?.streamUrl.replace(
          'https://streams-ivs.s3.eu-west-1.amazonaws.com',
          '/stream-proxy');

        console.log("VideoSrc after replace: ",  videoSrc);

        if (!videoSrc) return;

        if (Hls.isSupported()) {
          const hls = new Hls();
          hls.loadSource(videoSrc);
          hls.attachMedia(video);
        } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
          video.src = videoSrc;
        }
    }

}
