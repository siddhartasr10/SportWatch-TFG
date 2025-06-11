import { Component, WritableSignal, signal, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { firstValueFrom } from 'rxjs';

import { MatChipListbox, MatChipOption, MatChipsModule } from '@angular/material/chips';

import { LoggedHeaderComponent } from '../../shared/components/logged-header/logged-header.component';
import { VideoComponent } from '../../shared/components/video/video.component';
import { StreamingInfo } from '../../shared/interfaces/StreamingInfo';
// import { UploadService } from '../../shared/services/upload-service/upload-service.service';
import { FetchService } from '../../shared/services/fetch-service/fetch-service.service';
import { HttpErrorResponse } from '@angular/common/http';
import { FechaService } from '../../shared/services/fecha-service/fecha-service.service';
import { StreamService } from '../../shared/services/stream-service/stream-service.service';

@Component({
  selector: 'app-feed',
  imports: [LoggedHeaderComponent, MatChipsModule, VideoComponent],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.css'
})
export class FeedComponent {
    constructor(private router : Router, private fetchService : FetchService, private route : ActivatedRoute,
                public fechaService : FechaService, private streamService: StreamService) {

        this.route.queryParams.subscribe((params) => {
            console.log(params);
            this.search = signal(params["s"]);
        });
    }

    search : WritableSignal<string> = signal('');
    selectedChips : WritableSignal<string[]> = signal([]);

    videos : WritableSignal<StreamingInfo[]> = signal([]);
    filteredVideos : WritableSignal<StreamingInfo[]> = signal([]);

    async ngOnInit() {
        // this.loadVideos();
        // console.log("videos in this moment:", this.videos());
        // let videosCpy = {...this.videos()};
        // for (let video of videosCpy) {
        //     let videoCpy = {...video};
        //     videoCpy.viewerCount = await firstValueFrom(this.streamService.getStreamViews(videoCpy.streamId));
        // }

        // return videosCpy;
        await this.loadVideos();
    }

    // Me podría pasar los parámetros si quisiera del valor del .value del input si sacara el otro componente pero no sería escalable para funcionar en todos los componentes.
    onSearch(searchQuery : WritableSignal<string>) {
        this.search = searchQuery;
        this.filteredVideos.set(this.filterBySearch(this.videos()));
        this.filteredVideos.set(this.filterByCategory(this.filteredVideos()));
        // Después del search habría un get a la api y luego el @for se encargaría del resto.

    }

    async loadVideos() : Promise<void> {
        let videos = await firstValueFrom(this.fetchService.fetchAllStreams());

        let videosCpy = [...videos];
        for (let video of videosCpy) {
            video.viewerCount = await firstValueFrom(this.streamService.getStreamViews(video.streamId));;
        }

        this.videos.set(videosCpy);

        console.log("current search:", this.search());
        console.log("Videos before search filtering: ", this.videos());
        this.filteredVideos.set(this.filterBySearch(this.videos()));

        console.log("Videos after search filtering: ", this.filteredVideos());
        this.filteredVideos.set(this.filterByCategory(this.filteredVideos()));

    }

    updateSelectedChips(chipList : MatChipListbox) {
        let newChips = chipList.selected as MatChipOption[];

        this.selectedChips.update(currentChips => {
            // Vacio las chips que hayan.
            currentChips.splice(0, currentChips.length);
            for (let chip of newChips) {
                if (!chip.selected) continue;
                currentChips.push(chip.value);
            }


            return currentChips;
        });

        this.filteredVideos.set(this.filterByCategory(this.videos()));
        this.filteredVideos.set(this.filterBySearch(this.filteredVideos()));
        // usaria update en vez de set pero no se porque no funciona.
        }


    filterByCategory(videos: StreamingInfo[]) : StreamingInfo[] {
        if (this.selectedChips().length === 0) return videos;
        return videos.filter(video => this.selectedChips().filter(category => (video.category.toLowerCase()) === category.toLowerCase()).length !== 0);
    }

    filterBySearch(videos : StreamingInfo[]) : StreamingInfo[] {
        if (this.search() === "") return videos;
        const searchWords : string[] = this.search()?.split(" ");

        return videos?.filter(video => searchWords?.filter(word => new RegExp(`${word}`, "i").test(video.title)).length !== 0);
    }

}
