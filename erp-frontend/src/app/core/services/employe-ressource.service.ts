import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Ressource } from '../models/ressource.model';

@Injectable({
  providedIn: 'root'
})
export class EmployeRessourceService {
  private baseUrl = '/api/employe/ressources';

  constructor(private http: HttpClient) {}

  // Voir toutes les ressources ACTIVES uniquement
  getRessourcesActives(): Observable<Ressource[]> {
    return this.http.get<Ressource[]>(this.baseUrl);
  }

  // Demander une ressource (cocher)
  demanderRessource(id: number): Observable<{message: string}> {
    return this.http.post<{message: string}>(`${this.baseUrl}/${id}/demander`, {});
  }

  // Annuler sa propre demande
  annulerDemande(id: number): Observable<{message: string}> {
    return this.http.delete<{message: string}>(`${this.baseUrl}/${id}/annuler`);
  }

  // Voir ses propres demandes
  getMesDemandes(): Observable<Ressource[]> {
    return this.http.get<Ressource[]>(`${this.baseUrl}/mes-demandes`);
  }
}
