import { Component, WritableSignal, signal, } from '@angular/core';
import { Router } from '@angular/router';

import { MatChipListbox, MatChipOption, MatChipsModule } from '@angular/material/chips';

import { LoggedHeaderComponent } from '../../shared/components/logged-header/logged-header.component';
import { VideoComponent } from '../../shared/components/video/video.component';
import { Streaming } from '../../shared/interfaces/Streaming';

@Component({
  selector: 'app-feed',
  imports: [LoggedHeaderComponent, MatChipsModule, VideoComponent],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.css'
})
export class FeedComponent {
    constructor(router : Router) {}

    search : WritableSignal<string | null> = signal('');
    selectedChips : WritableSignal<string[]> = signal([]);

    videos : WritableSignal<Streaming[]> = signal([]);

    // Me podría pasar los parámetros si quisiera del valor del .value del input si sacara el otro componente pero no sería escalable para funcionar en todos los componentes.
    onSearch(searchQuery : string) {
        this.search.set(searchQuery);

        // Después del search habría un get a la api y luego el @for se encargaría del resto.

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
