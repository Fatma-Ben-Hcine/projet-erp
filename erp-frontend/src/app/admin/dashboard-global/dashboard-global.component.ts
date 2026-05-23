import { Component, OnDestroy, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { AdminSidebarComponent } from '../shared/sidebar/sidebar.component';
import { DashboardVersionService } from '../../core/services/dashboard-version.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-dashboard-global',
  standalone: true,
  imports: [CommonModule, AdminSidebarComponent],
  templateUrl: './dashboard-global.component.html',
  styleUrls: ['./dashboard-global.component.css'],
})
export class DashboardGlobalComponent implements OnInit, OnDestroy {
  @ViewChild('powerBiIframe') iframeElement!: ElementRef<HTMLIFrameElement>;

  // Power BI URL
  powerBiUrl!: SafeResourceUrl;
  private readonly POWERBI_BASE_URL =
    'https://app.powerbi.com/view?r=eyJrIjoiMGUyYjRkYjYtZWFlZS00YzE4LTgxNGEtMmU1Mzk3OWRjZGZhIiwidCI6ImRiZDY2NjRkLTRlYjktNDZlYi05OWQ4LTVjNDNiYTE1M2M2MSIsImMiOjl9&navContentPaneEnabled=true';

  isRefreshing = false;
  lastRefreshTime: Date | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private sanitizer: DomSanitizer,
    private dashboardVersionService: DashboardVersionService
  ) {}

  ngOnInit(): void {
    // Charger le dashboard initial
    this.loadDashboard();

    // S'abonner aux changements de version
    this.dashboardVersionService
      .getRefreshNeeded()
      .pipe(takeUntil(this.destroy$))
      .subscribe((needsRefresh) => {
        if (needsRefresh) {
          this.refreshDashboard();
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Charge le dashboard initial
   */
  loadDashboard(): void {
    this.powerBiUrl = this.sanitizer.bypassSecurityTrustResourceUrl(
      this.POWERBI_BASE_URL
    );
  }

  /**
   * Recharge l'iframe avec cache-busting
   * Power BI will fetch fresh data from the dataset
   */
  private refreshDashboard(): void {
    if (this.isRefreshing) {
      console.log('Refresh déjà en cours...');
      return;
    }

    this.isRefreshing = true;
    console.log('🔄 Recharge du dashboard Power BI');

    if (this.iframeElement) {
      const iframe = this.iframeElement.nativeElement;
      const timestamp = Date.now();
      const urlWithCacheBusting = `${this.POWERBI_BASE_URL}&t=${timestamp}`;

      this.powerBiUrl = this.sanitizer.bypassSecurityTrustResourceUrl(
        urlWithCacheBusting
      );

      // Recharger l'iframe
      setTimeout(() => {
        iframe.src = iframe.src;
        this.lastRefreshTime = new Date();
      }, 100);

      // Réinitialiser le flag
      setTimeout(() => {
        this.isRefreshing = false;
        this.dashboardVersionService.markRefreshDone();
      }, 500);
    } else {
      this.isRefreshing = false;
    }
  }

  /**
   * Refresh manuel
   */
  manualRefresh(): void {
    console.log('🔄 Vérification des changements');
    this.dashboardVersionService
      .checkVersion()
      .subscribe({
        next: () => {
          console.log('✓ Vérification complète');
        },
        error: (err) => {
          console.error('❌ Erreur lors de la vérification:', err);
        },
      });
  }
}
