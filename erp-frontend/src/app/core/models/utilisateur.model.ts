export interface UtilisateurResponse {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  CIN: string;
  numeroTel: string;
  poste: string;
  competences: string;
  role: 'ROLE_ADMIN' | 'ROLE_EMPLOYE';
  typeUtilisateur: 'PERMANENT' | 'TEMPORAIRE';
  actif: boolean;
  photo: string | null;
}

export interface CreateUtilisateurRequest {
  nom: string;
  prenom: string;
  email: string;
  motDePasse: string;
  CIN: string;
  numeroTel: string;
  poste: string;
  competences: string;
  role: 'ROLE_ADMIN' | 'ROLE_EMPLOYE';
  typeUtilisateur: 'PERMANENT' | 'TEMPORAIRE';
  photo?: string | null;
}

export interface UpdateUtilisateurRequest {
  nom: string;
  prenom: string;
  email: string;
  CIN: string;
  numeroTel: string;
  poste: string;
  competences: string;
  typeUtilisateur: 'PERMANENT' | 'TEMPORAIRE';
  photo?: string | null;
}
