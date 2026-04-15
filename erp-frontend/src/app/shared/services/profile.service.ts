import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface UserProfile {
  nom: string;
  prenom: string;
  email: string;
  CIN: string;
  numeroTel: string;
  poste: string;
  competences: string;
  role: string;
  photo?: string;
}

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.baseUrl}/user/profile`);
  }
}
