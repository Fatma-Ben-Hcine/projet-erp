import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ProjetResponse } from '../models/projet.model';

@Injectable({ providedIn: 'root' })
export class EmployeProjetService {
  private baseUrl = `${environment.apiUrl}/employe/projets`;

  constructor(private http: HttpClient) {}

  /**
   * Get all projects assigned to the current employee
   */
  getMesProjets(): Observable<ProjetResponse[]> {
    return this.http.get<ProjetResponse[]>(`${this.baseUrl}/mes-projets`);
  }

  /**
   * Get a specific project by ID (only if assigned to current employee)
   */
  getById(id: number): Observable<ProjetResponse> {
    return this.http.get<ProjetResponse>(`${this.baseUrl}/${id}`);
  }

  /**
   * Check if current user is chef de projet for a specific project
   */
  isChefDeProjet(id: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/${id}/is-chef`);
  }

  /**
   * Deposit a project (file or link) - POST /soumettre avec FormData
   */
  deposerProjet(id: number, depotData: { type: 'lien' | 'fichier', value: string | File }): Observable<ProjetResponse> {
    console.log('=== deposerProjet ===');
    console.log('depotData:', depotData);
    console.log('depotData.type:', depotData?.type);

    // Validation
    if (!depotData || !depotData.type) {
      console.error('ERREUR: depotData.type est manquant!');
      throw new Error('Type de dépôt non défini');
    }

    const formData = new FormData();
    formData.append('type', depotData.type);
    console.log('FormData: type ajouté =', depotData.type);

    if (depotData.type === 'fichier' && depotData.value instanceof File) {
      formData.append('fichier', depotData.value);
      console.log('FormData: fichier ajouté =', depotData.value.name);
    } else if (depotData.type === 'lien' && depotData.value) {
      formData.append('lien', String(depotData.value));
      console.log('FormData: lien ajouté =', depotData.value);
    }

    // Vérification finale
    console.log('=== FormData entries ===');
    formData.forEach((value, key) => {
      console.log(key, ':', value);
    });

    return this.http.post<ProjetResponse>(`${this.baseUrl}/${id}/soumettre`, formData);
  }

  /**
   * Update project status
   */
  updateStatut(id: number, statut: string): Observable<ProjetResponse> {
    return this.http.patch<ProjetResponse>(`${this.baseUrl}/${id}/statut`, { statut });
  }
}
