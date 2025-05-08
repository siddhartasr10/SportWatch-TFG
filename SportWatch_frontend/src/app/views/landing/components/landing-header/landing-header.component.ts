import { Component } from '@angular/core';
import { NavbarComponent } from '../../../../shared/components/navbar/navbar.component';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-landing-header',
  imports: [NavbarComponent, RouterLink],
  templateUrl: './landing-header.component.html',
  styleUrl: './landing-header.component.css'
})
export class LandingHeaderComponent {

}
