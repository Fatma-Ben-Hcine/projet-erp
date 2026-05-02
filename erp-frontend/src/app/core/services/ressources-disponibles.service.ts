import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RessourceDisponible } from '../models/ressource.model';

@Injectable({
  providedIn: 'root'
})
export class RessourcesDisponiblesService {
  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // Voir toutes les ressources actives avec flag dejaDemandeParMoi
  getRessourcesDisponibles(): Observable<RessourceDisponible[]> {
    return this.http.get<RessourceDisponible[]>(`${this.baseUrl}/employe/ressources`);
  }
}
