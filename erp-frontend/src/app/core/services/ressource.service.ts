import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Ressource, RessourceRequest } from '../models/ressource.model';

@Injectable({
  providedIn: 'root'
})
export class RessourceService {
  private apiUrl = `${environment.apiUrl}/ressources`;

  constructor(private http: HttpClient) {}

  // ========================
  // CRUD — ADMIN
  // ========================

  getAll(): Observable<Ressource[]> {
    return this.http.get<Ressource[]>(this.apiUrl);
  }

  getById(id: number): Observable<Ressource> {
    return this.http.get<Ressource>(`${this.apiUrl}/${id}`);
  }

  create(ressource: RessourceRequest): Observable<Ressource> {
    return this.http.post<Ressource>(this.apiUrl, ressource);
  }

  update(id: number, ressource: RessourceRequest): Observable<Ressource> {
    return this.http.put<Ressource>(`${this.apiUrl}/${id}`, ressource);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // ========================
  // ENDPOINTS EMPLOYÉ
  // ========================

  getDisponibles(): Observable<RessourceDisponible[]> {
    return this.http.get<RessourceDisponible[]>(`${this.apiUrl}/disponibles`);
  }
}
