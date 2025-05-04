import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatTabsModule } from '@angular/material/tabs';

import { LandingHeaderComponent } from '../landing-header/landing-header.component';

@Component({
  selector: 'app-landing',
  imports: [LandingHeaderComponent, MatButtonModule, MatCardModule, MatListModule, MatTabsModule],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.css'
})
export class LandingComponent {
    randomVal: number = Math.random() * 4 + 1;


    togglePanel(e: MouseEvent) : void {
        let panel = e.target as HTMLElement;
        panel.children[0].classList.toggle('hidden');
    }
}
