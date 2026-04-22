import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HeureSupplementaire, HeureSupplementaireRequest, StatutHeureSupplementaire } from '../models/heure-supplementaire.model';

@Injectable({
  providedIn: 'root'
})
export class HeureSupplementaireService {
  private readonly apiUrl = 'http://localhost:8080/api/heures-supplementaires';

  constructor(private http: HttpClient) {}

  // CRUD operations
  getAll(): Observable<HeureSupplementaire[]> {
    return this.http.get<HeureSupplementaire[]>(this.apiUrl);
  }

  getById(id: number): Observable<HeureSupplementaire> {
    return this.http.get<HeureSupplementaire>(`${this.apiUrl}/${id}`);
  }

  getByEmployeId(employeId: number): Observable<HeureSupplementaire[]> {
    return this.http.get<HeureSupplementaire[]>(`${this.apiUrl}/employe/${employeId}`);
  }

  create(heureSupplementaire: HeureSupplementaireRequest): Observable<HeureSupplementaire> {
    return this.http.post<HeureSupplementaire>(this.apiUrl, heureSupplementaire);
  }

  update(id: number, heureSupplementaire: HeureSupplementaireRequest): Observable<HeureSupplementaire> {
    return this.http.put<HeureSupplementaire>(`${this.apiUrl}/${id}`, heureSupplementaire);
  }

  delete(id: number): Observable<string> {
    return this.http.delete<string>(`${this.apiUrl}/${id}`);
  }

  // Status operations
  getByStatut(statut: StatutHeureSupplementaire): Observable<HeureSupplementaire[]> {
    return this.http.get<HeureSupplementaire[]>(`${this.apiUrl}/statut/${statut}`);
  }

  approuver(id: number): Observable<HeureSupplementaire> {
    return this.http.put<HeureSupplementaire>(`${this.apiUrl}/${id}/approuver`, {});
  }

  refuser(id: number): Observable<HeureSupplementaire> {
    return this.http.put<HeureSupplementaire>(`${this.apiUrl}/${id}/refuser`, {});
  }

  // Helper methods for UI
  getStatutLabel(statut: StatutHeureSupplementaire): string {
    switch (statut) {
      case StatutHeureSupplementaire.EN_ATTENTE:
        return 'En Attente';
      case StatutHeureSupplementaire.APPROUVEE:
        return 'Approuvée';
      case StatutHeureSupplementaire.REFUSEE:
        return 'Refusée';
      default:
        return statut;
    }
  }

  getStatutColor(statut: StatutHeureSupplementaire): string {
    switch (statut) {
      case StatutHeureSupplementaire.EN_ATTENTE:
        return 'warning';
      case StatutHeureSupplementaire.APPROUVEE:
        return 'success';
      case StatutHeureSupplementaire.REFUSEE:
        return 'danger';
      default:
        return 'info';
    }
  }

  getStatutOptions(): { value: StatutHeureSupplementaire; label: string }[] {
    return [
      { value: StatutHeureSupplementaire.EN_ATTENTE, label: 'En Attente' },
      { value: StatutHeureSupplementaire.APPROUVEE, label: 'Approuvée' },
      { value: StatutHeureSupplementaire.REFUSEE, label: 'Refusée' }
    ];
  }
}
