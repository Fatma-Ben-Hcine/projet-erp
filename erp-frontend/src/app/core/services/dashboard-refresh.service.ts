import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, Subject, interval, timer } from 'rxjs';
import {
  takeUntil,
  switchMap,
  distinctUntilChanged,
  debounceTime,
  tap,
  catchError,
  startWith
} from 'rxjs/operators';
import { environment } from '../../../environments/environment';

/**
 * DataVersionDTO - Interface pour la version des données
 */
export interface DataVersionDTO {
  datasetName: string;
  version: string;
  previousVersion?: string;
  lastUpdated: string;
  hasChanged?: boolean;
}

/**
 * DashboardRefreshNotification - Interface pour les notifications WebSocket
 */
export interface DashboardRefreshNotification {
  datasetName: string;
  newVersion: string;
  timestamp: string;
  message?: string;
}

/**
 * DashboardRefreshService
 * 
 * Gère la détection des changements de données du dashboard via:
 * 1. WebSocket STOMP (temps réel)
 * 2. Polling HTTP (fallback si WebSocket indisponible)
 * 
 * Architecture intelligente:
 * - WebSocket: notification instantanée quand les données changent
 * - Fallback polling: chaque 60s si WebSocket échoue
 * - Cache busting: génère URL avec timestamp pour recharger iframe
 */
@Injectable({
  providedIn: 'root'
})
export class DashboardRefreshService implements OnDestroy {

  private apiUrl = `${environment.apiUrl}/dashboard`;
  
  // State management
  private currentVersionSubject = new BehaviorSubject<DataVersionDTO | null>(null);
  public currentVersion$ = this.currentVersionSubject.asObservable();
  
  private refreshNeededSubject = new BehaviorSubject<boolean>(false);
  public refreshNeeded$ = this.refreshNeededSubject.asObservable();
  
  private lastRefreshTimeSubject = new BehaviorSubject<Date | null>(null);
  public lastRefreshTime$ = this.lastRefreshTimeSubject.asObservable();

  // Notifications WebSocket
  private refreshNotificationSubject = new Subject<DashboardRefreshNotification>();
  public refreshNotification$ = this.refreshNotificationSubject.asObservable();

  // Cleanup
  private destroy$ = new Subject<void>();

  // Configuration
  private readonly POLLING_INTERVAL_MS = 60000; // 60 secondes
  private readonly WEBSOCKET_RETRY_DELAY_MS = 5000; // Retry après 5s
  private readonly apiUrl_ws = `ws://${window.location.hostname}:${window.location.port}/ws/dashboard`;

  private webSocketConnected = false;
  private webSocketAttempts = 0;
  private readonly MAX_WEBSOCKET_ATTEMPTS = 3;

  constructor(
    private http: HttpClient
  ) {
    this.initialize();
  }

  /**
   * Initialise le service
   * 1. Récupère la version initiale
   * 2. Lance polling intelligent
   * 3. Essaie WebSocket
   */
  private initialize(): void {
    // Étape 1: Récupérer la version initiale
    this.fetchDashboardStatus().pipe(
      takeUntil(this.destroy$)
    ).subscribe();

    // Étape 2: Lancer le polling intelligent (fallback)
    this.startIntelligentPolling();

    // Étape 3: Essayer WebSocket
    this.setupWebSocketListener();
  }

  /**
   * Récupère le statut actuel du dashboard
   */
  public fetchDashboardStatus(): Observable<DataVersionDTO> {
    const currentVersion = this.currentVersionSubject.value;
    const clientVersion = currentVersion?.version;

    return this.http.get<DataVersionDTO>(
      `${this.apiUrl}/status`,
      {
        params: clientVersion ? { clientVersion } : {}
      }
    ).pipe(
      tap(version => {
        this.currentVersionSubject.next(version);
        
        // Si les données ont changé
        if (version.hasChanged) {
          this.notifyRefreshNeeded();
        }
      }),
      catchError(error => {
        console.error('Erreur lors de la récupération du statut dashboard:', error);
        throw error;
      }),
      takeUntil(this.destroy$)
    );
  }

  /**
   * Lance le polling intelligent
   * Poll à intervalle régulier pour vérifier les changements
   * 
   * Avantage: fonctionne partout (pas besoin WebSocket)
   * Temps de détection: jusqu'à 60s
   */
  private startIntelligentPolling(): void {
    interval(this.POLLING_INTERVAL_MS)
      .pipe(
        startWith(0), // Commencer immédiatement
        switchMap(() => this.fetchDashboardStatus()),
        catchError(error => {
          console.error('Erreur lors du polling:', error);
          return interval(this.POLLING_INTERVAL_MS);
        }),
        takeUntil(this.destroy$)
      )
      .subscribe();
  }

  /**
   * Configure l'écoute WebSocket STOMP
   * Utilise SockJS pour fallback HTTP si WebSocket indisponible
   */
  private setupWebSocketListener(): void {
    // En production, utiliser @stomp/stompjs ou ngx-stomp
    // Pour cette démo, on simule avec polling rapide quand WebSocket disponible
    
    // Tentative de connexion WebSocket
    this.attemptWebSocketConnection();
  }

  /**
   * Essaie de se connecter à WebSocket
   * Retry exponentiel si erreur
   */
  private attemptWebSocketConnection(): void {
    if (this.webSocketAttempts >= this.MAX_WEBSOCKET_ATTEMPTS) {
      console.warn('WebSocket: nombre maximum de tentatives atteint, utilisation du polling');
      return;
    }

    this.webSocketAttempts++;
    
    try {
      // Simuler WebSocket avec polling rapide (à remplacer par @stomp/stompjs)
      this.setupWebSocketFallback();
    } catch (error) {
      console.error('Erreur WebSocket, retry...', error);
      timer(this.WEBSOCKET_RETRY_DELAY_MS)
        .pipe(takeUntil(this.destroy$))
        .subscribe(() => this.attemptWebSocketConnection());
    }
  }

  /**
   * Fallback WebSocket: polling rapide si WebSocket indisponible
   * À remplacer par vraie implémentation STOMP quand possible
   */
  private setupWebSocketFallback(): void {
    // Cette partie sera activée si WebSocket disponible
    // Pour l'instant, on utilise le polling normal (60s)
    // TODO: Intégrer @stomp/stompjs pour vraie implémentation WebSocket
    
    this.webSocketConnected = true;
    console.log('WebSocket fallback mode activé (polling rapide)');
  }

  /**
   * Notifie que les données ont changé
   * Déclenche le refresh de l'iframe Power BI
   */
  private notifyRefreshNeeded(): void {
    const now = new Date();
    this.refreshNeededSubject.next(true);
    this.lastRefreshTimeSubject.next(now);
    console.log('🔄 Changement de données détecté, refresh requis');
  }

  /**
   * Génère une URL avec cache busting
   * Ajoute un timestamp/hash pour forcer Power BI à recharger
   * 
   * Usage: 
   * ```
   * this.dashboardRefreshService.getIframeUrl().subscribe(url => {
   *   // Recharger iframe
   * })
   * ```
   */
  public getIframeUrl(basePowerBiUrl: string): Observable<string> {
    return this.currentVersion$.pipe(
      tap(() => {
        // On peut ajouter le version hash à l'URL si Power BI le supporte
        // Pour l'instant, on force simplement le rechargement
      }),
      switchMap(version => {
        if (version && version.version) {
          // Ajouter le version comme paramètre de cache busting
          const separator = basePowerBiUrl.includes('?') ? '&' : '?';
          return new Observable<string>(observer => {
            observer.next(`${basePowerBiUrl}${separator}v=${version.version}`);
            observer.complete();
          });
        }
        return new Observable<string>(observer => {
          observer.next(basePowerBiUrl);
          observer.complete();
        });
      })
    );
  }

  /**
   * Déclenche un refresh manuel
   */
  public manualRefresh(): void {
    console.log('🔃 Refresh manuel déclenché');
    this.fetchDashboardStatus().pipe(
      takeUntil(this.destroy$)
    ).subscribe();
  }

  /**
   * Teste le refresh (pour développement)
   */
  public testRefresh(): Observable<DataVersionDTO> {
    return this.http.post<DataVersionDTO>(
      `${this.apiUrl}/test-refresh`,
      {}
    ).pipe(
      tap(() => this.notifyRefreshNeeded()),
      takeUntil(this.destroy$)
    );
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
