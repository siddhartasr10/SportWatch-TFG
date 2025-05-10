import { Component } from '@angular/core';
import { Route, RouterState } from '@angular/router';

import { LoggedHeaderComponent } from '../../shared/components/logged-header/logged-header.component';

@Component({
  selector: 'app-feed',
  imports: [LoggedHeaderComponent],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.css'
})
export class FeedComponent {
    constructor() {}

}
