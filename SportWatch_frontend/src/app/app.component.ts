import { Component } from '@angular/core';
import { RouterOutlet, RouterModule } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: 'app.component.html',
})

export class AppComponent {
  title = 'SportWatch_frontend';
}
