import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'priceDt', standalone: true })
export class PriceDtPipe implements PipeTransform {
  transform(value: number | string | null | undefined): string {
    if (value === null || value === undefined || value === '') {
      return '—';
    }
    return `${Number(value).toFixed(0)} TND`;
  }
}
