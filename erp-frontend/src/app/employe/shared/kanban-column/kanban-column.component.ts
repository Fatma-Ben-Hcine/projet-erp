import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Project } from '../../../core/models/project.model';
import { ProjectCardComponent } from '../project-card/project-card.component';

@Component({
  selector: 'app-kanban-column',
  standalone: true,
  imports: [CommonModule, ProjectCardComponent],
  templateUrl: './kanban-column.component.html',
  styleUrls: ['./kanban-column.component.css']
})
export class KanbanColumnComponent {
  @Input() title: string = '';
  @Input() dotColor: string = '';
  @Input() projects: Project[] = [];
  @Input() currentUserId: string = '';
  @Output() viewDetails = new EventEmitter<string>();
  @Output() deposit = new EventEmitter<string>();
  @Output() start = new EventEmitter<string>();
}
