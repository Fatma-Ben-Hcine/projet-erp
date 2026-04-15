import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HexagonBackgroundComponent } from './shared/components/hexagon-background/hexagon-background.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HexagonBackgroundComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'erp-frontend';
}
