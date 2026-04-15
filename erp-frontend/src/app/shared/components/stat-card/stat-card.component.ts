import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './stat-card.component.html',
  styleUrls: ['./stat-card.component.css']
})
export class StatCardComponent {
  @Input() title: string = '';
  @Input() value: number = 0;
  @Input() icon: string = 'info';
  @Input() color: 'primary' | 'success' | 'danger' | 'warning' | 'info' = 'primary';
}
