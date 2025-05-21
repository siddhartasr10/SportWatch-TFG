import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-video',
  imports: [RouterLink],
  templateUrl: './video.component.html',
  styleUrl: './video.component.css'
})
export class VideoComponent {
    @Input() videoLink : string = "";
    @Input() videoImgUrl : string = "";
    @Input() profileLink : string = "";
    @Input() profileImgUrl : string = "/shared/default_user.svg";
    @Input() videoTitle : string = "";
    @Input() streamer : string = "";
    @Input() viewers : number = 0;
}
