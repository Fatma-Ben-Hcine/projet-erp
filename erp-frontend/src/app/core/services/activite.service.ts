import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { 
  ActiviteRequest, 
  ActiviteResponse, 
  AssignEmployeToActiviteRequest,
  ActiviteProgressionResponse 
} from '../models/activite.model';

@Injectable({ providedIn: 'root' })
export class ActiviteService {
  private baseUrl = `${environment.apiUrl}/admin/activites`;

  constructor(private http: HttpClient) {}

  // CRUD Activités
  getAll(): Observable<ActiviteResponse[]> {
    return this.http.get<ActiviteResponse[]>(this.baseUrl);
  }

  getById(id: number): Observable<ActiviteResponse> {
    return this.http.get<ActiviteResponse>(`${this.baseUrl}/${id}`);
  }

  getByProjet(projetId: number): Observable<ActiviteResponse[]> {
    return this.http.get<ActiviteResponse[]>(`${this.baseUrl}/projet/${projetId}`);
  }

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
}
