import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminSidebarComponent } from '../shared/sidebar/sidebar.component';
import { RessourcesListComponent } from './ressources-list/ressources-list.component';

@Component({
  selector: 'app-admin-ressources',
  standalone: true,
  imports: [CommonModule, AdminSidebarComponent, RessourcesListComponent],
  templateUrl: './admin-ressources.component.html',
  styleUrls: ['./admin-ressources.component.scss']
})
export class AdminRessourcesComponent {
  constructor() { }
}
