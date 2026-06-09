export type ProjectStatus = 'new' | 'in-progress' | 'completed' | 'late';

export interface Task {
  id: string;
  name: string;
  description: string;
  endDate: string;
  status: 'new' | 'in-progress' | 'completed';
  assignedTo: string[];
}

export interface Activity {
  name: string;
  description: string;
  assignedEmployees: string[];
  tasks: Task[];
}

export interface Project {
  id: string;
  name: string;
  description: string;
  status: ProjectStatus;
  deadline: string;
  progress: number;
  budget: number;
  projectManager: string;
  clientId: string;
  assignedEmployees: string[];
  activities?: Activity[];
  isChef?: boolean; // true if current user is chef de projet for this project
}
