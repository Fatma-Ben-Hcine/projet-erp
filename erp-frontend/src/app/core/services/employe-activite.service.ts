import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { 
  ActiviteRequest, 
  ActiviteResponse, 
  AssignEmployeToActiviteRequest,
  ActiviteProgressionResponse 
} from '../models/activite.model';

@Injectable({ providedIn: 'root' })
export class EmployeActiviteService {
  private baseUrl = `${environment.apiUrl}/employe/activites`;

  constructor(private http: HttpClient) {}

  // CRUD Activités - Read only for employees
  getAll(): Observable<ActiviteResponse[]> {
    return this.http.get<ActiviteResponse[]>(this.baseUrl);
  }

  getById(id: number): Observable<ActiviteResponse> {
    return this.http.get<ActiviteResponse>(`${this.baseUrl}/${id}`);
  }

  getByProjet(projetId: number): Observable<ActiviteResponse[]> {
    return this.http.get<ActiviteResponse[]>(`${this.baseUrl}/projet/${projetId}`);
  }

  // Create/Update - requires chef de projet role (backend will check)
  create(request: ActiviteRequest): Observable<ActiviteResponse> {
    return this.http.post<ActiviteResponse>(this.baseUrl, request);
  }

  update(id: number, request: ActiviteRequest): Observable<ActiviteResponse> {
    return this.http.put<ActiviteResponse>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`, { responseType: 'text' });
  }

  // Gestion des employés assignés aux activités
  assignEmploye(activiteId: number, employeId: number, request: AssignEmployeToActiviteRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/${activiteId}/employes/${employeId}`, request);
  }

  unassignEmploye(activiteId: number, employeId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${activiteId}/employes/${employeId}`, { responseType: 'text' });
  }

  updateEmployeProgression(activiteId: number, employeId: number, progression: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/${activiteId}/employes/${employeId}/progression`, { progression });
  }

  // Statistiques et progression
  getProgression(activiteId: number): Observable<ActiviteProgressionResponse> {
    return this.http.get<ActiviteProgressionResponse>(`${this.baseUrl}/${activiteId}/progression`);
  }

  // Récupérer les employés d'une activité
  getEmployesByActiviteId(activiteId: number): Observable<Array<{ id: number; nom: string; prenom: string; progression: number }>> {
    return this.http.get<Array<{ id: number; nom: string; prenom: string; progression: number }>>(`${this.baseUrl}/${activiteId}/employes`);
  }

  // Dépôt d'activité - POST /soumettre avec FormData
  deposerActivite(id: number, depotData: { type: 'lien' | 'fichier', value: string | File }): Observable<ActiviteResponse> {
    console.log('=== deposerActivite ===');
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

    return this.http.post<ActiviteResponse>(`${this.baseUrl}/${id}/soumettre`, formData);
  }

  // Vérification de dépôt
  hasDepot(id: number): Observable<{ hasDepot: boolean; activiteId: number }> {
    return this.http.get<{ hasDepot: boolean; activiteId: number }>(`${this.baseUrl}/${id}/depot-exists`);
  }

  // Vérification si toutes les tâches sont déposées
  areAllTachesDeposees(id: number): Observable<{ allDeposees: boolean; activiteId: number }> {
    return this.http.get<{ allDeposees: boolean; activiteId: number }>(`${this.baseUrl}/${id}/toutes-taches-deposees`);
  }
}
