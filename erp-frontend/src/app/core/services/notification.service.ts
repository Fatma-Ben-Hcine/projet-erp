import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Notification } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private apiUrl = `${environment.apiUrl}/notifications`;

  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  private countNonLuesSubject = new BehaviorSubject<number>(0);

  public notifications$ = this.notificationsSubject.asObservable();
  public countNonLues$ = this.countNonLuesSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Initialiser le service (polling uniquement)
   */
  connectWebSocket(employeId: number): void {
    this.loadNotifications();
    this.loadCountNonLues();
  }

  /**
   * Déconnecter (vide car pas de WebSocket)
   */
  disconnectWebSocket(): void {
    // Pas de WebSocket à déconnecter
  }

  /**
   * Récupérer toutes les notifications
   */
  getNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(this.apiUrl);
  }

  /**
   * Compter les notifications non lues
   */
  getCountNonLues(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.apiUrl}/non-lues/count`);
  }

  /**
   * Marquer une notification comme lue
   */
  marquerLue(id: number): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/lue`, {});
  }

  /**
   * Marquer toutes les notifications comme lues
   */
  marquerToutesLues(): Observable<any> {
    return this.http.patch(`${this.apiUrl}/tout-lire`, {});
  }

  /**
   * Charger les notifications et mettre à jour les sujets
   */
  loadNotifications(): void {
    this.getNotifications().pipe(
      map((data: any) => {
        // Si le backend retourne { count: number } au lieu de Notification[]
        if (data && typeof data === 'object' && !Array.isArray(data) && 'count' in data) {
          return [];
        }
        // Si data est un objet avec une propriété data (wrapping)
        if (data && typeof data === 'object' && !Array.isArray(data) && 'data' in data) {
          return data.data || [];
        }
        return Array.isArray(data) ? data : [];
      })
    ).subscribe({
      next: (notifications: Notification[]) => {
        this.notificationsSubject.next(notifications);
        this.countNonLuesSubject.next(notifications.filter((n) => !n.estLue).length);
      },
      error: (err) => {
        console.error('Erreur chargement notifications:', err);
      },
    });
  }

  /**
   * Charger uniquement le compteur (polling)
   */
  loadCountNonLues(): void {
    this.getCountNonLues().subscribe({
      next: (res: any) => {
        const count = typeof res === 'object' && res !== null ? (res.count || 0) : 0;
        this.countNonLuesSubject.next(count);
      },
      error: (err) => {
        console.error('Erreur chargement compteur:', err);
      },
    });
  }
}
