import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Ressource, RessourceRequest } from '../models/ressource.model';

@Injectable({
  providedIn: 'root'
})
export class AdminRessourceService {
  private baseUrl = '/api/admin/ressources';

  constructor(private http: HttpClient) {}

  // Créer une ressource
  createRessource(request: RessourceRequest): Observable<Ressource> {
    return this.http.post<Ressource>(this.baseUrl, request);
  }

  // Lire toutes les ressources (admin voit tout)
  getAllRessources(): Observable<Ressource[]> {
    return this.http.get<Ressource[]>(this.baseUrl);
  }

  // Modifier une ressource (nom, description, type)
  updateRessource(id: number, request: RessourceRequest): Observable<Ressource> {
    return this.http.put<Ressource>(`${this.baseUrl}/${id}`, request);
  }

  // Supprimer une ressource
  deleteRessource(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  // Changer le statut ACTIVE/NON_ACTIVE
  changerStatut(id: number, statut: 'ACTIVE' | 'NON_ACTIVE'): Observable<Ressource> {
    return this.http.patch<Ressource>(`${this.baseUrl}/${id}/statut`, { statut });
  }

  // Remettre la situation à DISPONIBLE (demande traitée)
  libererRessource(id: number): Observable<Ressource> {
    return this.http.patch<Ressource>(`${this.baseUrl}/${id}/liberer`, {});
  }
}
