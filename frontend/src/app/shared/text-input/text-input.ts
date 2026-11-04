import { Directive } from '@angular/core';

@Directive({
  selector: 'input[appInput], textarea[appInput], select[appInput]',
  host: {
    class:
      'w-full rounded-xs border-[1.5px] border-ink bg-surface px-3 py-2.5 text-[15px] text-ink placeholder:text-muted focus:border-wine focus:ring-1 focus:ring-wine focus:outline-none [&.ng-invalid.ng-touched]:border-wine',
  },
})
export class TextInput {}