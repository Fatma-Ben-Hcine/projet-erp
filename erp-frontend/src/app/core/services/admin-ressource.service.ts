import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Ressource, RessourceRequest } from '../models/ressource.model';

@Injectable({
  providedIn: 'root'
})
export class AdminRessourceService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // CRUD de base
  getAll(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/admin/ressources`);
  }

  // Méthodes de compatibilité pour les composants existants
  getAllRessources(): Observable<any[]> {
    return this.getAll();
  }

  createRessource(request: RessourceRequest): Observable<any> {
    return this.create(request);
  }

  updateRessource(id: number, request: RessourceRequest): Observable<any> {
    return this.update(id, request);
  }

  deleteRessource(id: number): Observable<any> {
    return this.delete(id);
  }

  
  // Nouvelles méthodes standardisées
  create(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/admin/ressources`, data);
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/admin/ressources/${id}`, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/admin/ressources/${id}`);
  }

  // Actions spécifiques
  changerStatut(id: number, statut: string): Observable<any> {
    return this.http.patch(`${this.apiUrl}/admin/ressources/${id}/statut`, { statut });
  }
}
