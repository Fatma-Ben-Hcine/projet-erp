import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UtilisateurResponse, CreateUtilisateurRequest, UpdateUtilisateurRequest } from '../models/utilisateur.model';

@Injectable({
  providedIn: 'root'
})
export class UtilisateurService {

  private baseUrl = `${environment.apiUrl}/admin/utilisateurs`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<UtilisateurResponse[]> {
    return this.http.get<UtilisateurResponse[]>(this.baseUrl);
  }

  search(keyword?: string, role?: string): Observable<UtilisateurResponse[]> {
    let params = new HttpParams();
    if (keyword) params = params.set('keyword', keyword);
    if (role) params = params.set('role', role);
    return this.http.get<UtilisateurResponse[]>(`${this.baseUrl}/search`, { params });
  }

  create(data: CreateUtilisateurRequest): Observable<UtilisateurResponse> {
    return this.http.post<UtilisateurResponse>(this.baseUrl, data);
  }

  update(id: number, data: UpdateUtilisateurRequest): Observable<UtilisateurResponse> {
    return this.http.put<UtilisateurResponse>(`${this.baseUrl}/${id}`, data);
  }

  delete(id: number): Observable<string> {
    return this.http.delete(`${this.baseUrl}/${id}`, { responseType: 'text' });
  }

  toggleActivation(id: number): Observable<UtilisateurResponse> {
    return this.http.patch<UtilisateurResponse>(`${this.baseUrl}/${id}/toggle-activation`, {});
  }

  uploadPhoto(formData: FormData): Observable<{ photoUrl: string }> {
    return this.http.post<{ photoUrl: string }>(
      `${this.baseUrl}/upload-photo`,
      formData
    );
  }
}
