import { Component, WritableSignal, signal, } from '@angular/core';
import { Router } from '@angular/router';

import { MatChipListbox, MatChipOption, MatChipsModule } from '@angular/material/chips';

import { LoggedHeaderComponent } from '../../shared/components/logged-header/logged-header.component';
import { VideoComponent } from '../../shared/components/video/video.component';
import { StreamingInfo } from '../../shared/interfaces/StreamingInfo';
// import { UploadService } from '../../shared/services/upload-service/upload-service.service';
import { FetchService } from '../../shared/services/fetch-service/fetch-service.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-feed',
  imports: [LoggedHeaderComponent, MatChipsModule, VideoComponent],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.css'
})
export class FeedComponent {
    constructor(private router : Router, private fetchService : FetchService ) {
        this.loadVideos();
    }

    search : WritableSignal<string> = signal('');
    selectedChips : WritableSignal<string[]> = signal([]);

    videos : WritableSignal<StreamingInfo[]> = signal([]);
    filteredVideos : WritableSignal<StreamingInfo[]> = signal([]);


    // Me podría pasar los parámetros si quisiera del valor del .value del input si sacara el otro componente pero no sería escalable para funcionar en todos los componentes.
    onSearch(searchQuery : string) {
        this.search.set(searchQuery);
        this.filteredVideos.set(this.filterBySearch(this.videos()));
        this.filteredVideos.set(this.filterByCategory(this.filteredVideos()));
        // Después del search habría un get a la api y luego el @for se encargaría del resto.

    }

    loadVideos() : void {
        this.fetchService.fetchAllStreams().subscribe({
            next: (videos : StreamingInfo[]) => this.videos.set(videos),
            error: (error : HttpErrorResponse) => console.log("Error ocurred loading videos: ", error),
            complete: () => {
                this.filteredVideos.set(this.filterBySearch(this.videos()));
                this.filteredVideos.set(this.filterByCategory(this.filteredVideos()));
            }
        });

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
        // Me imagino que aquí filtraría la vista o llamaría a un filter con los videos que hay.
    }

    filterByCategory(videos : StreamingInfo[]) : StreamingInfo[] {
        if (this.selectedChips().length === 0) return videos;
        return videos.filter(video => this.selectedChips().filter(category => (video.category.toLowerCase()) === category.toLowerCase()).length !== 0);
    }

    filterBySearch(videos : StreamingInfo[]) : StreamingInfo[] {
        if (this.search() === "") return videos;
        return videos.filter(video => this.search()!.split(" ")!.filter(word => new RegExp(`\\b${word}\s`, "i").test(video.title)).length !== 0)
    }

}
