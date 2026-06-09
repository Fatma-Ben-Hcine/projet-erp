import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ContratRequest, ContratResponse } from '../models/contrat.model';

@Injectable({ providedIn: 'root' })
export class ContratService {
  private baseUrl = `${environment.apiUrl}/admin/contrats`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ContratResponse[]> {
    return this.http.get<ContratResponse[]>(this.baseUrl);
  }

  getById(id: number): Observable<ContratResponse> {
    return this.http.get<ContratResponse>(`${this.baseUrl}/${id}`);
  }

  getByClient(clientId: number): Observable<ContratResponse[]> {
    return this.http.get<ContratResponse[]>(
      `${this.baseUrl}/client/${clientId}`
    );
  }

  getByStatut(statut: string): Observable<ContratResponse[]> {
    return this.http.get<ContratResponse[]>(
      `${this.baseUrl}/statut/${statut}`
    );
  }

  create(request: ContratRequest): Observable<ContratResponse> {
    return this.http.post<ContratResponse>(this.baseUrl, request);
  }

  update(id: number, request: ContratRequest): Observable<ContratResponse> {
    return this.http.put<ContratResponse>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`, { responseType: 'text' });
  }
}
