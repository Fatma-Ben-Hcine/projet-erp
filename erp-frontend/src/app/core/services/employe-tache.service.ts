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
export class EmployeTacheService {
  private baseUrl = `${environment.apiUrl}/employe/taches`;

  constructor(private http: HttpClient) {}

  // CRUD Tâches - Read only for employees
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

  // Create/Update/Delete - requires chef de projet role (backend will check)
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

  // Dépôt de tâche - POST /soumettre pour éviter les problèmes de PATCH
  deposerTache(id: number, depotData: { type: 'lien' | 'fichier', value: string | File }): Observable<TacheResponse> {
    const url = `${this.baseUrl}/${id}/soumettre`;

    // Log pour débugger
    console.log('=== deposerTache ===');
    console.log('depotData reçu:', depotData);
    console.log('depotData.type:', depotData?.type);
    console.log('depotData.value:', depotData?.value);
    console.log('depotData.value instanceof File:', depotData?.value instanceof File);

    // Validation
    if (!depotData || !depotData.type) {
      console.error('ERREUR: depotData.type est manquant!');
      throw new Error('Type de dépôt non défini');
    }

    // Toujours utiliser FormData (multipart)
    const formData = new FormData();

    // ⚠️ OBLIGATOIRE — ajouter 'type' en premier
    formData.append('type', depotData.type);
    console.log('FormData: type ajouté =', depotData.type);

    // Ajouter fichier ou lien selon le type
    if (depotData.type === 'fichier' && depotData.value instanceof File) {
      formData.append('fichier', depotData.value);
      console.log('FormData: fichier ajouté =', depotData.value.name);
    } else if (depotData.type === 'lien' && depotData.value) {
      formData.append('lien', String(depotData.value));
      console.log('FormData: lien ajouté =', depotData.value);
    }

    // Vérification finale du FormData
    console.log('=== FormData entries ===');
    formData.forEach((value, key) => {
      console.log(key, ':', value);
    });

    // POST au lieu de PATCH - évite les problèmes de parsing Spring
    // ⚠️ Pas de headers — Angular gère Content-Type automatiquement
    return this.http.post<TacheResponse>(url, formData);
  }

  // Vérification de dépôt
  hasDepot(id: number): Observable<{ hasDepot: boolean; tacheId: number }> {
    return this.http.get<{ hasDepot: boolean; tacheId: number }>(`${this.baseUrl}/${id}/depot-exists`);
  }
}
