import { Project } from '../models/project.model';
import { Employee } from '../models/employee.model';

export const mockProjects: Project[] = [
  {
    id: '1',
    name: 'Système CRM',
    description: 'Développement système CRM personnalisé',
    status: 'new',
    deadline: '2026-08-31',
    progress: 0,
    budget: 50000,
    projectManager: 'emp1',
    clientId: 'cli1',
    assignedEmployees: ['emp1', 'emp2'],
    activities: []
  },
  {
    id: '2',
    name: 'Application Mobile E-commerce',
    description: 'Développement application mobile pour e-commerce',
    status: 'in-progress',
    deadline: '2026-06-30',
    progress: 45,
    budget: 80000,
    projectManager: 'emp1',
    clientId: 'cli2',
    assignedEmployees: ['emp1', 'emp3'],
    activities: []
  },
  {
    id: '3',
    name: 'Refonte Site Web',
    description: 'Refonte complète du site web corporate',
    status: 'late',
    deadline: '2025-12-31',
    progress: 30,
    budget: 30000,
    projectManager: 'emp2',
    clientId: 'cli1',
    assignedEmployees: ['emp2', 'emp4'],
    activities: []
  }
];

export const mockEmployees: Employee[] = [
  {
    id: 'emp1',
    firstName: 'Jean',
    lastName: 'Dupont',
    role: 'admin',
    isTemporary: false,
    joinDate: '2023-01-15',
    poste: 'Chef de projet'
  },
  {
    id: 'emp2',
    firstName: 'Marie',
    lastName: 'Martin',
    role: 'employee',
    isTemporary: false,
    joinDate: '2023-03-20',
    poste: 'Développeur'
  },
  {
    id: 'emp3',
    firstName: 'Pierre',
    lastName: 'Bernard',
    role: 'employee',
    isTemporary: true,
    joinDate: '2024-06-01',
    poste: 'Designer'
  },
  {
    id: 'emp4',
    firstName: 'Sophie',
    lastName: 'Leroy',
    role: 'employee',
    isTemporary: false,
    joinDate: '2023-08-10',
    poste: 'Développeur'
  },
  {
    id: 'emp5',
    firstName: 'Lucas',
    lastName: 'Moreau',
    role: 'employee',
    isTemporary: false,
    joinDate: '2023-05-12',
    poste: 'Testeur'
  }
];

export const mockTickets = [
  { id: 't1', title: 'Bug login', status: 'new' },
  { id: 't2', title: 'Erreur paiement', status: 'late' }
];
