import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RessourceDisponible } from '../models/ressource.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class RessourcesDisponiblesService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // Voir toutes les ressources actives avec flag dejaDemandeParMoi
  getRessourcesDisponibles(): Observable<RessourceDisponible[]> {
    return this.http.get<RessourceDisponible[]>(`${this.baseUrl}/employe/ressources`);
  }
}
