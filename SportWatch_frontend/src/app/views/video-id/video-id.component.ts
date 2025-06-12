import { Component, ElementRef, Signal, ViewChild, WritableSignal, computed, signal } from '@angular/core';
import { ActivatedRoute, Params, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom, forkJoin, map } from 'rxjs';
import  Hls from 'hls.js';

import { StreamingInfo } from '../../shared/interfaces/StreamingInfo';
import { FollowersStreamers, SuscribersStreamers } from '../../shared/interfaces/FollowsSuscribes';
import { Comment as StreamComment }  from '../../shared/interfaces/Comment';

import { FetchService } from '../../shared/services/fetch-service/fetch-service.service';
import { UserService } from '../../shared/services/user-service/user-service.service';

import { LoggedHeaderComponent } from '../../shared/components/logged-header/logged-header.component';
import { VideoComponent } from '../../shared/components/video/video.component';
import { AuthService } from '../../shared/services/auth-service/auth-service.service';
import { MatButton, MatButtonModule } from '@angular/material/button';
import { FechaService } from '../../shared/services/fecha-service/fecha-service.service';
import { CommentService } from '../../shared/services/comment-service/comment-service.service';
import { StreamService } from '../../shared/services/stream-service/stream-service.service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-video-id',
  imports: [LoggedHeaderComponent, VideoComponent, CommonModule, RouterLink, MatButtonModule, MatButton, MatFormFieldModule, ReactiveFormsModule, MatInputModule],
  templateUrl: './video-id.component.html',
  styleUrl: './video-id.component.css'
})
export class VideoIdComponent {
    @ViewChild('videoPlayer') videoRef!: ElementRef<HTMLVideoElement>;
    mainVideo! : StreamingInfo | undefined;

    actualUsername! : string;
    actualUserFollowsProfileUser!: boolean;
    actualUserSuscribedProfileUser!: boolean;

    videoId!: number;
    videos : WritableSignal<StreamingInfo[]> = signal([]);
    comments : WritableSignal<StreamComment[]> = signal([]);

    inputComment = new FormControl("");

    authorUserFollowers!: number[];
    authorUserSuscribers!: number[];

    // Seguir | Siguiendo
    followBtnMsg : String = "";
    // Unirme | Eres miembro
    suscribeBtnMsg : String = "";

    constructor(private fetchService: FetchService, private userService : UserService, private authService: AuthService, private streamService: StreamService,
                private route : ActivatedRoute, private router : Router, public fechaService : FechaService, private commentService: CommentService) {

        this.route.params.subscribe((params : Params) => {
            const id = params["id"] as string;
            // if this returns NaN its equal as false
            // console.log(Number(id));
            if (!Number(id))
                this.router.navigate(["/feed"]);

            this.videoId = Number(id);

            this.fetchService.fetchStreamById(this.videoId).subscribe({
                next: (stream) => {
                    this.mainVideo = stream;
                    this.mainVideo!.createdAt = this.fechaService.getTimeAgo(stream.createdAt);
                    this.streamService.getStreamViews(this.mainVideo.streamId).subscribe((views) => this.mainVideo!.viewerCount = views);

                    this.loadComments();
                    // Añado el view al stream si no lo tiene.
                    this.streamService.viewStream(stream.streamId).subscribe();
                    this.fetchService.fetchAllStreams().subscribe({
                        next: (videos: StreamingInfo[]) => {
                            this.videos.set(videos.filter((video) => video.streamId !== this.mainVideo!.streamId))
                            this.videos.update(videos => {
                                for (let video of videos) {
                                    this.streamService.getStreamViews(video.streamId).subscribe((views) => video.viewerCount= views)
                                }
                                return videos
                            });

                        },
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

    loadComments(): void {
        console.log("ena");
        this.commentService.listStreamComments(this.mainVideo!.streamId).subscribe({
            next: (comments: StreamComment[]) => {
                console.log("Comments", comments);
                this.comments.set(comments);

                if (!comments || comments.length === 0) return;

                const updatedComments$ = forkJoin(
                    comments.map(comment => this.userService.getUserById(comment.authorId!).pipe(
                            map(user => ({
                                ...comment,
                                author: user.username
                            }))
                        )
                    )
                );

                updatedComments$.subscribe(updatedComments => {
                    console.log("Updated Comments", updatedComments);
                    this.comments.set(updatedComments);
                });
            }
        });
    }

   createComment(ev: Event) {
        const inputEl = ev.target as HTMLInputElement;
        console.log("Stream id en el momento de inputear un comment:", this.videoId);
        let comment: StreamComment = {
            streamId: this.videoId,
            comment: inputEl.value,
        }

        // We only update db when you create a new comment,
        // if user wants to see new comments he has to reload the page,
        // polling comments is expensive and not needed.

        this.commentService.createComment(comment).subscribe(() => {
            this.loadComments();
        });

        inputEl.value = "";

    }
    // deleting takes time so to avoid race conditions.
    // that make the object doesnt get well send so no delete happens on the backend.
    deleteComment(ev: MouseEvent, comment: StreamComment) {
        console.log("Following comment is going to be deleted:" ,comment)

        this.comments.update(comments => {
            let commentIdx = comments.findIndex(comment => comment.commentId == comment.commentId);
            comments.splice(commentIdx, commentIdx + 1);
            // copiar por si acaso requiere nueva referencia.
            return comments;
        });

        this.commentService.deleteComment(comment.commentId!).subscribe();

    }

    editingComment: StreamComment | null = null;
    startEditingComment(comment: StreamComment) {
        this.editingComment = comment;
    }

    updateEditedComment(msg: string) {
        this.editingComment!.comment = msg;
        this.comments.update(comments => {
            let commentIdx = comments.findIndex(comment => comment.commentId == this.editingComment!.commentId);
            comments[commentIdx] = {...this.editingComment!};
            return [...comments];
        })

        this.commentService.updateCommentMsg(this.editingComment!.commentId!, msg).subscribe();
        this.editingComment = null;
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
