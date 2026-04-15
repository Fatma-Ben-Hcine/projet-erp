export interface ClientRequest {
  nom: string;
  prenom: string;
  email: string;
  numeroTelephone?: string;
  matriculeFiscale?: string;
}

export interface ClientResponse {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  numeroTelephone: string;
  matriculeFiscale: string;
  nombreContrats: number;
}
