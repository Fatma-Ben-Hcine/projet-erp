import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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
   * Deposit a project (file or link)
   */
  deposerProjet(id: number, depotData: { type: 'lien' | 'fichier', value: string | File }): Observable<ProjetResponse> {
    const formData = new FormData();
    formData.append('type', depotData.type);

    if (depotData.type === 'lien') {
      formData.append('lien', depotData.value as string);
    } else if (depotData.type === 'fichier') {
      formData.append('file', depotData.value as File);
      formData.append('nomFichier', (depotData.value as File).name);
    }

    return this.http.patch<ProjetResponse>(`${this.baseUrl}/${id}/depot`, formData);
  }
}
