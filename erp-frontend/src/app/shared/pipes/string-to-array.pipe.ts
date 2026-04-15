import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'stringToArray',
  standalone: true
})
export class StringToArrayPipe implements PipeTransform {
  transform(value: string | string[] | null | undefined): string[] {
    if (!value) return [];
    if (Array.isArray(value)) return value;
    if (typeof value === 'string') {
      return value.split(',').map(s => s.trim()).filter(Boolean);
    }
    return [];
  }
}
