import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Conge, CongeRequest, StatutConge, TypeConge } from '../models/conge.model';

@Injectable({
  providedIn: 'root'
})
export class CongeService {
  private readonly apiUrl = 'http://localhost:8080/api/conges';

  constructor(private http: HttpClient) {}

  // Employee operations
  demanderConge(conge: CongeRequest): Observable<Conge> {
    // Plus besoin d'envoyer employe_id, il est récupéré depuis le JWT
    return this.http.post<Conge>(this.apiUrl, conge);
  }

  modifierConge(id: number, conge: CongeRequest): Observable<Conge> {
    return this.http.put<Conge>(`${this.apiUrl}/${id}`, conge);
  }

  supprimerConge(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getMesConges(): Observable<Conge[]> {
    // Nouvel endpoint pour récupérer les congés de l'employé connecté
    return this.http.get<Conge[]>(`${this.apiUrl}/mes-conges`);
  }

  // Admin operations
  getAllConges(): Observable<Conge[]> {
    return this.http.get<Conge[]>(this.apiUrl);
  }

  getCongesByStatut(statut: StatutConge): Observable<Conge[]> {
    return this.http.get<Conge[]>(`${this.apiUrl}/statut/${statut}`);
  }

  validerConge(id: number): Observable<Conge> {
    return this.http.put<Conge>(`${this.apiUrl}/${id}/valider`, {});
  }

  refuserConge(id: number): Observable<Conge> {
    return this.http.put<Conge>(`${this.apiUrl}/${id}/refuser`, {});
  }

  getSoldeRestant(employeId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/solde/${employeId}`);
  }

  // Helper methods
  getTypeCongeLabel(type: TypeConge): string {
    const labels = {
      [TypeConge.MALADIE]: 'Maladie',
      [TypeConge.ANNUEL]: 'Congé Annuel',
      [TypeConge.MATERNITE]: 'Maternité',
      [TypeConge.PATERNITE]: 'Paternité',
      [TypeConge.SANS_SOLDE]: 'Sans Solde',
      [TypeConge.FORMATION]: 'Formation',
      [TypeConge.DECES]: 'Décès',
      [TypeConge.MARIAGE]: 'Mariage'
    };
    return labels[type] || type;
  }

  getStatutCongeLabel(statut: StatutConge): string {
    const labels = {
      [StatutConge.EN_ATTENTE]: 'En Attente',
      [StatutConge.VALIDE]: 'Validé',
      [StatutConge.REFUSE]: 'Refusé'
    };
    return labels[statut] || statut;
  }

  getStatutCongeColor(statut: StatutConge): string {
    const colors = {
      [StatutConge.EN_ATTENTE]: 'warning',
      [StatutConge.VALIDE]: 'success',
      [StatutConge.REFUSE]: 'danger'
    };
    return colors[statut] || 'info';
  }

  calculateDuration(dateDebut: string, dateFin: string): number {
    const start = new Date(dateDebut);
    const end = new Date(dateFin);
    const diffTime = Math.abs(end.getTime() - start.getTime());
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1; // +1 to include both days
  }
}
