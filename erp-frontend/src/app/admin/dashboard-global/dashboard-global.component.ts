import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { AdminSidebarComponent } from '../shared/sidebar/sidebar.component';

@Component({
  selector: 'app-dashboard-global',
  standalone: true,
  imports: [CommonModule, AdminSidebarComponent],
  templateUrl: './dashboard-global.component.html',
  styleUrls: ['./dashboard-global.component.css']
})
export class DashboardGlobalComponent implements OnInit, OnDestroy {
  powerBiUrl!: SafeResourceUrl;
  private refreshIntervalId: number | undefined;
  autoRefresh = true;
  readonly refreshIntervalMs = 30000;

  constructor(private sanitizer: DomSanitizer) {}

  ngOnInit(): void {
    this.loadDashboard();
    this.refreshIntervalId = window.setInterval(() => {
      if (this.autoRefresh) {
        this.loadDashboard();
      }
    }, this.refreshIntervalMs);
  }

  ngOnDestroy(): void {
    if (this.refreshIntervalId !== undefined) {
      window.clearInterval(this.refreshIntervalId);
    }
  }

  toggleAutoRefresh(): void {
    this.autoRefresh = !this.autoRefresh;
  }

  manualRefresh(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.powerBiUrl = this.sanitizer.bypassSecurityTrustResourceUrl(
      'https://app.powerbi.com/view?r=eyJrIjoiMGUyYjRkYjYtZWFlZS00YzE4LTgxNGEtMmU1Mzk3OWRjZGZhIiwidCI6ImRiZDY2NjRkLTRlYjktNDZlYi05OWQ4LTVjNDNiYTE1M2M2MSIsImMiOjl9&navContentPaneEnabled=true'
    );
  }
}
