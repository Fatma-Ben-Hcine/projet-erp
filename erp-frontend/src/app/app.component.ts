import { Component, OnInit } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HexagonBackgroundComponent } from './shared/components/hexagon-background/hexagon-background.component';
import { HeaderComponent } from './shared/components/header/header.component';
import { AuthService } from './auth/auth.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HexagonBackgroundComponent, HeaderComponent, CommonModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'erp-frontend';
  showHeader = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Check if header should be shown on route changes
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        const currentUrl = event.url;
        // Don't show header on login, forgot-password, reset-password pages
        this.showHeader = !(
          currentUrl.includes('/login') ||
          currentUrl.includes('/forgot-password') ||
          currentUrl.includes('/reset-password')
        ) && this.authService.isLoggedIn();
      });
  }
}
