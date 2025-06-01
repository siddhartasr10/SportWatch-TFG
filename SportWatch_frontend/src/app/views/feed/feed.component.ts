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

    search : WritableSignal<string | null> = signal('');
    selectedChips : WritableSignal<string[]> = signal([]);

    videos : WritableSignal<StreamingInfo[]> = signal([]);

    // Me podría pasar los parámetros si quisiera del valor del .value del input si sacara el otro componente pero no sería escalable para funcionar en todos los componentes.
    onSearch(searchQuery : string) {
        this.search.set(searchQuery);

        // Después del search habría un get a la api y luego el @for se encargaría del resto.

    }

    loadVideos() : void {
        this.fetchService.fetchAllStreams().subscribe({
            next: (videos : StreamingInfo[]) => this.videos.set(videos),
            error: (error : HttpErrorResponse) => console.log("Error ocurred loading videos: ", error),
            complete: () => console.log(this.videos()),
        });
    }

    updateSelectedChips(chipList : MatChipListbox) {
        let newChips = chipList.selected as MatChipOption[];

        this.selectedChips.update( currentChips => {
            // Vacio las chips que hayan.
            currentChips.splice(0, currentChips.length);
            for (let chip of newChips) {
                if (!chip.selected) continue;
                currentChips.push(chip.value);
            }

            return currentChips;
        });

        console.log("Selected Chips:", this.selectedChips());
        // Me imagino que aquí filtraría la vista o llamaría a un filter con los videos que hay.
    }

}
