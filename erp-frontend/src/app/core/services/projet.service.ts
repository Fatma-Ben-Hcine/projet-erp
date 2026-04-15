import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ProjetRequest, ProjetResponse } from '../models/projet.model';

@Injectable({ providedIn: 'root' })
export class ProjetService {
  private baseUrl = `${environment.apiUrl}/admin/projets`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ProjetResponse[]> {
    return this.http.get<ProjetResponse[]>(this.baseUrl);
  }

  getById(id: number): Observable<ProjetResponse> {
    return this.http.get<ProjetResponse>(`${this.baseUrl}/${id}`);
  }

  search(keyword: string): Observable<ProjetResponse[]> {
    return this.http.get<ProjetResponse[]>(
      `${this.baseUrl}/search?keyword=${keyword}`
    );
  }

  create(request: ProjetRequest): Observable<ProjetResponse> {
    return this.http.post<ProjetResponse>(this.baseUrl, request);
  }

  update(id: number, request: ProjetRequest): Observable<ProjetResponse> {
    return this.http.put<ProjetResponse>(`${this.baseUrl}/${id}`, request);
  }

  updateStatut(id: number, statut: string): Observable<ProjetResponse> {
    return this.http.patch<ProjetResponse>(`${this.baseUrl}/${id}/statut`, { statut });
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`, { responseType: 'text' });
  }
}
