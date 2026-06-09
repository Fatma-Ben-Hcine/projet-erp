import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

/**
 * DashboardVersionService
 * Solution SIMPLE et LÉGÈRE pour Power BI Free
 * 
 * NO automatic polling - manual check only via manualCheck()
 * L'utilisateur clique sur "Actualiser" pour vérifier les changements
 */
@Injectable({ providedIn: 'root' })
export class DashboardVersionService {
  private lastVersion = 0;
  private refreshNeeded$ = new BehaviorSubject<boolean>(false);

  constructor(private http: HttpClient) {}

  /**
   * Vérification manuelle de la version
   * À appeler quand l'utilisateur clique sur "Actualiser"
   */
  checkVersion(): Observable<number> {
    return this.http.get<number>(`${environment.apiUrl}/dashboard/version`).pipe(
      tap((newVersion) => {
        // Si la version a changé → besoin de refresh
        if (newVersion !== this.lastVersion) {
          console.log(
            `📊 Version du dashboard changée: ${this.lastVersion} → ${newVersion}`
          );
          this.lastVersion = newVersion;
          this.refreshNeeded$.next(true);
        }
      })
    );
  }

  /**
   * Observable indiquant si l'iframe doit se recharger
   */
  getRefreshNeeded() {
    return this.refreshNeeded$.asObservable();
  }

  /**
   * Réinitialise le flag après un refresh
   */
  markRefreshDone() {
    this.refreshNeeded$.next(false);
  }
}
