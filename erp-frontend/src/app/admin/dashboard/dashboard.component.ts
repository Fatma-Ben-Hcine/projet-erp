import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StatCardComponent } from '../../shared/components/stat-card/stat-card.component';
import { AdminSidebarComponent } from '../shared/sidebar/sidebar.component';
import { mockProjects, mockTickets, mockEmployees } from '../../core/data/mock-data';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, StatCardComponent, AdminSidebarComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  totalProjects = 0;
  completedProjects = 0;
  lateProjects = 0;

  totalTickets = 0;
  completedTickets = 0;
  lateTickets = 0;

  newEmployees = 0;
  permanentEmployees = 0;
  temporaryEmployees = 0;
  admins = 0;

  ngOnInit(): void {
    this.totalProjects = mockProjects.length;
    this.completedProjects = mockProjects.filter(p => p.status === 'completed').length;
    this.lateProjects = mockProjects.filter(p => p.status === 'late').length;

    this.totalTickets = mockTickets.length;
    this.completedTickets = mockTickets.filter(t => t.status === 'completed').length;
    this.lateTickets = mockTickets.filter(t => t.status === 'late').length;

    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);

    this.newEmployees = mockEmployees.filter(
      e => new Date(e.joinDate) > thirtyDaysAgo
    ).length;
    this.permanentEmployees = mockEmployees.filter(e => !e.isTemporary).length;
    this.temporaryEmployees = mockEmployees.filter(e => e.isTemporary).length;
    this.admins = mockEmployees.filter(e => e.role === 'admin').length;
  }
}
