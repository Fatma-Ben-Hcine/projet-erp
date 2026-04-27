import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { 
  TacheRequest, 
  TacheResponse, 
  AssignEmployeToTacheRequest,
  TacheProgressionResponse 
} from '../models/activite.model';

@Injectable({ providedIn: 'root' })
export class TacheService {
  private baseUrl = `${environment.apiUrl}/admin/taches`;

  constructor(private http: HttpClient) {}

  // CRUD Tâches
  getAll(): Observable<TacheResponse[]> {
    return this.http.get<TacheResponse[]>(this.baseUrl);
  }

  getById(id: number): Observable<TacheResponse> {
    return this.http.get<TacheResponse>(`${this.baseUrl}/${id}`);
  }

  getByActivite(activiteId: number): Observable<TacheResponse[]> {
    return this.http.get<TacheResponse[]>(`${this.baseUrl}/activite/${activiteId}`);
  }

  getByEmploye(employeId: number): Observable<TacheResponse[]> {
    return this.http.get<TacheResponse[]>(`${this.baseUrl}/employe/${employeId}`);
  }

  create(request: TacheRequest): Observable<TacheResponse> {
    return this.http.post<TacheResponse>(this.baseUrl, request);
  }

  update(id: number, request: TacheRequest): Observable<TacheResponse> {
    return this.http.put<TacheResponse>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`, { responseType: 'text' });
  }

  // Gestion des employés assignés aux tâches
  assignEmploye(tacheId: number, employeId: number, request: AssignEmployeToTacheRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/${tacheId}/employes/${employeId}`, request);
  }

  unassignEmploye(tacheId: number, employeId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${tacheId}/employes/${employeId}`, { responseType: 'text' });
  }

  updateEmployeStatut(tacheId: number, employeId: number, statut: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/${tacheId}/employes/${employeId}/statut`, { statut });
  }

  // Statistiques et progression
  getProgression(tacheId: number): Observable<TacheProgressionResponse> {
    return this.http.get<TacheProgressionResponse>(`${this.baseUrl}/${tacheId}/progression`);
  }

  getCountByActivite(activiteId: number): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.baseUrl}/activite/${activiteId}/count`);
  }

  getTermineesByEmploye(employeId: number): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.baseUrl}/employe/${employeId}/terminees/count`);
  }

  // Dépôt de tâche
  deposerTache(id: number, depotData: { type: 'lien' | 'fichier', value: string | File }): Observable<TacheResponse> {
    const url = `${this.baseUrl}/${id}/depot`;
    console.log('>>> tacheService.deposerTache - URL:', url, 'id:', id);
    
    const formData = new FormData();
    formData.append('type', depotData.type);

    if (depotData.type === 'lien') {
      formData.append('lien', depotData.value as string);
    } else if (depotData.type === 'fichier') {
      formData.append('file', depotData.value as File);
      formData.append('nomFichier', (depotData.value as File).name);
    }

    return this.http.patch<TacheResponse>(url, formData);
  }

  // Vérification de dépôt
  hasDepot(id: number): Observable<{ hasDepot: boolean; tacheId: number }> {
    return this.http.get<{ hasDepot: boolean; tacheId: number }>(`${this.baseUrl}/${id}/depot-exists`);
  }
}
