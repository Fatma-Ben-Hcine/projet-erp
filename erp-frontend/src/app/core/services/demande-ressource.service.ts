import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DemandeRessource, DemandeRessourceRequest, DemandeMultipleRequest } from '../models/ressource.model';

@Injectable({
  providedIn: 'root'
})
export class DemandeRessourceService {
  private apiUrl = `${environment.apiUrl}/demandes-ressources`;

  constructor(private http: HttpClient) {}

  // ========================
  // EMPLOYÉ
  // ========================

  createDemande(request: DemandeRessourceRequest): Observable<DemandeRessource> {
    return this.http.post<DemandeRessource>(this.apiUrl, request);
  }

  createDemandesMultiples(request: DemandeMultipleRequest): Observable<any> {
    return this.http.post<any>(this.apiUrl, request);
  }

  annulerDemande(ressourceId: number): Observable<any> {
    return this.http.delete<any>(`${environment.apiUrl}/employe/ressources/${ressourceId}/annuler`);
  }

  getMesDemandes(employeId: number): Observable<DemandeRessource[]> {
    return this.http.get<DemandeRessource[]>(`${this.apiUrl}/employe/${employeId}`);
  }

  // ========================
  // ADMIN
  // ========================

  getAllDemandes(): Observable<DemandeRessource[]> {
    return this.http.get<DemandeRessource[]>(this.apiUrl);
  }

  getDemandesNonTraitees(): Observable<DemandeRessource[]> {
    return this.http.get<DemandeRessource[]>(`${this.apiUrl}/non-traitees`);
  }

  marquerTraitee(id: number): Observable<DemandeRessource> {
    return this.http.put<DemandeRessource>(`${this.apiUrl}/${id}/traiter`, {});
  }

  deleteDemande(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
