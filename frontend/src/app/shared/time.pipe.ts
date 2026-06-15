import { Pipe, PipeTransform } from '@angular/core';
import { formatDistanceToNow, format } from 'date-fns';

@Pipe({ name: 'timeAgo', standalone: true, pure: true })
export class TimeAgoPipe implements PipeTransform {
  transform(value: string, mode: 'relative' | 'date' = 'relative') {
    if (!value) return '';
    const d = new Date(value);
    return mode === 'date' ? format(d, 'MMM d, yyyy') : formatDistanceToNow(d, { addSuffix: true });
  }
}
